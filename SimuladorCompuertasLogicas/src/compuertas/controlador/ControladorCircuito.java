package compuertas.controlador;

import compuertas.modelo.Circuito;
import compuertas.modelo.Compuerta;
import compuertas.modelo.EntradaLogica;
import compuertas.modelo.FuenteLogica;
import compuertas.modelo.SalidaLogica;
import compuertas.modelo.TablaVerdad;
import compuertas.modelo.excepciones.CircuitoException;
import compuertas.modelo.fabrica.CreadorAND;
import compuertas.modelo.fabrica.CreadorNOT;
import compuertas.modelo.fabrica.CreadorOR;
import compuertas.modelo.fabrica.CreadorXOR;
import compuertas.vista.ElementoPaleta;
import compuertas.vista.EscuchadorPanel;
import compuertas.vista.EscuchadorVentana;
import compuertas.vista.PanelCircuito;
import compuertas.vista.VistaCircuito;
import compuertas.vista.componentes.Cable;
import compuertas.vista.componentes.ComponenteVisual;
import compuertas.vista.componentes.PuntoConexion;
import compuertas.vista.componentes.VisualAND;
import compuertas.vista.componentes.VisualEntrada;
import compuertas.vista.componentes.VisualNOT;
import compuertas.vista.componentes.VisualOR;
import compuertas.vista.componentes.VisualSalida;
import compuertas.vista.componentes.VisualXOR;
import java.awt.Point;
import java.util.Map;

/**
 * Único punto de contacto entre la vista y el modelo. Atiende los eventos
 * (clics, cables soltados, botones), modifica el modelo, captura sus
 * excepciones y le pide a la vista que se actualice.
 */
public class ControladorCircuito implements EscuchadorPanel, EscuchadorVentana {

    private final Circuito circuito;
    private final VistaCircuito ventana;
    private final PanelCircuito panel;

    public ControladorCircuito(Circuito circuito, VistaCircuito ventana) {
        this.circuito = circuito;
        this.ventana = ventana;
        this.panel = ventana.getPanelCircuito();

        // Se registra como escuchador: a partir de aquí todo es dirigido por eventos.
        ventana.setEscuchador(this);
        panel.setEscuchador(this);
        ventana.mostrarEstado("Agrega elementos desde la paleta, o carga el ejemplo desde la barra superior.");
    }

    // ---------------------------------------------------------------
    // Eventos de la paleta y la barra
    // ---------------------------------------------------------------

    @Override
    public void alAgregarElemento(ElementoPaleta elemento) {
        try {
            ComponenteVisual visual = elemento.construir(circuito, 0, 0);
            Point libre = panel.encontrarPosicionLibre(visual, elemento.getColumna());
            visual.moverA(libre.x, libre.y);
            panel.agregarComponente(visual);
            ventana.mostrarEstado("Se agregó " + elemento.getEtiqueta() + ".");
        } catch (CircuitoException e) {
            ventana.mostrarError(e.getMessage());
        }
    }

    @Override
    public void alEvaluar() {
        try {
            Map<String, Boolean> resultados = circuito.evaluarSalidas();
            if (resultados.isEmpty()) {
                ventana.mostrarEstado("Agrega al menos una salida y conéctala para evaluar el circuito.");
                return;
            }
            StringBuilder sb = new StringBuilder("Resultado:");
            for (Map.Entry<String, Boolean> r : resultados.entrySet()) {
                sb.append("   ").append(r.getKey()).append(" = ").append(r.getValue() ? 1 : 0);
            }
            ventana.mostrarEstado(sb.toString());
            panel.repaint();
        } catch (CircuitoException e) {
            ventana.mostrarError(e.getMessage());
        }
    }

    @Override
    public void alGenerarTablaVerdad() {
        try {
            TablaVerdad tabla = circuito.generarTablaVerdad();
            ventana.getPanelTabla().mostrar(tabla);
            ventana.mostrarEstado("Tabla de verdad generada: " + tabla.getNumeroFilas() + " combinaciones.");
            panel.repaint(); // el modelo restauró los valores originales de las entradas
        } catch (CircuitoException e) {
            ventana.mostrarError(e.getMessage());
        }
    }

    @Override
    public void alEliminarSeleccion() {
        try {
            ComponenteVisual componente = panel.getComponenteSeleccionado();
            Cable cable = panel.getCableSeleccionado();
            if (componente != null) {
                componente.eliminarDelModelo(circuito);
                panel.eliminarComponente(componente);
                ventana.mostrarEstado("Componente eliminado junto con sus cables.");
            } else if (cable != null) {
                PuntoConexion destino = cable.getDestino();
                destino.getComponente().desconectarEntrada(circuito, destino.getIndice());
                panel.eliminarCable(cable);
                ventana.mostrarEstado("Cable eliminado.");
            } else {
                ventana.mostrarEstado("Selecciona un componente o un cable (clic sobre él) y vuelve a intentar.");
            }
        } catch (CircuitoException e) {
            ventana.mostrarError(e.getMessage());
        }
    }

