package co.edu.unbosque.sdgh.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.edu.unbosque.sdgh.dto.AulaDTO;
import co.edu.unbosque.sdgh.entity.Aula;
import co.edu.unbosque.sdgh.repository.AulaRepository;

/**
 * Servicio que gestiona la logica de negocio para las aulas fisicas
 * (CU04 - RF04, RF07, RF08).
 */
@Service
public class AulaService implements CRUDOperation<AulaDTO> {

	@Autowired
	private AulaRepository aulaRepo;

	public AulaService() {
	}

	/**
	 * Registra una nueva aula validando que su capacidad sea mayor a cero (CU04 / RF04).
	 *
	 * @param data DTO con los datos del aula a registrar.
	 * @return 0 si se registro correctamente;
	 *         2 si los datos son nulos o la capacidad es menor o igual a cero.
	 */
	@Override
	public int create(AulaDTO data) {
		if (!esAulaValida(data)) {
			return 2;
		}
		Aula entity = toEntity(data);
		aulaRepo.save(entity);
		return 0;
	}

	/**
	 * Obtiene todas las aulas registradas en el sistema.
	 *
	 * @return Lista de {@link AulaDTO}.
	 */
	@Override
	public List<AulaDTO> getAll() {
		List<AulaDTO> dtoList = new ArrayList<>();
		Iterable<Aula> entities = aulaRepo.findAll();
		for (Aula entity : entities) {
			dtoList.add(toDTO(entity));
		}
		return dtoList;
	}

	/**
	 * Elimina un aula por su ID.
	 *
	 * @param id Identificador del aula.
	 * @return 0 si se elimino correctamente; 1 si no existe o el id es nulo.
	 */
	@Override
	public int deleteById(Integer id) {
		if (id == null || !aulaRepo.existsById(id)) {
			return 1;
		}
		aulaRepo.deleteById(id);
		return 0;
	}

	/**
	 * Actualiza la capacidad y caracteristicas de un aula existente por su ID (CU04).
	 *
	 * @param id      Identificador del aula a actualizar.
	 * @param newData Nuevos datos del aula.
	 * @return 0 si se actualizo con exito;
	 *         1 si el aula no existe;
	 *         2 si la nueva capacidad es menor o igual a cero.
	 */
	@Override
	public int updateById(Integer id, AulaDTO newData) {
		if (id == null) {
			return 1;
		}
		Optional<Aula> foundOpt = aulaRepo.findById(id);
		if (foundOpt.isEmpty()) {
			return 1;
		}
		if (!esAulaValida(newData)) {
			return 2;
		}

		Aula entityToUpdate = foundOpt.get();
		entityToUpdate.setCapacidad(newData.getCapacidad());
		entityToUpdate.setTieneSillasMovibles(newData.isTieneSillasMovibles());
		entityToUpdate.setTieneComputadores(newData.isTieneComputadores());

		aulaRepo.save(entityToUpdate);
		return 0;
	}

	/**
	 * Cuenta el total de aulas registradas.
	 *
	 * @return Cantidad total de aulas.
	 */
	@Override
	public long count() {
		return aulaRepo.count();
	}

	/**
	 * Verifica si existe un aula con el ID indicado.
	 *
	 * @param id Identificador del aula.
	 * @return true si existe, false en caso contrario.
	 */
	@Override
	public boolean exist(Integer id) {
		return id != null && aulaRepo.existsById(id);
	}

	/**
	 * Busca un aula por su ID.
	 *
	 * @param id Identificador del aula.
	 * @return {@link AulaDTO} si existe, o null si no se encuentra.
	 */
	public AulaDTO getById(Integer id) {
		if (id == null) {
			return null;
		}
		return aulaRepo.findById(id).map(this::toDTO).orElse(null);
	}

	/**
	 * Busca las aulas con capacidad igual o superior a la requerida (RF07).
	 *
	 * @param capacidad Capacidad minima requerida.
	 * @return Lista de {@link AulaDTO} que cumplen la capacidad.
	 */
	public List<AulaDTO> getByCapacidadMinima(int capacidad) {
		List<AulaDTO> dtoList = new ArrayList<>();
		List<Aula> entities = aulaRepo.findByCapacidadGreaterThanEqual(capacidad);
		for (Aula entity : entities) {
			dtoList.add(toDTO(entity));
		}
		return dtoList;
	}

	/**
	 * Busca las aulas segun si disponen o no de computadores (RF08).
	 *
	 * @param tieneComputadores true para aulas con computadores, false para las demas.
	 * @return Lista de {@link AulaDTO} filtrada.
	 */
	public List<AulaDTO> getByTieneComputadores(boolean tieneComputadores) {
		List<AulaDTO> dtoList = new ArrayList<>();
		List<Aula> entities = aulaRepo.findByTieneComputadores(tieneComputadores);
		for (Aula entity : entities) {
			dtoList.add(toDTO(entity));
		}
		return dtoList;
	}

	/**
	 * Busca las aulas segun si disponen o no de sillas movibles (RF08).
	 *
	 * @param tieneSillasMovibles true para aulas con sillas movibles, false para las demas.
	 * @return Lista de {@link AulaDTO} filtrada.
	 */
	public List<AulaDTO> getByTieneSillasMovibles(boolean tieneSillasMovibles) {
		List<AulaDTO> dtoList = new ArrayList<>();
		List<Aula> entities = aulaRepo.findByTieneSillasMovibles(tieneSillasMovibles);
		for (Aula entity : entities) {
			dtoList.add(toDTO(entity));
		}
		return dtoList;
	}

	/**
	 * Busca las aulas con capacidad igual o superior a la requerida y que cumplan la condicion de computadores.
	 *
	 * @param capacidad         Capacidad minima requerida.
	 * @param tieneComputadores true si requiere computadores.
	 * @return Lista de {@link AulaDTO} que cumplen ambos criterios.
	 */
	public List<AulaDTO> getByCapacidadMinimaAndComputadores(int capacidad, boolean tieneComputadores) {
		List<AulaDTO> dtoList = new ArrayList<>();
		List<Aula> entities = aulaRepo.findByCapacidadGreaterThanEqualAndTieneComputadores(capacidad, tieneComputadores);
		for (Aula entity : entities) {
			dtoList.add(toDTO(entity));
		}
		return dtoList;
	}

	/**
	 * Valida que el aula tenga datos validos y capacidad positiva.
	 *
	 * @param data DTO a validar.
	 * @return true si es valido, false en caso contrario.
	 */
	private boolean esAulaValida(AulaDTO data) {
		return data != null && data.getCapacidad() > 0;
	}

	/**
	 * Convierte una entidad {@link Aula} a {@link AulaDTO}.
	 *
	 * @param entity Entidad Aula.
	 * @return Objeto AulaDTO.
	 */
	public AulaDTO toDTO(Aula entity) {
		if (entity == null) {
			return null;
		}
		return new AulaDTO(
				entity.getCapacidad(),
				entity.isTieneSillasMovibles(),
				entity.isTieneComputadores()
		);
	}

	/**
	 * Convierte un {@link AulaDTO} a entidad {@link Aula}.
	 *
	 * @param dto Objeto AulaDTO.
	 * @return Entidad Aula.
	 */
	public Aula toEntity(AulaDTO dto) {
		if (dto == null) {
			return null;
		}
		return new Aula(
				dto.getCapacidad(),
				dto.isTieneSillasMovibles(),
				dto.isTieneComputadores()
		);
	}

}
