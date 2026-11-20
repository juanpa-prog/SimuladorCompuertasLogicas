package compuertas.persistencia;

import compuertas.modelo.Circuito;
import compuertas.modelo.Compuerta;
import compuertas.modelo.EntradaLogica;
import compuertas.modelo.FuenteLogica;
import compuertas.modelo.SalidaLogica;
import compuertas.modelo.excepciones.CircuitoException;
import compuertas.modelo.excepciones.FormatoCircuitoException;
import compuertas.modelo.fabrica.CatalogoCompuertas;
import compuertas.persistencia.PosicionesCircuito.Posicion;
import compuertas.persistencia.PosicionesCircuito.Tipo;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Guarda y carga circuitos en un archivo de TEXTO (UTF-8), una línea por elemento.
 * Los campos se separan con «;» y las posiciones x;y al final son opcionales:
 *
 * <pre>
 * CIRCUITO;1;Circuito 1
 * ENTRADA;A;true;30;60
 * ENTRADA;B;false;30;170
 * COMPUERTA;G1;AND;A;B;230;100
 * COMPUERTA;G2;NOT;C;-;230;300      (un «-» indica una entrada sin conectar)
 * SALIDA;F;G1;700;60
 * </pre>
 *
 * La carga se hace en DOS PASADAS: la primera lee y valida las declaraciones (así una
 * compuerta puede referirse a otra definida más adelante) y la segunda establece las
 * conexiones. Todo error de contenido se informa con el número de línea mediante
 * {@link FormatoCircuitoException}; los fallos de acceso al archivo salen como
 * {@link IOException}. Los recursos se cierran con try-with-resources.
 */
public final class RepositorioTexto implements RepositorioCircuitos {

    public static final String CABECERA = "CIRCUITO";
    public static final int VERSION = 1;

    private static final String SEPARADOR = ";";
    private static final String SIN_CONEXION = "-";
    private static final String NOMBRE_DEFECTO = "Circuito";

    // ------------------------------------------------------------------
    // Guardar
    // ------------------------------------------------------------------

    @Override
    public void guardar(Circuito circuito, PosicionesCircuito posiciones, Path ruta) throws IOException {
        if (circuito == null || ruta == null) {
            throw new IllegalArgumentException("El circuito y la ruta son obligatorios");
        }
        // Todo se compone y valida ANTES de tocar el archivo: si algo falla, no se pierde el anterior.
        List<String> lineas = componerLineas(circuito, posiciones == null ? new PosicionesCircuito() : posiciones);

        Path carpeta = ruta.toAbsolutePath().getParent();
        if (carpeta != null) {
            Files.createDirectories(carpeta);
        }
        try (BufferedWriter escritor = Files.newBufferedWriter(ruta, StandardCharsets.UTF_8)) {
            for (String linea : lineas) {
                escritor.write(linea);
                escritor.newLine();
            }
        }
    }

    private List<String> componerLineas(Circuito circuito, PosicionesCircuito posiciones) {
        validarNombres(circuito);
        List<String> lineas = new ArrayList<>();
        lineas.add(CABECERA + SEPARADOR + VERSION + SEPARADOR + circuito.getNombre());

        for (EntradaLogica entrada : circuito.getEntradas()) {
            List<String> campos = new ArrayList<>();
            campos.add("ENTRADA");
            campos.add(entrada.getNombre());
            campos.add(String.valueOf(entrada.getValor()));
            lineas.add(unir(campos, posiciones.obtener(Tipo.ENTRADA, entrada.getNombre())));
        }
        for (Compuerta compuerta : circuito.getCompuertas()) {
            List<String> campos = new ArrayList<>();
            campos.add("COMPUERTA");
            campos.add(compuerta.getNombre());
            campos.add(compuerta.getTipo());
            for (int i = 0; i < compuerta.getNumeroEntradas(); i++) {
                campos.add(nombreDe(compuerta.getEntrada(i)));
            }
            lineas.add(unir(campos, posiciones.obtener(Tipo.COMPUERTA, compuerta.getNombre())));
        }
        for (SalidaLogica salida : circuito.getSalidas()) {
            List<String> campos = new ArrayList<>();
            campos.add("SALIDA");
            campos.add(salida.getNombre());
            campos.add(nombreDe(salida.getFuente()));
            lineas.add(unir(campos, posiciones.obtener(Tipo.SALIDA, salida.getNombre())));
        }
        return lineas;
    }

