package cl.duoc.pedidos360.orders;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import cl.duoc.pedidos360.common.ApiException;

/** Llamadas síncronas a ms-pedidos360-catalog reenviando el token del usuario. */
@Component
class CatalogClient {
    record Product(String id, String name, long price, int stock) {}

    private final RestClient http;

    CatalogClient(@Value("${catalog.url}") String url, RestClient.Builder builder) {
        this.http = builder.baseUrl(url).requestInterceptor((req, body, exec) -> {
            if (SecurityContextHolder.getContext().getAuthentication() instanceof JwtAuthenticationToken t) {
                req.getHeaders().setBearerAuth(t.getToken().getTokenValue());
            }
            return exec.execute(req, body);
        }).build();
    }

    List<Product> products() {
        return http.get().uri("/api/catalog/products").retrieve().body(new ParameterizedTypeReference<>() {});
    }

    void adjustStock(String productId, int delta) {
        try {
            http.patch().uri("/api/catalog/products/{id}/stock", productId).body(Map.of("delta", delta))
                    .retrieve().toBodilessEntity();
        } catch (HttpClientErrorException.Conflict e) {
            throw new ApiException(HttpStatus.CONFLICT, "INSUFFICIENT_STOCK", "Stock insuficiente para " + productId);
        }
    }
}
