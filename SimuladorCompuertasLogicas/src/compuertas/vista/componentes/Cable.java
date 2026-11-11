package compuertas.vista.componentes;

import compuertas.vista.contratos.Dibujable;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;

/**
 * Conexión visual entre el punto de salida de un componente y el punto de
 * entrada de otro. Se dibuja como una curva suave; verde cuando lleva un 1.
 */
public class Cable implements Dibujable {

    private static final Color COLOR_UNO = new Color(0, 150, 60);
    private static final Color COLOR_CERO = new Color(90, 90, 90);
    private static final Color COLOR_SELECCION = new Color(30, 90, 220);
    private static final double TOLERANCIA_CLIC = 7;

    private final PuntoConexion origen;
    private final PuntoConexion destino;
    private boolean seleccionado;

    /** @param origen punto de salida; @param destino punto de entrada */
    public Cable(PuntoConexion origen, PuntoConexion destino) {
        if (origen == null || destino == null) {
            throw new IllegalArgumentException("El cable necesita origen y destino");
        }
        this.origen = origen;
        this.destino = destino;
    }

    public PuntoConexion getOrigen() {
        return origen;
    }

    public PuntoConexion getDestino() {
        return destino;
    }

    /** @return true si el cable toca al componente indicado por cualquiera de sus extremos. */
    public boolean conecta(ComponenteVisual componente) {
        return origen.getComponente() == componente || destino.getComponente() == componente;
    }

    public boolean isSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(boolean seleccionado) {
        this.seleccionado = seleccionado;
    }

    /** Curva entre dos puntos con tangentes horizontales; también la usa el cable provisional. */
    public static CubicCurve2D crearCurva(double x1, double y1, double x2, double y2) {
        double holgura = Math.max(40, Math.abs(x2 - x1) / 2);
        return new CubicCurve2D.Double(x1, y1, x1 + holgura, y1, x2 - holgura, y2, x2, y2);
    }

    private CubicCurve2D crearCurva() {
        return crearCurva(origen.getXAbsoluta(), origen.getYAbsoluta(),
                destino.getXAbsoluta(), destino.getYAbsoluta());
    }

    @Override
    public void dibujar(Graphics2D g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (seleccionado) {
                g2.setColor(COLOR_SELECCION);
                g2.setStroke(new BasicStroke(5f));
            } else {
                g2.setColor(origen.getValor() ? COLOR_UNO : COLOR_CERO);
                g2.setStroke(new BasicStroke(3f));
            }
            g2.draw(crearCurva());
        } finally {
            g2.dispose();
        }
    }

    /** @return true si el punto (px, py) está lo bastante cerca de la curva. */
    public boolean contiene(int px, int py) {
        PathIterator it = crearCurva().getPathIterator(null, 1.0);
        double[] c = new double[6];
        double xAnterior = 0;
        double yAnterior = 0;
        while (!it.isDone()) {
            int tipo = it.currentSegment(c);
            if (tipo == PathIterator.SEG_LINETO
                    && Line2D.ptSegDist(xAnterior, yAnterior, c[0], c[1], px, py) <= TOLERANCIA_CLIC) {
                return true;
            }
            xAnterior = c[0];
            yAnterior = c[1];
            it.next();
        }
        return false;
    }
}
