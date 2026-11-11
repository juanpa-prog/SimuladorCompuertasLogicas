package compuertas.vista.componentes;

import compuertas.modelo.Compuerta;
import java.awt.Shape;

/** Compuerta AND: lado recto y curva a la derecha. */
public class VisualAND extends VisualCompuerta {

    public VisualAND(Compuerta compuerta) {
        this(compuerta, POSICION_DEFECTO, POSICION_DEFECTO);
    }

    public VisualAND(Compuerta compuerta, int x, int y) {
        super(compuerta, x, y, 0, 79);
    }

    @Override
    protected Shape crearForma() {
        return Formas.cuerpoAND();
    }
}
