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

import co.edu.unbosque.sdgh.dto.EstudianteDTO;
import co.edu.unbosque.sdgh.entity.Estudiante;
import co.edu.unbosque.sdgh.exception.EmptyFieldException;
import co.edu.unbosque.sdgh.exception.InvalidNumberException;
import co.edu.unbosque.sdgh.exception.LanzadorDeExcepcion;
import co.edu.unbosque.sdgh.exception.NullDataException;
import co.edu.unbosque.sdgh.exception.RecordAlreadyExistsException;
import co.edu.unbosque.sdgh.exception.StudentNotFoundException;
import co.edu.unbosque.sdgh.repository.EstudianteRepository;

/**
 * Servicio encargado de gestionar las operaciones CRUD de los estudiantes del
 * programa (CU08 - RF14).
 */
@Service
public class EstudianteService implements CRUDOperation<EstudianteDTO> {

	/**
	 * Repositorio encargado de las operaciones de persistencia de estudiantes.
	 */
	@Autowired
	private EstudianteRepository estudianteRepo;

	/**
	 * Mapper utilizado para convertir entidades en DTO y viceversa.
	 */
	@Autowired
	private ModelMapper mapper;

	/**
	 * Constructor vacio de la clase EstudianteService.
	 */
	public EstudianteService() {
	}

	/**
	 * Metodo que crea un nuevo estudiante validando unicidad de codigo, correo y
	 * documento, asi como los datos obligatorios (RF14).
	 * @param newData DTO con la informacion del estudiante a crear.
	 * @return Retorna 0 si el estudiante fue creado correctamente, 1 si el
	 *         codigo, el correo o el numero de documento ya existen o 2 si los
	 *         datos son nulos, estan incompletos o fuera de rango.
	 */
	@Override
	public int create(EstudianteDTO newData) {

		if (!esEstudianteValido(newData)) {
			return 2;
		}

		try {
			LanzadorDeExcepcion.verificarPositivo(newData.getCodigo());
		} catch (InvalidNumberException e) {
			System.out.println("Error de codigo: " + e.getMessage());
			return 2;
		}

		try {
			LanzadorDeExcepcion.revisarSiRegistroYaExiste(estudianteRepo.existsById(newData.getCodigo()),
					"codigo " + newData.getCodigo());
		} catch (RecordAlreadyExistsException e) {
			System.out.println("Error de codigo: " + e.getMessage());
			return 1;
		}

		try {
			LanzadorDeExcepcion.revisarSiRegistroYaExiste(estudianteRepo.existsByCorreo(newData.getCorreo().trim()),
					"correo " + newData.getCorreo().trim());
		} catch (RecordAlreadyExistsException e) {
			System.out.println("Error de correo: " + e.getMessage());
			return 1;
		}

		if (newData.getNumeroDocumento() != null && !newData.getNumeroDocumento().trim().isEmpty()) {

			try {
				LanzadorDeExcepcion.revisarSiRegistroYaExiste(
						estudianteRepo.existsByNumeroDocumento(newData.getNumeroDocumento().trim()),
						"documento " + newData.getNumeroDocumento().trim());
			} catch (RecordAlreadyExistsException e) {
				System.out.println("Error de documento: " + e.getMessage());
				return 1;
			}
		}

		Estudiante entity = mapper.map(newData, Estudiante.class);
		entity.setNombre(newData.getNombre().trim());
		entity.setCorreo(newData.getCorreo().trim());
		entity.setFacultad(newData.getFacultad().trim());
		estudianteRepo.save(entity);
		return 0;
	}

	/**
	 * Metodo que obtiene todos los estudiantes registrados.
	 * @return Retorna una lista con todos los estudiantes en formato DTO.
	 */
	@Override
	public List<EstudianteDTO> getAll() {

		List<Estudiante> entityList = (List<Estudiante>) estudianteRepo.findAll();
		List<EstudianteDTO> dtoList = new ArrayList<>();

		entityList.forEach((entidad) -> {

			EstudianteDTO dto = mapper.map(entidad, EstudianteDTO.class);
			dtoList.add(dto);
		});

		return dtoList;
	}

