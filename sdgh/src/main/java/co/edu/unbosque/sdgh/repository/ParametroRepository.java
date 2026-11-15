package co.edu.unbosque.sdgh.repository;

import org.springframework.data.repository.CrudRepository;

import co.edu.unbosque.sdgh.entity.Parametro;

/**
 * Repositorio encargado de gestionar la persistencia de los parámetros
 * de configuración del sistema.
 *
 * Utiliza la clave del parámetro como identificador principal
 * para realizar las operaciones de consulta y administración.
 *
 * @author Laura Gomez
 * @version 1.0
 * @since 1.0
 * @see Parametro
 * @see CrudRepository
 */
public interface ParametroRepository extends CrudRepository<Parametro, String> {

}