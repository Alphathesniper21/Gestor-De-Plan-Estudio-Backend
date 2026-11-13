package co.edu.unbosque.sdgh.repository;

import org.springframework.data.repository.CrudRepository;

import co.edu.unbosque.sdgh.entity.Parametro;

/**
 * Repositorio de los parametros del sistema (RF9). La clave es el
 * identificador.
 */
public interface ParametroRepository extends CrudRepository<Parametro, String> {

}
