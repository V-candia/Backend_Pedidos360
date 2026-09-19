package cl.duoc.pedidos360.orders;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
public class Order {
    @Embeddable
    public record Item(String productId, String productName, int qty, long price) {}

    @Id
    public String id;
    public String customerId;
    public String customerName;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "order_items", joinColumns = @JoinColumn(name = "order_id"))
    public List<Item> items = new ArrayList<>();
    @Enumerated(EnumType.STRING)
    public OrderStatus status;
    public Instant createdAt;
    public Instant updatedAt;
}
