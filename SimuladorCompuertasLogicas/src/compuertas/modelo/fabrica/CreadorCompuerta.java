package compuertas.modelo.fabrica;

import compuertas.modelo.Compuerta;
import compuertas.modelo.FuenteLogica;

/**
 * Creator del patrón Factory Method (GoF).
 *
 * {@link #crearCompuerta(String)} es el factory method: cada creador
 * concreto decide qué compuerta instanciar, y este código no sabe cuál.
 * {@link #crear(String, FuenteLogica...)} es la operación que aprovecha el
 * producto creado (igual que enviarPedido() en el ejemplo de la pizzería).
 *
 * Agregar una compuerta nueva (por ejemplo XNOR) solo requiere una clase
 * nueva y su creador: no hay ningún switch que modificar.
 */
public abstract class CreadorCompuerta {

    /** Nombre del tipo de compuerta que fabrica este creador. */
    public abstract String getTipo();

    /** FACTORY METHOD: devuelve una compuerta sin conectar. */
    protected abstract Compuerta crearCompuerta(String nombre);

    /**
     * Crea la compuerta y, si se entregan fuentes, las conecta en orden.
     * Sin fuentes devuelve la compuerta libre (la GUI conecta después).
     */
    public Compuerta crear(String nombre, FuenteLogica... entradas) {
        Compuerta compuerta = crearCompuerta(nombre);
        if (entradas.length != 0 && entradas.length != compuerta.getNumeroEntradas()) {
            throw new IllegalArgumentException("La compuerta " + compuerta.getTipo() + " requiere "
                    + compuerta.getNumeroEntradas() + " entrada(s), pero se entregaron " + entradas.length);
        }
        for (int i = 0; i < entradas.length; i++) {
            compuerta.conectar(i, entradas[i]);
        }
        return compuerta;
    }
}
