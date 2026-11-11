package compuertas.modelo.fabrica;

import compuertas.modelo.Compuerta;
import compuertas.modelo.CompuertaNAND;

/** Creador concreto de compuertas NAND. */
public class CreadorNAND extends CreadorCompuerta {

    @Override
    public String getTipo() {
        return "NAND";
    }

    @Override
    protected Compuerta crearCompuerta(String nombre) {
        return new CompuertaNAND(nombre);
    }
}