    private static String nombreDe(FuenteLogica fuente) {
        return fuente == null ? SIN_CONEXION : fuente.getNombre();
    }

    private static String unir(List<String> campos, Posicion posicion) {
        if (posicion != null) {
            campos.add(String.valueOf(posicion.getX()));
            campos.add(String.valueOf(posicion.getY()));
        }
        return String.join(SEPARADOR, campos);
    }

    /** Los nombres no pueden romper el formato ni repetirse entre entradas, compuertas y salidas. */
    private void validarNombres(Circuito circuito) {
        exigirSinSeparadores(circuito.getNombre(), "circuito");
        Set<String> usados = new HashSet<>();
        for (EntradaLogica e : circuito.getEntradas()) {
            exigirNombreGuardable(e.getNombre(), usados);
        }
        for (Compuerta c : circuito.getCompuertas()) {
            exigirNombreGuardable(c.getNombre(), usados);
        }
        for (SalidaLogica s : circuito.getSalidas()) {
            exigirNombreGuardable(s.getNombre(), usados);
        }
    }

    private void exigirNombreGuardable(String nombre, Set<String> usados) {
        exigirSinSeparadores(nombre, "elemento");
        if (nombre.equals(SIN_CONEXION)) {
            throw new CircuitoException("No se puede guardar: el nombre «-» está reservado.");
        }
        if (!usados.add(nombre)) {
            throw new CircuitoException("No se puede guardar: el nombre «" + nombre
                    + "» está repetido entre entradas, compuertas y salidas.");
        }
    }

    private void exigirSinSeparadores(String nombre, String que) {
        if (nombre.contains(SEPARADOR) || nombre.contains("\n") || nombre.contains("\r")) {
            throw new CircuitoException("No se puede guardar: el nombre del " + que + " «" + nombre
                    + "» contiene un carácter no permitido (; o salto de línea).");
        }
    }

    // ------------------------------------------------------------------
    // Cargar
    // ------------------------------------------------------------------

    @Override
    public CircuitoCargado cargar(Path ruta) throws IOException, FormatoCircuitoException {
        if (ruta == null) {
            throw new IllegalArgumentException("La ruta es obligatoria");
        }
        try (BufferedReader lector = Files.newBufferedReader(ruta, StandardCharsets.UTF_8)) {
            return leer(lector);
        } catch (CharacterCodingException e) {
            // Bytes que no son UTF-8: el archivo existe pero su contenido no es un circuito de texto.
            throw new FormatoCircuitoException("El archivo no es un texto UTF-8 válido (¿está dañado?).", 0, e);
        }
    }

    /** Estado de una lectura en curso. */
    private static final class Lectura {
        Circuito circuito;
        final PosicionesCircuito posiciones = new PosicionesCircuito();
        final Set<String> nombres = new HashSet<>();
        final List<Pendiente> pendientes = new ArrayList<>();
    }

    /** Conexión que se establece en la segunda pasada, cuando ya existen todos los elementos. */
    private static final class Pendiente {
        final int linea;
        final Compuerta compuerta;
        final SalidaLogica salida;
        final int indice;
        final String fuente;

        Pendiente(int linea, Compuerta compuerta, SalidaLogica salida, int indice, String fuente) {
            this.linea = linea;
            this.compuerta = compuerta;
            this.salida = salida;
            this.indice = indice;
            this.fuente = fuente;
        }
    }

