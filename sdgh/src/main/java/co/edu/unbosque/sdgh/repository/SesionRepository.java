package co.edu.unbosque.sdgh.repository;

import java.util.List;
import org.springframework.data.repository.CrudRepository;

import co.edu.unbosque.sdgh.entity.Sesion;

/**
 * Repositorio de sesiones de clase.
 */
public interface SesionRepository extends CrudRepository<Sesion, Long> {
	
	List<Sesion> findById(long id);
	
	/**
	 * Busca las sesiones de un grupo.
	 * @param grupoId Identificador del grupo.
	 * @return Lista de sesiones del grupo.
	 */
	List<Sesion> findByGrupoId(Long grupoId);
	
	
}