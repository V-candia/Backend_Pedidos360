# Backend Pedidos360 — entorno local sin Azure

Levantar (desde `infra/local`): `docker compose up -d --build`. Oracle tarda ~1-2 min en quedar listo la primera vez.

## Para el frontend

| Qué | URL (desde el navegador / host) |
|---|---|
| **Base del API (fake API Gateway, con CORS)** | `http://localhost:8090` |
| Emisor de tokens (reemplaza a Entra) | `http://localhost:8080/default/token` |
| RabbitMQ UI | `http://localhost:15672` (guest/guest) |
| Swagger por servicio | `http://localhost:8081/swagger-ui.html` (orders), `8082` catalog, `8084` audit, `8085` report |

Rutas (igual que `docs/api-spec.md`): `/api/orders`, `/api/catalog`, `/api/audit`, `/api/report`.
Si el frontend llama desde *dentro* de su contenedor (SSR/proxy), usar `http://host.docker.internal:8090`.

### Obtener un token (sin MSAL)

```bash
curl -s -X POST http://localhost:8080/default/token \
  -d grant_type=client_credentials -d client_id=admin -d client_secret=x
```

`client_id` elige el usuario y su rol (el `client_secret` es cualquier valor):

| client_id | rol (claim `roles`) | identidad |
|---|---|---|
| `admin` | Admin | admin@pedidos360.local |
| `operador` | Operador | operador@pedidos360.local |
| `cliente` | Cliente | cliente@pedidos360.local |

Respuesta: `{"access_token": "...", ...}`. Enviarlo como `Authorization: Bearer <access_token>`. Dura 24 h.
El frontend necesita un modo dev que haga este POST en vez de MSAL (MSAL no funciona contra este emisor).
Los tokens llevan `aud = api://pedidos360-local`.

### Datos iniciales

No hay seed: crear productos con el token `admin`:

```bash
curl -X POST http://localhost:8090/api/catalog/products -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" -d '{"name":"Pizza","price":4990,"stock":25}'
```

Los emails no se envían: `notify` los loguea (`docker compose logs -f notify`).
Reset total: `docker compose down -v`.
