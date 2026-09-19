package cl.duoc.pedidos360.common;

import java.time.Instant;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ApiException.class)
    ResponseEntity<ApiError> api(ApiException e) {
        return of(e.status, e.code, e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiError> denied(AccessDeniedException e) {
        return of(HttpStatus.FORBIDDEN, "FORBIDDEN", "Rol sin permiso para este endpoint");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> invalid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + " " + f.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return of(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", msg);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    ResponseEntity<ApiError> unreadable(Exception e) {
        return of(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Solicitud inválida");
    }

    private static ResponseEntity<ApiError> of(HttpStatus s, String code, String msg) {
        return ResponseEntity.status(s).body(new ApiError(s.value(), code, msg, Instant.now()));
    }
}
