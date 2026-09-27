package co.edu.unbosque.sdgh.service;

import java.util.List;

/**
 * Interfaz genérica que define las operaciones básicas de CRUD (Create, Read, Update, Delete)
 * para cualquier tipo de entidad o DTO en la capa de servicio del proyecto SDGH.
 *
 * @param <T> el tipo de dato que maneja el servicio (entidad JPA o DTO)
 * @param <ID> el tipo de dato del identificador único de la entidad
 */
public interface CRUDOperation<T, ID> {

    /**
     * Crea un nuevo registro.
     *
     * @param data el objeto a crear
     * @return número de filas afectadas (1 éxito, 0 fallo)
     */
    public int create(T data);

    /**
     * Obtiene todos los registros existentes.
     *
     * @return lista completa de los elementos
     */
    public List<T> getAll();

    /**
     * Elimina un registro por su identificador único.
     *
     * @param id el ID del registro a eliminar
     * @return número de filas afectadas (1 éxito, 0 fallo)
     */
    public int delateById(ID id);

    /**
     * Actualiza un registro existente.
     *
     * @param id   el ID del registro a actualizar
     * @param data el objeto con los datos actualizados
     * @return número de filas afectadas (1 éxito, 0 fallo)
     */
    public int updateById(ID id, T data);
}
