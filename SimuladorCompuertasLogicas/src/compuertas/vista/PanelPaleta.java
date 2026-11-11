package compuertas.vista;

import compuertas.modelo.fabrica.CreadorAND;
import compuertas.modelo.fabrica.CreadorNAND;
import compuertas.modelo.fabrica.CreadorNOR;
import compuertas.modelo.fabrica.CreadorNOT;
import compuertas.modelo.fabrica.CreadorOR;
import compuertas.modelo.fabrica.CreadorXOR;
import compuertas.vista.componentes.VisualAND;
import compuertas.vista.componentes.VisualEntrada;
import compuertas.vista.componentes.VisualNAND;
import compuertas.vista.componentes.VisualNOR;
import compuertas.vista.componentes.VisualNOT;
import compuertas.vista.componentes.VisualOR;
import compuertas.vista.componentes.VisualSalida;
import compuertas.vista.componentes.VisualXOR;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Paleta lateral: un botón con miniatura por cada elemento que se puede
 * agregar. Un clic en el botón pide al controlador agregar ese elemento.
 */
public class PanelPaleta extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final int COLUMNA_ENTRADAS = 40;
    private static final int COLUMNA_COMPUERTAS = 230;
    private static final int COLUMNA_SALIDAS = 700;

    private final List<ElementoPaleta> elementos;
    private EscuchadorVentana escuchador;

    public PanelPaleta() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        elementos = crearElementos();

        String grupoActual = null;
        for (ElementoPaleta elemento : elementos) {
            if (!elemento.getGrupo().equals(grupoActual)) {
                grupoActual = elemento.getGrupo();
                agregarTitulo(grupoActual);
            }
            add(crearBoton(elemento));
            add(Box.createVerticalStrut(4));
        }
        add(Box.createVerticalGlue());
    }

    public void setEscuchador(EscuchadorVentana escuchador) {
        this.escuchador = escuchador;
    }

    /** Catálogo de la paleta. Agregar una compuerta nueva = una línea más aquí. */
    private static List<ElementoPaleta> crearElementos() {
        List<ElementoPaleta> lista = new ArrayList<>();
        lista.add(new ElementoPaleta("Entradas y salidas", "Entrada", COLUMNA_ENTRADAS,
                (c, x, y) -> new VisualEntrada(c.agregarEntrada(), x, y)));
        lista.add(new ElementoPaleta("Entradas y salidas", "Salida", COLUMNA_SALIDAS,
                (c, x, y) -> new VisualSalida(c.agregarSalida(), x, y)));
        lista.add(new ElementoPaleta("Compuertas", "AND", COLUMNA_COMPUERTAS,
                (c, x, y) -> new VisualAND(c.agregarCompuerta(new CreadorAND()), x, y)));
        lista.add(new ElementoPaleta("Compuertas", "OR", COLUMNA_COMPUERTAS,
                (c, x, y) -> new VisualOR(c.agregarCompuerta(new CreadorOR()), x, y)));
        lista.add(new ElementoPaleta("Compuertas", "NOT", COLUMNA_COMPUERTAS,
                (c, x, y) -> new VisualNOT(c.agregarCompuerta(new CreadorNOT()), x, y)));
        lista.add(new ElementoPaleta("Compuertas", "XOR", COLUMNA_COMPUERTAS,
                (c, x, y) -> new VisualXOR(c.agregarCompuerta(new CreadorXOR()), x, y)));
        lista.add(new ElementoPaleta("Compuertas", "NAND", COLUMNA_COMPUERTAS,
                (c, x, y) -> new VisualNAND(c.agregarCompuerta(new CreadorNAND()), x, y)));
        lista.add(new ElementoPaleta("Compuertas", "NOR", COLUMNA_COMPUERTAS,
                (c, x, y) -> new VisualNOR(c.agregarCompuerta(new CreadorNOR()), x, y)));
        return lista;
    }

    private void agregarTitulo(String texto) {
        JLabel titulo = new JLabel(texto);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD));
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        titulo.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
        add(titulo);
    }

    private JButton crearBoton(ElementoPaleta elemento) {
        JButton boton = new JButton(elemento.getEtiqueta(), new MiniaturaIcono(elemento.crearVistaPrevia(), 76, 46));
        boton.setVerticalTextPosition(SwingConstants.BOTTOM);
        boton.setHorizontalTextPosition(SwingConstants.CENTER);
        boton.setAlignmentX(Component.CENTER_ALIGNMENT);
        Dimension tamano = new Dimension(120, 82);
        boton.setPreferredSize(tamano);
        boton.setMaximumSize(tamano);
        boton.setFocusable(false);
        boton.setToolTipText("Agregar " + elemento.getEtiqueta() + " al lienzo");
        boton.addActionListener(e -> {
            if (escuchador != null) {
                escuchador.alAgregarElemento(elemento);
            }
        });
        return boton;
    }
}
