package compuertas.modelo;

/** Compuerta XOR: verdadera cuando las entradas son diferentes. */
public class CompuertaXOR extends CompuertaBinaria {

    public CompuertaXOR(String nombre) {
        super(nombre);
    }

    public CompuertaXOR(String nombre, FuenteLogica a, FuenteLogica b) {
        super(nombre, a, b);
    }

    @Override
    public String getTipo() {
        return "XOR";
    }

    @Override
    public boolean evaluar() {
        // Se leen ambas entradas siempre, para detectar una entrada sin conectar.
        boolean a = valorA();
        boolean b = valorB();
        return a != b;
    }
}
