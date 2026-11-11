package compuertas.modelo;

import compuertas.modelo.excepciones.ConexionInvalidaException;
import compuertas.modelo.excepciones.EntradaSinConectarException;

/**
 * Superclase abstracta de todas las compuertas. No tiene sentido una
 * "compuerta genérica": solo define lo común (nombre, conexiones) y deja
 * a cada subclase la regla booleana en {@link #evaluar()}.
 *
 * Una compuerta ES una {@link FuenteLogica}, por lo que su salida puede
 * conectarse a la entrada de otra compuerta.
 */
public abstract class Compuerta implements FuenteLogica {

    private final String nombre;

    protected Compuerta(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la compuerta es obligatorio");
        }
        this.nombre = nombre.trim();
    }

    public String getNombre() {
        return nombre;
    }

    /** @return "AND", "OR", "NOT"... */
    public abstract String getTipo();

    public abstract int getNumeroEntradas();

    /** @return la fuente conectada en esa posición, o null si está libre. */
    public abstract FuenteLogica getEntrada(int indice);

    /** Cada subclase decide dónde guarda sus entradas. */
    protected abstract void asignarEntrada(int indice, FuenteLogica fuente);

    /** Regla booleana propia de cada compuerta (polimorfismo dinámico). */
    public abstract boolean evaluar();

    @Override
    public boolean getValor() {
        return evaluar();
    }

    // --- Conexiones: la validación vive aquí, protegiendo la invariante "sin ciclos" ---

    public final void conectar(int indice, FuenteLogica fuente) {
        validarIndice(indice);
        if (fuente == null) {
            throw new ConexionInvalidaException("La compuerta «" + nombre + "» necesita una fuente válida.");
        }
        if (fuente.dependeDe(this)) {
            throw new ConexionInvalidaException(
                    "Conexión inválida: «" + nombre + "» quedaría dependiendo de sí misma (ciclo).");
        }
        asignarEntrada(indice, fuente);
    }

    public final void desconectar(int indice) {
        validarIndice(indice);
        asignarEntrada(indice, null);
    }

    /** Libera toda entrada que esté conectada a la fuente indicada. */
    public final void desconectarFuente(FuenteLogica fuente) {
        for (int i = 0; i < getNumeroEntradas(); i++) {
            if (getEntrada(i) == fuente) {
                asignarEntrada(i, null);
            }
        }
    }

    public final boolean estaCompleta() {
        for (int i = 0; i < getNumeroEntradas(); i++) {
            if (getEntrada(i) == null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean dependeDe(FuenteLogica otra) {
        if (this == otra) {
            return true;
        }
        for (int i = 0; i < getNumeroEntradas(); i++) {
            FuenteLogica entrada = getEntrada(i);
            if (entrada != null && entrada.dependeDe(otra)) {
                return true;
            }
        }
        return false;
    }

    /** Para uso de las subclases: entrega la fuente o falla con un mensaje claro. */
    protected final FuenteLogica exigirEntrada(int indice) {
        FuenteLogica fuente = getEntrada(indice);
        if (fuente == null) {
            throw new EntradaSinConectarException(nombre, indice);
        }
        return fuente;
    }

    private void validarIndice(int indice) {
        if (indice < 0 || indice >= getNumeroEntradas()) {
            throw new ConexionInvalidaException(
                    "La compuerta «" + nombre + "» no tiene la entrada " + (indice + 1) + ".");
        }
    }

    @Override
    public String toString() {
        return nombre + " [" + getTipo() + "]";
    }
}
