package compuertas.vista;

import compuertas.modelo.fabrica.CatalogoCompuertas;
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

    public static final int COLUMNA_ENTRADAS = 40;
    public static final int COLUMNA_COMPUERTAS = 230;
    public static final int COLUMNA_SALIDAS = 700;

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
                (c, x, y) -> new VisualAND(c.agregarCompuerta(CatalogoCompuertas.obtener("AND")), x, y)));
        lista.add(new ElementoPaleta("Compuertas", "OR", COLUMNA_COMPUERTAS,
                (c, x, y) -> new VisualOR(c.agregarCompuerta(CatalogoCompuertas.obtener("OR")), x, y)));
        lista.add(new ElementoPaleta("Compuertas", "NOT", COLUMNA_COMPUERTAS,
                (c, x, y) -> new VisualNOT(c.agregarCompuerta(CatalogoCompuertas.obtener("NOT")), x, y)));
        lista.add(new ElementoPaleta("Compuertas", "XOR", COLUMNA_COMPUERTAS,
                (c, x, y) -> new VisualXOR(c.agregarCompuerta(CatalogoCompuertas.obtener("XOR")), x, y)));
        lista.add(new ElementoPaleta("Compuertas", "NAND", COLUMNA_COMPUERTAS,
                (c, x, y) -> new VisualNAND(c.agregarCompuerta(CatalogoCompuertas.obtener("NAND")), x, y)));
        lista.add(new ElementoPaleta("Compuertas", "NOR", COLUMNA_COMPUERTAS,
                (c, x, y) -> new VisualNOR(c.agregarCompuerta(CatalogoCompuertas.obtener("NOR")), x, y)));
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
