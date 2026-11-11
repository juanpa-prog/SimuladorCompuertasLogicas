package compuertas.modelo.fabrica;

import compuertas.modelo.Compuerta;
import compuertas.modelo.CompuertaOR;

/** Creador concreto de compuertas OR. */
public class CreadorOR extends CreadorCompuerta {

    @Override
    public String getTipo() {
        return "OR";
    }

    @Override
    protected Compuerta crearCompuerta(String nombre) {
        return new CompuertaOR(nombre);
    }
}
