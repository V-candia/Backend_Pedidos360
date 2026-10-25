package cl.duoc.pedidos360.common;

import java.time.Instant;

/** Comando RabbitMQ (q.cmd.invoice): generar el documento al entregar un pedido. */
public record InvoiceCommand(String type, String eventId, Instant timestamp, String orderId,
                             String customerName, long total) {}
