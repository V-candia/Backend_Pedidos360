package cl.duoc.pedidos360.mqadmin;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import cl.duoc.pedidos360.common.ApiException;

/**
 * Toda la lógica de creación y eliminación de colas, exchanges y bindings vive aquí; el controlador
 * solo llama a estos métodos de alto nivel y no conoce RabbitAdmin ni la librería de RabbitMQ.
 */
@Service
class RabbitAdminService {
    private final RabbitAdmin admin;

    RabbitAdminService(RabbitAdmin admin) {
        this.admin = admin;
    }

    void createQueue(String name) {
        run(() -> admin.declareQueue(new Queue(name, true)));
    }

    // RabbitAdmin.deleteQueue(String) no distingue "no existía" de "borrada": siempre devuelve true.
    // Se comprueba antes con getQueueProperties (declare pasivo, null si no existe).
    void deleteQueue(String name) {
        if (admin.getQueueProperties(name) == null) throw notFound("Cola", name);
        admin.deleteQueue(name);
    }

    void createExchange(String name, String type) {
        run(() -> admin.declareExchange(exchange(name, type)));
    }

    void deleteExchange(String name) {
        if (!admin.deleteExchange(name)) throw notFound("Exchange", name);
    }

    void createBinding(String exchange, String queue, String routingKey) {
        run(() -> admin.declareBinding(new Binding(queue, Binding.DestinationType.QUEUE, exchange, routingKey, null)));
    }

    void deleteBinding(String exchange, String queue, String routingKey) {
        admin.removeBinding(new Binding(queue, Binding.DestinationType.QUEUE, exchange, routingKey, null));
    }

    private static Exchange exchange(String name, String type) {
        return switch (type == null ? "direct" : type.toLowerCase()) {
            case "direct" -> new DirectExchange(name, true, false);
            case "topic" -> new TopicExchange(name, true, false);
            case "fanout" -> new FanoutExchange(name, true, false);
            default -> throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                    "Tipo de exchange inválido: " + type + " (usar direct, topic o fanout)");
        };
    }

    private static ApiException notFound(String what, String name) {
        return new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", what + " no encontrado: " + name);
    }

    /** declareX lanza si el broker rechaza la operación (ej. ya existe con otras propiedades); se traduce a 409. */
    private static void run(Runnable op) {
        try {
            op.run();
        } catch (AmqpException e) {
            throw new ApiException(HttpStatus.CONFLICT, "MQ_ERROR", e.getMessage());
        }
    }
}
