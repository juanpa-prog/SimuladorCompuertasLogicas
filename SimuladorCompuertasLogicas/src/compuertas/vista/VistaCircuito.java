package compuertas.vista;

/**
 * Lo que el controlador necesita de la vista principal. El controlador
 * depende de este contrato y no de JFrame: así la ventana puede
 * reemplazarse (por ejemplo, por una falsa en las pruebas).
 */
public interface VistaCircuito {

    void setEscuchador(EscuchadorVentana escuchador);

    PanelCircuito getPanelCircuito();

    PanelTablaVerdad getPanelTabla();

    void mostrarEstado(String mensaje);

    void mostrarError(String mensaje);

    boolean confirmar(String pregunta);

    /** Muestra el selector de archivos para guardar. @return la ruta elegida, o null si se canceló. */
    java.nio.file.Path elegirArchivoParaGuardar();

    /** Muestra el selector de archivos para abrir. @return la ruta elegida, o null si se canceló. */
    java.nio.file.Path elegirArchivoParaAbrir();
}
