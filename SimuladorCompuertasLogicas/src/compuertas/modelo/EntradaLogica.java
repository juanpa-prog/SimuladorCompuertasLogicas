package compuertas.modelo;

/**
 * Fuente lógica que el usuario controla (A, B, C...). El nombre es
 * inmutable; el valor cambia solo mediante operaciones con significado.
 */
public class EntradaLogica implements FuenteLogica {

    private static final String NOMBRE_DEFECTO = "Entrada";

    private final String nombre;
    private boolean valor;

    // --- Constructores sobrecargados: delegan con this(...) ---

    public EntradaLogica(boolean valor) {
        this(NOMBRE_DEFECTO, valor);
    }

    public EntradaLogica(String nombre) {
        this(nombre, false);
    }

    public EntradaLogica(String nombre, boolean valor) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la entrada es obligatorio");
        }
        this.nombre = nombre.trim();
        this.valor = valor;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public boolean getValor() {
        return valor;
    }

    public void setValor(boolean valor) {
        this.valor = valor;
    }

    /** Cambia el valor de 0 a 1 o de 1 a 0. */
    public void alternar() {
        valor = !valor;
    }

    @Override
    public String toString() {
        return nombre + "=" + (valor ? 1 : 0);
    }
}
