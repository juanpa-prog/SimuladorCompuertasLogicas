package compuertas.vista.componentes;

import compuertas.modelo.Compuerta;
import java.awt.Shape;

/** Compuerta NAND: la forma de AND con burbuja de negación. */
public class VisualNAND extends VisualCompuerta {

    public VisualNAND(Compuerta compuerta) {
        this(compuerta, POSICION_DEFECTO, POSICION_DEFECTO);
    }

    public VisualNAND(Compuerta compuerta, int x, int y) {
        super(compuerta, x, y, 0, 89);
    }

    @Override
    protected Shape crearForma() {
        return Formas.conBurbuja(Formas.cuerpoAND(), 84);
    }
}
