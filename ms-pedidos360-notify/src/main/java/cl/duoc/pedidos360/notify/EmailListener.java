package cl.duoc.pedidos360.notify;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import cl.duoc.pedidos360.common.EmailCommand;
import cl.duoc.pedidos360.common.MqNames;

@Component
class EmailListener {
    private static final Logger log = LoggerFactory.getLogger(EmailListener.class);

    // ponytail: idempotencia en memoria (por instancia, crece sin límite); pasar a Redis/DB al escalar a varias instancias
    private final Set<String> processed = ConcurrentHashMap.newKeySet();
    private final ObjectProvider<JavaMailSender> mail;

    EmailListener(ObjectProvider<JavaMailSender> mail) {
        this.mail = mail;
    }

    /**
     * ACK al terminar. Mensaje malformado (sin destinatario): no recuperable, se rechaza directo a
     * q.cmd.email.dlq sin reintentar (AmqpRejectAndDontRequeueException). Cualquier otra falla (ej. SMTP
     * caído) es transitoria: se reintenta hasta 3 veces (ver application.yml) y recién ahí cae a la DLQ.
     */
    @RabbitListener(queues = MqNames.QUEUE_EMAIL)
    void onEmail(EmailCommand c) {
        if (c.to() == null || c.to().isBlank()) {
            throw new AmqpRejectAndDontRequeueException("EmailCommand sin destinatario: " + c.eventId());
        }
        if (processed.contains(c.eventId())) return;
        String text = "Tu pedido " + c.orderId() + " está ahora en estado " + c.status();
        JavaMailSender sender = mail.getIfAvailable();
        if (sender == null) {
            log.info("[email simulado] to={} traceId={} {}", c.to(), c.traceId(), text);
        } else {
            var m = new SimpleMailMessage();
            m.setTo(c.to());
            m.setSubject("Pedidos360: actualización de tu pedido");
            m.setText(text);
            sender.send(m);
        }
        processed.add(c.eventId());
    }
}
