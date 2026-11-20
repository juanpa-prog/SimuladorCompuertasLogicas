package compuertas.vista.componentes;

import compuertas.modelo.Circuito;
import compuertas.modelo.EntradaLogica;
import compuertas.modelo.FuenteLogica;
import compuertas.persistencia.PosicionesCircuito.Tipo;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;

/**
 * Caja que representa una {@link EntradaLogica} (A, B, C...). Un clic
 * simple sobre su punto de salida alterna el valor 0 / 1.
 */
public class VisualEntrada extends ComponenteVisual {

    private static final int LADO = 44;
    private static final Font FUENTE_NOMBRE = new Font(Font.SANS_SERIF, Font.BOLD, 16);
    private static final Font FUENTE_VALOR = new Font(Font.MONOSPACED, Font.BOLD, 14);

    private final EntradaLogica entrada;

    public VisualEntrada(EntradaLogica entrada) {
        this(entrada, POSICION_DEFECTO, POSICION_DEFECTO);
    }

    public VisualEntrada(EntradaLogica entrada, int x, int y) {
        super(x, y);
        if (entrada == null) {
            throw new IllegalArgumentException("La entrada asociada es obligatoria");
        }
        this.entrada = entrada;
        agregarPin(PuntoConexion.Sentido.SALIDA, 0, LADO + 32, LADO / 2, LADO, LADO / 2, entrada::getValor);
    }

    public EntradaLogica getEntrada() {
        return entrada;
    }

    @Override
    protected Shape crearForma() {
        return new RoundRectangle2D.Double(0, 0, LADO, LADO, 12, 12);
    }

    @Override
    protected Color getColorRelleno() {
        return new Color(255, 243, 200);
    }

    @Override
    public FuenteLogica getFuente() {
        return entrada;
    }

    @Override
    public void alActivarPin(PuntoConexion pin) {
        entrada.alternar();
    }

    @Override
    public void eliminarDelModelo(Circuito circuito) {
        circuito.eliminarEntrada(entrada);
    }

    @Override
    public Tipo getTipoElemento() {
        return Tipo.ENTRADA;
    }

    @Override
    public String getNombreElemento() {
        return entrada.getNombre();
    }

    @Override
    protected void dibujarDetalles(Graphics2D g) {
        dibujarCentrado(g, entrada.getNombre(), FUENTE_NOMBRE, isMiniatura() ? 27 : 19);
        if (!isMiniatura()) {
            dibujarCentrado(g, entrada.getValor() ? "1" : "0", FUENTE_VALOR, 37);
        }
    }

    static void dibujarCentrado(Graphics2D g, String texto, Font fuente, int baseY) {
        g.setFont(fuente);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(texto, (LADO - fm.stringWidth(texto)) / 2, baseY);
    }
}