    private CircuitoCargado leer(BufferedReader lector) throws IOException, FormatoCircuitoException {
        Lectura lectura = new Lectura();
        String texto;
        int numero = 0;
        while ((texto = lector.readLine()) != null) {
            numero++;
            if (numero == 1 && texto.startsWith("\uFEFF")) {
                texto = texto.substring(1); // marca BOM de algunos editores
            }
            if (texto.trim().isEmpty()) {
                continue;
            }
            String[] campos = texto.split(SEPARADOR, -1);
            for (int i = 0; i < campos.length; i++) {
                campos[i] = campos[i].trim();
            }
            if (lectura.circuito == null) {
                lectura.circuito = leerCabecera(campos, numero);
                continue;
            }
            switch (campos[0]) {
                case "ENTRADA":
                    leerEntrada(lectura, campos, numero);
                    break;
                case "COMPUERTA":
                    leerCompuerta(lectura, campos, numero);
                    break;
                case "SALIDA":
                    leerSalida(lectura, campos, numero);
                    break;
                case CABECERA:
                    throw new FormatoCircuitoException("La cabecera " + CABECERA + " solo puede aparecer una vez.", numero);
                default:
                    throw new FormatoCircuitoException("Tipo de línea desconocido «" + campos[0] + "».", numero);
            }
        }
        if (lectura.circuito == null) {
            throw new FormatoCircuitoException("El archivo está vacío: falta la cabecera " + CABECERA + ";" + VERSION + ".");
        }
        resolverConexiones(lectura);
        return new CircuitoCargado(lectura.circuito, lectura.posiciones);
    }

    private Circuito leerCabecera(String[] c, int linea) throws FormatoCircuitoException {
        if (!c[0].equals(CABECERA)) {
            throw new FormatoCircuitoException("La primera línea debe ser la cabecera " + CABECERA + ";" + VERSION + ".", linea);
        }
        if (c.length < 2 || c.length > 3) {
            throw new FormatoCircuitoException("Cabecera inválida: use " + CABECERA + ";" + VERSION + ";nombre.", linea);
        }
        if (!c[1].equals(String.valueOf(VERSION))) {
            throw new FormatoCircuitoException("Versión de formato no soportada: «" + c[1]
                    + "» (se esperaba " + VERSION + ").", linea);
        }
        String nombre = c.length == 3 ? c[2] : NOMBRE_DEFECTO;
        if (nombre.isEmpty()) {
            throw new FormatoCircuitoException("El nombre del circuito está vacío.", linea);
        }
        return new Circuito(nombre);
    }

    private void leerEntrada(Lectura l, String[] c, int linea) throws FormatoCircuitoException {
        exigirLongitud(c, linea, "ENTRADA necesita nombre y valor (ENTRADA;A;true), con posición opcional.", 3);
        String nombre = declararNombre(l, c[1], linea);
        boolean valor = leerBooleano(c[2], linea);
        try {
            l.circuito.agregarEntrada(nombre).setValor(valor);
        } catch (CircuitoException e) {
            throw new FormatoCircuitoException(e.getMessage(), linea, e);
        }
        leerPosicion(l, c, 3, Tipo.ENTRADA, nombre, linea);
    }

    private void leerCompuerta(Lectura l, String[] c, int linea) throws FormatoCircuitoException {
        if (c.length < 4) {
            throw new FormatoCircuitoException("COMPUERTA necesita nombre, tipo y entradas (COMPUERTA;G1;AND;A;B).", linea);
        }
        String tipo = c[2];
        if (!CatalogoCompuertas.existe(tipo)) {
            throw new FormatoCircuitoException("Tipo de compuerta desconocido «" + tipo + "».", linea);
        }
        String nombre = declararNombre(l, c[1], linea);
        Compuerta compuerta;
        try {
            compuerta = l.circuito.agregarCompuerta(CatalogoCompuertas.obtener(tipo), nombre);
        } catch (CircuitoException e) {
            throw new FormatoCircuitoException(e.getMessage(), linea, e);
        }
        int n = compuerta.getNumeroEntradas();
        exigirLongitud(c, linea, "La compuerta " + tipo + " necesita exactamente " + n
                + " entrada(s) (use «-» si una no está conectada).", 3 + n);
        for (int i = 0; i < n; i++) {
            String fuente = c[3 + i];
            if (fuente.isEmpty()) {
                throw new FormatoCircuitoException("La entrada " + (i + 1) + " de «" + nombre
                        + "» está vacía (use «-» si no está conectada).", linea);
            }
            if (!fuente.equals(SIN_CONEXION)) {
                l.pendientes.add(new Pendiente(linea, compuerta, null, i, fuente));
            }
        }
        leerPosicion(l, c, 3 + n, Tipo.COMPUERTA, nombre, linea);
    }

