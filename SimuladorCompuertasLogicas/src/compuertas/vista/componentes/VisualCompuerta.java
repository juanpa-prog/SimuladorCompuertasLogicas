package compuertas.vista.componentes;

import compuertas.modelo.Circuito;
import compuertas.modelo.Compuerta;
import compuertas.modelo.FuenteLogica;
import java.awt.Font;
import java.awt.Graphics2D;

/**
 * Representación visual de una {@link Compuerta} del modelo (asociación
 * ComponenteVisual -> Compuerta). Construye sus pines a partir de
 * cuántas entradas tiene la compuerta, así AND, OR, XOR, NAND, NOR y NOT
 * comparten todo salvo la forma.
 */
public abstract class VisualCompuerta extends ComponenteVisual {

    private static final int LARGO_PIN = 32;
    private static final int[] ALTURAS_DOS_ENTRADAS = {24, 56};
    private static final int ALTURA_UNA_ENTRADA = 40;
    private static final Font FUENTE_NOMBRE = new Font(Font.SANS_SERIF, Font.BOLD, 12);

    private final Compuerta compuerta;

    /**
     * @param baseEntradaX x donde las entradas tocan el cuerpo
     * @param baseSalidaX  x donde la salida toca el cuerpo
     */
    protected VisualCompuerta(Compuerta compuerta, int x, int y, int baseEntradaX, int baseSalidaX) {
        super(x, y);
        if (compuerta == null) {
            throw new IllegalArgumentException("La compuerta asociada es obligatoria");
        }
        this.compuerta = compuerta;

        int n = compuerta.getNumeroEntradas();
        for (int i = 0; i < n; i++) {
            int altura = (n == 1) ? ALTURA_UNA_ENTRADA : ALTURAS_DOS_ENTRADAS[i];
            final int indice = i;
            agregarPin(PuntoConexion.Sentido.ENTRADA, indice, -LARGO_PIN, altura, baseEntradaX, altura,
                    () -> ValorSeguro.de(compuerta.getEntrada(indice)));
        }
        agregarPin(PuntoConexion.Sentido.SALIDA, 0, baseSalidaX + LARGO_PIN, 40, baseSalidaX, 40,
                () -> ValorSeguro.de(compuerta));
    }

    public Compuerta getCompuerta() {
        return compuerta;
    }

    @Override
    public FuenteLogica getFuente() {
        return compuerta;
    }

    @Override
    public void conectarEntrada(Circuito circuito, int indice, FuenteLogica fuente) {
        circuito.conectar(compuerta, indice, fuente);
    }

    @Override
    public void desconectarEntrada(Circuito circuito, int indice) {
        compuerta.desconectar(indice);
    }

    @Override
    public void eliminarDelModelo(Circuito circuito) {
        circuito.eliminarCompuerta(compuerta);
    }

    /** Dibuja el nombre de la compuerta (G1, G2...) sobre el cuerpo. */
    @Override
    protected void dibujarDetalles(Graphics2D g) {
        if (isMiniatura()) {
            return;
        }
        g.setFont(FUENTE_NOMBRE);
        g.drawString(compuerta.getNombre(), 0, -6);
    }
}
