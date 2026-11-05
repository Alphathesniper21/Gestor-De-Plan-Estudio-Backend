/**
 * Paquete que contiene los repositorios del aplicativo.
 */
package co.edu.unbosque.sdgh.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import co.edu.unbosque.sdgh.entity.Grupo;

/**
 * Repositorio de la entidad Grupo.
 * Gestiona la persistencia y consulta de los grupos (secciones) de cada materia.
 */
public interface GrupoRepository extends CrudRepository<Grupo, Long> {

	/**
	 * Busca todos los grupos de una materia.
	 *
	 * @param materiaId Codigo de la materia.
	 * @return Lista de grupos de la materia.
	 */
	List<Grupo> findByMateriaId(long materiaId);

	List<Grupo> findByNumeroGrupo(int numeroGrupo);

	Optional<Grupo> findByMateriaIdAndNumeroGrupo(long materiaId, int numeroGrupo);

	boolean existsByMateriaIdAndNumeroGrupo(long materiaId, int numeroGrupo);
	
	Optional<Grupo> findById(Long id);
}