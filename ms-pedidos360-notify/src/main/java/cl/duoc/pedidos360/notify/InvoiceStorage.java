package cl.duoc.pedidos360.notify;

import java.io.File;

// ponytail: disco local del contenedor, se pierde al recrearlo; pasar a un volumen o S3 si hace falta que persista.
final class InvoiceStorage {
    private static final File DIR = new File("/tmp/invoices");

    private InvoiceStorage() {}

    static File file(String orderId) {
        DIR.mkdirs();
        return new File(DIR, orderId + ".pdf");
    }
}
