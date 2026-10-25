package cl.duoc.pedidos360.notify;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import cl.duoc.pedidos360.common.KitchenTicketCommand;
import cl.duoc.pedidos360.common.MqNames;

/**
 * Simula la impresión del ticket de cocina al aceptar un pedido. Sin impresora real, solo loguea
 * (mismo patrón que EmailListener cuando no hay spring.mail.host).
 */
@Component
class KitchenTicketListener {
    private static final Logger log = LoggerFactory.getLogger(KitchenTicketListener.class);

    /** Ticket sin items: no recuperable, directo a q.cmd.kitchen.dlq. Cualquier otra falla reintenta (ver application.yml). */
    @RabbitListener(queues = MqNames.QUEUE_KITCHEN)
    void onTicket(KitchenTicketCommand c) {
        if (c.items() == null || c.items().isEmpty()) {
            throw new AmqpRejectAndDontRequeueException("Ticket de cocina sin items: " + c.eventId());
        }
        String detalle = c.items().stream().map(i -> i.qty() + "x " + i.productName()).reduce((a, b) -> a + ", " + b).orElse("");
        log.info("[ticket cocina] pedido={} cliente={} items={}", c.orderId(), c.customerName(), detalle);
    }
}
