package cl.duoc.pedidos360.orders;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import cl.duoc.pedidos360.common.EmailCommand;
import cl.duoc.pedidos360.common.InvoiceCommand;
import cl.duoc.pedidos360.common.KitchenTicketCommand;
import cl.duoc.pedidos360.common.MqNames;

/** Único punto que conoce el exchange y las routing keys de RabbitMQ; OrderService no las ve. */
@Component
class CommandPublisher {
    private final RabbitTemplate rabbit;

    CommandPublisher(RabbitTemplate rabbit) {
        this.rabbit = rabbit;
    }

    void sendEmail(EmailCommand c) {
        rabbit.convertAndSend(MqNames.EXCHANGE_CMD, MqNames.ROUTING_EMAIL, c);
    }

    void sendKitchenTicket(KitchenTicketCommand c) {
        rabbit.convertAndSend(MqNames.EXCHANGE_CMD, MqNames.ROUTING_KITCHEN, c);
    }

    void sendInvoice(InvoiceCommand c) {
        rabbit.convertAndSend(MqNames.EXCHANGE_CMD, MqNames.ROUTING_INVOICE, c);
    }
}
