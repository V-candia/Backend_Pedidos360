package cl.duoc.pedidos360.notify;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import cl.duoc.pedidos360.common.InvoiceCommand;
import cl.duoc.pedidos360.common.MqNames;

/** Genera el PDF de detalle del pedido al facturar (disparo manual, ver OrderController.generateInvoice). */
@Component
class InvoiceListener {
    private static final Logger log = LoggerFactory.getLogger(InvoiceListener.class);
    private static final PDType1Font FONT = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDType1Font FONT_BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    /** Factura con total negativo o sin items: no recuperable, directo a q.cmd.invoice.dlq. Lo demás reintenta. */
    @RabbitListener(queues = MqNames.QUEUE_INVOICE)
    void onInvoice(InvoiceCommand c) throws IOException {
        if (c.total() < 0 || c.items() == null || c.items().isEmpty()) {
            throw new AmqpRejectAndDontRequeueException("Factura inválida (total o items): " + c.eventId());
        }
        var file = InvoiceStorage.file(c.orderId());
        try (PDDocument doc = new PDDocument()) {
            var page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                float y = 760;
                y = line(cs, FONT_BOLD, 14, 50, y, "Pedidos360 - Factura");
                y = line(cs, FONT, 11, 50, y - 10, "Pedido: " + c.orderId());
                y = line(cs, FONT, 11, 50, y, "Cliente: " + c.customerName());
                y -= 20;
                y = line(cs, FONT_BOLD, 11, 50, y, "Cant.  Producto                              Precio");
                for (var i : c.items()) {
                    y = line(cs, FONT, 10, 50, y, String.format("%-6d %-38s $%d", i.qty(), i.productName(), i.price()));
                }
                y -= 10;
                line(cs, FONT_BOLD, 12, 50, y, "Total: $" + c.total());
            }
            doc.save(file);
        }
        log.info("[factura] pedido={} cliente={} total={} -> {}", c.orderId(), c.customerName(), c.total(), file.getAbsolutePath());
    }

    private static float line(PDPageContentStream cs, PDType1Font font, int size, float x, float y, String text) throws IOException {
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
        return y - (size + 6);
    }
}
