package cl.duoc.pedidos360.catalog;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.pedidos360.common.ApiException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/catalog/products")
public class ProductController {

    record ProductRequest(@NotBlank String name, @Min(0) long price, @Min(0) int stock) {}

    record StockDelta(int delta) {}

    private final ProductRepository repo;

    ProductController(ProductRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('Admin','Operador','Cliente')")
    List<Product> list() {
        return repo.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('Admin')")
    Product create(@Valid @RequestBody ProductRequest r) {
        var p = new Product();
        p.id = UUID.randomUUID().toString();
        return save(p, r);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    Product update(@PathVariable String id, @Valid @RequestBody ProductRequest r) {
        return save(find(id), r);
    }

    // Lo llama orders-service con el token del usuario que acepta/cancela (Operador o Admin); no se expone en el gateway.
    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasAnyRole('Admin','Operador')")
    Product adjustStock(@PathVariable String id, @RequestBody StockDelta d) {
        if (repo.adjustStock(id, d.delta()) == 0) {
            find(id);
            throw new ApiException(HttpStatus.CONFLICT, "INSUFFICIENT_STOCK", "El stock quedaría negativo");
        }
        return find(id);
    }

    private Product find(String id) {
        return repo.findById(id).orElseThrow(
                () -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Producto no encontrado"));
    }

    private Product save(Product p, ProductRequest r) {
        p.name = r.name();
        p.price = r.price();
        p.stock = r.stock();
        return repo.save(p);
    }
}
