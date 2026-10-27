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

import co.edu.unbosque.sdgh.dto.ProfesorDTO;
import co.edu.unbosque.sdgh.entity.Profesor;
import co.edu.unbosque.sdgh.exception.EmptyFieldException;
import co.edu.unbosque.sdgh.exception.InvalidNumberException;
import co.edu.unbosque.sdgh.exception.LanzadorDeExcepcion;
import co.edu.unbosque.sdgh.exception.NullDataException;
import co.edu.unbosque.sdgh.exception.RecordAlreadyExistsException;
import co.edu.unbosque.sdgh.exception.TeacherNotFoundException;
import co.edu.unbosque.sdgh.repository.ProfesorRepository;

/**
 * Servicio encargado de gestionar las operaciones CRUD de los docentes
 * (CU03 - RF03, RF17).
 */
@Service
public class ProfesorService implements CRUDOperation<ProfesorDTO> {

	/**
	 * Repositorio encargado de las operaciones de persistencia de docentes.
	 */
	@Autowired
	private ProfesorRepository profesorRepo;

	/**
	 * Mapper utilizado para convertir entidades en DTO y viceversa.
	 */
	@Autowired
	private ModelMapper mapper;

	/**
	 * Constructor vacio de la clase ProfesorService.
	 */
	public ProfesorService() {
	}

	/**
	 * Metodo que crea un nuevo docente validando unicidad de codigo y correo, asi
	 * como los datos obligatorios (CU03 / RF03).
	 * @param newData DTO con la informacion del docente a crear.
	 * @return Retorna 0 si el docente fue creado correctamente, 1 si el
	 *         codigo o el correo ya existen o 2 si los datos son nulos,
	 *         estan incompletos o son invalidos.
	 */
	@Override
	public int create(ProfesorDTO newData) {

		if (!esProfesorValido(newData)) {
			return 2;
		}

		try {
			LanzadorDeExcepcion.verificarPositivo(newData.getCodigo());
		} catch (InvalidNumberException e) {
			System.out.println("Error de codigo: " + e.getMessage());
			return 2;
		}

		try {
			LanzadorDeExcepcion.revisarSiRegistroYaExiste(profesorRepo.existsById(newData.getCodigo()),
					"codigo " + newData.getCodigo());
		} catch (RecordAlreadyExistsException e) {
			System.out.println("Error de codigo: " + e.getMessage());
			return 1;
		}

		try {
			LanzadorDeExcepcion.revisarSiRegistroYaExiste(profesorRepo.existsByCorreo(newData.getCorreo().trim()),
					"correo " + newData.getCorreo().trim());
		} catch (RecordAlreadyExistsException e) {
			System.out.println("Error de correo: " + e.getMessage());
			return 1;
		}

		Profesor entity = mapper.map(newData, Profesor.class);
		entity.setNombre(newData.getNombre().trim());
		entity.setCorreo(newData.getCorreo().trim());
		entity.setFacultad(newData.getFacultad().trim());
		entity.setEspecialidades(newData.getEspecialidades() != null
				? new ArrayList<>(newData.getEspecialidades())
				: new ArrayList<>());
		entity.setPublicaciones(newData.getPublicaciones() != null
				? new ArrayList<>(newData.getPublicaciones())
				: new ArrayList<>());
		profesorRepo.save(entity);
		return 0;
	}

	/**
	 * Metodo que obtiene todos los docentes registrados (CU03 / RF03).
	 * @return Retorna una lista con todos los docentes en formato DTO.
	 */
	@Override
	public List<ProfesorDTO> getAll() {

		List<Profesor> entityList = (List<Profesor>) profesorRepo.findAll();
		List<ProfesorDTO> dtoList = new ArrayList<>();

		entityList.forEach((entidad) -> {

			ProfesorDTO dto = mapper.map(entidad, ProfesorDTO.class);
			dtoList.add(dto);
		});

		return dtoList;
	}

