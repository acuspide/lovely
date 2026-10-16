package com.example.ecommerce.infrastructure.pdf;

import com.example.ecommerce.application.DatosNegocio;
import com.example.ecommerce.application.DocumentoFactura;
import com.example.ecommerce.application.GeneradorPdfFactura;
import com.example.ecommerce.domain.entity.Factura;
import com.example.ecommerce.domain.entity.Pedido;
import com.example.ecommerce.domain.valueobject.DatosEntrega;
import com.example.ecommerce.domain.valueobject.DesgloseIva;
import com.example.ecommerce.domain.valueobject.DetallePedido;
import com.example.ecommerce.domain.valueobject.TipoEntrega;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfGState;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Patrón: Adapter — implementa el puerto {@link GeneradorPdfFactura} con la
 * librería OpenPDF (RF-08.3).
 *
 * <p>El documento tiene: encabezado con los datos del negocio y el número de
 * factura, datos de la clienta y de la entrega, tabla de productos (cantidad,
 * precio unitario y subtotal), desglose base + IVA = total y una nota sobre
 * el alcance del documento (no reemplaza la factura electrónica de la DIAN,
 * RNF-11). Si la factura está anulada (RF-08.6) cada página lleva la marca
 * "ANULADA".</p>
 *
 * <p>Las fechas se muestran en la hora de Colombia; en la base están en UTC.</p>
 */
public class GeneradorPdfFacturaOpenPdf implements GeneradorPdfFactura {

    private static final ZoneId ZONA_COLOMBIA = ZoneId.of("America/Bogota");
    private static final Locale ES_CO = Locale.forLanguageTag("es-CO");
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy, h:mm a", ES_CO);

    private static final Color COLOR_MARCA = new Color(0x8E, 0x2C, 0x6E);
    private static final Color COLOR_SUAVE = new Color(0x6B, 0x5B, 0x6E);
    private static final Color COLOR_FONDO_TABLA = new Color(0xF7, 0xEE, 0xF4);
    private static final Color COLOR_BORDE = new Color(0xE2, 0xD3, 0xDE);

    private static final Font TITULO_NEGOCIO = new Font(Font.HELVETICA, 18, Font.BOLD, COLOR_MARCA);
    private static final Font TITULO_FACTURA = new Font(Font.HELVETICA, 13, Font.BOLD, COLOR_MARCA);
    private static final Font SECCION = new Font(Font.HELVETICA, 11, Font.BOLD, COLOR_MARCA);
    private static final Font NORMAL = new Font(Font.HELVETICA, 9.5f, Font.NORMAL, Color.BLACK);
    private static final Font NEGRITA = new Font(Font.HELVETICA, 9.5f, Font.BOLD, Color.BLACK);
    private static final Font SUAVE = new Font(Font.HELVETICA, 8.5f, Font.NORMAL, COLOR_SUAVE);
    private static final Font ENCABEZADO_TABLA = new Font(Font.HELVETICA, 9.5f, Font.BOLD, COLOR_MARCA);
    private static final Font TOTAL = new Font(Font.HELVETICA, 12, Font.BOLD, COLOR_MARCA);
    private static final Font MARCA_ANULADA = new Font(Font.HELVETICA, 90, Font.BOLD, new Color(0xC6, 0x28, 0x28));

    @Override
    public byte[] generar(DocumentoFactura documento) {
        Factura factura = documento.factura();
        Pedido pedido = documento.pedido();
        var salida = new ByteArrayOutputStream();
        Document pdf = new Document(PageSize.LETTER, 48, 48, 48, 48);
        try {
            PdfWriter escritor = PdfWriter.getInstance(pdf, salida);
            if (factura.estaAnulada()) {
                escritor.setPageEvent(new MarcaAnulada());
            }
            pdf.addTitle("Factura " + factura.getNumero().valor());
            pdf.addAuthor(documento.negocio().razonSocial());
            pdf.open();

            pdf.add(encabezado(documento.negocio(), factura, pedido));
            pdf.add(espacio(14));
            pdf.add(datosClientaYEntrega(factura, pedido.getEntrega()));
            pdf.add(espacio(14));
            pdf.add(tablaProductos(pedido));
            pdf.add(espacio(6));
            pdf.add(totales(documento.desglose()));
            pdf.add(espacio(18));
            agregarNotas(pdf, factura);
        } catch (DocumentException ex) {
            throw new GeneracionPdfException("No se pudo generar la factura " + factura.getNumero().valor(), ex);
        } finally {
            if (pdf.isOpen()) {
                pdf.close();
            }
        }
        return salida.toByteArray();
    }

    // ---- secciones ----

