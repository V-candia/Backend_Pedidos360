package cl.duoc.pedidos360.common;

import java.time.Instant;

public record ApiError(int status, String error, String message, Instant timestamp) {}
