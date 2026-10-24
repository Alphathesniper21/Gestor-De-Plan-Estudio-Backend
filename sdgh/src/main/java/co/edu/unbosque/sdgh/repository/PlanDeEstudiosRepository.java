/**
 * Paquete que contiene los repositorios del aplicativo.
 */
package co.edu.unbosque.sdgh.repository;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

import co.edu.unbosque.sdgh.entity.PlanDeEstudios;

/**
 * Repositorio de la entidad PlanDeEstudios.
 * Gestiona la persistencia y consulta de los planes de estudio del programa.
 */
public interface PlanDeEstudiosRepository extends CrudRepository<PlanDeEstudios, String> {

	/**
	 * Busca un plan de estudios por el nombre de su programa.
	 *
	 * @param nombrePrograma El nombre del programa a buscar.
	 * @return Un Optional con el plan de estudios si existe.
	 */
	Optional<PlanDeEstudios> findByNombrePrograma(String nombrePrograma);

	/**
	 * Verifica si ya existe un plan de estudios con el nombre de programa dado.
	 *
	 * @param nombrePrograma El nombre del programa a verificar.
	 * @return true si ya existe, false en caso contrario.
	 */
	boolean existsByNombrePrograma(String nombrePrograma);

	/**
	 * Busca un plan de estudios por el nombre de su programa y su version.
	 *
	 * @param nombrePrograma El nombre del programa a buscar.
	 * @param version La version del plan de estudios.
	 * @return Un Optional con el plan de estudios si existe.
	 */
	Optional<PlanDeEstudios> findByNombreProgramaAndVersion(String nombrePrograma, String version);

}