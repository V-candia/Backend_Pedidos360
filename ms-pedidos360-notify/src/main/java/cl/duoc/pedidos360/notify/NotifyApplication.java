package cl.duoc.pedidos360.notify;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackages = "cl.duoc.pedidos360")
public class NotifyApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotifyApplication.class, args);
    }

    @Bean
    MessageConverter jsonConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
