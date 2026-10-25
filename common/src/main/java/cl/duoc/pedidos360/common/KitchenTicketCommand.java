package cl.duoc.pedidos360.common;

import java.time.Instant;
import java.util.List;

/** Comando RabbitMQ (q.cmd.kitchen): ticket de cocina al aceptar un pedido. */
public record KitchenTicketCommand(String type, String eventId, Instant timestamp, String orderId,
                                   String customerName, List<Item> items) {
    public record Item(String productName, int qty) {}
}
