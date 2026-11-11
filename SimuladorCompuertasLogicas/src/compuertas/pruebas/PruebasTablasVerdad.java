package compuertas.pruebas;

import compuertas.config.Configuracion;
import compuertas.config.Preferencias;
import compuertas.modelo.Circuito;
import compuertas.modelo.Compuerta;
import compuertas.modelo.CompuertaAND;
import compuertas.modelo.CompuertaNOT;
import compuertas.modelo.CompuertaOR;
import compuertas.modelo.EntradaLogica;
import compuertas.modelo.SalidaLogica;
import compuertas.modelo.TablaVerdad;
import compuertas.modelo.excepciones.ConexionInvalidaException;
import compuertas.modelo.excepciones.EntradaSinConectarException;
import compuertas.modelo.fabrica.CreadorAND;
import compuertas.modelo.fabrica.CreadorCompuerta;
import compuertas.modelo.fabrica.CreadorNAND;
import compuertas.modelo.fabrica.CreadorNOR;
import compuertas.modelo.fabrica.CreadorNOT;
import compuertas.modelo.fabrica.CreadorOR;
import compuertas.modelo.fabrica.CreadorXOR;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Pruebas de consola del modelo (sin Swing). Ejecutar con clic derecho
 * -> Run File. Termina con código 1 si alguna verificación falla.
 */
public class PruebasTablasVerdad {

    private static int total;
    private static int fallos;

    public static void main(String[] args) {
        probarTablasDeVerdadDeCadaCompuerta();
        probarRedDelEnunciado();
        probarCircuitoConFyG();
        probarEvaluacionPolimorfica();
        probarErrores();
        probarEliminacionYRestauracion();
        probarSingletonEInyeccion();

        System.out.println();
        System.out.println("Verificaciones: " + total + " | Fallidas: " + fallos);
        System.out.println(fallos == 0 ? "TODAS LAS PRUEBAS PASARON" : "HAY PRUEBAS FALLIDAS");
        if (fallos > 0) {
            System.exit(1);
        }
    }

    // ------------------------------------------------------------------

    /** Combinaciones en el orden de la tabla: (A,B) = 00, 01, 10, 11. */
    private static void verificarTabla(CreadorCompuerta creador, boolean[] esperado) {
        EntradaLogica a = new EntradaLogica("A");
        EntradaLogica b = new EntradaLogica("B");
        Compuerta g = creador.crear("G", a, b);
        for (int i = 0; i < 4; i++) {
            a.setValor(((i >> 1) & 1) == 1);
            b.setValor((i & 1) == 1);
            verificar(creador.getTipo() + " con A=" + (a.getValor() ? 1 : 0) + ", B=" + (b.getValor() ? 1 : 0),
                    g.evaluar() == esperado[i]);
        }
    }

    private static void probarTablasDeVerdadDeCadaCompuerta() {
        titulo("Tablas de verdad de cada compuerta (creadas con Factory Method)");
        verificarTabla(new CreadorAND(), new boolean[]{false, false, false, true});
        verificarTabla(new CreadorOR(), new boolean[]{false, true, true, true});
        verificarTabla(new CreadorXOR(), new boolean[]{false, true, true, false});
        verificarTabla(new CreadorNAND(), new boolean[]{true, true, true, false});
        verificarTabla(new CreadorNOR(), new boolean[]{true, false, false, false});

        EntradaLogica c = new EntradaLogica("C");
        Compuerta not = new CreadorNOT().crear("G", c);
        c.setValor(false);
        verificar("NOT con C=0", not.evaluar());
        c.setValor(true);
        verificar("NOT con C=1", !not.evaluar());
    }

    /** Red de objetos tal como la muestra la diapositiva "Circuito como red de objetos". */
    private static void probarRedDelEnunciado() {
        titulo("Red de objetos: F = (A AND B) OR NOT(C)  (constructores con fuentes)");
        EntradaLogica a = new EntradaLogica(true);
        EntradaLogica b = new EntradaLogica(false);
        EntradaLogica c = new EntradaLogica(false);

        Compuerta g1 = new CompuertaAND("G1", a, b);
        Compuerta g2 = new CompuertaNOT("G2", c);
        Compuerta salida = new CompuertaOR("G3", g1, g2);

        verificar("A=1,B=0,C=0 -> F=1", salida.evaluar());
        c.setValor(true);
        verificar("A=1,B=0,C=1 -> F=0", !salida.evaluar());
        b.setValor(true);
        verificar("A=1,B=1,C=1 -> F=1", salida.evaluar());
    }

