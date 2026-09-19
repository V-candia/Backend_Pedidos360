package cl.duoc.pedidos360.orders;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;

@SpringBootApplication(scanBasePackages = "cl.duoc.pedidos360")
public class OrdersApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrdersApplication.class, args);
    }

    @Bean
    MessageConverter jsonConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    NewTopic ordersEvents() {
        return TopicBuilder.name("orders.events").partitions(3).config("retention.ms", "604800000").build(); // 7 días
    }

    @Bean
    NewTopic auditTimeline() {
        return TopicBuilder.name("audit.timeline").partitions(3)
                .config("cleanup.policy", "compact,delete").config("retention.ms", "1209600000").build(); // 14 días
    }
}
