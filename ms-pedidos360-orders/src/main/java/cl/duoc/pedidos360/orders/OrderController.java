package cl.duoc.pedidos360.orders;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    record Line(@NotBlank String productId, @Min(1) int qty) {}

    record CreateOrder(@NotBlank String customerName, @NotEmpty List<@Valid Line> items) {}

    record StatusUpdate(@NotNull OrderStatus status) {}

    private final OrderService service;

    OrderController(OrderService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('Admin','Operador','Cliente')")
    List<Order> list(@RequestParam(required = false) OrderStatus status, @RequestParam(required = false) Instant from,
                     @RequestParam(required = false) Instant to, Authentication auth) {
        return service.list(owner(auth), status, from, to);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin','Operador','Cliente')")
    Order get(@PathVariable String id, Authentication auth) {
        return service.get(id, owner(auth));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('Admin','Operador','Cliente')")
    Order create(@Valid @RequestBody CreateOrder r, Authentication auth) {
        var items = r.items().stream().map(l -> new OrderService.NewItem(l.productId(), l.qty())).toList();
        return service.create(r.customerName(), items, who(auth));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('Operador','Admin')")
    Order updateStatus(@PathVariable String id, @Valid @RequestBody StatusUpdate r, Authentication auth) {
        return service.updateStatus(id, r.status(), who(auth));
    }

    /** null para Admin/Operador (ven todo); para Cliente, su identidad del token (nunca un id enviado por el cliente). */
    private String owner(Authentication auth) {
        boolean staff = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_Admin") || a.getAuthority().equals("ROLE_Operador"));
        return staff ? null : who(auth);
    }

    private String who(Authentication auth) {
        Jwt jwt = ((JwtAuthenticationToken) auth).getToken();
        for (String claim : new String[] {"preferred_username", "email"}) {
            if (jwt.getClaimAsString(claim) != null) return jwt.getClaimAsString(claim);
        }
        return jwt.getSubject();
    }
}
