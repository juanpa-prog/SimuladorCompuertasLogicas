package compuertas.vista.componentes;

import compuertas.modelo.Compuerta;
import java.awt.Graphics2D;
import java.awt.Shape;

/** Compuerta XOR: la forma de OR más un arco adicional a la izquierda. */
public class VisualXOR extends VisualCompuerta {

    public VisualXOR(Compuerta compuerta) {
        this(compuerta, POSICION_DEFECTO, POSICION_DEFECTO);
    }

    public VisualXOR(Compuerta compuerta, int x, int y) {
        super(compuerta, x, y, 3, 92);
    }

    @Override
    protected Shape crearForma() {
        return Formas.cuerpoOR();
    }

    @Override
    protected void dibujarDetalles(Graphics2D g) {
        super.dibujarDetalles(g);
        g.draw(Formas.arcoXOR());
    }
}
