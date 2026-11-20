package compuertas.pruebas;

import compuertas.modelo.Circuito;
import compuertas.modelo.Compuerta;
import compuertas.modelo.EntradaLogica;
import compuertas.modelo.SalidaLogica;
import compuertas.modelo.excepciones.CircuitoException;
import compuertas.modelo.excepciones.FormatoCircuitoException;
import compuertas.modelo.fabrica.CatalogoCompuertas;
import compuertas.persistencia.CircuitoCargado;
import compuertas.persistencia.PosicionesCircuito;
import compuertas.persistencia.PosicionesCircuito.Posicion;
import compuertas.persistencia.PosicionesCircuito.Tipo;
import compuertas.persistencia.RepositorioCircuitos;
import compuertas.persistencia.RepositorioTexto;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;

/**
 * Pruebas de consola de guardar/cargar circuitos en archivos de texto, sin Swing.
 * Cubren el circuito completo, las posiciones y los archivos inválidos.
 * Termina con código 1 si alguna verificación falla.
 */
public class PruebasPersistencia {

    private static final String CABECERA = "CIRCUITO;1;Prueba\n";

    private static int total;
    private static int fallos;
    private static Path carpeta;
    private static final RepositorioCircuitos REPO = new RepositorioTexto();

    public static void main(String[] args) throws IOException {
        carpeta = Files.createTempDirectory("circuitos-prueba");
        probarIdaYVuelta();
        probarPosiciones();
        probarElementosSinConectar();
        probarOrdenLibreDeDeclaraciones();
        probarNombresAutomaticosTrasCargar();
        probarArchivosInvalidos();
        probarErroresDeAcceso();
        probarNombresNoGuardables();

        System.out.println();
        System.out.println("Verificaciones: " + total + " | Fallidas: " + fallos);
        System.out.println(fallos == 0 ? "TODAS LAS PRUEBAS PASARON" : "HAY PRUEBAS FALLIDAS");
        if (fallos > 0) {
            System.exit(1);
        }
    }

    // ------------------------------------------------------------------
    // Casos válidos
    // ------------------------------------------------------------------

    /** F = (A AND B) OR NOT(C)  y  G = XOR(F, A). */
    private static Circuito crearCircuitoFyG() {
        Circuito c = new Circuito("Tarea");
        EntradaLogica a = c.agregarEntrada();
        a.setValor(true);
        EntradaLogica b = c.agregarEntrada();
        EntradaLogica cc = c.agregarEntrada();
        Compuerta g1 = c.agregarCompuerta(CatalogoCompuertas.obtener("AND"));
        Compuerta g2 = c.agregarCompuerta(CatalogoCompuertas.obtener("NOT"));
        Compuerta g3 = c.agregarCompuerta(CatalogoCompuertas.obtener("OR"));
        Compuerta g4 = c.agregarCompuerta(CatalogoCompuertas.obtener("XOR"));
        SalidaLogica f = c.agregarSalida();
        SalidaLogica g = c.agregarSalida();
        c.conectar(g1, 0, a);
        c.conectar(g1, 1, b);
        c.conectar(g2, 0, cc);
        c.conectar(g3, 0, g1);
        c.conectar(g3, 1, g2);
        c.conectar(g4, 0, g3);
        c.conectar(g4, 1, a);
        c.conectarSalida(f, g3);
        c.conectarSalida(g, g4);
        return c;
    }