    /** Segunda función de la tarea: G = XOR(F, A). */
    private static void probarCircuitoConFyG() {
        titulo("Circuito con dos salidas: F = (A AND B) OR NOT(C), G = XOR(F, A)");
        Circuito circuito = new Circuito("Tarea");
        EntradaLogica a = circuito.agregarEntrada();
        EntradaLogica b = circuito.agregarEntrada();
        EntradaLogica c = circuito.agregarEntrada();
        Compuerta g1 = circuito.agregarCompuerta(new CreadorAND());
        Compuerta g2 = circuito.agregarCompuerta(new CreadorNOT());
        Compuerta g3 = circuito.agregarCompuerta(new CreadorOR());
        Compuerta g4 = circuito.agregarCompuerta(new CreadorXOR());
        SalidaLogica sf = circuito.agregarSalida();
        SalidaLogica sg = circuito.agregarSalida();

        circuito.conectar(g1, 0, a);
        circuito.conectar(g1, 1, b);
        circuito.conectar(g2, 0, c);
        circuito.conectar(g3, 0, g1);
        circuito.conectar(g3, 1, g2);
        circuito.conectar(g4, 0, g3);
        circuito.conectar(g4, 1, a);
        circuito.conectarSalida(sf, g3);
        circuito.conectarSalida(sg, g4);

        verificar("nombres automáticos A,B,C / G1..G4 / F,G",
                a.getNombre().equals("A") && c.getNombre().equals("C")
                && g4.getNombre().equals("G4") && sf.getNombre().equals("F") && sg.getNombre().equals("G"));

        TablaVerdad tabla = circuito.generarTablaVerdad();
        System.out.println(tabla);
        verificar("la tabla tiene 8 filas y 5 columnas", tabla.getNumeroFilas() == 8 && tabla.getNumeroColumnas() == 5);

        boolean todoCorrecto = true;
        for (int fila = 0; fila < tabla.getNumeroFilas(); fila++) {
            boolean va = tabla.getValor(fila, 0);
            boolean vb = tabla.getValor(fila, 1);
            boolean vc = tabla.getValor(fila, 2);
            boolean esperadoF = (va && vb) || !vc;
            boolean esperadoG = esperadoF != va;
            if (tabla.getValor(fila, 3) != esperadoF || tabla.getValor(fila, 4) != esperadoG) {
                todoCorrecto = false;
            }
        }
        verificar("las 8 filas coinciden con la fórmula booleana", todoCorrecto);

        Map<String, Boolean> resultados = circuito.evaluarSalidas();
        verificar("evaluarSalidas() entrega F y G", resultados.size() == 2);
    }

    /** Un solo bucle sobre List<Compuerta>: sin instanceof y sin if/else por tipo. */
    private static void probarEvaluacionPolimorfica() {
        titulo("Evaluación polimórfica: List<Compuerta> con las 6 compuertas");
        EntradaLogica a = new EntradaLogica("A", true);
        EntradaLogica b = new EntradaLogica("B", false);

        List<Compuerta> lista = new ArrayList<>();
        lista.add(new CreadorAND().crear("AND", a, b));
        lista.add(new CreadorOR().crear("OR", a, b));
        lista.add(new CreadorXOR().crear("XOR", a, b));
        lista.add(new CreadorNAND().crear("NAND", a, b));
        lista.add(new CreadorNOR().crear("NOR", a, b));
        lista.add(new CreadorNOT().crear("NOT", a));

        boolean[] esperado = {false, true, true, true, false, false};
        int i = 0;
        for (Compuerta g : lista) {
            verificar(g.getTipo() + " (A=1, B=0) = " + (esperado[i] ? 1 : 0), g.evaluar() == esperado[i]);
            i++;
        }
    }

