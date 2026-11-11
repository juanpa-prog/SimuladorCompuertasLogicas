package compuertas.vista;

/** Eventos de la paleta y de la barra de herramientas que atiende el controlador. */
public interface EscuchadorVentana {

    void alAgregarElemento(ElementoPaleta elemento);

    void alEvaluar();

    void alGenerarTablaVerdad();

    void alEliminarSeleccion();

    void alLimpiar();

    void alCargarEjemplo();
}
