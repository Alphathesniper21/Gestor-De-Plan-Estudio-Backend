/**
 * Paquete que contiene los repositorios del aplicativo.
 */
package co.edu.unbosque.sdgh.repository;

import java.util.List;
import org.springframework.data.repository.CrudRepository;

import co.edu.unbosque.sdgh.entity.Grupo;

/**
 * Repositorio de la entidad Grupo.
 * Gestiona la persistencia y consulta de los grupos (secciones) de cada materia.
 */
public interface GrupoRepository extends CrudRepository<Grupo, Integer> {

	/**
	 * Busca todos los grupos de una materia.
	 *
	 * @param materiaId Codigo de la materia.
	 * @return Lista de grupos de la materia.
	 */
	List<Grupo> findByMateriaId(long materiaId);

	/**
	 * Busca todos los grupos asignados a un docente.
	 *
	 * @param docenteId ID del docente.
	 * @return Lista de grupos del docente.
	 */
	List<Grupo> findByDocenteId(long docenteId);

	/**
	 * Busca todos los grupos asignados a un aula.
	 *
	 * @param aulaId ID del aula.
	 * @return Lista de grupos del aula.
	 */
	List<Grupo> findByAulaId(long aulaId);

}