	/**
	 * Metodo que elimina un docente mediante su identificador.
	 * @param id Identificador (codigo) del docente a eliminar.
	 * @return Retorna 0 si el docente fue eliminado correctamente o 1 si
	 *         el docente no existe o el identificador es nulo.
	 */
	@Override
	public int deleteById(Integer id) {

		if (id == null) {
			return 1;
		}

		Optional<Profesor> encontrado = profesorRepo.findById(id);

		try {
			LanzadorDeExcepcion.revisarSiExisteProfesor(encontrado);
		} catch (TeacherNotFoundException e) {
			System.out.println("Error de docente: " + e.getMessage());
			return 1;
		}

		profesorRepo.delete(encontrado.get());
		return 0;
	}

	/**
	 * Metodo que actualiza un docente mediante su identificador (CU03 / RF03).
	 * @param id Identificador (codigo) del docente a actualizar.
	 * @param newData DTO con la nueva informacion del docente.
	 * @return Retorna 0 si la actualizacion fue exitosa, 1 si el docente no
	 *         existe o el correo ya pertenece a otro docente, o 2 si los
	 *         datos nuevos son invalidos o estan incompletos.
	 */
	@Override
	public int updateById(Integer id, ProfesorDTO newData) {

		if (id == null) {
			return 1;
		}

		Optional<Profesor> encontrado = profesorRepo.findById(id);

		try {
			LanzadorDeExcepcion.revisarSiExisteProfesor(encontrado);
		} catch (TeacherNotFoundException e) {
			System.out.println("Error de docente: " + e.getMessage());
			return 1;
		}

		if (!esProfesorValido(newData)) {
			return 2;
		}

		Optional<Profesor> porCorreo = profesorRepo.findByCorreo(newData.getCorreo().trim());

		try {
			LanzadorDeExcepcion.revisarSiRegistroYaExiste(
					porCorreo.isPresent() && porCorreo.get().getCodigo() != id.intValue(),
					"correo " + newData.getCorreo().trim());
		} catch (RecordAlreadyExistsException e) {
			System.out.println("Error de correo: " + e.getMessage());
			return 1;
		}

		Profesor temp = encontrado.get();
		temp.setNombre(newData.getNombre().trim());
		temp.setCorreo(newData.getCorreo().trim());
		temp.setFacultad(newData.getFacultad().trim());
		temp.setTituloPregrado(newData.getTituloPregrado());
		temp.setActaGradoPregrado(newData.getActaGradoPregrado());
		temp.setTituloPosgrado(newData.getTituloPosgrado());
		temp.setNivelPosgrado(newData.getNivelPosgrado());
		temp.setTituloExtranjero(newData.isTituloExtranjero());
		temp.setResolucionConvalidacion(newData.getResolucionConvalidacion());
		temp.setHorasFormacionPedagogica(newData.getHorasFormacionPedagogica());
		temp.setMaximoNivelFormacion(newData.getMaximoNivelFormacion());
		temp.setTipoDocumento(newData.getTipoDocumento());
		temp.setNumeroDocumento(newData.getNumeroDocumento());
		temp.setTarjetaProfesional(newData.getTarjetaProfesional());
		temp.setRut(newData.getRut());
		temp.setEps(newData.getEps());
		temp.setFondoPensiones(newData.getFondoPensiones());
		temp.setTieneVisaTrabajo(newData.isTieneVisaTrabajo());
		temp.setEspecialidades(newData.getEspecialidades() != null
				? new ArrayList<>(newData.getEspecialidades())
				: new ArrayList<>());
		temp.setAniosExperiencia(newData.getAniosExperiencia());
		temp.setPublicaciones(newData.getPublicaciones() != null
				? new ArrayList<>(newData.getPublicaciones())
				: new ArrayList<>());
		temp.setNivelIngles(newData.getNivelIngles());
		temp.setExamenIngles(newData.getExamenIngles());
		temp.setPuntajeIngles(newData.getPuntajeIngles());
		temp.setHojaVidaActualizada(newData.isHojaVidaActualizada());
		profesorRepo.save(temp);
		return 0;
	}

	/**
	 * Metodo que cuenta la cantidad total de docentes registrados.
	 * @return Retorna la cantidad total de docentes.
	 */
	@Override
	public long count() {

		return profesorRepo.count();
	}