	/**
	 * Metodo que elimina un estudiante mediante su identificador.
	 * @param id Identificador (codigo) del estudiante a eliminar.
	 * @return Retorna 0 si el estudiante fue eliminado correctamente o 1 si
	 *         el estudiante no existe o el identificador es nulo.
	 */
	@Override
	public int deleteById(Integer id) {

		if (id == null) {
			return 1;
		}

		Optional<Estudiante> encontrado = estudianteRepo.findById(id);

		try {
			LanzadorDeExcepcion.revisarSiExisteEstudiante(encontrado);
		} catch (StudentNotFoundException e) {
			System.out.println("Error de estudiante: " + e.getMessage());
			return 1;
		}

		estudianteRepo.delete(encontrado.get());
		return 0;
	}

	/**
	 * Metodo que actualiza un estudiante mediante su identificador.
	 * @param id Identificador (codigo) del estudiante a actualizar.
	 * @param newData DTO con la nueva informacion del estudiante.
	 * @return Retorna 0 si la actualizacion fue exitosa, 1 si el estudiante
	 *         no existe o el correo ya pertenece a otro estudiante, o 2 si
	 *         los datos nuevos son invalidos o estan fuera de rango.
	 */
	@Override
	public int updateById(Integer id, EstudianteDTO newData) {

		if (id == null) {
			return 1;
		}

		Optional<Estudiante> encontrado = estudianteRepo.findById(id);

		try {
			LanzadorDeExcepcion.revisarSiExisteEstudiante(encontrado);
		} catch (StudentNotFoundException e) {
			System.out.println("Error de estudiante: " + e.getMessage());
			return 1;
		}

		if (!esEstudianteValido(newData)) {
			return 2;
		}

		Optional<Estudiante> porCorreo = estudianteRepo.findByCorreo(newData.getCorreo().trim());

		try {
			LanzadorDeExcepcion.revisarSiRegistroYaExiste(
					porCorreo.isPresent() && porCorreo.get().getCodigo() != id.intValue(),
					"correo " + newData.getCorreo().trim());
		} catch (RecordAlreadyExistsException e) {
			System.out.println("Error de correo: " + e.getMessage());
			return 1;
		}

		Estudiante temp = encontrado.get();
		temp.setNombre(newData.getNombre().trim());
		temp.setCorreo(newData.getCorreo().trim());
		temp.setFacultad(newData.getFacultad().trim());
		temp.setSemestreActual(newData.getSemestreActual());
		temp.setEstado(newData.isEstado());
		temp.setProgramaAcademico(newData.getProgramaAcademico());
		temp.setModalidad(newData.getModalidad());
		temp.setPromedioAcumulado(newData.getPromedioAcumulado());
		temp.setTipoDocumento(newData.getTipoDocumento());
		temp.setNumeroDocumento(newData.getNumeroDocumento());
		temp.setResultadoSaber11(newData.getResultadoSaber11());
		temp.setTieneTituloBachiller(newData.isTieneTituloBachiller());
		temp.setDiplomaOActaBachiller(newData.getDiplomaOActaBachiller());
		temp.setAfiliacionEPS(newData.getAfiliacionEPS());
		temp.setEsquemaVacunacionCompleto(newData.isEsquemaVacunacionCompleto());
		temp.setEsExtranjero(newData.isEsExtranjero());
		temp.setTieneVisaVigente(newData.isTieneVisaVigente());
		temp.setResolucionConvalidacion(newData.getResolucionConvalidacion());
		estudianteRepo.save(temp);
		return 0;
	}

	/**
	 * Metodo que cuenta la cantidad total de estudiantes registrados.
	 * @return Retorna la cantidad total de estudiantes.
	 */
	@Override
	public long count() {

		return estudianteRepo.count();
	}

	/**
	 * Metodo que verifica si un estudiante existe mediante su identificador.
	 * @param id Identificador (codigo) del estudiante a verificar.
	 * @return Retorna true si el estudiante existe o false en caso contrario.
	 */
	@Override
	public boolean exist(Integer id) {

		return id != null && estudianteRepo.existsById(id);
	}

	/**
	 * Metodo que busca un estudiante mediante su codigo unico.
	 * @param id Codigo del estudiante.
	 * @return Retorna el estudiante en formato DTO o null si no se encuentra.
	 */
	public EstudianteDTO getById(Integer id) {

		if (id == null) {
			return null;
		}

		return estudianteRepo.findById(id).map((entidad) -> mapper.map(entidad, EstudianteDTO.class)).orElse(null);
	}