    private PdfPTable encabezado(DatosNegocio negocio, Factura factura, Pedido pedido) throws DocumentException {
        PdfPTable tabla = new PdfPTable(new float[] {3f, 2f});
        tabla.setWidthPercentage(100);

        PdfPCell izquierda = celdaSinBorde();
        izquierda.addElement(new Paragraph(negocio.razonSocial(), TITULO_NEGOCIO));
        izquierda.addElement(new Paragraph("NIT " + negocio.nit(), NORMAL));
        agregarSiHay(izquierda, unir(negocio.direccion(), negocio.ciudad()), NORMAL);
        agregarSiHay(izquierda, unir(negocio.telefono(), negocio.email()), NORMAL);
        tabla.addCell(izquierda);

        PdfPCell derecha = celdaSinBorde();
        derecha.setHorizontalAlignment(Element.ALIGN_RIGHT);
        derecha.addElement(alineado(new Paragraph("FACTURA DE VENTA", TITULO_FACTURA)));
        derecha.addElement(alineado(new Paragraph(factura.getNumero().valor(), TITULO_FACTURA)));
        derecha.addElement(alineado(new Paragraph("Fecha: " + fecha(factura.getEmitidaEn()), NORMAL)));
        derecha.addElement(alineado(new Paragraph("Pedido: " + pedido.getNumero().valor(), NORMAL)));
        if (factura.estaAnulada()) {
            derecha.addElement(alineado(new Paragraph("ANULADA", new Font(Font.HELVETICA, 11, Font.BOLD,
                    MARCA_ANULADA.getColor()))));
        }
        tabla.addCell(derecha);
        return tabla;
    }

    private PdfPTable datosClientaYEntrega(Factura factura, DatosEntrega entrega) throws DocumentException {
        PdfPTable tabla = new PdfPTable(new float[] {1f, 1f});
        tabla.setWidthPercentage(100);

        PdfPCell clienta = celdaRecuadro();
        clienta.addElement(new Paragraph("Clienta", SECCION));
        clienta.addElement(new Paragraph(factura.getNombreClienta(), NEGRITA));
        clienta.addElement(new Paragraph(factura.getEmailClienta(), NORMAL));
        factura.getTelefonoClienta().ifPresent(tel -> clienta.addElement(new Paragraph("Tel. " + tel, NORMAL)));
        tabla.addCell(clienta);

        PdfPCell celdaEntrega = celdaRecuadro();
        celdaEntrega.addElement(new Paragraph("Entrega", SECCION));
        celdaEntrega.addElement(new Paragraph(etiqueta(entrega.getTipo()), NEGRITA));
        celdaEntrega.addElement(new Paragraph(entrega.getNombreContacto() + " · " + entrega.getTelefono().getValor(),
                NORMAL));
        if (entrega.getDireccion() != null) {
            celdaEntrega.addElement(new Paragraph(unir(entrega.getDireccion(), entrega.getCiudad()), NORMAL));
        }
        tabla.addCell(celdaEntrega);
        return tabla;
    }

    private PdfPTable tablaProductos(Pedido pedido) throws DocumentException {
        PdfPTable tabla = new PdfPTable(new float[] {5f, 1.2f, 2f, 2f});
        tabla.setWidthPercentage(100);
        tabla.setHeaderRows(1);
        tabla.addCell(encabezadoTabla("Producto", Element.ALIGN_LEFT));
        tabla.addCell(encabezadoTabla("Cant.", Element.ALIGN_RIGHT));
        tabla.addCell(encabezadoTabla("Precio unitario", Element.ALIGN_RIGHT));
        tabla.addCell(encabezadoTabla("Subtotal", Element.ALIGN_RIGHT));
        for (DetallePedido detalle : pedido.getDetalles()) {
            tabla.addCell(celdaTabla(detalle.getNombreArticulo(), Element.ALIGN_LEFT, NORMAL));
            tabla.addCell(celdaTabla(String.valueOf(detalle.getCantidad()), Element.ALIGN_RIGHT, NORMAL));
            tabla.addCell(celdaTabla(dinero(detalle.getPrecioUnitario().valor()), Element.ALIGN_RIGHT, NORMAL));
            tabla.addCell(celdaTabla(dinero(detalle.subtotal()), Element.ALIGN_RIGHT, NORMAL));
        }
        return tabla;
    }

    private PdfPTable totales(DesgloseIva desglose) throws DocumentException {
        PdfPTable tabla = new PdfPTable(new float[] {6.2f, 2f, 2f});
        tabla.setWidthPercentage(100);
        filaTotal(tabla, "Subtotal (sin IVA)", dinero(desglose.base()), NORMAL);
        filaTotal(tabla, "IVA (" + desglose.porcentaje() + " %)", dinero(desglose.iva()), NORMAL);
        filaTotal(tabla, "Total", dinero(desglose.total()), TOTAL);
        return tabla;
    }

