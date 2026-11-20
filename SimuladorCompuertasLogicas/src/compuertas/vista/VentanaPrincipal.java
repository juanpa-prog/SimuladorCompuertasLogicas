package compuertas.vista;

import compuertas.config.Preferencias;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.nio.file.Path;

/**
 * Ventana principal (JFrame). Solo arma la interfaz y reenvía los eventos
 * al {@link EscuchadorVentana}; no contiene reglas del dominio. Sus
 * componentes son atributos privados (encapsulación).
 */
public class VentanaPrincipal extends JFrame implements VistaCircuito {

    private static final long serialVersionUID = 1L;

    private final Preferencias preferencias;
    private final PanelPaleta panelPaleta = new PanelPaleta();
    private final PanelCircuito panelCircuito;
    private final PanelTablaVerdad panelTabla = new PanelTablaVerdad();
    private final JLabel lblEstado = new JLabel(" ");
    private final JFileChooser selector = crearSelector();
    private EscuchadorVentana escuchador;

    public VentanaPrincipal(Preferencias preferencias) {
        super("Simulador de Compuertas Lógicas");
        this.preferencias = preferencias;
        this.panelCircuito = new PanelCircuito(preferencias); // inyección de dependencias

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        add(crearBarra(), BorderLayout.NORTH);
        add(new JScrollPane(panelPaleta, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.WEST);

        JSplitPane division = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(panelCircuito), panelTabla);
        division.setResizeWeight(0.7);
        division.setDividerLocation(500);
        add(division, BorderLayout.CENTER);

        lblEstado.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        add(lblEstado, BorderLayout.SOUTH);

        setSize(new Dimension(1280, 820));
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
    }

    private JToolBar crearBarra() {
        JToolBar barra = new JToolBar();
        barra.setFloatable(false);
        barra.add(crearBoton("Abrir…", "Abre un circuito guardado en un archivo de texto (.txt)", () -> escuchador.alAbrir()));
        barra.add(crearBoton("Guardar…", "Guarda el circuito en un archivo de texto (.txt)", () -> escuchador.alGuardar()));
        barra.addSeparator();
        barra.add(crearBoton("Evaluar", "Calcula el valor de cada salida", () -> escuchador.alEvaluar()));
        barra.add(crearBoton("Tabla de verdad", "Genera la tabla con todas las combinaciones",
                () -> escuchador.alGenerarTablaVerdad()));
        barra.addSeparator();
        barra.add(crearBoton("Eliminar selección", "Elimina la compuerta o el cable seleccionado (tecla Supr)",
                () -> escuchador.alEliminarSeleccion()));
        barra.add(crearBoton("Limpiar", "Borra todo el circuito", () -> escuchador.alLimpiar()));
        barra.addSeparator();
        barra.add(crearBoton("Cargar ejemplo F y G", "F = (A AND B) OR NOT(C)   y   G = XOR(F, A)",
                () -> escuchador.alCargarEjemplo()));
        barra.addSeparator();

        JCheckBox chkCuadricula = new JCheckBox("Mostrar cuadrícula", preferencias.isMostrarCuadricula());
        chkCuadricula.setFocusable(false);
        chkCuadricula.addActionListener(e -> {
            preferencias.setMostrarCuadricula(chkCuadricula.isSelected());
            panelCircuito.repaint();
        });
        barra.add(chkCuadricula);
        return barra;
    }

    private JButton crearBoton(String texto, String ayuda, Runnable accion) {
        JButton boton = new JButton(texto);
        boton.setToolTipText(ayuda);
        boton.setFocusable(false);
        boton.addActionListener(e -> {
            if (escuchador != null) {
                accion.run();
            }
        });
        return boton;
    }

    // --- API para el controlador ---

    @Override
    public void setEscuchador(EscuchadorVentana escuchador) {
        this.escuchador = escuchador;
        panelPaleta.setEscuchador(escuchador);
    }

    @Override
    public PanelCircuito getPanelCircuito() {
        return panelCircuito;
    }

    @Override
    public PanelTablaVerdad getPanelTabla() {
        return panelTabla;
    }

    @Override
    public void mostrarEstado(String mensaje) {
        lblEstado.setText(mensaje);
    }

    @Override
    public void mostrarError(String mensaje) {
        lblEstado.setText("⚠ " + mensaje);
        JOptionPane.showMessageDialog(this, mensaje, "No se pudo completar la acción", JOptionPane.WARNING_MESSAGE);
    }

    @Override
    public Path elegirArchivoParaGuardar() {
        selector.setDialogTitle("Guardar circuito");
        if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        Path ruta = selector.getSelectedFile().toPath();
        // Si el usuario no escribió extensión, se agrega .txt para que el archivo se reconozca después.
        if (!ruta.getFileName().toString().contains(".")) {
            ruta = ruta.resolveSibling(ruta.getFileName() + ".txt");
        }
        return ruta;
    }

    @Override
    public Path elegirArchivoParaAbrir() {
        selector.setDialogTitle("Abrir circuito");
        if (selector.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        return selector.getSelectedFile().toPath();
    }

    private static JFileChooser crearSelector() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Circuitos de texto (*.txt)", "txt"));
        return chooser;
    }

    @Override
    public boolean confirmar(String pregunta) {
        return JOptionPane.showConfirmDialog(this, pregunta, "Confirmar", JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION;
    }
}
