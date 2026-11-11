package compuertas.modelo;

/** Compuerta NAND: negación de AND. */
public class CompuertaNAND extends CompuertaBinaria {

    public CompuertaNAND(String nombre) {
        super(nombre);
    }

    public CompuertaNAND(String nombre, FuenteLogica a, FuenteLogica b) {
        super(nombre, a, b);
    }

    @Override
    public String getTipo() {
        return "NAND";
    }

    @Override
    public boolean evaluar() {
        // Se leen ambas entradas siempre, para detectar una entrada sin conectar.
        boolean a = valorA();
        boolean b = valorB();
        return !(a && b);
    }
}
