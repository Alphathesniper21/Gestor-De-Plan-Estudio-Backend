/**
 * Paquete que contiene los repositorios del aplicativo.
 */
package co.edu.unbosque.sdgh.repository;

import java.util.List;
import org.springframework.data.repository.CrudRepository;

import co.edu.unbosque.sdgh.entity.Aula;

/**
 * Repositorio de la entidad Aula.
 * Gestiona la persistencia y consulta de las aulas disponibles para programar los grupos.
 */
public interface AulaRepository extends CrudRepository<Aula, Integer> {

	/**
	 * Busca las aulas con capacidad igual o superior a la dada.
	 *
	 * @param capacidad Capacidad minima requerida.
	 * @return Lista de aulas que cumplen la capacidad.
	 */
	List<Aula> findByCapacidadGreaterThanEqual(int capacidad);

	/**
	 * Busca las aulas segun si cuentan o no con computadores.
	 *
	 * @param tieneComputadores true para aulas con computadores, false para las demas.
	 * @return Lista de aulas que coinciden con el filtro.
	 */
	List<Aula> findByTieneComputadores(boolean tieneComputadores);

	/**
	 * Busca las aulas segun si cuentan o no con sillas movibles.
	 *
	 * @param tieneSillasMovibles true para aulas con sillas movibles, false para las demas.
	 * @return Lista de aulas que coinciden con el filtro.
	 */
	List<Aula> findByTieneSillasMovibles(boolean tieneSillasMovibles);

	/**
	 * Busca las aulas con capacidad igual o superior a la dada que ademas cumplan con la condicion de computadores.
	 *
	 * @param capacidad Capacidad minima requerida.
	 * @param tieneComputadores true para aulas con computadores, false para las demas.
	 * @return Lista de aulas que cumplen ambos criterios.
	 */
	List<Aula> findByCapacidadGreaterThanEqualAndTieneComputadores(int capacidad, boolean tieneComputadores);

}