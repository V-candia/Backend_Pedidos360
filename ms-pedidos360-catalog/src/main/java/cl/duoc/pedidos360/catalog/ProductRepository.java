package cl.duoc.pedidos360.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface ProductRepository extends JpaRepository<Product, String> {

    /** Update atómico: no deja el stock negativo aunque haya llamadas concurrentes. Devuelve 0 filas si no aplica. */
    @Modifying
    @Transactional
    @Query("update Product p set p.stock = p.stock + :delta where p.id = :id and p.stock + :delta >= 0")
    int adjustStock(String id, int delta);
}
