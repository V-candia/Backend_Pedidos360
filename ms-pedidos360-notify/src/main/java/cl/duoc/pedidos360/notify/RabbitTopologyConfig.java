package cl.duoc.pedidos360.notify;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cl.duoc.pedidos360.common.MqNames;

/** Declara la topología de RabbitMQ (exchange, colas y bindings) para cada caso de uso: email, cocina y factura. */
@Configuration
class RabbitTopologyConfig {

    @Bean
    DirectExchange cmdExchange() {
        return new DirectExchange(MqNames.EXCHANGE_CMD, true, false);
    }

    @Bean
    DirectExchange dlxExchange() {
        return new DirectExchange(MqNames.EXCHANGE_DLX, true, false);
    }

    @Bean
    Queue emailQueue() {
        return dead(MqNames.QUEUE_EMAIL, MqNames.ROUTING_EMAIL);
    }

    @Bean
    Queue kitchenQueue() {
        return dead(MqNames.QUEUE_KITCHEN, MqNames.ROUTING_KITCHEN);
    }

    @Bean
    Queue invoiceQueue() {
        return dead(MqNames.QUEUE_INVOICE, MqNames.ROUTING_INVOICE);
    }

    @Bean
    Queue emailDlq() {
        return QueueBuilder.durable(MqNames.QUEUE_EMAIL_DLQ).build();
    }

    @Bean
    Queue kitchenDlq() {
        return QueueBuilder.durable(MqNames.QUEUE_KITCHEN_DLQ).build();
    }

    @Bean
    Queue invoiceDlq() {
        return QueueBuilder.durable(MqNames.QUEUE_INVOICE_DLQ).build();
    }

    @Bean
    Binding emailBinding() {
        return bind(MqNames.QUEUE_EMAIL, MqNames.EXCHANGE_CMD, MqNames.ROUTING_EMAIL);
    }

    @Bean
    Binding kitchenBinding() {
        return bind(MqNames.QUEUE_KITCHEN, MqNames.EXCHANGE_CMD, MqNames.ROUTING_KITCHEN);
    }

    @Bean
    Binding invoiceBinding() {
        return bind(MqNames.QUEUE_INVOICE, MqNames.EXCHANGE_CMD, MqNames.ROUTING_INVOICE);
    }

    @Bean
    Binding emailDlqBinding() {
        return bind(MqNames.QUEUE_EMAIL_DLQ, MqNames.EXCHANGE_DLX, MqNames.ROUTING_EMAIL);
    }

    @Bean
    Binding kitchenDlqBinding() {
        return bind(MqNames.QUEUE_KITCHEN_DLQ, MqNames.EXCHANGE_DLX, MqNames.ROUTING_KITCHEN);
    }

    @Bean
    Binding invoiceDlqBinding() {
        return bind(MqNames.QUEUE_INVOICE_DLQ, MqNames.EXCHANGE_DLX, MqNames.ROUTING_INVOICE);
    }

    /** Cola durable con dead-lettering hacia cmd.dead.dlx (misma routing key, igual que infra/mq/definitions.json). */
    private static Queue dead(String name, String routingKey) {
        return QueueBuilder.durable(name)
                .withArgument("x-dead-letter-exchange", MqNames.EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", routingKey)
                .build();
    }

    private static Binding bind(String queue, String exchange, String routingKey) {
        return new Binding(queue, Binding.DestinationType.QUEUE, exchange, routingKey, null);
    }
}
