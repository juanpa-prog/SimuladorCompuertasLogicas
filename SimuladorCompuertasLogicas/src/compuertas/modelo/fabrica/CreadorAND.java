package compuertas.modelo.fabrica;

import compuertas.modelo.Compuerta;
import compuertas.modelo.CompuertaAND;

/** Creador concreto de compuertas AND. */
public class CreadorAND extends CreadorCompuerta {

    @Override
    public String getTipo() {
        return "AND";
    }

    @Override
    protected Compuerta crearCompuerta(String nombre) {
        return new CompuertaAND(nombre);
    }
}
