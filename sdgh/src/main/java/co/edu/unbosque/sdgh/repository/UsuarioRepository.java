/**
 * Paquete que contiene los repositorios del aplicativo.
 */
package co.edu.unbosque.sdgh.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import co.edu.unbosque.sdgh.entity.Usuario;

/**
 * Repositorio encargado de gestionar la persistencia y consulta
 * de los usuarios registrados en el aplicativo.
 *
 * Permite realizar búsquedas por nombre de usuario y correo electrónico,
 * además de verificar la existencia de registros con dichos datos.
 *
 * @author Laura Gomez
 * @version 1.0
 * @since 1.0
 * @see Usuario
 * @see CrudRepository
 */
public interface UsuarioRepository extends CrudRepository<Usuario, Long> {

    /**
     * Busca un usuario mediante su nombre de usuario,
     * utilizado como credencial de acceso al sistema.
     *
     * @param username nombre de usuario que se desea consultar.
     * @return Optional que contiene el usuario si existe,
     *         o vacío si no se encuentra.
     */
    Optional<Usuario> findByUsername(String username);

    /**
     * Busca un usuario mediante su dirección de correo electrónico.
     *
     * @param correo dirección de correo electrónico que se desea consultar.
     * @return Optional que contiene el usuario si existe,
     *         o vacío si no se encuentra.
     */
    Optional<Usuario> findByCorreo(String correo);

    /**
     * Verifica si existe un usuario registrado con el correo indicado.
     *
     * @param correo dirección de correo electrónico que se desea verificar.
     * @return true si existe un usuario con ese correo,
     *         false en caso contrario.
     */
    boolean existsByCorreo(String correo);

    /**
     * Verifica si existe un usuario registrado con el nombre de usuario indicado.
     *
     * @param username nombre de usuario que se desea verificar.
     * @return true si existe un usuario con ese nombre de usuario,
     *         false en caso contrario.
     */
    boolean existsByUsername(String username);

}