/**
 * Paquete que contiene los repositorios del aplicativo.
 */
package co.edu.unbosque.sdgh.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

import co.edu.unbosque.sdgh.entity.Materia;

/**
 * Repositorio de la entidad Materia.
 * Gestiona la persistencia y consulta de las materias del plan de estudios.
 */
public interface MateriaRepository extends CrudRepository<Materia, Long> {

	/**
	 * Busca una materia por su nombre.
	 *
	 * @param nombre El nombre a buscar.
	 * @return Un Optional con la materia si existe.
	 */
	Optional<Materia> findByNombre(String nombre);

	/**
	 * Busca las materias de un semestre.
	 *
	 * @param semestre El semestre a filtrar.
	 * @return Lista de materias del semestre.
	 */
	List<Materia> findBySemestre(int semestre);

	/**
	 * Busca las materias segun si son correlativas o no.
	 *
	 * @param correlativa true para materias correlativas, false para las demas.
	 * @return Lista de materias que coinciden con el filtro.
	 */
	List<Materia> findByCorrelativa(boolean correlativa);

}