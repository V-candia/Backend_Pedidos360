package cl.duoc.pedidos360.mqadmin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "cl.duoc.pedidos360")
public class MqAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(MqAdminApplication.class, args);
    }
}
