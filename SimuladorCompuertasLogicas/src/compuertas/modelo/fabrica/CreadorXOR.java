package compuertas.modelo.fabrica;

import compuertas.modelo.Compuerta;
import compuertas.modelo.CompuertaXOR;

/** Creador concreto de compuertas XOR. */
public class CreadorXOR extends CreadorCompuerta {

    @Override
    public String getTipo() {
        return "XOR";
    }

    @Override
    protected Compuerta crearCompuerta(String nombre) {
        return new CompuertaXOR(nombre);
    }
}
