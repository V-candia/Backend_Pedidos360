package cl.duoc.pedidos360.catalog;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackages = "cl.duoc.pedidos360")
public class CatalogApplication {
    public static void main(String[] args) {
        SpringApplication.run(CatalogApplication.class, args);
    }

    /** Solo con APP_SEED=true (compose local) y catálogo vacío. */
    @Bean
    ApplicationRunner seed(ProductRepository repo, @Value("${app.seed:false}") boolean enabled) {
        return args -> {
            if (!enabled || repo.count() > 0) return;
            record S(String name, long price, int stock) {}
            for (var s : List.of(new S("Pizza margarita", 6990, 50), new S("Hamburguesa clásica", 5490, 40),
                    new S("Ensalada César", 4990, 25), new S("Bebida 500ml", 1500, 100), new S("Postre del día", 2990, 3))) {
                var p = new Product();
                p.id = UUID.randomUUID().toString();
                p.name = s.name();
                p.price = s.price();
                p.stock = s.stock();
                repo.save(p);
            }
        };
    }
}
