package cl.duoc.pedidos360.common;

/**
 * Nombres de exchanges, colas y routing keys de RabbitMQ, centralizados: ningún productor ni
 * consumidor escribe un string suelto, todos referencian estas constantes.
 */
public final class MqNames {
    private MqNames() {}

    public static final String EXCHANGE_CMD = "cmd.direct";
    public static final String EXCHANGE_DLX = "cmd.dead.dlx";

    public static final String ROUTING_EMAIL = "email.send";
    public static final String ROUTING_KITCHEN = "kitchen.ticket";
    public static final String ROUTING_INVOICE = "invoice.gen";

    public static final String QUEUE_EMAIL = "q.cmd.email";
    public static final String QUEUE_KITCHEN = "q.cmd.kitchen";
    public static final String QUEUE_INVOICE = "q.cmd.invoice";

    public static final String QUEUE_EMAIL_DLQ = "q.cmd.email.dlq";
    public static final String QUEUE_KITCHEN_DLQ = "q.cmd.kitchen.dlq";
    public static final String QUEUE_INVOICE_DLQ = "q.cmd.invoice.dlq";
}
