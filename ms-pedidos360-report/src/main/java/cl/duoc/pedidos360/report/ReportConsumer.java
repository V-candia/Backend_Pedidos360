package cl.duoc.pedidos360.report;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import cl.duoc.pedidos360.common.OrderEvent;

@Component
class ReportConsumer {
    private final OrderFactRepository repo;
    private final ObjectMapper mapper;

    ReportConsumer(OrderFactRepository repo, ObjectMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    /** Idempotente: solo fija estado y fechas, reprocesar un evento deja el mismo resultado. */
    @KafkaListener(topics = "orders.events")
    void on(String json) throws Exception {
        OrderEvent e = mapper.readValue(json, OrderEvent.class);
        OrderFact f = repo.findById(e.orderId()).orElseGet(() -> {
            var n = new OrderFact();
            n.id = e.orderId();
            return n;
        });
        f.status = e.to();
        if (e.from() == null) { // OrderCreated
            f.total = e.total();
            f.createdAt = e.timestamp();
        }
        if ("ENTREGADO".equals(e.to())) f.deliveredAt = e.timestamp();
        repo.save(f);
    }
}
