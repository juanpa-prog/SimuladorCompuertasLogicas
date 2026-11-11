package compuertas.modelo.fabrica;

import compuertas.modelo.Compuerta;
import compuertas.modelo.CompuertaNOT;

/** Creador concreto de compuertas NOT. */
public class CreadorNOT extends CreadorCompuerta {

    @Override
    public String getTipo() {
        return "NOT";
    }

    @Override
    protected Compuerta crearCompuerta(String nombre) {
        return new CompuertaNOT(nombre);
    }
}
