package compuertas.vista.componentes;

import compuertas.modelo.Circuito;
import compuertas.modelo.FuenteLogica;
import compuertas.modelo.SalidaLogica;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;

/** Caja que representa una {@link SalidaLogica} (F, G...) y muestra su valor. */
public class VisualSalida extends ComponenteVisual {

    private static final int LADO = 44;
    private static final Font FUENTE_NOMBRE = new Font(Font.SANS_SERIF, Font.BOLD, 16);
    private static final Font FUENTE_VALOR = new Font(Font.MONOSPACED, Font.BOLD, 14);

    private final SalidaLogica salida;

    public VisualSalida(SalidaLogica salida) {
        this(salida, POSICION_DEFECTO, POSICION_DEFECTO);
    }

    public VisualSalida(SalidaLogica salida, int x, int y) {
        super(x, y);
        if (salida == null) {
            throw new IllegalArgumentException("La salida asociada es obligatoria");
        }
        this.salida = salida;
        agregarPin(PuntoConexion.Sentido.ENTRADA, 0, -32, LADO / 2, 0, LADO / 2,
                () -> ValorSeguro.de(salida.getFuente()));
    }

    public SalidaLogica getSalida() {
        return salida;
    }

    @Override
    protected Shape crearForma() {
        return new RoundRectangle2D.Double(0, 0, LADO, LADO, 12, 12);
    }

    @Override
    protected Color getColorRelleno() {
        return new Color(215, 245, 215);
    }

    @Override
    public void conectarEntrada(Circuito circuito, int indice, FuenteLogica fuente) {
        circuito.conectarSalida(salida, fuente);
    }

    @Override
    public void desconectarEntrada(Circuito circuito, int indice) {
        salida.desconectar();
    }

    @Override
    public void eliminarDelModelo(Circuito circuito) {
        circuito.eliminarSalida(salida);
    }

    @Override
    protected void dibujarDetalles(Graphics2D g) {
        VisualEntrada.dibujarCentrado(g, salida.getNombre(), FUENTE_NOMBRE, isMiniatura() ? 27 : 19);
        if (!isMiniatura()) {
            VisualEntrada.dibujarCentrado(g, ValorSeguro.texto(salida.getFuente()), FUENTE_VALOR, 37);
        }
    }
}
