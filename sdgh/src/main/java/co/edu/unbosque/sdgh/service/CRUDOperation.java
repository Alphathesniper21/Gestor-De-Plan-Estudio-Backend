/**
 * Paquete que contiene los servicios del aplicativo.
 */
package co.edu.unbosque.sdgh.service;

import java.util.List;

/**
 * Interfaz que define las operaciones CRUD generales para la gestion de
 * entidades o DTO dentro del aplicativo.
 *
 * @param <T> Tipo de entidad o DTO sobre el que se realizaran las operaciones.
 * @author Sebastian Toyo
 */
public interface CRUDOperation<T> {

	/**
	 * Metodo que permite crear una nueva entidad.
	 *
	 * @param data Entidad que se desea crear.
	 * @return Codigo de resultado de la operacion.
	 */
	public int create(T data);

	/**
	 * Metodo que permite obtener todas las entidades registradas.
	 *
	 * @return Lista con todas las entidades.
	 */
	public List<T> getAll();

	/**
	 * Metodo que permite eliminar una entidad mediante su identificador.
	 *
	 * @param id Identificador de la entidad que se desea eliminar.
	 * @return Codigo de resultado de la operacion.
	 */
	public int deleteById(Long id);

	/**
	 * Metodo que permite actualizar una entidad mediante su identificador.
	 *
	 * @param id Identificador de la entidad que se desea actualizar.
	 * @param newData Nuevos datos que se asignaran a la entidad.
	 * @return Codigo de resultado de la operacion.
	 */
	public int updateById(Long id, T newData);

	/**
	 * Metodo que permite contar la cantidad total de entidades registradas.
	 *
	 * @return Numero total de entidades.
	 */
	public long count();

	/**
	 * Metodo que permite verificar si existe una entidad mediante su
	 * identificador.
	 *
	 * @param id Identificador de la entidad que se desea verificar.
	 * @return true si la entidad existe o false en caso contrario.
	 */
	public boolean existById(Long id);

}