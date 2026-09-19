package cl.duoc.pedidos360.report;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/** Proyección de un pedido construida solo con eventos de orders.events. */
@Entity
public class OrderFact {
    @Id
    public String id;
    public long total;
    public String status;
    public Instant createdAt;
    public Instant deliveredAt;
}
