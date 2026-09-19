package cl.duoc.pedidos360.audit;

import java.util.Map;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import cl.duoc.pedidos360.common.OrderEvent;

@Component
class AuditConsumer {
    private final AuditRepository repo;
    private final ObjectMapper mapper;

    AuditConsumer(AuditRepository repo, ObjectMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    /** Si el JSON no parsea o la DB falla, DefaultErrorHandler reintenta y manda a audit.timeline.DLT. */
    @KafkaListener(topics = "audit.timeline")
    void on(String json) throws Exception {
        OrderEvent e = mapper.readValue(json, OrderEvent.class);
        var a = new AuditEvent();
        a.id = e.eventId(); // idempotente: reprocesar el mismo evento sobreescribe la misma fila
        a.entityId = e.orderId();
        a.eventType = e.type();
        a.actor = e.actor();
        a.timestamp = e.timestamp();
        a.metadata = mapper.writeValueAsString(
                e.from() == null ? Map.of("to", e.to()) : Map.of("from", e.from(), "to", e.to()));
        repo.save(a);
    }
}