    private static void probarIdaYVuelta() throws IOException {
        titulo("Ida y vuelta: guardar y cargar F = (A AND B) OR NOT(C), G = XOR(F, A)");
        Circuito original = crearCircuitoFyG();
        Path ruta = carpeta.resolve("fyg.txt");
        try {
            REPO.guardar(original, null, ruta);
            List<String> lineas = Files.readAllLines(ruta, StandardCharsets.UTF_8);
            verificar("la primera línea es la cabecera CIRCUITO;1;nombre", lineas.get(0).equals("CIRCUITO;1;Tarea"));
            verificar("las entradas se guardan con su valor", lineas.contains("ENTRADA;A;true")
                    && lineas.contains("ENTRADA;B;false"));
            verificar("las compuertas guardan tipo y fuentes", lineas.contains("COMPUERTA;G1;AND;A;B")
                    && lineas.contains("COMPUERTA;G2;NOT;C"));
            verificar("las salidas guardan su fuente", lineas.contains("SALIDA;F;G3") && lineas.contains("SALIDA;G;G4"));
            verificar("el archivo tiene 1 + 3 + 4 + 2 líneas", lineas.size() == 10);

            Circuito cargado = REPO.cargar(ruta).getCircuito();
            verificar("el nombre del circuito se conserva", cargado.getNombre().equals("Tarea"));
            verificar("misma cantidad de entradas, compuertas y salidas", cargado.getEntradas().size() == 3
                    && cargado.getCompuertas().size() == 4 && cargado.getSalidas().size() == 2);
            verificar("el valor de A (true) se conserva", cargado.buscarEntrada("A").getValor()
                    && !cargado.buscarEntrada("B").getValor());
            verificar("las conexiones se conservan (G3 recibe G1 y G2)",
                    cargado.buscarCompuerta("G3").getEntrada(0) == cargado.buscarCompuerta("G1")
                    && cargado.buscarCompuerta("G3").getEntrada(1) == cargado.buscarCompuerta("G2"));
            verificar("A alimenta G1 y G4 con la MISMA entrada (referencia compartida)",
                    cargado.buscarCompuerta("G1").getEntrada(0) == cargado.buscarEntrada("A")
                    && cargado.buscarCompuerta("G4").getEntrada(1) == cargado.buscarEntrada("A"));
            verificar("las salidas apuntan a las compuertas correctas",
                    cargado.buscarSalida("F").getFuente() == cargado.buscarCompuerta("G3")
                    && cargado.buscarSalida("G").getFuente() == cargado.buscarCompuerta("G4"));
            verificar("la tabla de verdad del circuito cargado es idéntica a la original",
                    original.generarTablaVerdad().toString().equals(cargado.generarTablaVerdad().toString()));
            verificar("evaluarSalidas() coincide", original.evaluarSalidas().equals(cargado.evaluarSalidas()));
        } catch (IOException | FormatoCircuitoException e) {
            verificar("ida y vuelta sin errores: " + e, false);
        }

        Circuito vacio = new Circuito("Vacío");
        Path rutaVacia = carpeta.resolve("vacio.txt");
        try {
            REPO.guardar(vacio, null, rutaVacia);
            verificar("un circuito vacío se guarda y se carga",
                    REPO.cargar(rutaVacia).getCircuito().cantidadComponentes() == 0);
        } catch (IOException | FormatoCircuitoException e) {
            verificar("circuito vacío sin errores: " + e, false);
        }
    }

    private static void probarPosiciones() throws IOException {
        titulo("Posiciones x/y como estado necesario");
        Circuito c = crearCircuitoFyG();
        PosicionesCircuito pos = new PosicionesCircuito();
        pos.poner(Tipo.ENTRADA, "A", 30, 60);
        pos.poner(Tipo.COMPUERTA, "G1", 230, 100);
        pos.poner(Tipo.SALIDA, "F", 700, 60);
        Path ruta = carpeta.resolve("posiciones.txt");
        try {
            REPO.guardar(c, pos, ruta);
            List<String> lineas = Files.readAllLines(ruta, StandardCharsets.UTF_8);
            verificar("la línea de A termina en sus coordenadas", lineas.contains("ENTRADA;A;true;30;60"));
            verificar("la compuerta con posición la incluye al final", lineas.contains("COMPUERTA;G1;AND;A;B;230;100"));
            CircuitoCargado cargado = REPO.cargar(ruta);
            Posicion pa = cargado.getPosiciones().obtener(Tipo.ENTRADA, "A");
            Posicion pg = cargado.getPosiciones().obtener(Tipo.COMPUERTA, "G1");
            Posicion pf = cargado.getPosiciones().obtener(Tipo.SALIDA, "F");
            verificar("se recuperan las posiciones guardadas", pa != null && pa.getX() == 30 && pa.getY() == 60
                    && pg != null && pg.getX() == 230 && pg.getY() == 100
                    && pf != null && pf.getX() == 700 && pf.getY() == 60);
            verificar("un elemento sin posición guardada devuelve null",
                    cargado.getPosiciones().obtener(Tipo.ENTRADA, "B") == null);
            verificar("solo se recuperan las 3 posiciones guardadas", cargado.getPosiciones().cantidad() == 3);
        } catch (IOException | FormatoCircuitoException e) {
            verificar("posiciones sin errores: " + e, false);
        }
    }

