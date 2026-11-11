package compuertas.config;

/**
 * Contrato de las preferencias de la aplicación. Los componentes de la
 * vista dependen de esta interfaz (no del Singleton), de modo que pueden
 * probarse con otra implementación: es la "alternativa testeable al
 * Singleton" vista en clase (inyección por constructor).
 */
public interface Preferencias {

    boolean isMostrarCuadricula();

    void setMostrarCuadricula(boolean mostrar);
}
