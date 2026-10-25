package cl.duoc.pedidos360.notify;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import cl.duoc.pedidos360.common.InvoiceCommand;
import cl.duoc.pedidos360.common.MqNames;

/** Simula la generación del documento de factura al entregar un pedido. Sin facturador real, solo loguea. */
@Component
class InvoiceListener {
    private static final Logger log = LoggerFactory.getLogger(InvoiceListener.class);

    /** Factura con total negativo o nulo: no recuperable, directo a q.cmd.invoice.dlq. Lo demás reintenta. */
    @RabbitListener(queues = MqNames.QUEUE_INVOICE)
    void onInvoice(InvoiceCommand c) {
        if (c.total() < 0) {
            throw new AmqpRejectAndDontRequeueException("Factura con total inválido: " + c.eventId());
        }
        log.info("[factura] pedido={} cliente={} total={}", c.orderId(), c.customerName(), c.total());
    }
}