	/**
	 * Metodo que verifica si un docente existe mediante su identificador.
	 * @param id Identificador (codigo) del docente a verificar.
	 * @return Retorna true si el docente existe o false en caso contrario.
	 */
	@Override
	public boolean exist(Integer id) {

		return id != null && profesorRepo.existsById(id);
	}

	/**
	 * Metodo que busca un docente mediante su codigo unico.
	 * @param id Codigo del docente.
	 * @return Retorna el docente en formato DTO o null si no se encuentra.
	 */
	public ProfesorDTO getById(Integer id) {

		if (id == null) {
			return null;
		}

		return profesorRepo.findById(id).map((entidad) -> mapper.map(entidad, ProfesorDTO.class)).orElse(null);
	}

	/**
	 * Metodo que busca un docente mediante su correo electronico.
	 * @param correo Correo institucional del docente.
	 * @return Retorna el docente en formato DTO o null si no se encuentra.
	 */
	public ProfesorDTO getByCorreo(String correo) {

		if (correo == null || correo.trim().isEmpty()) {
			return null;
		}

		return profesorRepo.findByCorreo(correo.trim()).map((entidad) -> mapper.map(entidad, ProfesorDTO.class))
				.orElse(null);
	}

	/**
	 * Metodo que obtiene todos los docentes adscritos a una facultad especifica.
	 * @param facultad Nombre de la facultad.
	 * @return Retorna una lista de docentes en formato DTO de la facultad.
	 */
	public List<ProfesorDTO> getByFacultad(String facultad) {

		List<ProfesorDTO> dtoList = new ArrayList<>();

		if (facultad == null || facultad.trim().isEmpty()) {
			return dtoList;
		}

		List<Profesor> entityList = profesorRepo.findByFacultad(facultad.trim());

		entityList.forEach((entidad) -> {

			ProfesorDTO dto = mapper.map(entidad, ProfesorDTO.class);
			dtoList.add(dto);
		});

		return dtoList;
	}

	/**
	 * Metodo que obtiene los docentes con una cantidad de anios de experiencia igual
	 * o superior a la indicada (RF03).
	 * @param anios Anios minimos de experiencia.
	 * @return Retorna una lista de docentes en formato DTO que cumplen el criterio.
	 */
	public List<ProfesorDTO> getByAniosExperienciaMinima(int anios) {

		List<Profesor> entityList = profesorRepo.findByAniosExperienciaGreaterThanEqual(anios);
		List<ProfesorDTO> dtoList = new ArrayList<>();

		entityList.forEach((entidad) -> {

			ProfesorDTO dto = mapper.map(entidad, ProfesorDTO.class);
			dtoList.add(dto);
		});

		return dtoList;
	}

	/**
	 * Metodo que obtiene los docentes filtrados por su nivel de ingles.
	 * @param nivelIngles Nivel de ingles (ej. A2, B1, B2, C1).
	 * @return Retorna una lista de docentes en formato DTO con ese nivel.
	 */
	public List<ProfesorDTO> getByNivelIngles(String nivelIngles) {

		List<ProfesorDTO> dtoList = new ArrayList<>();

		if (nivelIngles == null || nivelIngles.trim().isEmpty()) {
			return dtoList;
		}

		List<Profesor> entityList = profesorRepo.findByNivelIngles(nivelIngles.trim());

		entityList.forEach((entidad) -> {

			ProfesorDTO dto = mapper.map(entidad, ProfesorDTO.class);
			dtoList.add(dto);
		});

		return dtoList;
	}

	/**
	 * Metodo que obtiene los docentes segun si tienen su hoja de vida actualizada o no.
	 * @param hojaVidaActualizada true para hojas de vida actualizadas, false en caso contrario.
	 * @return Retorna una lista de docentes en formato DTO filtrada.
	 */
	public List<ProfesorDTO> getByHojaVidaActualizada(boolean hojaVidaActualizada) {

		List<Profesor> entityList = profesorRepo.findByHojaVidaActualizada(hojaVidaActualizada);
		List<ProfesorDTO> dtoList = new ArrayList<>();

		entityList.forEach((entidad) -> {

			ProfesorDTO dto = mapper.map(entidad, ProfesorDTO.class);
			dtoList.add(dto);
		});

		return dtoList;
	}

