package cl.duoc.pedidos360.notify;

import java.io.IOException;
import java.nio.file.Files;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.pedidos360.common.ApiException;

/** Descarga el PDF que generó InvoiceListener al facturar un pedido. */
@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('Admin')")
    ResponseEntity<ByteArrayResource> download(@PathVariable String orderId) throws IOException {
        var file = InvoiceStorage.file(orderId);
        if (!file.exists()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Aún no se ha generado la factura de este pedido");
        }
        var body = new ByteArrayResource(Files.readAllBytes(file.toPath()));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + orderId + ".pdf\"")
                .body(body);
    }
}
