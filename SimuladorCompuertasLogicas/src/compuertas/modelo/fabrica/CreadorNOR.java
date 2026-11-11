package compuertas.modelo.fabrica;

import compuertas.modelo.Compuerta;
import compuertas.modelo.CompuertaNOR;

/** Creador concreto de compuertas NOR. */
public class CreadorNOR extends CreadorCompuerta {

    @Override
    public String getTipo() {
        return "NOR";
    }

    @Override
    protected Compuerta crearCompuerta(String nombre) {
        return new CompuertaNOR(nombre);
    }
}
