package compuertas.vista.componentes;

import compuertas.modelo.Compuerta;
import java.awt.Shape;

/** Compuerta NOR: la forma de OR con burbuja de negación. */
public class VisualNOR extends VisualCompuerta {

    public VisualNOR(Compuerta compuerta) {
        this(compuerta, POSICION_DEFECTO, POSICION_DEFECTO);
    }

    public VisualNOR(Compuerta compuerta, int x, int y) {
        super(compuerta, x, y, 12, 102);
    }

    @Override
    protected Shape crearForma() {
        return Formas.conBurbuja(Formas.cuerpoOR(), 97);
    }
}
