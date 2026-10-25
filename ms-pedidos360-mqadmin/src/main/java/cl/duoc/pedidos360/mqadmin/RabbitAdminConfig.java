package cl.duoc.pedidos360.mqadmin;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Spring Boot no publica un RabbitAdmin por defecto; este servicio lo necesita para crear/borrar a demanda. */
@Configuration
class RabbitAdminConfig {
    @Bean
    RabbitAdmin rabbitAdmin(ConnectionFactory cf) {
        return new RabbitAdmin(cf);
    }
}