    private static void probarElementosSinConectar() throws IOException {
        titulo("Compuertas y salidas sin conectar");
        Circuito c = new Circuito("Incompleto");
        EntradaLogica a = c.agregarEntrada();
        Compuerta and = c.agregarCompuerta(CatalogoCompuertas.obtener("AND"));
        c.agregarCompuerta(CatalogoCompuertas.obtener("NOT"));
        c.agregarSalida();
        c.conectar(and, 0, a);
        Path ruta = carpeta.resolve("incompleto.txt");
        try {
            REPO.guardar(c, null, ruta);
            List<String> lineas = Files.readAllLines(ruta, StandardCharsets.UTF_8);
            verificar("una entrada libre se guarda como «-»", lineas.contains("COMPUERTA;G1;AND;A;-")
                    && lineas.contains("COMPUERTA;G2;NOT;-") && lineas.contains("SALIDA;F;-"));
            Circuito cargado = REPO.cargar(ruta).getCircuito();
            verificar("se recuperan las entradas libres",
                    cargado.buscarCompuerta("G1").getEntrada(0) == cargado.buscarEntrada("A")
                    && cargado.buscarCompuerta("G1").getEntrada(1) == null
                    && cargado.buscarCompuerta("G2").getEntrada(0) == null
                    && cargado.buscarSalida("F").getFuente() == null);
        } catch (IOException | FormatoCircuitoException e) {
            verificar("sin conectar, sin errores: " + e, false);
        }
    }

    private static void probarOrdenLibreDeDeclaraciones() throws IOException {
        titulo("Dos pasadas: una compuerta puede referirse a otra declarada después");
        String contenido = CABECERA
                + "SALIDA;F;G2\n"
                + "COMPUERTA;G2;NOT;G1\n"
                + "COMPUERTA;G1;AND;A;B\n"
                + "ENTRADA;A;true\n"
                + "ENTRADA;B;true\n";
        try {
            Circuito c = REPO.cargar(escribir("desorden.txt", contenido)).getCircuito();
            verificar("las referencias hacia adelante se resuelven", c.buscarCompuerta("G2").getEntrada(0) == c.buscarCompuerta("G1")
                    && c.buscarSalida("F").getFuente() == c.buscarCompuerta("G2"));
            verificar("NOT(AND(1,1)) = 0", !c.evaluarSalidas().get("F"));
        } catch (IOException | FormatoCircuitoException e) {
            verificar("orden libre sin errores: " + e, false);
        }
    }

    private static void probarNombresAutomaticosTrasCargar() throws IOException {
        titulo("Tras cargar, las compuertas nuevas no repiten nombres");
        String contenido = CABECERA + "ENTRADA;A;false\nCOMPUERTA;G3;NOT;A\n";
        try {
            Circuito c = REPO.cargar(escribir("g3.txt", contenido)).getCircuito();
            Compuerta nueva = c.agregarCompuerta(CatalogoCompuertas.obtener("AND"));
            verificar("la siguiente compuerta automática es G4 (no G1 ni G3)", nueva.getNombre().equals("G4"));
        } catch (IOException | FormatoCircuitoException e) {
            verificar("nombres automáticos sin errores: " + e, false);
        }
    }

    // ------------------------------------------------------------------
    // Archivos inválidos: cada uno debe producir FormatoCircuitoException con su línea
    // ------------------------------------------------------------------

