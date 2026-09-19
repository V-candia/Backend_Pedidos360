package cl.duoc.pedidos360.catalog;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Product {
    @Id
    public String id;
    public String name;
    public long price;
    public int stock;
}
