package compuertas.pruebas;

import compuertas.config.Configuracion;
import compuertas.controlador.ControladorCircuito;
import compuertas.modelo.Circuito;
import compuertas.modelo.fabrica.CatalogoCompuertas;
import compuertas.persistencia.RepositorioTexto;
import compuertas.vista.ElementoPaleta;
import compuertas.vista.EscuchadorVentana;
import compuertas.vista.PanelCircuito;
import compuertas.vista.PanelTablaVerdad;
import compuertas.vista.VistaCircuito;
import compuertas.vista.componentes.ComponenteVisual;
import compuertas.vista.componentes.VisualAND;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Pruebas del flujo Guardar / Abrir del controlador SIN abrir ninguna ventana: usa una
 * {@link VistaCircuito} falsa que registra mensajes y responde con rutas programadas.
 * (Ejecutar con -Djava.awt.headless=true si no hay pantalla.)
 */
public class PruebasControlador {

    private static int total;
    private static int fallos;

    /** Vista falsa: no muestra nada, solo registra y contesta lo que la prueba le indique. */
    private static final class VistaFalsa implements VistaCircuito {
        final PanelCircuito panel = new PanelCircuito(Configuracion.INSTANCIA);
        final PanelTablaVerdad tabla = new PanelTablaVerdad();
        final List<String> estados = new ArrayList<>();
        final List<String> errores = new ArrayList<>();
        Path rutaGuardar;
        Path rutaAbrir;
        boolean respuesta = true;

        @Override
        public void setEscuchador(EscuchadorVentana escuchador) {
        }

        @Override
        public PanelCircuito getPanelCircuito() {
            return panel;
        }

        @Override
        public PanelTablaVerdad getPanelTabla() {
            return tabla;
        }

        @Override
        public void mostrarEstado(String mensaje) {
            estados.add(mensaje);
        }

        @Override
        public void mostrarError(String mensaje) {
            errores.add(mensaje);
        }

        @Override
        public boolean confirmar(String pregunta) {
            return respuesta;
        }

        @Override
        public Path elegirArchivoParaGuardar() {
            return rutaGuardar;
        }

        @Override
        public Path elegirArchivoParaAbrir() {
            return rutaAbrir;
        }

        String ultimoEstado() {
            return estados.isEmpty() ? "" : estados.get(estados.size() - 1);
        }

        String ultimoError() {
            return errores.isEmpty() ? "" : errores.get(errores.size() - 1);
        }
    }

    public static void main(String[] args) throws IOException {
        Path carpeta = Files.createTempDirectory("controlador-prueba");
        VistaFalsa vista = new VistaFalsa();
        ControladorCircuito controlador = new ControladorCircuito(new Circuito("Circuito 1"), vista, new RepositorioTexto());

        probarGuardarYAbrir(controlador, vista, carpeta);
        probarAbrirConErrores(controlador, vista, carpeta);
        probarConfirmaciones(controlador, vista, carpeta);
        probarCircuitoSinPosiciones(controlador, vista, carpeta);
        probarErroresAlGuardar(controlador, vista, carpeta);

        System.out.println();
        System.out.println("Verificaciones: " + total + " | Fallidas: " + fallos);
        System.out.println(fallos == 0 ? "TODAS LAS PRUEBAS PASARON" : "HAY PRUEBAS FALLIDAS");
        if (fallos > 0) {
            System.exit(1);
        }
    }

    // ------------------------------------------------------------------

