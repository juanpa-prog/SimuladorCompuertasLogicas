package compuertas.vista.componentes;

import compuertas.modelo.Compuerta;
import java.awt.Shape;

/** Compuerta NOT: triángulo con burbuja de negación. */
public class VisualNOT extends VisualCompuerta {

    public VisualNOT(Compuerta compuerta) {
        this(compuerta, POSICION_DEFECTO, POSICION_DEFECTO);
    }

    public VisualNOT(Compuerta compuerta, int x, int y) {
        super(compuerta, x, y, 0, 80);
    }

    @Override
    protected Shape crearForma() {
        return Formas.conBurbuja(Formas.triangulo(), 75);
    }
}
