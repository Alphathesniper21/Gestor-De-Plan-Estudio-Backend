/**
 * Paquete que contiene los repositorios del aplicativo.
 */
package co.edu.unbosque.sdgh.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

import co.edu.unbosque.sdgh.entity.Profesor;

/**
 * Repositorio de la entidad Profesor.
 * Gestiona la persistencia y consulta de los profesores registrados en el aplicativo.
 */
public interface ProfesorRepository extends CrudRepository<Profesor, Integer> {

	/**
	 * Busca un profesor por su correo electronico.
	 *
	 * @param correo El correo a buscar.
	 * @return Un Optional con el profesor si existe.
	 */
	Optional<Profesor> findByCorreo(String correo);

	/**
	 * Verifica si ya existe un profesor con el correo dado.
	 *
	 * @param correo El correo a verificar.
	 * @return true si ya existe, false en caso contrario.
	 */
	boolean existsByCorreo(String correo);

	/**
	 * Busca los profesores de una facultad.
	 *
	 * @param facultad La facultad a filtrar.
	 * @return Lista de profesores de la facultad.
	 */
	List<Profesor> findByFacultad(String facultad);

	/**
	 * Busca los profesores con anios de experiencia iguales o superiores a los dados.
	 *
	 * @param anios Anios minimos de experiencia.
	 * @return Lista de profesores que cumplen la experiencia.
	 */
	List<Profesor> findByAniosExperienciaGreaterThanEqual(int anios);

	/**
	 * Busca los profesores por su nivel de ingles.
	 *
	 * @param nivelIngles El nivel de ingles a filtrar.
	 * @return Lista de profesores con ese nivel de ingles.
	 */
	List<Profesor> findByNivelIngles(String nivelIngles);

	/**
	 * Busca los profesores segun si su hoja de vida esta actualizada.
	 *
	 * @param hojaVidaActualizada true para hojas de vida actualizadas, false para las demas.
	 * @return Lista de profesores que coinciden con el filtro.
	 */
	List<Profesor> findByHojaVidaActualizada(boolean hojaVidaActualizada);

}