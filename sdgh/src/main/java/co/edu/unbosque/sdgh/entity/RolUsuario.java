package co.edu.unbosque.sdgh.entity;

/**
 * Enumeración que define los roles disponibles para los usuarios
 * dentro del sistema.
 *
 * Los roles permiten diferenciar los permisos y las operaciones
 * que puede realizar cada usuario.
 *
 * @author Laura Gomez
 * @version 1.0
 * @since 1.0
 * @see Usuario
 */
public enum RolUsuario {

    /**
     * Rol asignado al usuario encargado de gestionar las operaciones
     * administrativas del sistema, como la creación y modificación
     * de información relacionada con los horarios.
     */
    GESTOR,

    /**
     * Rol asignado al usuario que tiene permisos de consulta
     * de la información disponible en el sistema.
     */
    CONSULTANTE

}