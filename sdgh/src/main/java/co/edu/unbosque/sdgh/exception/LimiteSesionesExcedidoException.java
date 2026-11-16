package co.edu.unbosque.sdgh.exception;

/**
 * Excepción personalizada que se produce cuando se supera
 * el número máximo de sesiones permitido por el sistema.
 *
 * @author David Velasquez
 * @version 1.0
 * @since 1.0
 * @see RuntimeException
 */
public class LimiteSesionesExcedidoException extends RuntimeException {

    /**
     * Construye una excepción con un mensaje que describe
     * el límite de sesiones que ha sido excedido.
     *
     * @param mensaje descripción del límite de sesiones excedido.
     */
    public LimiteSesionesExcedidoException(String mensaje) {
        super(mensaje);
    }
}