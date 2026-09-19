package cl.duoc.pedidos360.audit;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit")
public class AuditController {
    private final AuditRepository repo;

    AuditController(AuditRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/events")
    @PreAuthorize("hasAnyRole('Admin','Auditor')")
    List<AuditEvent> events(@RequestParam(required = false) String userId, @RequestParam(required = false) Instant from,
                            @RequestParam(required = false) Instant to, @RequestParam(required = false) String eventType) {
        Specification<AuditEvent> s = (r, q, cb) -> cb.conjunction();
        if (userId != null) s = s.and((r, q, cb) -> cb.equal(r.get("actor"), userId));
        if (eventType != null) s = s.and((r, q, cb) -> cb.equal(r.get("eventType"), eventType));
        if (from != null) s = s.and((r, q, cb) -> cb.greaterThanOrEqualTo(r.get("timestamp"), from));
        if (to != null) s = s.and((r, q, cb) -> cb.lessThanOrEqualTo(r.get("timestamp"), to));
        return repo.findAll(s, Sort.by("timestamp"));
    }
}
