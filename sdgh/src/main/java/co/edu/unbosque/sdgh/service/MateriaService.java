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

import co.edu.unbosque.sdgh.dto.MateriaDTO;
import co.edu.unbosque.sdgh.entity.Materia;
import co.edu.unbosque.sdgh.exception.DatosInvalidosException;
import co.edu.unbosque.sdgh.exception.RecursoDuplicadoException;
import co.edu.unbosque.sdgh.exception.RecursoNoEncontradoException;
import co.edu.unbosque.sdgh.repository.MateriaRepository;

/**
 * Servicio encargado de gestionar las operaciones CRUD de las materias del plan
 * de estudios (CU02 - RF01, RF26). Las validaciones se hacen al inicio de cada
 * metodo y lanzan excepciones personalizadas que son capturadas en el
 * controlador.
 */
@Service
public class MateriaService implements CRUDOperation<MateriaDTO> {

	/**
	 * Semestre minimo del plan de estudios.
	 */
	private static final int SEMESTRE_MINIMO = 1;

	/**
	 * Semestre maximo del plan de estudios (Ing. de Sistemas SNIES 4952).
	 */
	private static final int SEMESTRE_MAXIMO = 9;

	/**
	 * Repositorio encargado de las operaciones de persistencia de materias.
	 */
	@Autowired
	private MateriaRepository materiaRepo;

	/**
	 * Mapper utilizado para convertir entidades en DTO y viceversa.
	 */
	@Autowired
	private ModelMapper mapper;

	/**
	 * Constructor vacio de la clase MateriaService.
	 */
	public MateriaService() {
	}

	/**
	 * Metodo que crea una nueva materia validando los datos obligatorios y la
	 * unicidad de codigo y nombre (CU02 / RF01).
	 * @param newData DTO con la informacion de la materia a crear.
	 * @return Retorna 0 si la materia fue creada correctamente.
	 * @throws DatosInvalidosException si los datos son nulos, estan incompletos o
	 *         fuera de rango.
	 * @throws RecursoDuplicadoException si el codigo o el nombre ya existen.
	 */
	@Override
	public int create(MateriaDTO newData) {

		if (newData == null) {
			throw new DatosInvalidosException("Los datos de la materia no pueden ser nulos");
		}
		if (newData.getNombre() == null || newData.getNombre().trim().isEmpty()) {
			throw new DatosInvalidosException("El nombre de la materia no puede estar vacio");
		}
		if (newData.getSemestre() < SEMESTRE_MINIMO || newData.getSemestre() > SEMESTRE_MAXIMO) {
			throw new DatosInvalidosException(
					"El semestre debe estar entre " + SEMESTRE_MINIMO + " y " + SEMESTRE_MAXIMO);
		}
		if (newData.getNcreditos() <= 0) {
			throw new DatosInvalidosException("El numero de creditos debe ser mayor a 0");
		}
		if (newData.getHorasDeLaMateria() <= 0) {
			throw new DatosInvalidosException("Las horas de la materia deben ser mayores a 0");
		}
		if (newData.getId() <= 0) {
			throw new DatosInvalidosException("El codigo de la materia debe ser un numero positivo");
		}

		String nombre = newData.getNombre().trim();

		if (materiaRepo.existsById(newData.getId())) {
			throw new RecursoDuplicadoException("Ya existe una materia con el codigo " + newData.getId());
		}
		if (materiaRepo.findByNombre(nombre).isPresent()) {
			throw new RecursoDuplicadoException("Ya existe una materia con el nombre " + nombre);
		}

		Materia entity = mapper.map(newData, Materia.class);
		entity.setNombre(nombre);
		materiaRepo.save(entity);
		return 0;
	}

	/**
	 * Metodo que obtiene todas las materias registradas (RF26).
	 * @return Retorna una lista con todas las materias en formato DTO.
	 */
	@Override
	public List<MateriaDTO> getAll() {

		List<Materia> entityList = (List<Materia>) materiaRepo.findAll();
		List<MateriaDTO> dtoList = new ArrayList<>();

		entityList.forEach((entidad) -> {

			MateriaDTO dto = mapper.map(entidad, MateriaDTO.class);
			dtoList.add(dto);
		});

		return dtoList;
	}