    private void leerSalida(Lectura l, String[] c, int linea) throws FormatoCircuitoException {
        exigirLongitud(c, linea, "SALIDA necesita nombre y fuente (SALIDA;F;G1), con posición opcional.", 3);
        String nombre = declararNombre(l, c[1], linea);
        SalidaLogica salida;
        try {
            salida = l.circuito.agregarSalida(nombre);
        } catch (CircuitoException e) {
            throw new FormatoCircuitoException(e.getMessage(), linea, e);
        }
        String fuente = c[2];
        if (fuente.isEmpty()) {
            throw new FormatoCircuitoException("La fuente de la salida «" + nombre
                    + "» está vacía (use «-» si no está conectada).", linea);
        }
        if (!fuente.equals(SIN_CONEXION)) {
            l.pendientes.add(new Pendiente(linea, null, salida, 0, fuente));
        }
        leerPosicion(l, c, 3, Tipo.SALIDA, nombre, linea);
    }

    /** Segunda pasada: todo existe ya, se enlazan las fuentes y el modelo rechaza lo inválido (p. ej. ciclos). */
    private void resolverConexiones(Lectura l) throws FormatoCircuitoException {
        for (Pendiente p : l.pendientes) {
            FuenteLogica fuente = l.circuito.buscarEntrada(p.fuente);
            if (fuente == null) {
                fuente = l.circuito.buscarCompuerta(p.fuente);
            }
            if (fuente == null) {
                throw new FormatoCircuitoException("La fuente «" + p.fuente + "» no existe.", p.linea);
            }
            try {
                if (p.compuerta != null) {
                    l.circuito.conectar(p.compuerta, p.indice, fuente);
                } else {
                    l.circuito.conectarSalida(p.salida, fuente);
                }
            } catch (CircuitoException e) {
                throw new FormatoCircuitoException(e.getMessage(), p.linea, e);
            }
        }
    }

    // ------------------------------------------------------------------
    // Utilidades de validación
    // ------------------------------------------------------------------

    /** La línea debe tener {@code base} campos o {@code base + 2} (con posición x;y). */
    private void exigirLongitud(String[] c, int linea, String mensaje, int base) throws FormatoCircuitoException {
        if (c.length != base && c.length != base + 2) {
            throw new FormatoCircuitoException(mensaje, linea);
        }
    }

    private String declararNombre(Lectura l, String nombre, int linea) throws FormatoCircuitoException {
        if (nombre.isEmpty()) {
            throw new FormatoCircuitoException("El nombre está vacío.", linea);
        }
        if (nombre.equals(SIN_CONEXION)) {
            throw new FormatoCircuitoException("El nombre «-» está reservado.", linea);
        }
        if (!l.nombres.add(nombre)) {
            throw new FormatoCircuitoException("El nombre «" + nombre + "» está repetido.", linea);
        }
        return nombre;
    }

    private boolean leerBooleano(String texto, int linea) throws FormatoCircuitoException {
        if (texto.equals("true")) {
            return true;
        }
        if (texto.equals("false")) {
            return false;
        }
        throw new FormatoCircuitoException("Valor lógico inválido «" + texto + "»: use true o false.", linea);
    }

    private void leerPosicion(Lectura l, String[] c, int indice, Tipo tipo, String nombre, int linea)
            throws FormatoCircuitoException {
        if (c.length == indice) {
            return; // posición opcional ausente
        }
        try {
            l.posiciones.poner(tipo, nombre, Integer.parseInt(c[indice]), Integer.parseInt(c[indice + 1]));
        } catch (NumberFormatException e) {
            throw new FormatoCircuitoException("La posición debe ser dos números enteros (x;y).", linea, e);
        }
    }
}
