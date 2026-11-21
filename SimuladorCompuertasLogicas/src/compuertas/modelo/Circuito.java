package compuertas.modelo;

import compuertas.modelo.excepciones.CircuitoException;
import compuertas.modelo.excepciones.ConexionInvalidaException;
import compuertas.modelo.fabrica.CreadorCompuerta;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Agregado raíz del modelo.
 */
public class Circuito {

    private static final String[] NOMBRES_ENTRADA = {"A", "B", "C", "D", "E"};
    private static final String[] NOMBRES_SALIDA = {"F", "G", "H", "I", "J"};

    private final String nombre;
    private final List<EntradaLogica> entradas = new ArrayList<>();
    private final List<Compuerta> compuertas = new ArrayList<>();
    private final List<SalidaLogica> salidas = new ArrayList<>();
    private int contadorCompuertas;

    public Circuito(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del circuito es obligatorio");
        }
        this.nombre = nombre.trim();
    }

    public String getNombre() {
        return nombre;
    }

    // ---------------------------------------------------------------
    // Creación de componentes 
    // ---------------------------------------------------------------

 
    public EntradaLogica agregarEntrada() {
        return agregarEntrada(siguienteNombreEntrada());
    }

    public EntradaLogica agregarEntrada(String nombre) {
        if (buscarEntrada(nombre) != null) {
            throw new CircuitoException("Ya existe una entrada llamada «" + nombre + "».");
        }
        EntradaLogica entrada = new EntradaLogica(nombre);
        entradas.add(entrada);
        return entrada;
    }


    public SalidaLogica agregarSalida() {
        return agregarSalida(siguienteNombreSalida());
    }

    public SalidaLogica agregarSalida(String nombre) {
        if (buscarSalida(nombre) != null) {
            throw new CircuitoException("Ya existe una salida llamada «" + nombre + "».");
        }
        SalidaLogica salida = new SalidaLogica(nombre);
        salidas.add(salida);
        return salida;
    }

    /** El tipo de compuerta lo decide el creador (Factory Method); el nombre es G1, G2... */
    public Compuerta agregarCompuerta(CreadorCompuerta creador) {
        if (creador == null) {
            throw new IllegalArgumentException("El creador de compuertas es obligatorio");
        }
        String nombreNuevo;
        do {
            contadorCompuertas++;
            nombreNuevo = "G" + contadorCompuertas;
        } while (buscarCompuerta(nombreNuevo) != null);

        Compuerta compuerta = creador.crear(nombreNuevo);
        compuertas.add(compuerta);
        return compuerta;
    }

   
    public Compuerta agregarCompuerta(CreadorCompuerta creador, String nombre) {
        if (creador == null) {
            throw new IllegalArgumentException("El creador de compuertas es obligatorio");
        }
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new CircuitoException("El nombre de la compuerta es obligatorio.");
        }
        String limpio = nombre.trim();
        if (buscarCompuerta(limpio) != null) {
            throw new CircuitoException("Ya existe una compuerta llamada «" + limpio + "».");
        }
        Compuerta compuerta = creador.crear(limpio);
        compuertas.add(compuerta);
        if (limpio.matches("G\\d{1,6}")) {
            contadorCompuertas = Math.max(contadorCompuertas, Integer.parseInt(limpio.substring(1)));
        }
        return compuerta;
    }

    // ---------------------------------------------------------------
    // Conexiones
    // ---------------------------------------------------------------

    public void conectar(Compuerta destino, int indiceEntrada, FuenteLogica origen) {
        if (!compuertas.contains(destino)) {
            throw new ConexionInvalidaException("La compuerta destino no pertenece a este circuito.");
        }
        exigirPertenencia(origen);
        destino.conectar(indiceEntrada, origen);
    }

    public void conectarSalida(SalidaLogica salida, FuenteLogica origen) {
        if (!salidas.contains(salida)) {
            throw new ConexionInvalidaException("La salida no pertenece a este circuito.");
        }
        exigirPertenencia(origen);
        salida.conectar(origen);
    }

    private void exigirPertenencia(FuenteLogica origen) {
        if (origen == null || !(entradas.contains(origen) || compuertas.contains(origen))) {
            throw new ConexionInvalidaException("La fuente de origen no pertenece a este circuito.");
        }
    }

    // ---------------------------------------------------------------
    // Eliminación: se limpian también las conexiones que apuntaban al elemento
    // ---------------------------------------------------------------

    public boolean eliminarEntrada(EntradaLogica entrada) {
        if (!entradas.remove(entrada)) {
            return false;
        }
        liberarConexionesHacia(entrada);
        return true;
    }

    public boolean eliminarCompuerta(Compuerta compuerta) {
        if (!compuertas.remove(compuerta)) {
            return false;
        }
        liberarConexionesHacia(compuerta);
        return true;
    }

    public boolean eliminarSalida(SalidaLogica salida) {
        return salidas.remove(salida);
    }

    private void liberarConexionesHacia(FuenteLogica fuente) {
        for (Compuerta otra : compuertas) {
            otra.desconectarFuente(fuente);
        }
        for (SalidaLogica salida : salidas) {
            salida.desconectarFuente(fuente);
        }
    }

    public void limpiar() {
        entradas.clear();
        compuertas.clear();
        salidas.clear();
        contadorCompuertas = 0;
    }

    // ---------------------------------------------------------------
    // Búsqueda (por nombre)
    // ---------------------------------------------------------------

    public EntradaLogica buscarEntrada(String nombre) {
        for (EntradaLogica entrada : entradas) {
            if (entrada.getNombre().equals(nombre)) {
                return entrada;
            }
        }
        return null;
    }

    public Compuerta buscarCompuerta(String nombre) {
        for (Compuerta compuerta : compuertas) {
            if (compuerta.getNombre().equals(nombre)) {
                return compuerta;
            }
        }
        return null;
    }

    public SalidaLogica buscarSalida(String nombre) {
        for (SalidaLogica salida : salidas) {
            if (salida.getNombre().equals(nombre)) {
                return salida;
            }
        }
        return null;
    }

    // ---------------------------------------------------------------
    // Evaluación (polimórfica)
    // ---------------------------------------------------------------

  
    public Map<String, Boolean> evaluarTodas() {
        Map<String, Boolean> resultados = new LinkedHashMap<>();
        for (Compuerta compuerta : compuertas) {
            resultados.put(compuerta.getNombre(), compuerta.evaluar());
        }
        return resultados;
    }

    public Map<String, Boolean> evaluarSalidas() {
        Map<String, Boolean> resultados = new LinkedHashMap<>();
        for (SalidaLogica salida : salidas) {
            resultados.put(salida.getNombre(), salida.getValor());
        }
        return resultados;
    }

    public TablaVerdad generarTablaVerdad() {
        return new TablaVerdad(entradas, salidas);
    }

    // ---------------------------------------------------------------
    // Consulta (vistas de solo lectura)
    // ---------------------------------------------------------------

    public List<EntradaLogica> getEntradas() {
        return Collections.unmodifiableList(entradas);
    }

    public List<Compuerta> getCompuertas() {
        return Collections.unmodifiableList(compuertas);
    }

    public List<SalidaLogica> getSalidas() {
        return Collections.unmodifiableList(salidas);
    }

    public int cantidadComponentes() {
        return entradas.size() + compuertas.size() + salidas.size();
    }

    // ---------------------------------------------------------------
    // Nombres automáticos
    // ---------------------------------------------------------------

    private String siguienteNombreEntrada() {
        for (String candidato : NOMBRES_ENTRADA) {
            if (buscarEntrada(candidato) == null) {
                return candidato;
            }
        }
        int n = 1;
        while (buscarEntrada("X" + n) != null) {
            n++;
        }
        return "X" + n;
    }

    private String siguienteNombreSalida() {
        for (String candidato : NOMBRES_SALIDA) {
            if (buscarSalida(candidato) == null) {
                return candidato;
            }
        }
        int n = 1;
        while (buscarSalida("Y" + n) != null) {
            n++;
        }
        return "Y" + n;
    }
}
