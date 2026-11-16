package co.edu.unbosque.sdgh.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import co.edu.unbosque.sdgh.entity.Sesion;

/**
 * Repositorio encargado de gestionar la persistencia y consulta
 * de las sesiones programadas en el sistema.
 *
 * Extiende CrudRepository para disponer de las operaciones
 * básicas de acceso a los registros de sesiones.
 *
 * @author Laura Gomez
 * @version 1.0
 * @since 1.0
 * @see Sesion
 * @see CrudRepository
 */
public interface SesionRepository extends CrudRepository<Sesion, Long> {

    /**
     * Busca las sesiones asociadas al identificador indicado.
     *
     * @param id identificador utilizado para realizar la búsqueda.
     * @return lista de sesiones que coinciden con el identificador.
     */
    List<Sesion> findById(long id);

    /**
     * Busca todas las sesiones pertenecientes a un grupo específico.
     *
     * @param grupoId identificador del grupo cuyas sesiones se desean consultar.
     * @return lista de sesiones asociadas al grupo indicado.
     */
    List<Sesion> findByGrupoId(Long grupoId);

}