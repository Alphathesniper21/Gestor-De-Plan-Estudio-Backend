/**
 * Paquete que contiene los repositorios del aplicativo.
 */
package co.edu.unbosque.sdgh.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

import co.edu.unbosque.sdgh.entity.Usuario;

/**
 * Repositorio de la entidad Usuario.
 * Gestiona la persistencia y consulta de los usuarios registrados en el aplicativo.
 */
public interface UsuarioRepository extends CrudRepository<Usuario, Integer> {

	/**
	 * Busca un usuario por su nombre de usuario (credencial de login).
	 *
	 * @param username El nombre de usuario a buscar.
	 * @return Un Optional con el usuario si existe.
	 */
	Optional<Usuario> findByUsername(String username);
	
	/**
	 * Busca un usuario por su correo electronico.
	 *
	 * @param correo El correo a buscar.
	 * @return Un Optional con el usuario si existe.
	 */
	Optional<Usuario> findByCorreo(String correo);

	/**
	 * Verifica si ya existe un usuario con el correo dado.
	 *
	 * @param correo El correo a verificar.
	 * @return true si ya existe, false en caso contrario.
	 */
	boolean existsByCorreo(String correo);

	/**
	 * Busca los usuarios de una facultad.
	 *
	 * @param facultad La facultad a filtrar.
	 * @return Lista de usuarios de la facultad.
	 */
	List<Usuario> findByFacultad(String facultad);

}