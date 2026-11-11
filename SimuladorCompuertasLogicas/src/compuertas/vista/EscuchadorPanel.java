package compuertas.vista;

import compuertas.vista.componentes.PuntoConexion;

/**
 * Eventos de alto nivel que el lienzo comunica al controlador. El lienzo
 * detecta el mouse; quién decide qué hacer con el modelo es el controlador.
 */
public interface EscuchadorPanel {

    /** El usuario soltó un cable entre dos puntos de conexión. */
    void alConectar(PuntoConexion pinA, PuntoConexion pinB);

    /** El usuario hizo clic simple (sin arrastrar) sobre un punto de conexión. */
    void alHacerClicEnPin(PuntoConexion pin);

    /** El usuario pidió eliminar lo seleccionado (tecla Supr). */
    void alEliminarSeleccion();
}
