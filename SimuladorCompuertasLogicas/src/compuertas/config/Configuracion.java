package compuertas.config;

/**
 * Singleton implementado como enumeración (UML: «enumeration» Configuracion
 * { INSTANCIA }). Java garantiza una única instancia, incluso con hilos o
 * serialización. El cliente accede con {@code Configuracion.INSTANCIA}.
 */
public enum Configuracion implements Preferencias {

    INSTANCIA;

    private boolean mostrarCuadricula = true;

    @Override
    public boolean isMostrarCuadricula() {
        return mostrarCuadricula;
    }

    @Override
    public void setMostrarCuadricula(boolean mostrar) {
        this.mostrarCuadricula = mostrar;
    }
}
