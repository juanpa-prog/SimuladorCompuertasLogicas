package compuertas.vista.componentes;

import compuertas.modelo.Circuito;
import compuertas.modelo.FuenteLogica;
import compuertas.modelo.excepciones.ConexionInvalidaException;
import compuertas.vista.contratos.Arrastrable;
import compuertas.vista.contratos.Dibujable;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * Superclase de todo lo que se ve en el lienzo. Implementa los contratos
 * {@link Dibujable} y {@link Arrastrable}.
 *
 * Guarda la posición (x, y) y los puntos de conexión; la VISTA conoce la
 * posición, el MODELO no. Cada subclase solo define su forma
 * ({@link #crearForma()}) y a qué elemento del modelo está asociada.
 *
 * El dibujo sigue la técnica del profesor: g.create(), translate(x, y),
 * relleno + contorno de un Shape, y dispose().
 */
public abstract class ComponenteVisual implements Dibujable, Arrastrable {

    /** Posición usada cuando no se indica una explícita. */
    public static final int POSICION_DEFECTO = 100;

    protected static final Color COLOR_RELLENO = new Color(220, 240, 255);
    private static final Color COLOR_SELECCION = new Color(30, 90, 220);

    private int x;
    private int y;
    private boolean seleccionado;
    private boolean miniatura;
    private final List<PuntoConexion> pines = new ArrayList<>();

    // --- Constructores sobrecargados: posición por defecto o explícita ---

    protected ComponenteVisual() {
        this(POSICION_DEFECTO, POSICION_DEFECTO);
    }

    protected ComponenteVisual(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // ---------------------------------------------------------------
    // Lo que cada subclase define (polimorfismo)
    // ---------------------------------------------------------------

    /** Forma del cuerpo, en coordenadas relativas al componente. Sirve para dibujar y para detectar clics. */
    protected abstract Shape crearForma();

    /** Quita del modelo el elemento asociado (el controlador lo invoca antes de quitar la vista). */
    public abstract void eliminarDelModelo(Circuito circuito);

    protected Color getColorRelleno() {
        return COLOR_RELLENO;
    }

    /** Adornos extra dentro del cuerpo (textos, arcos...). Por defecto ninguno. */
    protected void dibujarDetalles(Graphics2D g) {
    }

    /** Fuente que este componente entrega por su salida; null si no tiene salida. */
    public FuenteLogica getFuente() {
        return null;
    }

    /** Conecta una fuente a la entrada indicada. Por defecto el componente no admite entradas. */
    public void conectarEntrada(Circuito circuito, int indice, FuenteLogica fuente) {
        throw new ConexionInvalidaException("Este componente no tiene entradas.");
    }

    public void desconectarEntrada(Circuito circuito, int indice) {
        throw new ConexionInvalidaException("Este componente no tiene entradas.");
    }

    /** Reacción a un clic simple sobre uno de sus puntos. Por defecto, ninguna. */
    public void alActivarPin(PuntoConexion pin) {
    }

    // ---------------------------------------------------------------
    // Pines
    // ---------------------------------------------------------------

    protected final void agregarPin(PuntoConexion.Sentido sentido, int indice,
                                    int puntaX, int puntaY, int baseX, int baseY, BooleanSupplier valor) {
        pines.add(new PuntoConexion(this, sentido, indice, puntaX, puntaY, baseX, baseY, valor));
    }

    public List<PuntoConexion> getPines() {
        return Collections.unmodifiableList(pines);
    }

    public PuntoConexion getPinSalida() {
        for (PuntoConexion pin : pines) {
            if (pin.getSentido() == PuntoConexion.Sentido.SALIDA) {
                return pin;
            }
        }
        return null;
    }

    public PuntoConexion getPinEntrada(int indice) {
        for (PuntoConexion pin : pines) {
            if (pin.getSentido() == PuntoConexion.Sentido.ENTRADA && pin.getIndice() == indice) {
                return pin;
            }
        }
        return null;
    }

    /** @return el pin bajo el punto (px, py) del lienzo, o null. */
    public PuntoConexion pinEn(int px, int py) {
        for (PuntoConexion pin : pines) {
            if (pin.contiene(px, py)) {
                return pin;
            }
        }
        return null;
    }

    // ---------------------------------------------------------------
    // Contrato Dibujable
    // ---------------------------------------------------------------

    @Override
    public void dibujar(Graphics2D g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.translate(x, y);

            // 1) cables cortos (pines): se dibujan primero para que el cuerpo tape su tramo interno
            g2.setStroke(new BasicStroke(3f));
            g2.setColor(Color.BLACK);
            for (PuntoConexion pin : pines) {
                pin.dibujarLinea(g2);
            }

            // 2) cuerpo: relleno y contorno
            Shape forma = crearForma();
            g2.setColor(getColorRelleno());
            g2.fill(forma);
            g2.setColor(seleccionado ? COLOR_SELECCION : Color.BLACK);
            g2.setStroke(new BasicStroke(seleccionado ? 4f : 3f));
            g2.draw(forma);

            // 3) adornos propios de la subclase
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(3f));
            dibujarDetalles(g2);

            // 4) indicadores 0 / 1
            for (PuntoConexion pin : pines) {
                pin.dibujarIndicador(g2);
            }
        } finally {
            g2.dispose();
        }
    }

    // ---------------------------------------------------------------
    // Contrato Arrastrable
    // ---------------------------------------------------------------

    @Override
    public boolean contiene(int px, int py) {
        return crearForma().contains(px - x, py - y);
    }

    @Override
    public void moverA(int nuevoX, int nuevoY) {
        this.x = nuevoX;
        this.y = nuevoY;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    // ---------------------------------------------------------------
    // Utilidades
    // ---------------------------------------------------------------

    /** En modo miniatura (paleta) se omiten nombres y valores, que no tienen sentido allí. */
    public boolean isMiniatura() {
        return miniatura;
    }

    public void setMiniatura(boolean miniatura) {
        this.miniatura = miniatura;
    }

    public boolean isSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(boolean seleccionado) {
        this.seleccionado = seleccionado;
    }

    /** Rectángulo que envuelve cuerpo y pines, en coordenadas del lienzo. */
    public Rectangle getLimites() {
        Rectangle r = crearForma().getBounds();
        for (PuntoConexion pin : pines) {
            r.add(pin.getXAbsoluta() - x - 7, pin.getYAbsoluta() - y - 7);
            r.add(pin.getXAbsoluta() - x + 7, pin.getYAbsoluta() - y + 7);
        }
        r.translate(x, y);
        return r;
    }
}