    @Override
    public void alLimpiar() {
        if (circuito.cantidadComponentes() == 0) {
            return;
        }
        if (ventana.confirmar("¿Eliminar todo el circuito?")) {
            reiniciar();
            ventana.mostrarEstado("Circuito vacío.");
        }
    }

    @Override
    public void alCargarEjemplo() {
        if (circuito.cantidadComponentes() > 0 && !ventana.confirmar("Se reemplazará el circuito actual. ¿Continuar?")) {
            return;
        }
        reiniciar();
        try {
            construirEjemplo();
            ventana.mostrarEstado("Ejemplo cargado. Haz clic en los puntos de A, B y C para cambiar sus valores "
                    + "y pulsa «Evaluar» o «Tabla de verdad».");
        } catch (CircuitoException e) {
            ventana.mostrarError(e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // Eventos del lienzo
    // ---------------------------------------------------------------

    @Override
    public void alConectar(PuntoConexion pinA, PuntoConexion pinB) {
        PuntoConexion origen;
        PuntoConexion destino;
        if (pinA.getSentido() == PuntoConexion.Sentido.SALIDA && pinB.getSentido() == PuntoConexion.Sentido.ENTRADA) {
            origen = pinA;
            destino = pinB;
        } else if (pinA.getSentido() == PuntoConexion.Sentido.ENTRADA && pinB.getSentido() == PuntoConexion.Sentido.SALIDA) {
            origen = pinB;
            destino = pinA;
        } else {
            ventana.mostrarEstado("Un cable debe unir una salida con una entrada.");
            return;
        }
        conectar(origen, destino);
    }

    @Override
    public void alHacerClicEnPin(PuntoConexion pin) {
        pin.getComponente().alActivarPin(pin);
        panel.repaint();
    }

    // ---------------------------------------------------------------
    // Operaciones internas
    // ---------------------------------------------------------------

    /** Conecta en el modelo y, solo si fue válido, dibuja el cable. */
    private void conectar(PuntoConexion origen, PuntoConexion destino) {
        try {
            FuenteLogica fuente = origen.getComponente().getFuente();
            destino.getComponente().conectarEntrada(circuito, destino.getIndice(), fuente);
            panel.agregarCable(new Cable(origen, destino));
            ventana.mostrarEstado("Conexión creada.");
        } catch (CircuitoException e) {
            ventana.mostrarError(e.getMessage());
        }
    }

    private void reiniciar() {
        circuito.limpiar();
        panel.limpiar();
        ventana.getPanelTabla().limpiar();
    }

    /** F = (A AND B) OR NOT(C)  y  G = XOR(F, A): la red de objetos de la tarea. */
    private void construirEjemplo() {
        EntradaLogica a = circuito.agregarEntrada();
        a.setValor(true);
        EntradaLogica b = circuito.agregarEntrada();
        EntradaLogica c = circuito.agregarEntrada();
        Compuerta g1 = circuito.agregarCompuerta(new CreadorAND());
        Compuerta g2 = circuito.agregarCompuerta(new CreadorNOT());
        Compuerta g3 = circuito.agregarCompuerta(new CreadorOR());
        Compuerta g4 = circuito.agregarCompuerta(new CreadorXOR());
        SalidaLogica sf = circuito.agregarSalida();
        SalidaLogica sg = circuito.agregarSalida();

        VisualEntrada vA = new VisualEntrada(a, 30, 60);
        VisualEntrada vB = new VisualEntrada(b, 30, 170);
        VisualEntrada vC = new VisualEntrada(c, 30, 300);
        VisualAND vG1 = new VisualAND(g1, 230, 100);
        VisualNOT vG2 = new VisualNOT(g2, 230, 300);
        VisualOR vG3 = new VisualOR(g3, 440, 190);
        VisualXOR vG4 = new VisualXOR(g4, 650, 330);
        VisualSalida vF = new VisualSalida(sf, 700, 60);
        VisualSalida vG = new VisualSalida(sg, 890, 350);

        ComponenteVisual[] todos = {vA, vB, vC, vG1, vG2, vG3, vG4, vF, vG};
        for (ComponenteVisual visual : todos) {
            panel.agregarComponente(visual);
        }

        conectar(vA.getPinSalida(), vG1.getPinEntrada(0));
        conectar(vB.getPinSalida(), vG1.getPinEntrada(1));
        conectar(vC.getPinSalida(), vG2.getPinEntrada(0));
        conectar(vG1.getPinSalida(), vG3.getPinEntrada(0));
        conectar(vG2.getPinSalida(), vG3.getPinEntrada(1));
        conectar(vG3.getPinSalida(), vG4.getPinEntrada(0));
        conectar(vA.getPinSalida(), vG4.getPinEntrada(1));
        conectar(vG3.getPinSalida(), vF.getPinEntrada(0));
        conectar(vG4.getPinSalida(), vG.getPinEntrada(0));
    }
}
