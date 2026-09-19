# mock-oauth2-server sobre JRE Alpine (la app es Java puro; solo se reutiliza su /app)
FROM ghcr.io/navikt/mock-oauth2-server:2.1.11 AS src

FROM eclipse-temurin:21-jre-alpine
COPY --from=src /app /app
ENTRYPOINT ["java", "-cp", "@/app/jib-classpath-file", "no.nav.security.mock.oauth2.StandaloneMockOAuth2ServerKt"]
