package compuertas.vista;

import compuertas.modelo.Circuito;
import compuertas.vista.componentes.ComponenteVisual;

/**
 * Ítem de la paleta. Sabe cómo construir, junto con su elemento del
 * modelo, el componente visual correspondiente. Así agregar una compuerta
 * nueva a la paleta no requiere ningún switch por tipo.
 */
public class ElementoPaleta {

    /** Crea el elemento en el modelo y devuelve su representación visual. */
    @FunctionalInterface
    public interface Constructor {
        ComponenteVisual construir(Circuito circuito, int x, int y);
    }

    private final String grupo;
    private final String etiqueta;
    private final int columna;
    private final Constructor constructor;

    public ElementoPaleta(String grupo, String etiqueta, int columna, Constructor constructor) {
        this.grupo = grupo;
        this.etiqueta = etiqueta;
        this.columna = columna;
        this.constructor = constructor;
    }

    public String getGrupo() {
        return grupo;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    /** X preferida al colocarlo en el lienzo (entradas a la izquierda, salidas a la derecha). */
    public int getColumna() {
        return columna;
    }

    public ComponenteVisual construir(Circuito circuito, int x, int y) {
        return constructor.construir(circuito, x, y);
    }

    /** Componente descartable, creado sobre un circuito de mentira, para dibujar la miniatura. */
    public ComponenteVisual crearVistaPrevia() {
        return constructor.construir(new Circuito("vista previa"), 0, 0);
    }
}
