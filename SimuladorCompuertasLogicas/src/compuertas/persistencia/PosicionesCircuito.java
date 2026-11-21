package compuertas.persistencia;

import java.util.HashMap;
import java.util.Map;

/*
 Posiciones (x, y) de los componentes en el lienzo.
 */
public final class PosicionesCircuito {

    public enum Tipo { ENTRADA, COMPUERTA, SALIDA }

    
    public static final class Posicion {
        private final int x;
        private final int y;

        public Posicion(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }

    private final Map<String, Posicion> posiciones = new HashMap<>();

    public void poner(Tipo tipo, String nombre, int x, int y) {
        posiciones.put(clave(tipo, nombre), new Posicion(x, y));
    }

    /** @return la posición guardada, o null si no hay ninguna para ese elemento. */
    public Posicion obtener(Tipo tipo, String nombre) {
        return posiciones.get(clave(tipo, nombre));
    }

    public int cantidad() {
        return posiciones.size();
    }

    private static String clave(Tipo tipo, String nombre) {
        return tipo.name() + ":" + nombre;
    }
}
