package compuertas.config;

/**
 * Singleton implementado como enumeración 
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