    private static void probarGuardarYAbrir(ControladorCircuito c, VistaFalsa v, Path carpeta) throws IOException {
        titulo("Guardar el ejemplo F y G, vaciar y volver a abrirlo");
        c.alCargarEjemplo();
        verificar("el ejemplo dibuja 9 componentes y 9 cables", v.panel.getComponentes().size() == 9
                && v.panel.getCables().size() == 9);
        c.alEvaluar();
        verificar("evaluar da F = 1 y G = 0", v.ultimoEstado().contains("F = 1") && v.ultimoEstado().contains("G = 0"));

        Path ruta = carpeta.resolve("ejemplo.txt");
        v.rutaGuardar = ruta;
        c.alGuardar();
        verificar("guardar informa que se guardó", v.ultimoEstado().contains("guardado") && v.errores.isEmpty());
        List<String> lineas = Files.readAllLines(ruta, StandardCharsets.UTF_8);
        verificar("el archivo guarda las posiciones de la vista (A en 30,60)", lineas.contains("ENTRADA;A;true;30;60"));
        verificar("el archivo guarda la posición de G1 (230,100)", lineas.contains("COMPUERTA;G1;AND;A;B;230;100"));

        c.alLimpiar();
        verificar("limpiar deja el lienzo vacío", v.panel.getComponentes().isEmpty() && v.panel.getCables().isEmpty());

        v.rutaAbrir = ruta;
        c.alAbrir();
        verificar("abrir informa el éxito", v.ultimoEstado().contains("abierto") && v.errores.isEmpty());
        verificar("se recuperan 9 componentes y 9 cables", v.panel.getComponentes().size() == 9
                && v.panel.getCables().size() == 9);
        ComponenteVisual a = buscar(v, "A");
        ComponenteVisual g1 = buscar(v, "G1");
        verificar("se recuperan las posiciones (A en 30,60; G1 en 230,100)", a != null && a.getX() == 30 && a.getY() == 60
                && g1 != null && g1.getX() == 230 && g1.getY() == 100);
        c.alEvaluar();
        verificar("el circuito abierto da otra vez F = 1 y G = 0", v.ultimoEstado().contains("F = 1")
                && v.ultimoEstado().contains("G = 0"));
        c.alGenerarTablaVerdad();
        verificar("la tabla de verdad funciona sobre el circuito abierto", v.ultimoEstado().contains("8 combinaciones"));

        // El controlador trabaja ahora con el circuito CARGADO: la compuerta nueva no repite nombres.
        ElementoPaleta and = new ElementoPaleta("Compuertas", "AND", 230,
                (circ, x, y) -> new VisualAND(circ.agregarCompuerta(CatalogoCompuertas.obtener("AND")), x, y));
        c.alAgregarElemento(and);
        verificar("al agregar una compuerta tras abrir se llama G5 (continúa la numeración)",
                buscar(v, "G5") != null && v.panel.getComponentes().size() == 10);
    }

    private static void probarAbrirConErrores(ControladorCircuito c, VistaFalsa v, Path carpeta) throws IOException {
        titulo("Abrir con errores: el circuito actual se conserva y el mensaje es claro");
        c.alCargarEjemplo(); // estado conocido: 9 componentes
        v.respuesta = true;
        int errores = v.errores.size();

        v.rutaAbrir = escribir(carpeta, "malo.txt", "CIRCUITO;1;X\nENTRADA;A;true\nCOMPUERTA;G1;XNOR;A;A\n");
        c.alAbrir();
        verificar("formato inválido: el mensaje indica la línea y el motivo", v.errores.size() == errores + 1
                && v.ultimoError().contains("Línea 3") && v.ultimoError().contains("XNOR"));
        verificar("formato inválido: avisa que el circuito actual no se modificó", v.ultimoError().contains("no se modificó"));
        verificar("formato inválido: el lienzo conserva sus 9 componentes y 9 cables", v.panel.getComponentes().size() == 9
                && v.panel.getCables().size() == 9);
        c.alEvaluar();
        verificar("formato inválido: el circuito anterior sigue evaluándose (F = 1)", v.ultimoEstado().contains("F = 1"));

        v.rutaAbrir = escribir(carpeta, "ciclo.txt",
                "CIRCUITO;1;X\nENTRADA;A;true\nCOMPUERTA;G1;AND;A;G2\nCOMPUERTA;G2;NOT;G1\n");
        c.alAbrir();
        verificar("un circuito con ciclo se rechaza sin tocar el actual", v.ultimoError().contains("ciclo")
                && v.panel.getComponentes().size() == 9);

        v.rutaAbrir = escribir(carpeta, "vacio.txt", "");
        c.alAbrir();
        verificar("archivo vacío se rechaza sin tocar el actual", v.ultimoError().contains("vacío")
                && v.panel.getComponentes().size() == 9);

        v.rutaAbrir = carpeta.resolve("no-existe.txt");
        c.alAbrir();
        verificar("archivo inexistente: mensaje distinto («No existe el archivo»)",
                v.ultimoError().startsWith("No existe el archivo") && v.panel.getComponentes().size() == 9);

        int antes = v.errores.size();
        v.rutaAbrir = null;
        c.alAbrir();
        verificar("cancelar el selector no hace nada ni muestra errores",
                v.errores.size() == antes && v.panel.getComponentes().size() == 9);
    }

