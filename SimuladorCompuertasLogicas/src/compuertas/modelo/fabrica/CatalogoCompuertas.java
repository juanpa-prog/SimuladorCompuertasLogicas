package compuertas.modelo.fabrica;

import compuertas.modelo.excepciones.CircuitoException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*
  Catálogo único de los creadores de compuertas (usa el Factory Method).
 */
public final class CatalogoCompuertas {

    private static final Map<String, CreadorCompuerta> CREADORES = new LinkedHashMap<>();

    static {
        registrar(new CreadorAND());
        registrar(new CreadorOR());
        registrar(new CreadorNOT());
        registrar(new CreadorXOR());
        registrar(new CreadorNAND());
        registrar(new CreadorNOR());
    }

    private CatalogoCompuertas() {
    }

    private static void registrar(CreadorCompuerta creador) {
        CREADORES.put(creador.getTipo(), creador);
    }

    public static boolean existe(String tipo) {
        return tipo != null && CREADORES.containsKey(tipo);
    }

    /** @throws CircuitoException si el tipo no está registrado. */
    public static CreadorCompuerta obtener(String tipo) {
        CreadorCompuerta creador = tipo == null ? null : CREADORES.get(tipo);
        if (creador == null) {
            throw new CircuitoException("Tipo de compuerta desconocido: «" + tipo + "».");
        }
        return creador;
    }

    /** Tipos disponibles, en el orden de registro. */
    public static List<String> tipos() {
        return Collections.unmodifiableList(new ArrayList<>(CREADORES.keySet()));
    }
}