	/**
	 * Metodo que elimina una materia mediante su identificador (codigo) recibido
	 * como Integer (RF26).
	 * @param id Codigo de la materia a eliminar.
	 * @return Retorna 0 si la materia fue eliminada correctamente.
	 * @throws DatosInvalidosException si el identificador es nulo.
	 * @throws RecursoNoEncontradoException si la materia no existe.
	 */
	@Override
	public int deleteById(Long id) {

		if (id == null) {
			throw new DatosInvalidosException("El codigo de la materia no puede ser nulo");
		}

		return deleteByCodigo(id.longValue());
	}

	/**
	 * Metodo que elimina una materia mediante su codigo unico de tipo Long (RF26).
	 * @param codigo Codigo unico de la materia a eliminar.
	 * @return Retorna 0 si la materia fue eliminada correctamente.
	 * @throws DatosInvalidosException si el codigo es nulo.
	 * @throws RecursoNoEncontradoException si la materia no existe.
	 */
	public int deleteByCodigo(Long codigo) {

		if (codigo == null) {
			throw new DatosInvalidosException("El codigo de la materia no puede ser nulo");
		}

		Materia encontrada = materiaRepo.findById(codigo)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe una materia con el codigo " + codigo));

		materiaRepo.delete(encontrada);
		return 0;
	}

	/**
	 * Metodo que actualiza una materia mediante su identificador (codigo) recibido
	 * como Integer (CU02 / RF26).
	 * @param id Codigo de la materia a actualizar.
	 * @param newData DTO con la nueva informacion de la materia.
	 * @return Retorna 0 si la actualizacion fue exitosa.
	 * @throws DatosInvalidosException si el identificador es nulo o los datos
	 *         nuevos son invalidos o estan incompletos.
	 * @throws RecursoNoEncontradoException si la materia no existe.
	 * @throws RecursoDuplicadoException si el nombre pertenece a otra materia.
	 */
	@Override
	public int updateById(Long id, MateriaDTO newData) {

		if (id == null) {
			throw new DatosInvalidosException("El codigo de la materia no puede ser nulo");
		}

		return updateByCodigo(id.longValue(), newData);
	}

	/**
	 * Metodo que actualiza una materia mediante su codigo unico de tipo Long (CU02 / RF26).
	 * @param codigo Codigo de la materia a actualizar.
	 * @param newData DTO con la nueva informacion de la materia.
	 * @return Retorna 0 si la actualizacion fue exitosa.
	 * @throws DatosInvalidosException si el codigo es nulo o los datos nuevos son
	 *         invalidos o estan incompletos.
	 * @throws RecursoNoEncontradoException si la materia no existe.
	 * @throws RecursoDuplicadoException si el nombre ya pertenece a otra materia.
	 */
	public int updateByCodigo(Long codigo, MateriaDTO newData) {

		if (codigo == null) {
			throw new DatosInvalidosException("El codigo de la materia no puede ser nulo");
		}

		Materia temp = materiaRepo.findById(codigo)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe una materia con el codigo " + codigo));

		if (newData == null) {
			throw new DatosInvalidosException("Los datos de la materia no pueden ser nulos");
		}
		if (newData.getNombre() == null || newData.getNombre().trim().isEmpty()) {
			throw new DatosInvalidosException("El nombre de la materia no puede estar vacio");
		}
		if (newData.getSemestre() < SEMESTRE_MINIMO || newData.getSemestre() > SEMESTRE_MAXIMO) {
			throw new DatosInvalidosException(
					"El semestre debe estar entre " + SEMESTRE_MINIMO + " y " + SEMESTRE_MAXIMO);
		}
		if (newData.getNcreditos() <= 0) {
			throw new DatosInvalidosException("El numero de creditos debe ser mayor a 0");
		}
		if (newData.getHorasDeLaMateria() <= 0) {
			throw new DatosInvalidosException("Las horas de la materia deben ser mayores a 0");
		}

		String nombre = newData.getNombre().trim();
		Optional<Materia> porNombre = materiaRepo.findByNombre(nombre);

		if (porNombre.isPresent() && porNombre.get().getId() != codigo.longValue()) {
			throw new RecursoDuplicadoException("Ya existe otra materia con el nombre " + nombre);
		}

		temp.setNombre(nombre);
		temp.setSemestre(newData.getSemestre());
		temp.setNcreditos(newData.getNcreditos());
		temp.setHorasDeLaMateria(newData.getHorasDeLaMateria());
		temp.setCorrelativa(newData.isCorrelativa());
		materiaRepo.save(temp);
		return 0;
	}

