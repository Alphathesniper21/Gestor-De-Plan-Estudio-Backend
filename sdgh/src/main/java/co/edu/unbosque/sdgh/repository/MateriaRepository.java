/**
 * Paquete que contiene los repositorios del aplicativo.
 */
package co.edu.unbosque.sdgh.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import co.edu.unbosque.sdgh.entity.Materia;

/**
 * Repositorio encargado de gestionar la persistencia y consulta
 * de las materias registradas en el plan de estudios.
 *
 * Extiende CrudRepository para disponer de las operaciones
 * básicas de creación, consulta, actualización y eliminación.
 *
 * @author Laura Gomez
 * @version 1.0
 * @since 1.0
 * @see Materia
 * @see CrudRepository
 */
public interface MateriaRepository extends CrudRepository<Materia, Long> {

    /**
     * Busca una materia utilizando su nombre.
     *
     * @param nombre nombre de la materia que se desea consultar.
     * @return Optional que contiene la materia si se encuentra,
     *         o vacío si no existe.
     */
    Optional<Materia> findByNombre(String nombre);

    /**
     * Busca todas las materias pertenecientes a un semestre específico.
     *
     * @param semestre número del semestre que se desea consultar.
     * @return lista de materias que pertenecen al semestre indicado.
     */
    List<Materia> findBySemestre(int semestre);

    /**
     * Busca las materias según su condición de correlatividad.
     *
     * @param correlativa true para consultar materias correlativas,
     *                    false para consultar las que no lo son.
     * @return lista de materias que coinciden con la condición indicada.
     */
    List<Materia> findByCorrelativa(boolean correlativa);

    /**
     * Busca una materia utilizando su código.
     *
     * @param codigo código de la materia que se desea consultar.
     * @return Optional que contiene la materia si existe,
     *         o vacío si no se encuentra.
     */
    Optional<Materia> findByCodigo(long codigo);

    /**
     * Verifica si existe una materia registrada con el código indicado.
     *
     * @param codigo código de la materia que se desea verificar.
     * @return true si existe una materia con ese código,
     *         false en caso contrario.
     */
    boolean existsByCodigo(long codigo);
}