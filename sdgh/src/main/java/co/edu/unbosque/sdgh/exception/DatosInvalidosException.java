package co.edu.unbosque.sdgh.exception;

/**
 * Excepción personalizada que se utiliza cuando los datos
 * proporcionados no cumplen con las condiciones de validación
 * establecidas por el sistema.
 *
 * @author David Velasquez
 * @version 1.0
 * @since 1.0
 * @see RuntimeException
 */
public class DatosInvalidosException extends RuntimeException {

    /**
     * Construye una excepción con un mensaje que describe
     * los datos que presentan inconsistencias.
     *
     * @param mensaje descripción de los datos inválidos.
     */
    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }
}