/**
 * Paquete de servicios del aplicativo.
 */
package co.edu.unbosque.sdgh.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.edu.unbosque.sdgh.dto.AulaDTO;
import co.edu.unbosque.sdgh.entity.Aula;
import co.edu.unbosque.sdgh.exception.ClassroomNotFoundException;
import co.edu.unbosque.sdgh.exception.InvalidNumberException;
import co.edu.unbosque.sdgh.exception.LanzadorDeExcepcion;
import co.edu.unbosque.sdgh.exception.NullDataException;
import co.edu.unbosque.sdgh.repository.AulaRepository;

/**
 * Servicio encargado de gestionar las operaciones CRUD de las aulas fisicas
 * (CU04 - RF04, RF07, RF08).
 */
@Service
public class AulaService implements CRUDOperation<AulaDTO> {

	/**
	 * Repositorio encargado de las operaciones de persistencia de aulas.
	 */
	@Autowired
	private AulaRepository aulaRepo;

	/**
	 * Mapper utilizado para convertir entidades en DTO y viceversa.
	 */
	@Autowired
	private ModelMapper mapper;

	/**
	 * Constructor vacio de la clase AulaService.
	 */
	public AulaService() {
	}

	/**
	 * Metodo que crea una nueva aula validando que su capacidad sea mayor a cero (CU04 / RF04).
	 * @param newData DTO con la informacion del aula a crear.
	 * @return Retorna 0 si el aula fue creada correctamente o 2 si los datos
	 *         son nulos o la capacidad es menor o igual a cero.
	 */
	@Override
	public int create(AulaDTO newData) {

		if (!esAulaValida(newData)) {
			return 2;
		}

		aulaRepo.save(mapper.map(newData, Aula.class));
		return 0;
	}

	/**
	 * Metodo que obtiene todas las aulas registradas.
	 * @return Retorna una lista con todas las aulas en formato DTO.
	 */
	@Override
	public List<AulaDTO> getAll() {

		List<Aula> entityList = (List<Aula>) aulaRepo.findAll();
		List<AulaDTO> dtoList = new ArrayList<>();

		entityList.forEach((entidad) -> {

			AulaDTO dto = mapper.map(entidad, AulaDTO.class);
			dtoList.add(dto);
		});

		return dtoList;
	}

	/**
	 * Metodo que elimina un aula mediante su identificador.
	 * @param id Identificador del aula a eliminar.
	 * @return Retorna 0 si el aula fue eliminada correctamente o 1 si el
	 *         aula no existe o el identificador es nulo.
	 */
	@Override
	public int deleteById(Integer id) {

		if (id == null) {
			return 1;
		}

		Optional<Aula> encontrado = aulaRepo.findById(id);

		try {
			LanzadorDeExcepcion.revisarSiExisteAula(encontrado);
		} catch (ClassroomNotFoundException e) {
			System.out.println("Error de aula: " + e.getMessage());
			return 1;
		}

		aulaRepo.delete(encontrado.get());
		return 0;
	}

	/**
	 * Metodo que actualiza la capacidad y las caracteristicas de un aula mediante
	 * su identificador (CU04).
	 * @param id Identificador del aula a actualizar.
	 * @param newData DTO con la nueva informacion del aula.
	 * @return Retorna 0 si la actualizacion fue exitosa, 1 si el aula no
	 *         existe o 2 si la nueva capacidad es menor o igual a cero.
	 */
	@Override
	public int updateById(Integer id, AulaDTO newData) {

		if (id == null) {
			return 1;
		}

		Optional<Aula> encontrado = aulaRepo.findById(id);

		try {
			LanzadorDeExcepcion.revisarSiExisteAula(encontrado);
		} catch (ClassroomNotFoundException e) {
			System.out.println("Error de aula: " + e.getMessage());
			return 1;
		}

		if (!esAulaValida(newData)) {
			return 2;
		}

		Aula temp = encontrado.get();
		temp.setCapacidad(newData.getCapacidad());
		temp.setTieneSillasMovibles(newData.isTieneSillasMovibles());
		temp.setTieneComputadores(newData.isTieneComputadores());
		aulaRepo.save(temp);
		return 0;
	}

	/**
	 * Metodo que cuenta la cantidad total de aulas registradas.
	 * @return Retorna la cantidad total de aulas.
	 */
	@Override
	public long count() {

		return aulaRepo.count();
	}

