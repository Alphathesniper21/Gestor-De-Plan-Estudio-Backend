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
import co.edu.unbosque.sdgh.dto.PlanDeEstudiosDTO;
import co.edu.unbosque.sdgh.entity.Materia;
import co.edu.unbosque.sdgh.entity.PlanDeEstudios;
import co.edu.unbosque.sdgh.exception.EmptyFieldException;
import co.edu.unbosque.sdgh.exception.InvalidNumberException;
import co.edu.unbosque.sdgh.exception.LanzadorDeExcepcion;
import co.edu.unbosque.sdgh.exception.NullDataException;
import co.edu.unbosque.sdgh.exception.RecordAlreadyExistsException;
import co.edu.unbosque.sdgh.exception.StudyPlanNotFoundException;
import co.edu.unbosque.sdgh.repository.PlanDeEstudiosRepository;

/**
 * Servicio encargado de gestionar las operaciones CRUD del plan de estudios del
 * programa (CU02 - RF01, RF02).
 */
@Service
public class PlanDeEstudiosService implements CRUDOperation<PlanDeEstudiosDTO> {

	/**
	 * Repositorio encargado de las operaciones de persistencia de planes de estudio.
	 */
	@Autowired
	private PlanDeEstudiosRepository planRepo;

	/**
	 * Mapper utilizado para convertir entidades en DTO y viceversa.
	 */
	@Autowired
	private ModelMapper mapper;

	/**
	 * Constructor vacio de la clase PlanDeEstudiosService.
	 */
	public PlanDeEstudiosService() {
	}

	/**
	 * Metodo que crea un nuevo plan de estudios validando unicidad de identificador
	 * y de nombre de programa (RF02).
	 * @param newData DTO con la informacion del plan de estudios a crear.
	 * @return Retorna 0 si el plan fue creado correctamente, 1 si el
	 *         identificador o el nombre de programa ya existen o 2 si los
	 *         datos son nulos o estan incompletos.
	 */
	@Override
	public int create(PlanDeEstudiosDTO newData) {

		if (!esPlanValido(newData)) {
			return 2;
		}

		try {
			LanzadorDeExcepcion.verificarCampoVacio(newData.getId());
		} catch (EmptyFieldException e) {
			System.out.println("Error de identificador: " + e.getMessage());
			return 2;
		}

		try {
			LanzadorDeExcepcion.revisarSiRegistroYaExiste(planRepo.existsById(newData.getId().trim()),
					"identificador " + newData.getId().trim());
		} catch (RecordAlreadyExistsException e) {
			System.out.println("Error de identificador: " + e.getMessage());
			return 1;
		}

		try {
			LanzadorDeExcepcion.revisarSiRegistroYaExiste(
					planRepo.existsByNombrePrograma(newData.getNombrePrograma().trim()),
					"programa " + newData.getNombrePrograma().trim());
		} catch (RecordAlreadyExistsException e) {
			System.out.println("Error de programa: " + e.getMessage());
			return 1;
		}

		PlanDeEstudios entity = mapper.map(newData, PlanDeEstudios.class);
		entity.setNombrePrograma(newData.getNombrePrograma().trim());
		entity.setVersion(newData.getVersion() != null ? newData.getVersion().trim() : null);
		entity.setEtapas(entity.getEtapas() != null ? entity.getEtapas() : new ArrayList<>());
		entity.setEspaciosConocimiento(
				entity.getEspaciosConocimiento() != null ? entity.getEspaciosConocimiento() : new ArrayList<>());
		entity.setMaterias(entity.getMaterias() != null ? entity.getMaterias() : new ArrayList<>());
		planRepo.save(entity);
		return 0;
	}

	/**
	 * Metodo que obtiene todos los planes de estudio registrados.
	 * @return Retorna una lista con todos los planes de estudio en formato DTO.
	 */
	@Override
	public List<PlanDeEstudiosDTO> getAll() {

		List<PlanDeEstudios> entityList = (List<PlanDeEstudios>) planRepo.findAll();
		List<PlanDeEstudiosDTO> dtoList = new ArrayList<>();

		entityList.forEach((entidad) -> {

			PlanDeEstudiosDTO dto = mapper.map(entidad, PlanDeEstudiosDTO.class);
			dtoList.add(dto);
		});

		return dtoList;
	}

	/**
	 * Metodo que elimina un plan de estudios mediante su identificador recibido
	 * como Integer (delegando a String).
	 * @param id Identificador numerico del plan de estudios a eliminar.
	 * @return Retorna 0 si el plan fue eliminado correctamente o 1 si el
	 *         plan no existe o el identificador es nulo.
	 */
	@Override
	public int deleteById(Integer id) {

		if (id == null) {
			return 1;
		}

		return deleteByStringId(String.valueOf(id));
	}

