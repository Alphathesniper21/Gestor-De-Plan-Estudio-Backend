package co.edu.unbosque.sdgh.exception;

/**
 * Excepción personalizada que se utiliza cuando se intenta
 * consultar o acceder a un recurso que no existe en el sistema.
 *
 * @author David Velasquez
 * @version 1.0
 * @since 1.0
 * @see RuntimeException
 */
public class RecursoNoEncontradoException extends RuntimeException {

    /**
     * Construye una excepción con un mensaje que describe
     * el recurso que no pudo encontrarse.
     *
     * @param mensaje descripción del recurso no encontrado.
     */
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}