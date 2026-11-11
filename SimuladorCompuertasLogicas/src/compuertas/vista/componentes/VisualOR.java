package compuertas.vista.componentes;

import compuertas.modelo.Compuerta;
import java.awt.Shape;

/** Compuerta OR: curva interior a la izquierda y punta a la derecha. */
public class VisualOR extends VisualCompuerta {

    public VisualOR(Compuerta compuerta) {
        this(compuerta, POSICION_DEFECTO, POSICION_DEFECTO);
    }

    public VisualOR(Compuerta compuerta, int x, int y) {
        super(compuerta, x, y, 12, 92);
    }

    @Override
    protected Shape crearForma() {
        return Formas.cuerpoOR();
    }
}
