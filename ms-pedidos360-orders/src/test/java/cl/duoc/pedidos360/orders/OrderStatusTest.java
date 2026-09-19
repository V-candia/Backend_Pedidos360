package cl.duoc.pedidos360.orders;

import static cl.duoc.pedidos360.orders.OrderStatus.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class OrderStatusTest {
    @Test
    void flowRules() {
        assertTrue(CREADO.canMoveTo(ACEPTADO));
        assertTrue(DESPACHADO.canMoveTo(ENTREGADO));
        assertTrue(EN_PREPARACION.canMoveTo(CANCELADO));
        assertFalse(CREADO.canMoveTo(DESPACHADO)); // no se despacha sin aceptar
        assertFalse(ACEPTADO.canMoveTo(CREADO));
        assertFalse(CREADO.canMoveTo(CREADO));
        assertFalse(ENTREGADO.canMoveTo(CANCELADO)); // terminales
        assertFalse(CANCELADO.canMoveTo(ACEPTADO));
    }
}
