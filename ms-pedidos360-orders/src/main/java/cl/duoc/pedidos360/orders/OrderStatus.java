package cl.duoc.pedidos360.orders;

public enum OrderStatus {
    CREADO, ACEPTADO, EN_PREPARACION, DESPACHADO, ENTREGADO, CANCELADO; // CANCELADO debe ir al final

    /** Solo el siguiente paso del flujo, o CANCELADO desde un estado no terminal. */
    public boolean canMoveTo(OrderStatus next) {
        if (this == ENTREGADO || this == CANCELADO) return false;
        return next == CANCELADO || next.ordinal() == ordinal() + 1;
    }

    public String eventType() {
        return switch (this) {
            case CREADO -> "OrderCreated";
            case ACEPTADO -> "OrderAccepted";
            case EN_PREPARACION -> "OrderPreparing";
            case DESPACHADO -> "OrderDispatched";
            case ENTREGADO -> "OrderDelivered";
            case CANCELADO -> "OrderCancelled";
        };
    }
}
