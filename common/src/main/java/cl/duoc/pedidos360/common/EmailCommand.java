package cl.duoc.pedidos360.common;

import java.time.Instant;

/** Comando RabbitMQ (q.cmd.email) con el envelope común: type, eventId, timestamp, traceId, correlationId. */
public record EmailCommand(String type, String eventId, Instant timestamp, String traceId, String correlationId,
                           String to, String orderId, String status) {}
