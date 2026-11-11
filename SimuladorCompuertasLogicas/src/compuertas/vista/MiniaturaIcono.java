package compuertas.vista;

import compuertas.vista.componentes.ComponenteVisual;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import javax.swing.Icon;

/** Icono que dibuja un componente visual reducido, para los botones de la paleta. */
public class MiniaturaIcono implements Icon {

    private final ComponenteVisual componente;
    private final int ancho;
    private final int alto;

    public MiniaturaIcono(ComponenteVisual componente, int ancho, int alto) {
        this.componente = componente;
        componente.setMiniatura(true);
        this.ancho = ancho;
        this.alto = alto;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Rectangle lim = componente.getLimites();
            double escala = Math.min(1.0, Math.min((ancho - 4.0) / lim.width, (alto - 4.0) / lim.height));
            double dx = x + (ancho - lim.width * escala) / 2.0 - lim.x * escala;
            double dy = y + (alto - lim.height * escala) / 2.0 - lim.y * escala;
            g2.translate(dx, dy);
            g2.scale(escala, escala);
            componente.dibujar(g2);
        } finally {
            g2.dispose();
        }
    }

    @Override
    public int getIconWidth() {
        return ancho;
    }

    @Override
    public int getIconHeight() {
        return alto;
    }
}
