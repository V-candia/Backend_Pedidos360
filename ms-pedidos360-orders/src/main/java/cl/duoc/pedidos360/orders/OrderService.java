package cl.duoc.pedidos360.orders;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import cl.duoc.pedidos360.common.ApiException;
import cl.duoc.pedidos360.common.EmailCommand;
import cl.duoc.pedidos360.common.OrderEvent;

@Service
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    public record NewItem(String productId, int qty) {}

    private final OrderRepository repo;
    private final CatalogClient catalog;
    private final KafkaTemplate<String, Object> kafka;
    private final RabbitTemplate rabbit;

    OrderService(OrderRepository repo, CatalogClient catalog, KafkaTemplate<String, Object> kafka, RabbitTemplate rabbit) {
        this.repo = repo;
        this.catalog = catalog;
        this.kafka = kafka;
        this.rabbit = rabbit;
    }

    /** owner != null restringe a los pedidos de ese cliente. */
    List<Order> list(String owner, OrderStatus status, Instant from, Instant to) {
        Specification<Order> s = (r, q, cb) -> cb.conjunction();
        if (owner != null) s = s.and((r, q, cb) -> cb.equal(r.get("customerId"), owner));
        if (status != null) s = s.and((r, q, cb) -> cb.equal(r.get("status"), status));
        if (from != null) s = s.and((r, q, cb) -> cb.greaterThanOrEqualTo(r.get("createdAt"), from));
        if (to != null) s = s.and((r, q, cb) -> cb.lessThanOrEqualTo(r.get("createdAt"), to));
        return repo.findAll(s, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    /** 404 (no 403) si el pedido es de otro cliente, para no filtrar su existencia. */
    Order get(String id, String owner) {
        return repo.findById(id).filter(o -> owner == null || owner.equals(o.customerId)).orElseThrow(this::notFound);
    }

    Order create(String customerName, List<NewItem> items, String who) {
        Map<String, CatalogClient.Product> products =
                catalog.products().stream().collect(Collectors.toMap(CatalogClient.Product::id, Function.identity()));
        var o = new Order();
        o.id = UUID.randomUUID().toString();
        o.customerId = who;
        o.customerName = customerName;
        for (var i : items) {
            var p = products.get(i.productId());
            if (p == null) throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Producto inexistente: " + i.productId());
            if (p.stock() < i.qty()) throw new ApiException(HttpStatus.CONFLICT, "INSUFFICIENT_STOCK", "Stock insuficiente para " + p.name());
            o.items.add(new Order.Item(p.id(), p.name(), i.qty(), p.price())); // precio siempre desde el catálogo
        }
        o.status = OrderStatus.CREADO;
        o.createdAt = o.updatedAt = Instant.now();
        repo.save(o);
        emit(o, null, who);
        return o;
    }

    // ponytail: dos PATCH concurrentes sobre el mismo pedido pueden descontar stock dos veces; agregar @Version si importa
    Order updateStatus(String id, OrderStatus next, String actor) {
        Order o = repo.findById(id).orElseThrow(this::notFound);
        OrderStatus from = o.status;
        if (!from.canMoveTo(next)) {
            throw new ApiException(HttpStatus.CONFLICT, "INVALID_TRANSITION", "No se puede pasar de " + from + " a " + next);
        }
        if (next == OrderStatus.ACEPTADO) adjustStock(o, -1);
        else if (next == OrderStatus.CANCELADO && from != OrderStatus.CREADO) adjustStock(o, +1); // devuelve el stock
        o.status = next;
        o.updatedAt = Instant.now();
        repo.save(o);
        emit(o, from, actor);
        return o;
    }

    /** Ajusta el stock de todas las líneas; si una falla, revierte las ya aplicadas. */
    private void adjustStock(Order o, int sign) {
        var done = new ArrayList<Order.Item>();
        try {
            for (var i : o.items) {
                catalog.adjustStock(i.productId(), sign * i.qty());
                done.add(i);
            }
        } catch (RuntimeException e) {
            done.forEach(i -> catalog.adjustStock(i.productId(), -sign * i.qty()));
            throw e;
        }
    }

    // ponytail: sin outbox; si Kafka/RabbitMQ caen tras guardar, el evento se pierde (se loguea). Outbox si hace falta garantía.
    private void emit(Order o, OrderStatus from, String actor) {
        try {
            long total = o.items.stream().mapToLong(i -> i.qty() * i.price()).sum();
            var e = new OrderEvent(UUID.randomUUID().toString(), o.status.eventType(), o.id, o.customerId, actor,
                    o.updatedAt, total, from == null ? null : from.name(), o.status.name());
            kafka.send("orders.events", o.id, e); // misma key = mismo partition = orden por pedido
            kafka.send("audit.timeline", e.eventId(), e);
            rabbit.convertAndSend("cmd.direct", "email.send", new EmailCommand("email.send", e.eventId(), e.timestamp(),
                    UUID.randomUUID().toString(), o.id, o.customerId, o.id, o.status.name()));
        } catch (RuntimeException ex) {
            log.warn("No se pudo publicar evento/notificación del pedido {}", o.id, ex);
        }
    }

    private ApiException notFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Pedido no encontrado");
    }
}
