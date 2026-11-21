package compuertas.modelo;

import compuertas.modelo.excepciones.CircuitoException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Tabla de verdad de un circuito: una fila por cada combinación de sus
 * entradas y una columna por cada entrada y cada salida. 
 */
public class TablaVerdad {

    public static final int MAX_ENTRADAS = 10;

    private final List<String> encabezados = new ArrayList<>();
    private final int numeroEntradas;
    private final boolean[][] filas;

    public TablaVerdad(List<EntradaLogica> entradas, List<SalidaLogica> salidas) {
        if (entradas.isEmpty()) {
            throw new CircuitoException("Agrega al menos una entrada para generar la tabla de verdad.");
        }
        if (salidas.isEmpty()) {
            throw new CircuitoException("Agrega al menos una salida para generar la tabla de verdad.");
        }
        if (entradas.size() > MAX_ENTRADAS) {
            throw new CircuitoException("La tabla admite como máximo " + MAX_ENTRADAS + " entradas.");
        }

        numeroEntradas = entradas.size();
        for (EntradaLogica entrada : entradas) {
            encabezados.add(entrada.getNombre());
        }
        for (SalidaLogica salida : salidas) {
            encabezados.add(salida.getNombre());
        }

        int n = entradas.size();
        filas = new boolean[1 << n][n + salidas.size()];

        boolean[] originales = new boolean[n];
        for (int i = 0; i < n; i++) {
            originales[i] = entradas.get(i).getValor();
        }
        try {
            for (int fila = 0; fila < filas.length; fila++) {
                for (int i = 0; i < n; i++) {
                    boolean valor = ((fila >> (n - 1 - i)) & 1) == 1;
                    entradas.get(i).setValor(valor);
                    filas[fila][i] = valor;
                }
                for (int j = 0; j < salidas.size(); j++) {
                    filas[fila][n + j] = salidas.get(j).getValor();
                }
            }
        } finally {
            for (int i = 0; i < n; i++) {
                entradas.get(i).setValor(originales[i]);
            }
        }
    }

    public List<String> getEncabezados() {
        return Collections.unmodifiableList(encabezados);
    }

    public int getNumeroEntradas() {
        return numeroEntradas;
    }

    public int getNumeroFilas() {
        return filas.length;
    }

    public int getNumeroColumnas() {
        return encabezados.size();
    }

    public boolean getValor(int fila, int columna) {
        return filas[fila][columna];
    }

    /** Representación de texto para la consola. */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int c = 0; c < encabezados.size(); c++) {
            if (c == numeroEntradas) {
                sb.append("| ");
            }
            sb.append(String.format("%-4s", encabezados.get(c)));
        }
        sb.append('\n');
        for (boolean[] fila : filas) {
            for (int c = 0; c < fila.length; c++) {
                if (c == numeroEntradas) {
                    sb.append("| ");
                }
                sb.append(String.format("%-4s", fila[c] ? "1" : "0"));
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