	/**
	 * Metodo que elimina un plan de estudios mediante su identificador de tipo String.
	 * @param id Identificador del plan de estudios (ej. SNIES 4952).
	 * @return Retorna 0 si el plan fue eliminado correctamente o 1 si el
	 *         plan no existe o el identificador es nulo o vacio.
	 */
	public int deleteByStringId(String id) {

		try {
			LanzadorDeExcepcion.verificarCampoVacio(id);
		} catch (EmptyFieldException e) {
			System.out.println("Error de identificador: " + e.getMessage());
			return 1;
		}

		Optional<PlanDeEstudios> encontrado = planRepo.findById(id.trim());

		try {
			LanzadorDeExcepcion.revisarSiExistePlan(encontrado);
		} catch (StudyPlanNotFoundException e) {
			System.out.println("Error de plan de estudios: " + e.getMessage());
			return 1;
		}

		planRepo.delete(encontrado.get());
		return 0;
	}

	/**
	 * Metodo que actualiza un plan de estudios mediante su identificador recibido
	 * como Integer (delegando a String).
	 * @param id Identificador del plan de estudios a actualizar.
	 * @param newData DTO con la nueva informacion del plan de estudios.
	 * @return Retorna 0 si la actualizacion fue exitosa, 1 si el plan no
	 *         existe o hay conflicto con el nombre del programa, o 2 si los
	 *         datos nuevos son invalidos.
	 */
	@Override
	public int updateById(Integer id, PlanDeEstudiosDTO newData) {

		if (id == null) {
			return 1;
		}

		return updateByStringId(String.valueOf(id), newData);
	}

	/**
	 * Metodo que actualiza un plan de estudios mediante su identificador de tipo String.
	 * @param id Identificador del plan de estudios a actualizar.
	 * @param newData DTO con la nueva informacion del plan de estudios.
	 * @return Retorna 0 si la actualizacion fue exitosa, 1 si el plan no
	 *         existe o el nombre de programa pertenece a otro plan, o 2 si
	 *         los datos nuevos son nulos o invalidos.
	 */
	public int updateByStringId(String id, PlanDeEstudiosDTO newData) {

		try {
			LanzadorDeExcepcion.verificarCampoVacio(id);
		} catch (EmptyFieldException e) {
			System.out.println("Error de identificador: " + e.getMessage());
			return 1;
		}

		Optional<PlanDeEstudios> encontrado = planRepo.findById(id.trim());

		try {
			LanzadorDeExcepcion.revisarSiExistePlan(encontrado);
		} catch (StudyPlanNotFoundException e) {
			System.out.println("Error de plan de estudios: " + e.getMessage());
			return 1;
		}

		if (!esPlanValido(newData)) {
			return 2;
		}

		Optional<PlanDeEstudios> porNombre = planRepo.findByNombrePrograma(newData.getNombrePrograma().trim());

		try {
			LanzadorDeExcepcion.revisarSiRegistroYaExiste(
					porNombre.isPresent() && !porNombre.get().getId().equals(id.trim()),
					"programa " + newData.getNombrePrograma().trim());
		} catch (RecordAlreadyExistsException e) {
			System.out.println("Error de programa: " + e.getMessage());
			return 1;
		}

		PlanDeEstudios nuevo = mapper.map(newData, PlanDeEstudios.class);

		PlanDeEstudios temp = encontrado.get();
		temp.setNombrePrograma(newData.getNombrePrograma().trim());
		temp.setVersion(newData.getVersion() != null ? newData.getVersion().trim() : null);
		temp.setTotalCreditos(newData.getTotalCreditos());
		temp.setNumeroSemestres(newData.getNumeroSemestres());
		temp.setEtapas(nuevo.getEtapas() != null ? nuevo.getEtapas() : new ArrayList<>());
		temp.setEspaciosConocimiento(
				nuevo.getEspaciosConocimiento() != null ? nuevo.getEspaciosConocimiento() : new ArrayList<>());
		temp.setMaterias(nuevo.getMaterias() != null ? nuevo.getMaterias() : new ArrayList<>());
		planRepo.save(temp);
		return 0;
	}

	/**
	 * Metodo que asocia una materia a un plan de estudios existente validando que
	 * no este duplicada (CU02 / RF02).
	 * @param planId Identificador del plan de estudios.
	 * @param materia DTO de la materia a asociar.
	 * @return Retorna 0 si la materia fue asociada correctamente, 1 si el plan
	 *         no existe o la materia ya estaba asociada al plan, o 2 si los
	 *         datos de la materia son nulos o invalidos.
	 */
	public int agregarMateriaAPlan(String planId, MateriaDTO materia) {

		try {
			LanzadorDeExcepcion.verificarCampoVacio(planId);
		} catch (EmptyFieldException e) {
			System.out.println("Error de identificador: " + e.getMessage());
			return 1;
		}

		try {
			LanzadorDeExcepcion.verificarDatosNoNulos(materia);
		} catch (NullDataException e) {
			System.out.println("Error de materia: " + e.getMessage());
			return 2;
		}

		try {
			LanzadorDeExcepcion.verificarPositivo(materia.getCodigo());
		} catch (InvalidNumberException e) {
			System.out.println("Error de codigo de materia: " + e.getMessage());
			return 2;
		}

		try {
			LanzadorDeExcepcion.verificarCampoVacio(materia.getNombre());
		} catch (EmptyFieldException e) {
			System.out.println("Error de nombre de materia: " + e.getMessage());
			return 2;
		}

		Optional<PlanDeEstudios> encontrado = planRepo.findById(planId.trim());

		try {
			LanzadorDeExcepcion.revisarSiExistePlan(encontrado);
		} catch (StudyPlanNotFoundException e) {
			System.out.println("Error de plan de estudios: " + e.getMessage());
			return 1;
		}

		PlanDeEstudios plan = encontrado.get();
		List<Materia> materias = plan.getMaterias() != null ? plan.getMaterias() : new ArrayList<>();
		boolean duplicada = false;

		for (Materia existente : materias) {

			if (existente.getCodigo() == materia.getCodigo()) {
				duplicada = true;
			}
		}

		try {
			LanzadorDeExcepcion.revisarSiRegistroYaExiste(duplicada, "materia " + materia.getCodigo());
		} catch (RecordAlreadyExistsException e) {
			System.out.println("Error de materia: " + e.getMessage());
			return 1;
		}

		Materia nueva = mapper.map(materia, Materia.class);
		nueva.setNombre(materia.getNombre().trim());
		materias.add(nueva);
		plan.setMaterias(materias);
		planRepo.save(plan);
		return 0;
	}

