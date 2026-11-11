package compuertas.vista.componentes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.util.function.BooleanSupplier;

/**
 * Extremo de un cable (entrada o salida de un componente) con su
 * indicador de valor:
 *   círculo VACÍO   = 0
 *   círculo RELLENO = 1
 *
 * El punto no calcula nada: solo pregunta el valor (BooleanSupplier) y
 * lo dibuja. También es la zona donde se inician y terminan los cables.
 */
public class PuntoConexion {

    public enum Sentido { ENTRADA, SALIDA }

    private static final int RADIO = 6;
    private static final int RADIO_CLIC = 10;
    private static final Color COLOR_UNO = new Color(0, 170, 70);
    private static final Color COLOR_RESALTE = new Color(255, 140, 0);

    private final ComponenteVisual componente;
    private final Sentido sentido;
    private final int indice;
    private final int puntaX;
    private final int puntaY;
    private final int baseX;
    private final int baseY;
    private final BooleanSupplier valor;
    private boolean resaltado;

    /**
     * @param puntaX, puntaY extremo libre del pin (donde va el indicador), relativo al componente
     * @param baseX, baseY   punto donde el pin toca el cuerpo, relativo al componente
     */
    PuntoConexion(ComponenteVisual componente, Sentido sentido, int indice,
                  int puntaX, int puntaY, int baseX, int baseY, BooleanSupplier valor) {
        this.componente = componente;
        this.sentido = sentido;
        this.indice = indice;
        this.puntaX = puntaX;
        this.puntaY = puntaY;
        this.baseX = baseX;
        this.baseY = baseY;
        this.valor = valor;
    }

    public ComponenteVisual getComponente() {
        return componente;
    }

    public Sentido getSentido() {
        return sentido;
    }

    /** Posición de la entrada dentro de su compuerta (0 = primera). */
    public int getIndice() {
        return indice;
    }

    /** Coordenada X del indicador en el lienzo. */
    public int getXAbsoluta() {
        return componente.getX() + puntaX;
    }

    /** Coordenada Y del indicador en el lienzo. */
    public int getYAbsoluta() {
        return componente.getY() + puntaY;
    }

    public boolean getValor() {
        return valor.getAsBoolean();
    }

    public void setResaltado(boolean resaltado) {
        this.resaltado = resaltado;
    }

    /** Zona sensible al mouse: un poco mayor que el punto dibujado. */
    public boolean contiene(int px, int py) {
        int dx = px - getXAbsoluta();
        int dy = py - getYAbsoluta();
        return dx * dx + dy * dy <= RADIO_CLIC * RADIO_CLIC;
    }

    // Se invocan desde ComponenteVisual con el sistema de coordenadas ya trasladado.

    void dibujarLinea(Graphics2D g) {
        g.drawLine(baseX, baseY, puntaX, puntaY);
    }

    void dibujarIndicador(Graphics2D g) {
        Ellipse2D punto = new Ellipse2D.Double(puntaX - RADIO, puntaY - RADIO, 2 * RADIO, 2 * RADIO);
        g.setColor(valor.getAsBoolean() ? COLOR_UNO : Color.WHITE);
        g.fill(punto);
        g.setStroke(new BasicStroke(2f));
        g.setColor(Color.BLACK);
        g.draw(punto);
        if (resaltado) {
            int r = RADIO + 3;
            g.setColor(COLOR_RESALTE);
            g.draw(new Ellipse2D.Double(puntaX - r, puntaY - r, 2 * r, 2 * r));
        }
    }
}
