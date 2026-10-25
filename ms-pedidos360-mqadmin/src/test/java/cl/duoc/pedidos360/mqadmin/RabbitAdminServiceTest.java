package cl.duoc.pedidos360.mqadmin;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

import cl.duoc.pedidos360.common.ApiException;

class RabbitAdminServiceTest {
    private final RabbitAdmin admin = mock(RabbitAdmin.class);
    private final RabbitAdminService service = new RabbitAdminService(admin);

    @Test
    void tipoDeExchangeInvalidoNoLlegaAlBroker() {
        assertThrows(ApiException.class, () -> service.createExchange("x", "no-existe"));
    }

    @Test
    void tipoDirectPorDefectoSinEspecificarlo() {
        service.createExchange("x", null);
        verify(admin).declareExchange(org.mockito.ArgumentMatchers.argThat(e -> e instanceof DirectExchange));
    }

    @Test
    void eliminarColaInexistenteDa404() {
        when(admin.getQueueProperties("fantasma")).thenReturn(null);
        assertThrows(ApiException.class, () -> service.deleteQueue("fantasma"));
    }
}
