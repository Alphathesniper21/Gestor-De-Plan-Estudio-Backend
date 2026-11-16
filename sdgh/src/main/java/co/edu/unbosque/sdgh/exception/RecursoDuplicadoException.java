package co.edu.unbosque.sdgh.exception;

/**
 * Excepción personalizada que se genera cuando se intenta registrar
 * un recurso que ya existe dentro del sistema.
 *
 * @author David Velasquez
 * @version 1.0
 * @since 1.0
 * @see RuntimeException
 */
public class RecursoDuplicadoException extends RuntimeException {

    /**
     * Construye una excepción con un mensaje que describe
     * el recurso que presenta duplicidad.
     *
     * @param mensaje descripción del recurso duplicado.
     */
    public RecursoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}