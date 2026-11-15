/**
 * Paquete que contiene los repositorios del aplicativo.
 */
package co.edu.unbosque.sdgh.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import co.edu.unbosque.sdgh.entity.Grupo;

/**
 * Repositorio encargado de gestionar la persistencia y consulta
 * de los grupos o secciones asociados a las materias.
 *
 * Extiende CrudRepository para disponer de las operaciones básicas
 * de creación, consulta, actualización y eliminación de registros.
 *
 * @author Laura Gomez
 * @version 1.0
 * @since 1.0
 * @see Grupo
 * @see CrudRepository
 */
public interface GrupoRepository extends CrudRepository<Grupo, Long> {

    /**
     * Busca todos los grupos asociados a una materia específica.
     *
     * @param materiaId identificador de la materia que se desea consultar.
     * @return lista de grupos pertenecientes a la materia indicada.
     */
    List<Grupo> findByMateriaId(long materiaId);

    /**
     * Busca los grupos que tienen asignado un número de grupo específico.
     *
     * @param numeroGrupo número del grupo que se desea consultar.
     * @return lista de grupos que coinciden con el número indicado.
     */
    List<Grupo> findByNumeroGrupo(int numeroGrupo);

    /**
     * Busca un grupo mediante la combinación del identificador
     * de su materia y el número de grupo.
     *
     * @param materiaId identificador de la materia asociada.
     * @param numeroGrupo número del grupo que se desea consultar.
     * @return Optional que contiene el grupo si existe,
     *         o vacío si no se encuentra.
     */
    Optional<Grupo> findByMateriaIdAndNumeroGrupo(long materiaId, int numeroGrupo);

    /**
     * Verifica si existe un grupo asociado a una materia
     * con el número de grupo indicado.
     *
     * @param materiaId identificador de la materia asociada.
     * @param numeroGrupo número del grupo que se desea verificar.
     * @return true si existe un grupo con esos datos,
     *         false en caso contrario.
     */
    boolean existsByMateriaIdAndNumeroGrupo(long materiaId, int numeroGrupo);

    /**
     * Busca un grupo mediante su identificador.
     *
     * @param id identificador único del grupo.
     * @return Optional que contiene el grupo si existe,
     *         o vacío si no se encuentra.
     */
    Optional<Grupo> findById(Long id);

}