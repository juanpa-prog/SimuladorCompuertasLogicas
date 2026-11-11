package compuertas.vista.componentes;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

/**
 * Geometría de las compuertas, a partir de los trazos del profesor
 * (GeneralPath con moveTo / lineTo / curveTo) normalizados al origen (0,0)
 * y a una altura de 80 px. Todas las figuras comparten estas medidas.
 */
final class Formas {

    /** Diámetro de la burbuja de negación (NOT, NAND, NOR). */
    static final double DIAMETRO_BURBUJA = 10;

    private Formas() {
    }

    /** Cuerpo de AND: lado recto y curva a la derecha. Ancho 79. */
    static Path2D cuerpoAND() {
        Path2D.Double p = new Path2D.Double();
        p.moveTo(0, 0);
        p.lineTo(40, 0);
        p.curveTo(92, 0, 92, 80, 40, 80);
        p.lineTo(0, 80);
        p.closePath();
        return p;
    }

    /** Cuerpo de OR: curva interior a la izquierda y punta a la derecha. Ancho 92. */
    static Path2D cuerpoOR() {
        Path2D.Double p = new Path2D.Double();
        p.moveTo(0, 0);
        p.curveTo(36, 0, 64, 16, 92, 40);
        p.curveTo(64, 64, 36, 80, 0, 80);
        p.curveTo(20, 56, 20, 24, 0, 0);
        p.closePath();
        return p;
    }

    /** Arco adicional de XOR, desplazado a la izquierda del cuerpo OR. */
    static Path2D arcoXOR() {
        Path2D.Double p = new Path2D.Double();
        p.moveTo(-10, 0);
        p.curveTo(10, 24, 10, 56, -10, 80);
        return p;
    }

    /** Triángulo de NOT. Ancho 70. */
    static Path2D triangulo() {
        Path2D.Double p = new Path2D.Double();
        p.moveTo(0, 0);
        p.lineTo(0, 80);
        p.lineTo(70, 40);
        p.closePath();
        return p;
    }

    /** Une un cuerpo con una burbuja de negación pegada a su derecha, centrada en (cx, 40). */
    static Shape conBurbuja(Shape cuerpo, double cx) {
        Area area = new Area(cuerpo);
        area.add(new Area(new Ellipse2D.Double(cx - DIAMETRO_BURBUJA / 2, 40 - DIAMETRO_BURBUJA / 2,
                DIAMETRO_BURBUJA, DIAMETRO_BURBUJA)));
        return area;
    }
}