	/**
	 * Metodo que busca un estudiante mediante su correo electronico.
	 * @param correo Correo institucional del estudiante.
	 * @return Retorna el estudiante en formato DTO o null si no se encuentra.
	 */
	public EstudianteDTO getByCorreo(String correo) {

		if (correo == null || correo.trim().isEmpty()) {
			return null;
		}

		return estudianteRepo.findByCorreo(correo.trim()).map((entidad) -> mapper.map(entidad, EstudianteDTO.class))
				.orElse(null);
	}

	/**
	 * Metodo que obtiene todos los estudiantes pertenecientes a una facultad especifica.
	 * @param facultad Nombre de la facultad.
	 * @return Retorna una lista de estudiantes en formato DTO de la facultad.
	 */
	public List<EstudianteDTO> getByFacultad(String facultad) {

		List<EstudianteDTO> dtoList = new ArrayList<>();

		if (facultad == null || facultad.trim().isEmpty()) {
			return dtoList;
		}

		List<Estudiante> entityList = estudianteRepo.findByFacultad(facultad.trim());

		entityList.forEach((entidad) -> {

			EstudianteDTO dto = mapper.map(entidad, EstudianteDTO.class);
			dtoList.add(dto);
		});

		return dtoList;
	}

	/**
	 * Metodo que obtiene todos los estudiantes inscritos en un programa academico especifico.
	 * @param programaAcademico Nombre del programa academico.
	 * @return Retorna una lista de estudiantes en formato DTO del programa.
	 */
	public List<EstudianteDTO> getByProgramaAcademico(String programaAcademico) {

		List<EstudianteDTO> dtoList = new ArrayList<>();

		if (programaAcademico == null || programaAcademico.trim().isEmpty()) {
			return dtoList;
		}

		List<Estudiante> entityList = estudianteRepo.findByProgramaAcademico(programaAcademico.trim());

		entityList.forEach((entidad) -> {

			EstudianteDTO dto = mapper.map(entidad, EstudianteDTO.class);
			dtoList.add(dto);
		});

		return dtoList;
	}

	/**
	 * Metodo que obtiene todos los estudiantes que cursan actualmente un semestre especifico (1 a 9).
	 * @param semestreActual Numero del semestre actual.
	 * @return Retorna una lista de estudiantes en formato DTO del semestre indicado.
	 */
	public List<EstudianteDTO> getBySemestreActual(int semestreActual) {

		List<Estudiante> entityList = estudianteRepo.findBySemestreActual(semestreActual);
		List<EstudianteDTO> dtoList = new ArrayList<>();

		entityList.forEach((entidad) -> {

			EstudianteDTO dto = mapper.map(entidad, EstudianteDTO.class);
			dtoList.add(dto);
		});

		return dtoList;
	}

	/**
	 * Metodo que obtiene los estudiantes filtrados por su estado academico (activo o inactivo).
	 * @param estado true para estudiantes activos, false para inactivos.
	 * @return Retorna una lista de estudiantes en formato DTO filtrada.
	 */
	public List<EstudianteDTO> getByEstado(boolean estado) {

		List<Estudiante> entityList = estudianteRepo.findByEstado(estado);
		List<EstudianteDTO> dtoList = new ArrayList<>();

		entityList.forEach((entidad) -> {

			EstudianteDTO dto = mapper.map(entidad, EstudianteDTO.class);
			dtoList.add(dto);
		});

		return dtoList;
	}

	/**
	 * Metodo que valida los datos obligatorios de un estudiante segun las reglas del
	 * plan de estudios de Ingenieria de Sistemas (SNIES 4952: 9 semestres).
	 * @param data DTO del estudiante a validar.
	 * @return Retorna true si los datos son validos o false en caso contrario.
	 */
	private boolean esEstudianteValido(EstudianteDTO data) {

		try {
			LanzadorDeExcepcion.verificarDatosNoNulos(data);
		} catch (NullDataException e) {
			System.out.println("Error de estudiante: " + e.getMessage());
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
			LanzadorDeExcepcion.verificarRango(data.getSemestreActual(), 1, 9);
		} catch (InvalidNumberException e) {
			System.out.println("Error de semestre: " + e.getMessage());
			return false;
		}

		try {
			LanzadorDeExcepcion.verificarRango(data.getPromedioAcumulado(), 0.0, 5.0);
		} catch (InvalidNumberException e) {
			System.out.println("Error de promedio: " + e.getMessage());
			return false;
		}

		return true;
	}
}