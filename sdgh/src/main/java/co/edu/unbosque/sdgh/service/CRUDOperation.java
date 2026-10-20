/**
 * Paquete que contiene los servicios del aplicativo.
 */
package co.edu.unbosque.sdgh.service;

import java.util.List;

/**
 * Interfaz que permite definir las operaciones CRUD.
 *
 * @param <T> Tipo de entidad (o DTO) sobre el que se realizaran las operaciones.
 */
public interface CRUDOperation<T> {

	/**
	 * Metodo que permite crear una nueva entidad.
	 *
	 * @param data La entidad a crear.
	 * @return Codigo de resultado de la operacion.
	 */
	public int create(T data);

	/**
	 * Metodo que permite obtener todas las entidades.
	 *
	 * @return Lista con todas las entidades.
	 */
	public List<T> getAll();

	/**
	 * Metodo que permite eliminar una entidad por su ID.
	 *
	 * @param id ID de la entidad a eliminar.
	 * @return Codigo de resultado de la operacion.
	 */
	public int deleteById(Integer id);

	/**
	 * Metodo que permite actualizar entidades por su ID.
	 *
	 * @param id      ID de la entidad a actualizar.
	 * @param newData Nuevos datos para la entidad.
	 * @return Codigo de resultado de la operacion.
	 */
	public int updateById(Integer id, T newData);

	/**
	 * Metodo que permite contar la cantidad total de entidades.
	 *
	 * @return Numero total de entidades.
	 */
	public long count();

	/**
	 * Metodo que permite verificar la existencia de una entidad a partir de su ID.
	 *
	 * @param id ID de la entidad a verificar.
	 * @return true si existe, false en caso contrario.
	 */
	public boolean exist(Integer id);

}