    private static void probarArchivosInvalidos() throws IOException {
        titulo("Archivos inválidos → FormatoCircuitoException con mensaje y línea");
        esperarFormato("archivo vacío", "", "vacío", 0);
        esperarFormato("solo líneas en blanco", "\n   \n\n", "vacío", 0);
        esperarFormato("cabecera incorrecta", "HOLA;1\n", "cabecera", 1);
        esperarFormato("cabecera sin versión", "CIRCUITO\n", "Cabecera inválida", 1);
        esperarFormato("versión no soportada", "CIRCUITO;2;X\n", "Versión", 1);
        esperarFormato("nombre de circuito vacío", "CIRCUITO;1;\n", "nombre del circuito", 1);
        esperarFormato("cabecera repetida", CABECERA + "CIRCUITO;1;Otro\n", "solo puede aparecer una vez", 2);
        esperarFormato("tipo de línea desconocido", CABECERA + "PUERTA;X\n", "Tipo de línea desconocido", 2);
        esperarFormato("compuerta de tipo desconocido", CABECERA + "ENTRADA;A;true\nCOMPUERTA;G1;XNOR;A;A\n",
                "Tipo de compuerta desconocido", 3);
        esperarFormato("referencia a una fuente inexistente", CABECERA + "ENTRADA;A;true\nCOMPUERTA;G1;AND;A;Z\n",
                "no existe", 3);
        esperarFormato("salida con fuente inexistente", CABECERA + "SALIDA;F;G9\n", "no existe", 2);
        esperarFormato("nombre duplicado", CABECERA + "ENTRADA;A;true\nENTRADA;A;false\n", "repetido", 3);
        esperarFormato("nombre repetido entre entrada y compuerta",
                CABECERA + "ENTRADA;G1;true\nCOMPUERTA;G1;NOT;G1\n", "repetido", 3);
        esperarFormato("NOT con dos entradas", CABECERA + "ENTRADA;A;true\nCOMPUERTA;G1;NOT;A;A\n",
                "necesita exactamente 1", 3);
        esperarFormato("AND con una sola entrada", CABECERA + "ENTRADA;A;true\nCOMPUERTA;G1;AND;A\n",
                "necesita exactamente 2", 3);
        esperarFormato("entrada de compuerta vacía", CABECERA + "ENTRADA;A;true\nCOMPUERTA;G1;AND;A;\n", "vacía", 3);
        esperarFormato("ENTRADA sin valor", CABECERA + "ENTRADA;A\n", "ENTRADA necesita", 2);
        esperarFormato("valor lógico inválido", CABECERA + "ENTRADA;A;quizas\n", "Valor lógico inválido", 2);
        esperarFormato("posición no numérica", CABECERA + "ENTRADA;A;true;x;y\n", "dos números enteros", 2);
        esperarFormato("posición incompleta", CABECERA + "ENTRADA;A;true;30\n", "ENTRADA necesita", 2);
        esperarFormato("nombre reservado «-»", CABECERA + "ENTRADA;-;true\n", "reservado", 2);
        esperarFormato("ciclo G1 → G2 → G1",
                CABECERA + "ENTRADA;A;true\nCOMPUERTA;G1;AND;A;G2\nCOMPUERTA;G2;NOT;G1\n", "ciclo", 4);
        esperarFormato("autoconexión de una compuerta", CABECERA + "COMPUERTA;G1;NOT;G1\n", "ciclo", 2);

        // Contenido binario: no es UTF-8 válido
        Path binario = carpeta.resolve("binario.txt");
        Files.write(binario, new byte[]{(byte) 0xC3, (byte) 0x28, (byte) 0xFF, (byte) 0xFE, 0x00});
        try {
            REPO.cargar(binario);
            verificar("un archivo binario debe rechazarse", false);
        } catch (FormatoCircuitoException e) {
            verificar("archivo binario → FormatoCircuitoException (" + e.getMessage() + ")", e.getCause() != null);
        } catch (IOException e) {
            verificar("el binario no debe salir como IOException genérica: " + e, false);
        }
    }