	/**
	 * Metodo que obtiene los docentes que cuentan con una especialidad especifica y
	 * pueden orientar cursos afines (RF17).
	 * @param especialidad Nombre de la especialidad o area de conocimiento.
	 * @return Retorna una lista de docentes en formato DTO que poseen la especialidad.
	 */
	public List<ProfesorDTO> getByEspecialidad(String especialidad) {

		List<ProfesorDTO> dtoList = new ArrayList<>();

		if (especialidad == null || especialidad.trim().isEmpty()) {
			return dtoList;
		}

		String buscada = especialidad.trim().toLowerCase();
		List<Profesor> entityList = (List<Profesor>) profesorRepo.findAll();

		entityList.forEach((entidad) -> {

			if (entidad.getEspecialidades() != null) {

				for (String esp : entidad.getEspecialidades()) {

					if (esp != null && esp.trim().toLowerCase().contains(buscada)) {
						dtoList.add(mapper.map(entidad, ProfesorDTO.class));
						break;
					}
				}
			}
		});

		return dtoList;
	}

	/**
	 * Metodo que verifica si un docente especifico tiene una especialidad que le
	 * permita orientar una materia (RF17).
	 * @param idDocente Codigo del docente.
	 * @param especialidad Especialidad o nombre de la materia a verificar.
	 * @return Retorna true si el docente posee la especialidad o false en caso contrario.
	 */
	public boolean puedeOrientarEspecialidad(Integer idDocente, String especialidad) {

		if (idDocente == null || especialidad == null || especialidad.trim().isEmpty()) {
			return false;
		}

		Optional<Profesor> encontrado = profesorRepo.findById(idDocente);

		try {
			LanzadorDeExcepcion.revisarSiExisteProfesor(encontrado);
		} catch (TeacherNotFoundException e) {
			System.out.println("Error de docente: " + e.getMessage());
			return false;
		}

		if (encontrado.get().getEspecialidades() == null) {
			return false;
		}

		String buscada = especialidad.trim().toLowerCase();

		for (String esp : encontrado.get().getEspecialidades()) {

			if (esp != null && esp.trim().toLowerCase().contains(buscada)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Metodo que valida que los datos obligatorios del docente sean consistentes (CU03 / RF03).
	 * @param data DTO del docente a validar.
	 * @return Retorna true si los datos son validos o false en caso contrario.
	 */
	private boolean esProfesorValido(ProfesorDTO data) {

		try {
			LanzadorDeExcepcion.verificarDatosNoNulos(data);
		} catch (NullDataException e) {
			System.out.println("Error de docente: " + e.getMessage());
			return false;
		}

		try {
			LanzadorDeExcepcion.verificarCampoVacio(data.getNombre());
		} catch (EmptyFieldException e) {
			System.out.println("Error de nombre: " + e.getMessage());
			return false;
		}

		try {
			LanzadorDeExcepcion.verificarCampoVacio(data.getCorreo());
		} catch (EmptyFieldException e) {
			System.out.println("Error de correo: " + e.getMessage());
			return false;
		}

		try {
			LanzadorDeExcepcion.verificarCampoVacio(data.getFacultad());
		} catch (EmptyFieldException e) {
			System.out.println("Error de facultad: " + e.getMessage());
			return false;
		}

		try {
			LanzadorDeExcepcion.verificarNoNegativo(data.getAniosExperiencia());
		} catch (InvalidNumberException e) {
			System.out.println("Error de anios de experiencia: " + e.getMessage());
			return false;
		}

		try {
			LanzadorDeExcepcion.verificarNoNegativo(data.getHorasFormacionPedagogica());
		} catch (InvalidNumberException e) {
			System.out.println("Error de horas de formacion pedagogica: " + e.getMessage());
			return false;
		}

		try {
			LanzadorDeExcepcion.verificarNoNegativo(data.getPuntajeIngles());
		} catch (InvalidNumberException e) {
			System.out.println("Error de puntaje de ingles: " + e.getMessage());
			return false;
		}

		return true;
	}
}