package cl.duoc.pedidos360.common;

import java.time.Instant;

/** Evento de negocio publicado en Kafka (orders.events / audit.timeline). from es null en OrderCreated. */
public record OrderEvent(String eventId, String type, String orderId, String customerId, String actor,
                         Instant timestamp, long total, String from, String to) {}
