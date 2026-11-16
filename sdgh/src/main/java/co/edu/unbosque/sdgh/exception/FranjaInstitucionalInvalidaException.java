package co.edu.unbosque.sdgh.exception;

/**
 * Excepción personalizada que se genera cuando una franja horaria
 * no cumple con las condiciones o restricciones institucionales
 * establecidas para la programación.
 *
 * @author David Velasquez
 * @version 1.0
 * @since 1.0
 * @see RuntimeException
 */
public class FranjaInstitucionalInvalidaException extends RuntimeException {

    /**
     * Construye una excepción con un mensaje que describe
     * el incumplimiento de las restricciones institucionales.
     *
     * @param mensaje descripción de la franja institucional inválida.
     */
    public FranjaInstitucionalInvalidaException(String mensaje) {
        super(mensaje);
    }
}