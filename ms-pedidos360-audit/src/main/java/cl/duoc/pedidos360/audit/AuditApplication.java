package cl.duoc.pedidos360.audit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@SpringBootApplication(scanBasePackages = "cl.duoc.pedidos360")
public class AuditApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuditApplication.class, args);
    }

    /** 3 reintentos y luego audit.timeline.DLT con el mensaje original + headers de error (stack, offset, etc.). */
    @Bean
    DefaultErrorHandler errorHandler(KafkaTemplate<Object, Object> template) {
        return new DefaultErrorHandler(new DeadLetterPublishingRecoverer(template), new FixedBackOff(1000L, 3));
    }
}
