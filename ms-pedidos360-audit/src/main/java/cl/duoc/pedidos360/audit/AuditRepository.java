package cl.duoc.pedidos360.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AuditRepository extends JpaRepository<AuditEvent, String>, JpaSpecificationExecutor<AuditEvent> {}