    private void agregarNotas(Document pdf, Factura factura) throws DocumentException {
        if (factura.estaAnulada()) {
            pdf.add(new Paragraph("Factura anulada el " + fecha(factura.getAnuladaEn().orElseThrow())
                    + " por cancelación del pedido.", NEGRITA));
        }
        pdf.add(new Paragraph("Los precios incluyen IVA. Gracias por tu compra.", SUAVE));
        pdf.add(new Paragraph("Documento generado por el Sistema de Gestión Lovely Girl. No reemplaza la factura "
                + "electrónica de venta ante la DIAN.", SUAVE));
    }

    // ---- utilidades de formato ----

    private static void filaTotal(PdfPTable tabla, String etiqueta, String valor, Font fuente) {
        tabla.addCell(celdaSinBorde());
        PdfPCell celdaEtiqueta = new PdfPCell(new Phrase(etiqueta, fuente));
        celdaEtiqueta.setBorder(Rectangle.NO_BORDER);
        celdaEtiqueta.setHorizontalAlignment(Element.ALIGN_RIGHT);
        celdaEtiqueta.setPadding(4);
        tabla.addCell(celdaEtiqueta);
        PdfPCell celdaValor = new PdfPCell(new Phrase(valor, fuente));
        celdaValor.setBorder(Rectangle.NO_BORDER);
        celdaValor.setHorizontalAlignment(Element.ALIGN_RIGHT);
        celdaValor.setPadding(4);
        tabla.addCell(celdaValor);
    }

    private static PdfPCell encabezadoTabla(String texto, int alineacion) {
        PdfPCell celda = celdaTabla(texto, alineacion, ENCABEZADO_TABLA);
        celda.setBackgroundColor(COLOR_FONDO_TABLA);
        return celda;
    }

    private static PdfPCell celdaTabla(String texto, int alineacion, Font fuente) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, fuente));
        celda.setHorizontalAlignment(alineacion);
        celda.setBorder(Rectangle.BOTTOM);
        celda.setBorderColor(COLOR_BORDE);
        celda.setPadding(6);
        return celda;
    }

    private static PdfPCell celdaSinBorde() {
        PdfPCell celda = new PdfPCell();
        celda.setBorder(Rectangle.NO_BORDER);
        return celda;
    }

    private static PdfPCell celdaRecuadro() {
        PdfPCell celda = new PdfPCell();
        celda.setBorderColor(COLOR_BORDE);
        celda.setPadding(10);
        celda.setPaddingTop(4);
        return celda;
    }

    private static Paragraph alineado(Paragraph parrafo) {
        parrafo.setAlignment(Element.ALIGN_RIGHT);
        return parrafo;
    }

    private static Paragraph espacio(float alto) {
        Paragraph parrafo = new Paragraph(" ", NORMAL);
        parrafo.setLeading(alto);
        return parrafo;
    }

    private static void agregarSiHay(PdfPCell celda, String texto, Font fuente) {
        if (!texto.isBlank()) {
            celda.addElement(new Paragraph(texto, fuente));
        }
    }

    /** Une los textos que no estén vacíos con " · ". */
    private static String unir(String... partes) {
        var resultado = new StringBuilder();
        for (String parte : partes) {
            if (parte != null && !parte.isBlank()) {
                if (!resultado.isEmpty()) {
                    resultado.append(" · ");
                }
                resultado.append(parte.strip());
            }
        }
        return resultado.toString();
    }

    private static String dinero(BigDecimal valor) {
        var simbolos = new DecimalFormatSymbols(ES_CO);
        simbolos.setGroupingSeparator('.');
        simbolos.setDecimalSeparator(',');
        DecimalFormat formato = new DecimalFormat("#,##0.00", simbolos);
        return "$ " + formato.format(valor);
    }

    private static String fecha(Instant instante) {
        return FORMATO_FECHA.format(instante.atZone(ZONA_COLOMBIA));
    }

    private static String etiqueta(TipoEntrega tipo) {
        return switch (tipo) {
            case RETIRO_EN_TIENDA -> "Retiro en tienda";
            case DOMICILIO -> "Domicilio";
            case ENVIO -> "Envío nacional";
        };
    }

    /** RF-08.6: marca de agua "ANULADA" en diagonal, semitransparente, en cada página. */
    private static final class MarcaAnulada extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter escritor, Document documento) {
            PdfContentByte lienzo = escritor.getDirectContent();
            lienzo.saveState();
            PdfGState transparencia = new PdfGState();
            transparencia.setFillOpacity(0.18f);
            lienzo.setGState(transparencia);
            Rectangle pagina = documento.getPageSize();
            ColumnText.showTextAligned(lienzo, Element.ALIGN_CENTER, new Phrase("ANULADA", MARCA_ANULADA),
                    pagina.getWidth() / 2, pagina.getHeight() / 2, 45);
            lienzo.restoreState();
        }
    }
}
