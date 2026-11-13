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
 * controlador. Las horas semanales de cada materia no se reciben ni se guardan:
 * se calculan a partir de sus creditos (Decreto 1330 de 2019, art. 2.5.3.2.4.1).
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
	 * Horas de trabajo academico del estudiante que equivalen a un credito en un
	 * periodo academico (Decreto 1330 de 2019, art. 2.5.3.2.4.1).
	 */
	private static final int HORAS_POR_CREDITO = 48;

	/**
	 * Horas de trabajo independiente por cada hora con acompanamiento directo del
	 * docente en pregrado. Es una decision institucional: confirmar con el
	 * Programa.
	 */
	private static final int HORAS_INDEPENDIENTES_POR_HORA_DIRECTA = 2;

	/**
	 * Semanas del periodo academico. Es una decision institucional: confirmar con
	 * el Programa.
	 */
	private static final int SEMANAS_PERIODO = 16;

	/**
	 * Duracion en horas de cada sesion de clase (RF3).
	 */
	private static final int HORAS_POR_SESION = 2;

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
	 * unicidad del nombre (CU02 / RF01). Las horas de la materia no se guardan:
	 * se calculan a partir de sus creditos con {@link #calcularHorasSemanales(int)}.
	 * @param newData DTO con la informacion de la materia a crear.
	 * @return Retorna 0 si la materia fue creada correctamente.
	 * @throws DatosInvalidosException si los datos son nulos, estan incompletos o
	 *         fuera de rango.
	 * @throws RecursoDuplicadoException si el nombre ya existe.
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
		if (newData.getCodigo() < 100000L || newData.getCodigo() > 9999999999L) {
			throw new DatosInvalidosException(
					"El codigo de la materia debe ser numerico y tener entre 6 y 10 digitos, como una cedula");
		}

		String nombre = newData.getNombre().trim();

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

//	/**
//	 * Metodo que elimina una materia mediante su identificador (codigo) recibido
//	 * como Integer (RF26).
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
	 * Metodo que elimina una materia mediante su identificador unico de tipo Long
	 * (RF26).
	 * @param id Identificador unico de la materia a eliminar.
	 * @return Retorna 0 si la materia fue eliminada correctamente.
	 * @throws DatosInvalidosException si el id es nulo.
	 * @throws RecursoNoEncontradoException si la materia no existe.
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
	 * Metodo que actualiza una materia mediante su identificador unico de tipo
	 * Long (CU02 / RF26).
	 * @param id Identificador de la materia a actualizar.
	 * @param newData DTO con la nueva informacion de la materia.
	 * @return Retorna 0 si la actualizacion fue exitosa.
	 * @throws DatosInvalidosException si el id es nulo o los datos nuevos son
	 *         invalidos o estan incompletos.
	 * @throws RecursoNoEncontradoException si la materia no existe.
	 * @throws RecursoDuplicadoException si el nombre ya pertenece a otra materia.
	 */
	@Override
	public int updateById(Long id, MateriaDTO newData) {

		if (id == null) {
			throw new DatosInvalidosException("El id de la materia no puede ser nulo");
		}

		Materia temp = materiaRepo.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe una materia con el id " + id));

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

		String nombre = newData.getNombre().trim();
		Optional<Materia> porNombre = materiaRepo.findByNombre(nombre);

		if (porNombre.isPresent() && porNombre.get().getId() != id.longValue()) {
			throw new RecursoDuplicadoException("Ya existe otra materia con el nombre " + nombre);
		}

		temp.setNombre(nombre);
		temp.setSemestre(newData.getSemestre());
		temp.setNcreditos(newData.getNcreditos());
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
	 * Metodo que verifica si una materia existe mediante su identificador de tipo
	 * Long.
	 * @param id Identificador unico de la materia a verificar.
	 * @return Retorna true si la materia existe o false en caso contrario.
	 */
	@Override
	public boolean existById(Long id) {

		return id != null && materiaRepo.existsById(id);
	}

	/**
	 * Metodo que busca una materia mediante su identificador unico.
	 * @param id Identificador de la materia.
	 * @return Retorna la materia en formato DTO.
	 * @throws RecursoNoEncontradoException si la materia no existe.
	 */
	public MateriaDTO getById(long id) {

		Materia encontrada = materiaRepo.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe una materia con el id " + id));

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

		Materia encontrada = materiaRepo.findByNombre(nombre.trim()).orElseThrow(
				() -> new RecursoNoEncontradoException("No existe una materia con el nombre " + nombre.trim()));

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

	/**
	 * Metodo que calcula las horas semanales con acompanamiento directo del docente
	 * a partir de los creditos de la materia, redondeadas hacia arriba. Un credito
	 * equivale a 48 horas de trabajo academico del estudiante por periodo
	 * academico (Decreto 1330 de 2019, art. 2.5.3.2.4.1); la proporcion entre
	 * horas con docente y horas de trabajo independiente y las semanas del periodo
	 * son decision de la institucion y estan en las constantes de esta clase. Con
	 * los valores actuales (relacion 1:2 y 16 semanas) cada credito equivale a 1
	 * hora semanal.
	 * @param ncreditos Numero de creditos de la materia.
	 * @return Retorna las horas semanales de la materia.
	 */
	public int calcularHorasSemanales(int ncreditos) {

		int horasDirectasPeriodo = (ncreditos * HORAS_POR_CREDITO) / (1 + HORAS_INDEPENDIENTES_POR_HORA_DIRECTA);
		return (horasDirectasPeriodo + SEMANAS_PERIODO - 1) / SEMANAS_PERIODO;
	}

	/**
	 * Metodo que calcula las sesiones semanales de 2 horas que necesita la materia
	 * a partir de sus creditos, redondeadas hacia arriba (RF1, RF3, RF7). Con los
	 * valores actuales, una materia de 3 creditos tiene 3 horas semanales y por
	 * tanto 2 sesiones.
	 * @param ncreditos Numero de creditos de la materia.
	 * @return Retorna las sesiones semanales de la materia.
	 */
	public int calcularSesionesSemanales(int ncreditos) {

		return (calcularHorasSemanales(ncreditos) + HORAS_POR_SESION - 1) / HORAS_POR_SESION;
	}
	
	public int getHorasTotalesSemestre(Long id) {

		if (id == null) {
			throw new DatosInvalidosException("El id de la materia no puede ser nulo");
		}

		MateriaDTO materia = getById(id);
		return materia.getNcreditos() * HORAS_POR_CREDITO;
	}
}