    private static void probarErrores() {
        titulo("Errores controlados");
        Circuito circuito = new Circuito("Errores");
        EntradaLogica a = circuito.agregarEntrada();
        Compuerta g1 = circuito.agregarCompuerta(new CreadorAND());
        Compuerta g2 = circuito.agregarCompuerta(new CreadorNOT());

        try {
            g1.evaluar();
            verificar("evaluar con entradas sin conectar debe fallar", false);
        } catch (EntradaSinConectarException e) {
            verificar("entrada sin conectar: " + e.getMessage(), true);
        }

        // Aunque A=0 haría falsa la AND, la entrada B libre debe detectarse igual.
        circuito.conectar(g1, 0, a);
        try {
            g1.evaluar();
            verificar("la AND con solo A conectada debe fallar", false);
        } catch (EntradaSinConectarException e) {
            verificar("entrada 2 libre detectada aunque A=0", true);
        }

        circuito.conectar(g2, 0, g1);
        try {
            circuito.conectar(g1, 1, g2);
            verificar("un ciclo debe rechazarse", false);
        } catch (ConexionInvalidaException e) {
            verificar("ciclo G1 -> G2 -> G1 rechazado", true);
        }
        try {
            circuito.conectar(g1, 1, g1);
            verificar("autoconexión debe rechazarse", false);
        } catch (ConexionInvalidaException e) {
            verificar("autoconexión rechazada", true);
        }
        try {
            circuito.conectar(g1, 7, a);
            verificar("índice inexistente debe rechazarse", false);
        } catch (ConexionInvalidaException e) {
            verificar("entrada inexistente rechazada", true);
        }
        try {
            circuito.conectar(g1, 1, new EntradaLogica("AJENA"));
            verificar("fuente ajena al circuito debe rechazarse", false);
        } catch (ConexionInvalidaException e) {
            verificar("fuente ajena rechazada", true);
        }
        try {
            circuito.agregarEntrada("A");
            verificar("nombre duplicado debe rechazarse", false);
        } catch (RuntimeException e) {
            verificar("entrada duplicada rechazada", true);
        }
        try {
            new EntradaLogica("  ");
            verificar("nombre vacío debe rechazarse", false);
        } catch (IllegalArgumentException e) {
            verificar("el constructor valida el nombre", true);
        }
    }

    private static void probarEliminacionYRestauracion() {
        titulo("Eliminación, desconexión y restauración de valores");
        Circuito circuito = new Circuito("Eliminar");
        EntradaLogica a = circuito.agregarEntrada();
        EntradaLogica b = circuito.agregarEntrada();
        Compuerta g1 = circuito.agregarCompuerta(new CreadorAND());
        Compuerta g2 = circuito.agregarCompuerta(new CreadorNOT());
        SalidaLogica f = circuito.agregarSalida();
        circuito.conectar(g1, 0, a);
        circuito.conectar(g1, 1, b);
        circuito.conectar(g2, 0, g1);
        circuito.conectarSalida(f, g2);

        a.setValor(true);
        circuito.generarTablaVerdad();
        verificar("la tabla restaura los valores originales (A sigue en 1)", a.getValor() && !b.getValor());

        circuito.eliminarCompuerta(g1);
        verificar("al eliminar G1, el NOT queda sin conexión", g2.getEntrada(0) == null);
        verificar("la colección ya no contiene G1", circuito.buscarCompuerta("G1") == null);

        circuito.eliminarCompuerta(g2);
        verificar("al eliminar G2, la salida F queda sin conexión", f.getFuente() == null);

        verificar("getCompuertas() es de solo lectura", esSoloLectura(circuito.getCompuertas()));
    }

    private static boolean esSoloLectura(List<Compuerta> lista) {
        try {
            lista.add(null);
            return false;
        } catch (UnsupportedOperationException e) {
            return true;
        }
    }

    private static void probarSingletonEInyeccion() {
        titulo("Singleton (enum) y preferencias inyectables");
        verificar("Configuracion.INSTANCIA es única", Configuracion.INSTANCIA == Configuracion.valueOf("INSTANCIA"));
        Configuracion.INSTANCIA.setMostrarCuadricula(false);
        verificar("el cambio se ve desde cualquier referencia", !Configuracion.INSTANCIA.isMostrarCuadricula());
        Configuracion.INSTANCIA.setMostrarCuadricula(true);

        Preferencias falsa = new Preferencias() {
            private boolean valor;

            @Override
            public boolean isMostrarCuadricula() {
                return valor;
            }

            @Override
            public void setMostrarCuadricula(boolean mostrar) {
                valor = mostrar;
            }
        };
        verificar("otra implementación de Preferencias (para pruebas)", !falsa.isMostrarCuadricula());
    }

    // ------------------------------------------------------------------

    private static void titulo(String texto) {
        System.out.println();
        System.out.println("== " + texto);
    }

    private static void verificar(String descripcion, boolean condicion) {
        total++;
        if (!condicion) {
            fallos++;
        }
        System.out.println((condicion ? "  [OK]    " : "  [FALLO] ") + descripcion);
    }
}
