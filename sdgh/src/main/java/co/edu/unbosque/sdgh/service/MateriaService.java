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
import co.edu.unbosque.sdgh.entity.ClaveParametro;
import co.edu.unbosque.sdgh.entity.Materia;
import co.edu.unbosque.sdgh.exception.DatosInvalidosException;
import co.edu.unbosque.sdgh.exception.RecursoDuplicadoException;
import co.edu.unbosque.sdgh.exception.RecursoNoEncontradoException;
import co.edu.unbosque.sdgh.repository.MateriaRepository;

/**
 * Servicio encargado de gestionar las operaciones CRUD de los cursos del plan
 * de estudios (CU02, RF1). Cada curso tiene codigo, nombre, semestre, creditos y
 * horas semanales; las sesiones semanales se derivan de las horas y deben estar
 * entre 1 y el maximo configurado. Las validaciones se hacen al inicio de cada
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
	 * Servicio de parametros: maximo de sesiones semanales (RF1, RF9).
	 */
	@Autowired
	private ParametroService parametroServ;

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
	 * Metodo que crea una nueva materia validando los datos obligatorios, el
	 * rango de sesiones y la unicidad de codigo y nombre (CU02, RF1).
	 * @param newData DTO con la informacion de la materia a crear.
	 * @return Retorna 0 si la materia fue creada correctamente.
	 * @throws DatosInvalidosException si los datos son nulos, estan incompletos o
	 *         fuera de rango.
	 * @throws RecursoDuplicadoException si el codigo o el nombre ya existen.
	 */
	@Override
	public int create(MateriaDTO newData) {

		validarDatos(newData);

		String nombre = newData.getNombre().trim();

		if (materiaRepo.findByCodigo(newData.getCodigo()).isPresent()) {
			throw new RecursoDuplicadoException("Ya existe una materia con el codigo " + newData.getCodigo());
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
	 * Metodo que obtiene todas las materias registradas (RF1).
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

//	/**
//	 * Metodo que elimina una materia mediante su identificador (codigo) recibido
//	 * como Integer (RF1).
//	 * @param id id de la materia a eliminar.
//	 * @return Retorna 0 si la materia fue eliminada correctamente.
//	 * @throws DatosInvalidosException si el identificador es nulo.
//	 * @throws RecursoNoEncontradoException si la materia no existe.
//	 */
//	@Override
//	public int deleteById(Long id) {
//
//		if (id == null) {
//			throw new DatosInvalidosException("El id de la materia no puede ser nulo");
//		}
//
//		return deleteByCodigo(id.longValue());
//	}

	/**
	 * Metodo que elimina una materia mediante su codigo unico de tipo Long (RF1).
	 * @param codigo Codigo unico de la materia a eliminar.
	 * @return Retorna 0 si la materia fue eliminada correctamente.
	 * @throws DatosInvalidosException si el codigo es nulo.
	 * @throws RecursoNoEncontradoException si la materia nidte.
	 */
	@Override
	public int deleteById(Long id) {

		if (id == null) {
			throw new DatosInvalidosException("El id de la materia no puede ser nulo");
		}

		Materia encontrada = materiaRepo.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe una materia con el id " + id));

		materiaRepo.delete(encontrada);
		return 0;
	}

	/**
	 * Metodo que actualiza una materia mediante su identificador (id) recibido
	 * como Integer (CU02 / RF1).
	 * @param id id de la materia a actualizar.
	 * @param newData DTO con la nueva informacion de la materia.
	 * @return Retorna 0 si la actualizacion fue exitosa.
	 * @throws DatosInvalidosException si el identificador es nulo o los datos
	 *         nuevos son invalidos o estan incompletos.
	 * @throws RecursoNoEncontradoException si la materia no existe.
	 * @throws RecursoDuplicadoException si el nombre pertenece a otra materia.
	 */
//	@Override
//	public int updateById(Long id, MateriaDTO newData) {
//
//		if (id == null) {
//			throw new DatosInvalidosException("El id de la materia no puede ser nulo");
//		}
//
//		return updateById(id.longValue(), newData);
//	}

	/**
	 * Metodo que actualiza una materia mediante su id unico de tipo Long (CU02 / RF1).
	 * @param id de la materia a actualizar.
	 * @param newData DTO con la nueva informacion de la materia.
	 * @return Retorna 0 si la actualizacion fue exitosa.
	 * @throws DatosInvalidosException si el id es nulo o los datos nuevos son
	 *         invalidos o estan incompletos.
	 * @throws RecursoNoEncontradoException si la materia no existe.
	 * @throws RecursoDuplicadoException si el codigo o el nombre ya pertenecen a
	 *         otra materia.
	 */
	@Override
	public int updateById(Long id, MateriaDTO newData) {

		if (id == null) {
			throw new DatosInvalidosException("El id de la materia no puede ser nulo");
		}

		Materia temp = materiaRepo.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe una materia con el id " + id));

		validarDatos(newData);

		String nombre = newData.getNombre().trim();
		Optional<Materia> porCodigo = materiaRepo.findByCodigo(newData.getCodigo());
		Optional<Materia> porNombre = materiaRepo.findByNombre(nombre);

		if (porCodigo.isPresent() && porCodigo.get().getId() != id.longValue()) {
			throw new RecursoDuplicadoException("Ya existe otra materia con el codigo " + newData.getCodigo());
		}
		if (porNombre.isPresent() && porNombre.get().getId() != id.longValue()) {
			throw new RecursoDuplicadoException("Ya existe otra materia con el nombre " + nombre);
		}

		temp.setCodigo(newData.getCodigo());
		temp.setNombre(nombre);
		temp.setSemestre(newData.getSemestre());
		temp.setNcreditos(newData.getNcreditos());
		temp.setHorasDeLaMateria(newData.getHorasDeLaMateria());
		temp.setCorrelativa(newData.isCorrelativa());
		materiaRepo.save(temp);
		return 0;
	}

	/**
	 * Metodo que valida los datos de una materia: codigo positivo, nombre, semestre
	 * de 1 a 9, creditos y horas positivos, y que las sesiones semanales (horas
	 * divididas entre 2) esten entre 1 y el maximo configurado (RF1).
	 * @param newData DTO con los datos a validar.
	 * @throws DatosInvalidosException si algun dato es invalido.
	 */
	private void validarDatos(MateriaDTO newData) {

		if (newData == null) {
			throw new DatosInvalidosException("Los datos de la materia no pueden ser nulos");
		}
		if (newData.getCodigo() <= 0) {
			throw new DatosInvalidosException("El codigo de la materia debe ser un numero positivo");
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

		int maximo = parametroServ.getEntero(ClaveParametro.SESIONES_MAXIMAS);
		int sesiones = newData.getHorasDeLaMateria() / 2;

		if (sesiones < 1 || sesiones > maximo) {
			throw new DatosInvalidosException("Las horas de la materia deben dar entre 1 y " + maximo
					+ " sesiones semanales de 2 horas (entre 2 y " + (maximo * 2) + " horas)");
		}
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
	 * Metodo que verifica si una materia existe mediante su identificador (id)
	 * recibido como Integer.
	 * @param id id de la materia a verificar.
	 * @return Retorna true si la materia existe o false en caso contrario.
	 */
//	@Override
//	public boolean exist(Long id) {
//
//		if (id == null) {
//			return false;
//		}
//
//		return existByCodigo(id.longValue());
//	}

	/**
	 * Metodo que verifica si una materia existe mediante su codigo de tipo Long.
	 * @param codigo Codigo unico de la materia a verificar.
	 * @return Retorna true si la materia existe o false en caso contrario.
	 */
	@Override
	public boolean existById(Long id) {
		
		return id != null && materiaRepo.existsById(id);
	}

	/**
	 * Metodo que busca una materia mediante su codigo unico.
	 * @param codigo Codigo de la materia.
	 * @return Retorna la materia en formato DTO.
	 * @throws RecursoNoEncontradoException si la materia no existe.
	 */
	public MateriaDTO getById(long id) {

		Materia encontrada = materiaRepo.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe una materia con el codigo " + id));

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