	/**
	 * Metodo que cuenta la cantidad total de materias registradas.
	 * @return Retorna la cantidad total de materias.
	 */
	@Override
	public long count() {

		return materiaRepo.count();
	}

	/**
	 * Metodo que verifica si una materia existe mediante su identificador (codigo)
	 * recibido como Integer.
	 * @param id Codigo de la materia a verificar.
	 * @return Retorna true si la materia existe o false en caso contrario.
	 */
	@Override
	public boolean exist(Long id) {

		if (id == null) {
			return false;
		}

		return existByCodigo(id.longValue());
	}

	/**
	 * Metodo que verifica si una materia existe mediante su codigo de tipo Long.
	 * @param codigo Codigo unico de la materia a verificar.
	 * @return Retorna true si la materia existe o false en caso contrario.
	 */
	public boolean existByCodigo(Long codigo) {

		return codigo != null && materiaRepo.existsById(codigo);
	}

	/**
	 * Metodo que busca una materia mediante su codigo unico.
	 * @param codigo Codigo de la materia.
	 * @return Retorna la materia en formato DTO.
	 * @throws RecursoNoEncontradoException si la materia no existe.
	 */
	public MateriaDTO getByCodigo(long codigo) {

		Materia encontrada = materiaRepo.findById(codigo)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe una materia con el codigo " + codigo));

		return mapper.map(encontrada, MateriaDTO.class);
	}

	/**
	 * Metodo que busca una materia mediante su nombre exacto.
	 * @param nombre Nombre de la materia.
	 * @return Retorna la materia en formato DTO.
	 * @throws DatosInvalidosException si el nombre es nulo o esta vacio.
	 * @throws RecursoNoEncontradoException si la materia no existe.
	 */
	public MateriaDTO getByNombre(String nombre) {

		if (nombre == null || nombre.trim().isEmpty()) {
			throw new DatosInvalidosException("El nombre de la materia no puede estar vacio");
		}

		Materia encontrada = materiaRepo.findByNombre(nombre.trim())
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe una materia con el nombre " + nombre.trim()));

		return mapper.map(encontrada, MateriaDTO.class);
	}

	/**
	 * Metodo que obtiene todas las materias correspondientes a un semestre especifico (1 a 9).
	 * @param semestre Numero de semestre.
	 * @return Retorna una lista de materias en formato DTO del semestre indicado.
	 * @throws DatosInvalidosException si el semestre esta fuera del rango 1 a 9.
	 */
	public List<MateriaDTO> getBySemestre(int semestre) {

		if (semestre < SEMESTRE_MINIMO || semestre > SEMESTRE_MAXIMO) {
			throw new DatosInvalidosException(
					"El semestre debe estar entre " + SEMESTRE_MINIMO + " y " + SEMESTRE_MAXIMO);
		}

		List<Materia> entityList = materiaRepo.findBySemestre(semestre);
		List<MateriaDTO> dtoList = new ArrayList<>();

		entityList.forEach((entidad) -> {

			MateriaDTO dto = mapper.map(entidad, MateriaDTO.class);
			dtoList.add(dto);
		});

		return dtoList;
	}

	/**
	 * Metodo que obtiene las materias filtradas segun si son correlativas (tienen
	 * prerrequisito) o no.
	 * @param correlativa true para materias correlativas, false en caso contrario.
	 * @return Retorna una lista de materias en formato DTO que cumplen el filtro.
	 */
	public List<MateriaDTO> getByCorrelativa(boolean correlativa) {

		List<Materia> entityList = materiaRepo.findByCorrelativa(correlativa);
		List<MateriaDTO> dtoList = new ArrayList<>();

		entityList.forEach((entidad) -> {

			MateriaDTO dto = mapper.map(entidad, MateriaDTO.class);
			dtoList.add(dto);
		});

		return dtoList;
	}
}