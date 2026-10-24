/**
 * Paquete que contiene los repositorios del aplicativo.
 */
package co.edu.unbosque.sdgh.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

import co.edu.unbosque.sdgh.entity.Estudiante;

/**
 * Repositorio de la entidad Estudiante.
 * Gestiona la persistencia y consulta de los estudiantes registrados en el aplicativo.
 */
public interface EstudianteRepository extends CrudRepository<Estudiante, Integer> {

	/**
	 * Busca un estudiante por su correo electronico.
	 *
	 * @param correo El correo a buscar.
	 * @return Un Optional con el estudiante si existe.
	 */
	Optional<Estudiante> findByCorreo(String correo);

	/**
	 * Verifica si ya existe un estudiante con el correo dado.
	 *
	 * @param correo El correo a verificar.
	 * @return true si ya existe, false en caso contrario.
	 */
	boolean existsByCorreo(String correo);

	/**
	 * Busca los estudiantes de una facultad.
	 *
	 * @param facultad La facultad a filtrar.
	 * @return Lista de estudiantes de la facultad.
	 */
	List<Estudiante> findByFacultad(String facultad);

	/**
	 * Busca los estudiantes de un programa academico.
	 *
	 * @param programaAcademico El programa academico a filtrar.
	 * @return Lista de estudiantes del programa.
	 */
	List<Estudiante> findByProgramaAcademico(String programaAcademico);

	/**
	 * Busca los estudiantes que cursan un semestre.
	 *
	 * @param semestreActual El semestre a filtrar.
	 * @return Lista de estudiantes del semestre.
	 */
	List<Estudiante> findBySemestreActual(int semestreActual);

	/**
	 * Busca los estudiantes segun su estado (activo o inactivo).
	 *
	 * @param estado true para estudiantes activos, false para inactivos.
	 * @return Lista de estudiantes que coinciden con el estado.
	 */
	List<Estudiante> findByEstado(boolean estado);

	/**
	 * Verifica si ya existe un estudiante con el numero de documento dado.
	 *
	 * @param numeroDocumento El numero de documento a verificar.
	 * @return true si ya existe, false en caso contrario.
	 */
	boolean existsByNumeroDocumento(String numeroDocumento);

}