	/**
	 * Metodo que verifica si un aula existe mediante su identificador.
	 * @param id Identificador del aula a verificar.
	 * @return Retorna true si el aula existe o false en caso contrario.
	 */
	@Override
	public boolean exist(Integer id) {

		return id != null && aulaRepo.existsById(id);
	}

	/**
	 * Metodo que busca un aula mediante su identificador.
	 * @param id Identificador del aula.
	 * @return Retorna el aula en formato DTO o null si no se encuentra.
	 */
	public AulaDTO getById(Integer id) {

		if (id == null) {
			return null;
		}

		Optional<Aula> encontrado = aulaRepo.findById(id);

		try {
			LanzadorDeExcepcion.revisarSiExisteAula(encontrado);
		} catch (ClassroomNotFoundException e) {
			System.out.println("Error de aula: " + e.getMessage());
			return null;
		}

		return mapper.map(encontrado.get(), AulaDTO.class);
	}

	/**
	 * Metodo que busca las aulas con capacidad igual o superior a la requerida (RF07).
	 * @param capacidad Capacidad minima requerida.
	 * @return Retorna una lista de aulas en formato DTO que cumplen la capacidad.
	 */
	public List<AulaDTO> getByCapacidadMinima(int capacidad) {

		List<Aula> entityList = aulaRepo.findByCapacidadGreaterThanEqual(capacidad);
		List<AulaDTO> dtoList = new ArrayList<>();

		entityList.forEach((entidad) -> {

			AulaDTO dto = mapper.map(entidad, AulaDTO.class);
			dtoList.add(dto);
		});

		return dtoList;
	}

	/**
	 * Metodo que busca las aulas segun si disponen o no de computadores (RF08).
	 * @param tieneComputadores true para aulas con computadores, false para las demas.
	 * @return Retorna una lista de aulas en formato DTO filtrada.
	 */
	public List<AulaDTO> getByTieneComputadores(boolean tieneComputadores) {

		List<Aula> entityList = aulaRepo.findByTieneComputadores(tieneComputadores);
		List<AulaDTO> dtoList = new ArrayList<>();

		entityList.forEach((entidad) -> {

			AulaDTO dto = mapper.map(entidad, AulaDTO.class);
			dtoList.add(dto);
		});

		return dtoList;
	}

	/**
	 * Metodo que busca las aulas segun si disponen o no de sillas movibles (RF08).
	 * @param tieneSillasMovibles true para aulas con sillas movibles, false para las demas.
	 * @return Retorna una lista de aulas en formato DTO filtrada.
	 */
	public List<AulaDTO> getByTieneSillasMovibles(boolean tieneSillasMovibles) {

		List<Aula> entityList = aulaRepo.findByTieneSillasMovibles(tieneSillasMovibles);
		List<AulaDTO> dtoList = new ArrayList<>();

		entityList.forEach((entidad) -> {

			AulaDTO dto = mapper.map(entidad, AulaDTO.class);
			dtoList.add(dto);
		});

		return dtoList;
	}

	/**
	 * Metodo que busca las aulas con capacidad igual o superior a la requerida y
	 * que cumplan la condicion de computadores.
	 * @param capacidad Capacidad minima requerida.
	 * @param tieneComputadores true si requiere computadores.
	 * @return Retorna una lista de aulas en formato DTO que cumplen ambos criterios.
	 */
	public List<AulaDTO> getByCapacidadMinimaAndComputadores(int capacidad, boolean tieneComputadores) {

		List<Aula> entityList = aulaRepo.findByCapacidadGreaterThanEqualAndTieneComputadores(capacidad,
				tieneComputadores);
		List<AulaDTO> dtoList = new ArrayList<>();

		entityList.forEach((entidad) -> {

			AulaDTO dto = mapper.map(entidad, AulaDTO.class);
			dtoList.add(dto);
		});

		return dtoList;
	}

	/**
	 * Metodo que valida que el aula tenga datos validos y capacidad positiva.
	 * @param data DTO del aula a validar.
	 * @return Retorna true si los datos son validos o false en caso contrario.
	 */
	private boolean esAulaValida(AulaDTO data) {

		try {
			LanzadorDeExcepcion.verificarDatosNoNulos(data);
		} catch (NullDataException e) {
			System.out.println("Error de aula: " + e.getMessage());
			return false;
		}

		try {
			LanzadorDeExcepcion.verificarPositivo(data.getCapacidad());
		} catch (InvalidNumberException e) {
			System.out.println("Error de capacidad: " + e.getMessage());
			return false;
		}

		return true;
	}
}