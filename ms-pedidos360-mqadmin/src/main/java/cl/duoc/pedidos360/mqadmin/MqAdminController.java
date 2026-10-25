package cl.duoc.pedidos360.mqadmin;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

/** Administración dinámica de RabbitMQ: crear y eliminar colas, exchanges y bindings. Solo Admin. */
@RestController
@RequestMapping("/api/mq")
@PreAuthorize("hasRole('Admin')")
public class MqAdminController {

    record QueueRequest(@NotBlank(message = "name no puede estar vacío") String name) {}

    record ExchangeRequest(@NotBlank(message = "name no puede estar vacío") String name, String type) {}

    record BindingRequest(@NotBlank String exchange, @NotBlank String queue, @NotBlank String routingKey) {}

    private final RabbitAdminService service;

    MqAdminController(RabbitAdminService service) {
        this.service = service;
    }

    @PostMapping("/queues")
    @ResponseStatus(HttpStatus.CREATED)
    QueueRequest createQueue(@Valid @RequestBody QueueRequest r) {
        service.createQueue(r.name());
        return r;
    }

    @DeleteMapping("/queues/{name}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteQueue(@PathVariable String name) {
        service.deleteQueue(name);
    }

    @PostMapping("/exchanges")
    @ResponseStatus(HttpStatus.CREATED)
    ExchangeRequest createExchange(@Valid @RequestBody ExchangeRequest r) {
        service.createExchange(r.name(), r.type());
        return r;
    }

    @DeleteMapping("/exchanges/{name}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteExchange(@PathVariable String name) {
        service.deleteExchange(name);
    }

    @PostMapping("/bindings")
    @ResponseStatus(HttpStatus.CREATED)
    BindingRequest createBinding(@Valid @RequestBody BindingRequest r) {
        service.createBinding(r.exchange(), r.queue(), r.routingKey());
        return r;
    }

    @DeleteMapping("/bindings")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteBinding(@RequestParam String exchange, @RequestParam String queue, @RequestParam String routingKey) {
        service.deleteBinding(exchange, queue, routingKey);
    }
}
