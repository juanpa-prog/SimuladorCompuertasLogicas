package compuertas;

import compuertas.config.Configuracion;
import compuertas.controlador.ControladorCircuito;
import compuertas.modelo.Circuito;
import compuertas.persistencia.RepositorioTexto;
import compuertas.vista.VentanaPrincipal;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Punto de entrada: ensambla modelo, vista y controlador, y arranca la interfaz en el hilo de eventos. */
public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Si falla, Swing usa su apariencia por defecto.
        }
        // Las interfaces Swing se crean siempre en el Event Dispatch Thread.
        SwingUtilities.invokeLater(() -> {
            Circuito circuito = new Circuito("Circuito 1");
            VentanaPrincipal ventana = new VentanaPrincipal(Configuracion.INSTANCIA);
            new ControladorCircuito(circuito, ventana, new RepositorioTexto());
            ventana.setVisible(true);
        });
    }
}
