package co.edu.unbosque.sdgh.exception;

/**
 * Excepción personalizada que se genera cuando se supera el cupo
 * permitido para un grupo o recurso.
 *
 * @author David Velasquez
 * @version 1.0
 * @since 1.0
 * @see RuntimeException
 */
public class CupoExcedidoException extends RuntimeException {

    /**
     * Construye una excepción con un mensaje que describe
     * el exceso de cupo.
     *
     * @param mensaje descripción del límite de cupo excedido.
     */
    public CupoExcedidoException(String mensaje) {
        super(mensaje);
    }
}