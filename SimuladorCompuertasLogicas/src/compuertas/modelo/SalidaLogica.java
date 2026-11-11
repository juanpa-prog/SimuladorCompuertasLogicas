package compuertas.modelo;

import compuertas.modelo.excepciones.ConexionInvalidaException;
import compuertas.modelo.excepciones.EntradaSinConectarException;

/**
 * Receptor final de un resultado (F, G...). No es una fuente: solo lee
 * el valor de la fuente a la que está conectada.
 */
public class SalidaLogica {

    private final String nombre;
    private FuenteLogica fuente;

    public SalidaLogica(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la salida es obligatorio");
        }
        this.nombre = nombre.trim();
    }

    public String getNombre() {
        return nombre;
    }

    public FuenteLogica getFuente() {
        return fuente;
    }

    public void conectar(FuenteLogica fuente) {
        if (fuente == null) {
            throw new ConexionInvalidaException("La salida «" + nombre + "» necesita una fuente válida.");
        }
        this.fuente = fuente;
    }

    public void desconectar() {
        fuente = null;
    }

    /** Se desconecta solo si estaba conectada a la fuente indicada. */
    public void desconectarFuente(FuenteLogica otra) {
        if (fuente == otra) {
            fuente = null;
        }
    }

    public boolean getValor() {
        if (fuente == null) {
            throw new EntradaSinConectarException("La salida «" + nombre + "» no tiene ninguna fuente conectada.");
        }
        return fuente.getValor();
    }

    @Override
    public String toString() {
        return nombre;
    }
}
