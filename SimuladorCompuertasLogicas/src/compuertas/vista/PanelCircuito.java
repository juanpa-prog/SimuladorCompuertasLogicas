package compuertas.vista;

import compuertas.config.Preferencias;
import compuertas.vista.componentes.Cable;
import compuertas.vista.componentes.ComponenteVisual;
import compuertas.vista.componentes.PuntoConexion;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

/**
 * Lienzo del circuito. Contiene una colección de componentes visuales y de
 * cables, los dibuja de forma polimórfica y traduce el mouse en eventos:
 *   - clic en el cuerpo y arrastrar -> mover el componente (técnica del profesor);
 *   - arrastrar de un punto a otro  -> conectar (avisa al escuchador);
 *   - clic simple en un punto       -> avisa al escuchador.
 * Recibe las preferencias por constructor (inyección), no usa el Singleton directamente.
 */
public class PanelCircuito extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final int PASO_CUADRICULA = 20;
    private static final int LIMITE_X = 45;
    private static final int LIMITE_Y = 25;

    private final Preferencias preferencias;
    private final List<ComponenteVisual> componentes = new ArrayList<>();
    private final List<Cable> cables = new ArrayList<>();
    private EscuchadorPanel escuchador;

    // Estado de la interacción en curso
    private ComponenteVisual componenteSeleccionado;
    private Cable cableSeleccionado;
    private ComponenteVisual arrastrado;
    private int offsetX;
    private int offsetY;
    private PuntoConexion pinOrigen;
    private Point mouseActual;
    private PuntoConexion pinResaltado;

    public PanelCircuito(Preferencias preferencias) {
        if (preferencias == null) {
            throw new IllegalArgumentException("Las preferencias son obligatorias");
        }
        this.preferencias = preferencias;
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(1000, 640));
        setFocusable(true);

        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                alPresionar(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                alArrastrar(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                alSoltar(e);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                alMover(e);
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "eliminar");
        getActionMap().put("eliminar", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (escuchador != null) {
                    escuchador.alEliminarSeleccion();
                }
            }
        });
    }

    public void setEscuchador(EscuchadorPanel escuchador) {
        this.escuchador = escuchador;
    }

    // ---------------------------------------------------------------
    // Colecciones encapsuladas
    // ---------------------------------------------------------------

    public void agregarComponente(ComponenteVisual componente) {
        if (componente == null) {
            throw new IllegalArgumentException("El componente es obligatorio");
        }
        componentes.add(componente);
        repaint();
    }

    /** Quita el componente y todos los cables que llegaban o salían de él. */
    public void eliminarComponente(ComponenteVisual componente) {
        cables.removeIf(cable -> cable.conecta(componente));
        componentes.remove(componente);
        if (componenteSeleccionado == componente) {
            componenteSeleccionado = null;
        }
        if (cableSeleccionado != null && !cables.contains(cableSeleccionado)) {
            cableSeleccionado = null;
        }
        repaint();
    }

    /** Agrega el cable; si el punto de entrada ya tenía uno, lo reemplaza. */
    public void agregarCable(Cable cable) {
        Iterator<Cable> it = cables.iterator();
        while (it.hasNext()) {
            Cable existente = it.next();
            if (existente.getDestino() == cable.getDestino()) {
                if (existente == cableSeleccionado) {
                    cableSeleccionado = null;
                }
                it.remove();
            }
        }
        cables.add(cable);
        repaint();
    }

    public void eliminarCable(Cable cable) {
        cables.remove(cable);
        if (cableSeleccionado == cable) {
            cableSeleccionado = null;
        }
        repaint();
    }

    public void limpiar() {
        componentes.clear();
        cables.clear();
        componenteSeleccionado = null;
        cableSeleccionado = null;
        arrastrado = null;
        pinOrigen = null;
        pinResaltado = null;
        repaint();
    }

    public List<ComponenteVisual> getComponentes() {
        return Collections.unmodifiableList(componentes);
    }

    public List<Cable> getCables() {
        return Collections.unmodifiableList(cables);
    }

    public ComponenteVisual getComponenteSeleccionado() {
        return componenteSeleccionado;
    }

    public Cable getCableSeleccionado() {
        return cableSeleccionado;
    }

    /** Primer lugar libre de la columna indicada (para colocar elementos nuevos sin encimarlos). */
    public Point encontrarPosicionLibre(ComponenteVisual nuevo, int xBase) {
        Rectangle propio = nuevo.getLimites();
        // Se recorre la columna preferida de arriba hacia abajo; si se llena, se pasa a la siguiente.
        for (int desplazamiento = 0; desplazamiento < 1000; desplazamiento += 210) {
            for (int y = 40; y <= 560; y += 20) {
                int x = xBase + desplazamiento;
                Rectangle candidato = new Rectangle(x + propio.x - 12, y + propio.y - 22,
                        propio.width + 24, propio.height + 34);
                if (estaLibre(candidato)) {
                    return new Point(x, y);
                }
            }
        }
        return new Point(xBase, LIMITE_Y);
    }

    private boolean estaLibre(Rectangle zona) {
        for (ComponenteVisual otro : componentes) {
            if (otro.getLimites().intersects(zona)) {
                return false;
            }
        }
        return true;
    }

    // ---------------------------------------------------------------
    // Selección
    // ---------------------------------------------------------------

    private void limpiarSeleccion() {
        if (componenteSeleccionado != null) {
            componenteSeleccionado.setSeleccionado(false);
            componenteSeleccionado = null;
        }
        if (cableSeleccionado != null) {
            cableSeleccionado.setSeleccionado(false);
            cableSeleccionado = null;
        }
    }

    private void seleccionarComponente(ComponenteVisual componente) {
        limpiarSeleccion();
        componenteSeleccionado = componente;
        componente.setSeleccionado(true);
    }

    private void seleccionarCable(Cable cable) {
        limpiarSeleccion();
        cableSeleccionado = cable;
        cable.setSeleccionado(true);
    }

    // ---------------------------------------------------------------
    // Búsqueda bajo el mouse (de arriba hacia abajo en el orden de dibujo)
    // ---------------------------------------------------------------

    private PuntoConexion buscarPin(Point p) {
        for (int i = componentes.size() - 1; i >= 0; i--) {
            PuntoConexion pin = componentes.get(i).pinEn(p.x, p.y);
            if (pin != null) {
                return pin;
            }
        }
        return null;
    }

    private ComponenteVisual buscarComponente(Point p) {
        for (int i = componentes.size() - 1; i >= 0; i--) {
            if (componentes.get(i).contiene(p.x, p.y)) {
                return componentes.get(i);
            }
        }
        return null;
    }

    private Cable buscarCable(Point p) {
        for (int i = cables.size() - 1; i >= 0; i--) {
            if (cables.get(i).contiene(p.x, p.y)) {
                return cables.get(i);
            }
        }
        return null;
    }

    // ---------------------------------------------------------------
    // Eventos del mouse
    // ---------------------------------------------------------------

    private void alPresionar(MouseEvent e) {
        requestFocusInWindow();
        if (!SwingUtilities.isLeftMouseButton(e)) {
            return;
        }
        Point p = e.getPoint();

        PuntoConexion pin = buscarPin(p);
        if (pin != null) {
            pinOrigen = pin;
            mouseActual = p;
            return;
        }

        ComponenteVisual componente = buscarComponente(p);
        if (componente != null) {
            seleccionarComponente(componente);
            arrastrado = componente;
            // Distancia exacta del clic respecto a la esquina del componente (evita saltos)
            offsetX = p.x - componente.getX();
            offsetY = p.y - componente.getY();
            // Se lleva al frente para que quede encima de los demás
            componentes.remove(componente);
            componentes.add(componente);
        } else {
            Cable cable = buscarCable(p);
            if (cable != null) {
                seleccionarCable(cable);
            } else {
                limpiarSeleccion();
            }
        }
        repaint();
    }

    private void alArrastrar(MouseEvent e) {
        Point p = e.getPoint();
        if (arrastrado != null) {
            arrastrado.moverA(Math.max(LIMITE_X, p.x - offsetX), Math.max(LIMITE_Y, p.y - offsetY));
        } else if (pinOrigen != null) {
            mouseActual = p;
            resaltar(buscarPin(p));
        }
        repaint();
    }

    private void alSoltar(MouseEvent e) {
        if (pinOrigen != null) {
            PuntoConexion origen = pinOrigen;
            PuntoConexion destino = buscarPin(e.getPoint());
            pinOrigen = null;
            mouseActual = null;
            resaltar(null);
            if (escuchador != null) {
                if (destino == origen) {
                    escuchador.alHacerClicEnPin(origen);
                } else if (destino != null) {
                    escuchador.alConectar(origen, destino);
                }
            }
        }
        arrastrado = null;
        repaint();
    }

    private void alMover(MouseEvent e) {
        PuntoConexion pin = buscarPin(e.getPoint());
        resaltar(pin);
        setCursor(Cursor.getPredefinedCursor(pin != null ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
    }

    private void resaltar(PuntoConexion pin) {
        if (pin != pinResaltado) {
            if (pinResaltado != null) {
                pinResaltado.setResaltado(false);
            }
            pinResaltado = pin;
            if (pin != null) {
                pin.setResaltado(true);
            }
            repaint();
        }
    }

    // ---------------------------------------------------------------
    // Dibujo
    // ---------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (preferencias.isMostrarCuadricula()) {
                dibujarCuadricula(g2);
            }
            // Polimorfismo: cada Dibujable sabe dibujarse; aquí no se pregunta de qué tipo es.
            for (Cable cable : cables) {
                cable.dibujar(g2);
            }
            for (ComponenteVisual componente : componentes) {
                componente.dibujar(g2);
            }
            dibujarCableProvisional(g2);

            if (componentes.isEmpty()) {
                g2.setColor(new Color(150, 150, 150));
                g2.drawString("Agrega entradas, compuertas y salidas desde la paleta, "
                        + "o carga el ejemplo desde la barra superior.", 30, 40);
            }
        } finally {
            g2.dispose();
        }
    }

    private void dibujarCuadricula(Graphics2D g2) {
        g2.setColor(new Color(235, 235, 240));
        g2.setStroke(new BasicStroke(1f));
        for (int x = 0; x < getWidth(); x += PASO_CUADRICULA) {
            g2.drawLine(x, 0, x, getHeight());
        }
        for (int y = 0; y < getHeight(); y += PASO_CUADRICULA) {
            g2.drawLine(0, y, getWidth(), y);
        }
    }

    private void dibujarCableProvisional(Graphics2D g2) {
        if (pinOrigen == null || mouseActual == null) {
            return;
        }
        boolean desdeSalida = pinOrigen.getSentido() == PuntoConexion.Sentido.SALIDA;
        double x1 = desdeSalida ? pinOrigen.getXAbsoluta() : mouseActual.x;
        double y1 = desdeSalida ? pinOrigen.getYAbsoluta() : mouseActual.y;
        double x2 = desdeSalida ? mouseActual.x : pinOrigen.getXAbsoluta();
        double y2 = desdeSalida ? mouseActual.y : pinOrigen.getYAbsoluta();
        g2.setColor(new Color(255, 140, 0));
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f,
                new float[]{8f, 6f}, 0f));
        g2.draw(Cable.crearCurva(x1, y1, x2, y2));
    }
}