    private static void esperarFormato(String descripcion, String contenido, String fragmento, int lineaEsperada)
            throws IOException {
        Path ruta = escribir("invalido.txt", contenido);
        try {
            REPO.cargar(ruta);
            verificar(descripcion + ": debía rechazarse", false);
        } catch (FormatoCircuitoException e) {
            boolean mensaje = e.getMessage().contains(fragmento);
            boolean linea = e.getLinea() == lineaEsperada;
            verificar(descripcion + " → «" + e.getMessage() + "»", mensaje && linea);
            if (!(mensaje && linea)) {
                System.out.println("           (se esperaba «" + fragmento + "» en la línea " + lineaEsperada
                        + "; llegó la línea " + e.getLinea() + ")");
            }
        } catch (IOException e) {
            verificar(descripcion + ": no debía ser IOException: " + e, false);
        }
    }

    // ------------------------------------------------------------------
    // Errores de acceso y nombres no guardables
    // ------------------------------------------------------------------

    private static void probarErroresDeAcceso() {
        titulo("Errores de acceso al archivo → IOException (no FormatoCircuitoException)");
        try {
            REPO.cargar(carpeta.resolve("no-existe.txt"));
            verificar("una ruta inexistente debe fallar", false);
        } catch (NoSuchFileException e) {
            verificar("archivo inexistente → NoSuchFileException", true);
        } catch (IOException | FormatoCircuitoException e) {
            verificar("archivo inexistente debía ser NoSuchFileException, llegó " + e, false);
        }
        try {
            REPO.cargar(carpeta);
            verificar("un directorio no es un circuito", false);
        } catch (IOException e) {
            verificar("leer un directorio → IOException", true);
        } catch (FormatoCircuitoException e) {
            verificar("un directorio no debe tratarse como formato inválido", false);
        }
        try {
            Path anidada = carpeta.resolve("sub").resolve("otra").resolve("c.txt");
            REPO.guardar(crearCircuitoFyG(), null, anidada);
            verificar("guardar crea las carpetas que falten", Files.isRegularFile(anidada));
        } catch (IOException e) {
            verificar("guardar en carpeta nueva sin errores: " + e, false);
        }
    }

    private static void probarNombresNoGuardables() throws IOException {
        titulo("Nombres que romperían el formato no se guardan, y no dañan el archivo anterior");
        Path ruta = carpeta.resolve("seguro.txt");
        REPO.guardar(crearCircuitoFyG(), null, ruta);
        String antes = new String(Files.readAllBytes(ruta), StandardCharsets.UTF_8);

        Circuito malo = new Circuito("Malo");
        malo.agregarEntrada("A;B");
        intentarGuardarInvalido("nombre con «;»", malo, ruta);

        Circuito guion = new Circuito("Guion");
        guion.agregarEntrada("-");
        intentarGuardarInvalido("nombre «-» reservado", guion, ruta);

        Circuito repetido = new Circuito("Repetido");
        repetido.agregarEntrada("G1");
        repetido.agregarCompuerta(CatalogoCompuertas.obtener("NOT")); // se llama G1
        intentarGuardarInvalido("nombre repetido entre entrada y compuerta", repetido, ruta);

        String despues = new String(Files.readAllBytes(ruta), StandardCharsets.UTF_8);
        verificar("el archivo anterior quedó intacto tras los intentos fallidos", antes.equals(despues));
    }

    private static void intentarGuardarInvalido(String descripcion, Circuito c, Path ruta) {
        try {
            REPO.guardar(c, null, ruta);
            verificar(descripcion + ": debía rechazarse", false);
        } catch (CircuitoException e) {
            verificar(descripcion + " → " + e.getMessage(), true);
        } catch (IOException e) {
            verificar(descripcion + ": no debía ser IOException: " + e, false);
        }
    }

    // ------------------------------------------------------------------

    private static Path escribir(String nombre, String contenido) throws IOException {
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
