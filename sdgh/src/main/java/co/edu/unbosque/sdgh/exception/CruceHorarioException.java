package co.edu.unbosque.sdgh.exception;

/**
 * Excepción personalizada que se produce cuando se detecta un cruce
 * entre los horarios de dos o más sesiones.
 *
 * Permite identificar situaciones en las que una programación
 * presenta conflictos de horario.
 *
 * @author David Velasquez
 * @version 1.0
 * @since 1.0
 * @see RuntimeException
 */
public class CruceHorarioException extends RuntimeException {

    /**
     * Construye una excepción con un mensaje descriptivo del cruce
     * de horario detectado.
     *
     * @param mensaje descripción del conflicto de horario.
     */
    public CruceHorarioException(String mensaje) {
        super(mensaje);
    }
}