package compuertas.vista.contratos;

import java.awt.Graphics2D;

/** Contrato: todo lo que sabe dibujarse a sí mismo sobre el lienzo. */
public interface Dibujable {

    void dibujar(Graphics2D g);
}