	/**
	 * Metodo que cuenta la cantidad total de planes de estudio registrados.
	 * @return Retorna la cantidad total de planes de estudio.
	 */
	@Override
	public long count() {

		return planRepo.count();
	}

	/**
	 * Metodo que verifica si un plan de estudios existe mediante su identificador
	 * recibido como Integer.
	 * @param id Identificador del plan a verificar.
	 * @return Retorna true si el plan existe o false en caso contrario.
	 */
	@Override
	public boolean exist(Integer id) {

		if (id == null) {
			return false;
		}

		return existByStringId(String.valueOf(id));
	}

	/**
	 * Metodo que verifica si un plan de estudios existe mediante su identificador
	 * de tipo String.
	 * @param id Identificador del plan de estudios a verificar.
	 * @return Retorna true si el plan existe o false en caso contrario.
	 */
	public boolean existByStringId(String id) {

		return id != null && !id.trim().isEmpty() && planRepo.existsById(id.trim());
	}

	/**
	 * Metodo que busca un plan de estudios mediante su identificador de tipo String.
	 * @param id Identificador del plan de estudios.
	 * @return Retorna el plan en formato DTO o null si no se encuentra.
	 */
	public PlanDeEstudiosDTO getById(String id) {

		if (id == null || id.trim().isEmpty()) {
			return null;
		}

		return planRepo.findById(id.trim()).map((entidad) -> mapper.map(entidad, PlanDeEstudiosDTO.class))
				.orElse(null);
	}

	/**
	 * Metodo que busca un plan de estudios mediante el nombre del programa academico.
	 * @param nombrePrograma Nombre del programa.
	 * @return Retorna el plan en formato DTO o null si no se encuentra.
	 */
	public PlanDeEstudiosDTO getByNombrePrograma(String nombrePrograma) {

		if (nombrePrograma == null || nombrePrograma.trim().isEmpty()) {
			return null;
		}

		return planRepo.findByNombrePrograma(nombrePrograma.trim())
				.map((entidad) -> mapper.map(entidad, PlanDeEstudiosDTO.class)).orElse(null);
	}

	/**
	 * Metodo que busca un plan de estudios mediante el nombre del programa y su version.
	 * @param nombrePrograma Nombre del programa.
	 * @param version Version del plan (ej. 2025).
	 * @return Retorna el plan en formato DTO o null si no se encuentra.
	 */
	public PlanDeEstudiosDTO getByNombreProgramaAndVersion(String nombrePrograma, String version) {

		if (nombrePrograma == null || nombrePrograma.trim().isEmpty() || version == null
				|| version.trim().isEmpty()) {
			return null;
		}

		return planRepo.findByNombreProgramaAndVersion(nombrePrograma.trim(), version.trim())
				.map((entidad) -> mapper.map(entidad, PlanDeEstudiosDTO.class)).orElse(null);
	}

	/**
	 * Metodo que valida los datos obligatorios de un plan de estudios.
	 * @param data DTO del plan de estudios a validar.
	 * @return Retorna true si los datos son validos o false en caso contrario.
	 */
	private boolean esPlanValido(PlanDeEstudiosDTO data) {

		try {
			LanzadorDeExcepcion.verificarDatosNoNulos(data);
		} catch (NullDataException e) {
			System.out.println("Error de plan de estudios: " + e.getMessage());
			return false;
		}

		try {
			LanzadorDeExcepcion.verificarCampoVacio(data.getNombrePrograma());
		} catch (EmptyFieldException e) {
			System.out.println("Error de programa: " + e.getMessage());
			return false;
		}

		try {
			LanzadorDeExcepcion.verificarPositivo(data.getTotalCreditos());
		} catch (InvalidNumberException e) {
			System.out.println("Error de creditos: " + e.getMessage());
			return false;
		}

		try {
			LanzadorDeExcepcion.verificarPositivo(data.getNumeroSemestres());
		} catch (InvalidNumberException e) {
			System.out.println("Error de semestres: " + e.getMessage());
			return false;
		}

		return true;
	}
}