package compuertas.vista.componentes;

import compuertas.modelo.FuenteLogica;
import compuertas.modelo.excepciones.CircuitoException;

/**
 * Dibujar nunca debe lanzar excepciones: si una compuerta está incompleta,
 * simplemente se pinta como 0 (la excepción informativa la muestra el
 * controlador cuando el usuario pide evaluar).
 */
final class ValorSeguro {

    private ValorSeguro() {
    }

    /** @return el valor de la fuente, o false si es null o está incompleta. */
    static boolean de(FuenteLogica fuente) {
        if (fuente == null) {
            return false;
        }
        try {
            return fuente.getValor();
        } catch (CircuitoException e) {
            return false;
        }
    }

    /** @return "0", "1" o "?" si no se puede determinar. */
    static String texto(FuenteLogica fuente) {
        if (fuente == null) {
            return "?";
        }
        try {
            return fuente.getValor() ? "1" : "0";
        } catch (CircuitoException e) {
            return "?";
        }
    }
}
