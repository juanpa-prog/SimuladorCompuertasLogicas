package compuertas.vista.componentes;

import compuertas.modelo.Compuerta;
import compuertas.modelo.excepciones.CircuitoException;
import java.util.HashMap;
import java.util.Map;

/**
 * Crea el componente visual que corresponde a una compuerta YA existente en el modelo
 * (por ejemplo, al abrir un archivo). Se elige por el tipo de la compuerta con una tabla,
 * sin switch ni instanceof; agregar una compuerta nueva es registrar una línea aquí.
 */
public final class CatalogoVisuales {

    @FunctionalInterface
    private interface Constructor {
        VisualCompuerta crear(Compuerta compuerta, int x, int y);
    }

    private static final Map<String, Constructor> CONSTRUCTORES = new HashMap<>();

    static {
        CONSTRUCTORES.put("AND", VisualAND::new);
        CONSTRUCTORES.put("OR", VisualOR::new);
        CONSTRUCTORES.put("NOT", VisualNOT::new);
        CONSTRUCTORES.put("XOR", VisualXOR::new);
        CONSTRUCTORES.put("NAND", VisualNAND::new);
        CONSTRUCTORES.put("NOR", VisualNOR::new);
    }

    private CatalogoVisuales() {
    }

    /** @throws CircuitoException si no hay representación visual para ese tipo de compuerta. */
    public static VisualCompuerta crear(Compuerta compuerta, int x, int y) {
        Constructor constructor = CONSTRUCTORES.get(compuerta.getTipo());
        if (constructor == null) {
            throw new CircuitoException("No hay representación gráfica para la compuerta «"
                    + compuerta.getTipo() + "».");
        }
        return constructor.crear(compuerta, x, y);
    }
}
