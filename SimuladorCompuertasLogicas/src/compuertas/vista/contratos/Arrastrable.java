package compuertas.vista.contratos;

/**
 * Contrato: todo lo que el usuario puede tomar con el mouse y mover.
 * Las coordenadas son las del lienzo (esquina del componente).
 */
public interface Arrastrable {

    /** @return true si el punto (px, py) del lienzo cae dentro del cuerpo del componente. */
    boolean contiene(int px, int py);

    void moverA(int x, int y);

    int getX();

    int getY();
}
