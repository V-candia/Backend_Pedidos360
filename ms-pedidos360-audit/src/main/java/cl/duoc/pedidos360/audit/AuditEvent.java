package cl.duoc.pedidos360.audit;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonRawValue;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class AuditEvent {
    @Id
    public String id;
    public String entityId;
    public String eventType;
    public String actor;
    @Column(name = "event_ts")
    public Instant timestamp;
    @JsonRawValue // se guarda como JSON en texto y se devuelve como objeto
    @Column(length = 2000)
    public String metadata;
}
