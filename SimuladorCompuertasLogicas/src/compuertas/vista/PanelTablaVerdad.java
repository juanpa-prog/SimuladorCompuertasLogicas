package compuertas.vista;

import compuertas.modelo.TablaVerdad;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/** Muestra una {@link TablaVerdad} en un JTable de solo lectura. */
public class PanelTablaVerdad extends JPanel {

    private static final long serialVersionUID = 1L;

    private final DefaultTableModel modelo = new DefaultTableModel() {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private int numeroEntradas;

    public PanelTablaVerdad() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Tabla de verdad"));
        setPreferredSize(new Dimension(300, 250));

        tabla.setRowHeight(22);
        tabla.setEnabled(false);
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor, boolean sel,
                                                           boolean foco, int fila, int columna) {
                super.getTableCellRendererComponent(t, valor, sel, foco, fila, columna);
                setHorizontalAlignment(SwingConstants.CENTER);
                boolean esSalida = columna >= numeroEntradas;
                setFont(getFont().deriveFont(esSalida ? Font.BOLD : Font.PLAIN));
                setBackground(esSalida ? new Color(232, 250, 232) : Color.WHITE);
                return this;
            }
        });
        add(new JScrollPane(tabla), BorderLayout.CENTER);
    }

    public void mostrar(TablaVerdad tablaVerdad) {
        numeroEntradas = tablaVerdad.getNumeroEntradas();
        modelo.setRowCount(0);
        modelo.setColumnIdentifiers(tablaVerdad.getEncabezados().toArray());
        for (int f = 0; f < tablaVerdad.getNumeroFilas(); f++) {
            Object[] fila = new Object[tablaVerdad.getNumeroColumnas()];
            for (int c = 0; c < fila.length; c++) {
                fila[c] = tablaVerdad.getValor(f, c) ? 1 : 0;
            }
            modelo.addRow(fila);
        }
    }

    public void limpiar() {
        modelo.setRowCount(0);
        modelo.setColumnCount(0);
    }
}
