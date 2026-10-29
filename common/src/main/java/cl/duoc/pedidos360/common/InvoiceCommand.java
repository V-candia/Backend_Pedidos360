package cl.duoc.pedidos360.common;

import java.time.Instant;
import java.util.List;

/** Comando RabbitMQ (q.cmd.invoice): generar el PDF de detalle al facturar un pedido (disparo manual). */
public record InvoiceCommand(String type, String eventId, Instant timestamp, String orderId,
                             String customerName, long total, List<Item> items) {
    public record Item(String productName, int qty, long price) {}
}
