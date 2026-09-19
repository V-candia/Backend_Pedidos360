package cl.duoc.pedidos360.report;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    record Sale(Instant hour, long total) {}

    record Kpis(List<Sale> salesByHour, double leadTimeAvgMinutes, Map<String, Long> activeOrdersByStatus) {}

    private final OrderFactRepository repo;

    ReportController(OrderFactRepository repo) {
        this.repo = repo;
    }

    // ponytail: agrega en memoria sobre todos los pedidos (O(n) por request); pasar a queries GROUP BY o a una tabla horaria si crece
    @GetMapping("/kpis")
    @PreAuthorize("hasRole('Admin')")
    Kpis kpis() {
        List<OrderFact> all = repo.findAll();

        var byHour = new TreeMap<Instant, Long>();
        all.stream().filter(f -> f.createdAt != null && !"CANCELADO".equals(f.status))
                .forEach(f -> byHour.merge(f.createdAt.truncatedTo(ChronoUnit.HOURS), f.total, Long::sum));

        double leadMinutes = all.stream().filter(f -> f.createdAt != null && f.deliveredAt != null)
                .mapToLong(f -> Duration.between(f.createdAt, f.deliveredAt).toSeconds())
                .average().orElse(0) / 60;

        Map<String, Long> active = all.stream()
                .filter(f -> !"ENTREGADO".equals(f.status) && !"CANCELADO".equals(f.status))
                .collect(Collectors.groupingBy(f -> f.status, Collectors.counting()));

        return new Kpis(byHour.entrySet().stream().map(e -> new Sale(e.getKey(), e.getValue())).toList(),
                Math.round(leadMinutes * 10) / 10.0, active);
    }
}