    private static void probarConfirmaciones(ControladorCircuito c, VistaFalsa v, Path carpeta) throws IOException {
        titulo("Confirmaciones: reemplazar el circuito y sobrescribir un archivo");
        c.alCargarEjemplo();
        Path pequeno = escribir(carpeta, "pequeno.txt", "CIRCUITO;1;Pequeño\nENTRADA;A;true\n");

        v.rutaAbrir = pequeno;
        v.respuesta = false;
        c.alAbrir();
        verificar("si no se confirma el reemplazo, el circuito actual se queda", v.panel.getComponentes().size() == 9);
        v.respuesta = true;
        c.alAbrir();
        verificar("si se confirma, se reemplaza por el abierto (1 componente)", v.panel.getComponentes().size() == 1
                && v.panel.getCables().isEmpty());

        Path existente = escribir(carpeta, "existente.txt", "CONTENIDO ORIGINAL\n");
        v.rutaGuardar = existente;
        v.respuesta = false;
        c.alGuardar();
        verificar("si no se confirma sobrescribir, el archivo no cambia",
                new String(Files.readAllBytes(existente), StandardCharsets.UTF_8).equals("CONTENIDO ORIGINAL\n"));
        v.respuesta = true;
        c.alGuardar();
        verificar("si se confirma, el archivo se reemplaza por el circuito",
                Files.readAllLines(existente, StandardCharsets.UTF_8).get(0).startsWith("CIRCUITO;1;"));
    }

    private static void probarCircuitoSinPosiciones(ControladorCircuito c, VistaFalsa v, Path carpeta) throws IOException {
        titulo("Un archivo sin posiciones se acomoda solo, sin encimar elementos");
        v.respuesta = true;
        v.rutaAbrir = escribir(carpeta, "sinpos.txt", "CIRCUITO;1;Sin posiciones\n"
                + "ENTRADA;A;true\nENTRADA;B;false\nCOMPUERTA;G1;AND;A;B\nSALIDA;F;G1\n");
        c.alAbrir();
        ComponenteVisual a = buscar(v, "A");
        ComponenteVisual b = buscar(v, "B");
        verificar("se dibuja todo: 4 componentes y 3 cables", v.panel.getComponentes().size() == 4
                && v.panel.getCables().size() == 3);
        verificar("las dos entradas quedan en filas distintas", a != null && b != null && a.getX() == b.getX()
                && a.getY() != b.getY());
        c.alEvaluar();
        verificar("y el circuito evalúa bien (A AND B = 0)", v.ultimoEstado().contains("F = 0"));
    }

    private static void probarErroresAlGuardar(ControladorCircuito c, VistaFalsa v, Path carpeta) {
        titulo("Errores al guardar se informan sin romper la aplicación");
        int errores = v.errores.size();
        v.respuesta = true;
        v.rutaGuardar = carpeta; // una carpeta no puede escribirse como archivo
        c.alGuardar();
        verificar("guardar sobre una carpeta informa «No fue posible guardar»", v.errores.size() == errores + 1
                && v.ultimoError().startsWith("No fue posible guardar"));
        int antes = v.errores.size();
        v.rutaGuardar = null;
        c.alGuardar();
        verificar("cancelar el selector al guardar no hace nada", v.errores.size() == antes);
    }

    // ------------------------------------------------------------------

    private static ComponenteVisual buscar(VistaFalsa v, String nombre) {
        for (ComponenteVisual visual : v.panel.getComponentes()) {
            if (visual.getNombreElemento().equals(nombre)) {
                return visual;
            }
        }
        return null;
    }

    private static Path escribir(Path carpeta, String nombre, String contenido) throws IOException {
        Path ruta = carpeta.resolve(nombre);
        Files.write(ruta, contenido.getBytes(StandardCharsets.UTF_8));
        return ruta;
    }

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
