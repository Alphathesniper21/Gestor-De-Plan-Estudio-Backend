package co.edu.unbosque.sdgh.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.edu.unbosque.sdgh.dto.EstudianteDTO;
import co.edu.unbosque.sdgh.entity.Estudiante;
import co.edu.unbosque.sdgh.repository.EstudianteRepository;

/**
 * Servicio que gestiona la logica de negocio para los estudiantes del programa
 * (CU08 - RF14).
 */
@Service
public class EstudianteService implements CRUDOperation<EstudianteDTO> {

	@Autowired
	private EstudianteRepository estudianteRepo;

	public EstudianteService() {
	}

	/**
	 * Registra un nuevo estudiante validando unicidad de codigo, correo y documento,
	 * asi como los datos obligatorios (RF14).
	 *
	 * @param data DTO con los datos del estudiante a registrar.
	 * @return 0 si se registro con exito;
	 *         1 si el codigo, correo o numero de documento ya existe (duplicado);
	 *         2 si los datos son nulos, incompletos o fuera de rango.
	 */
	@Override
	public int create(EstudianteDTO data) {
		if (!esEstudianteValido(data)) {
			return 2;
		}
		if (estudianteRepo.existsById(data.getCodigo())) {
			return 1;
		}
		if (estudianteRepo.existsByCorreo(data.getCorreo().trim())) {
			return 1;
		}
		if (data.getNumeroDocumento() != null && !data.getNumeroDocumento().trim().isEmpty()
				&& estudianteRepo.existsByNumeroDocumento(data.getNumeroDocumento().trim())) {
			return 1;
		}

		Estudiante entity = toEntity(data);
		estudianteRepo.save(entity);
		return 0;
	}

	/**
	 * Obtiene todos los estudiantes registrados en el sistema.
	 *
	 * @return Lista de {@link EstudianteDTO}.
	 */
	@Override
	public List<EstudianteDTO> getAll() {
		List<EstudianteDTO> dtoList = new ArrayList<>();
		Iterable<Estudiante> entities = estudianteRepo.findAll();
		for (Estudiante entity : entities) {
			dtoList.add(toDTO(entity));
		}
		return dtoList;
	}

	/**
	 * Elimina un estudiante por su ID (codigo).
	 *
	 * @param id Codigo del estudiante a eliminar.
	 * @return 0 si se elimino correctamente; 1 si no existe o el id es nulo.
	 */
	@Override
	public int deleteById(Integer id) {
		if (id == null || !estudianteRepo.existsById(id)) {
			return 1;
		}
		estudianteRepo.deleteById(id);
		return 0;
	}

	/**
	 * Actualiza la informacion de un estudiante existente por su ID (codigo).
	 *
	 * @param id      Codigo del estudiante a actualizar.
	 * @param newData Nuevos datos del estudiante.
	 * @return 0 si se actualizo con exito;
	 *         1 si el estudiante no existe o el correo ya pertenece a otro estudiante;
	 *         2 si los datos nuevos son invalidos o fuera de rango.
	 */
	@Override
	public int updateById(Integer id, EstudianteDTO newData) {
		if (id == null) {
			return 1;
		}
		Optional<Estudiante> foundOpt = estudianteRepo.findById(id);
		if (foundOpt.isEmpty()) {
			return 1;
		}
		if (newData == null || newData.getNombre() == null || newData.getNombre().trim().isEmpty()
				|| newData.getCorreo() == null || newData.getCorreo().trim().isEmpty()
				|| newData.getFacultad() == null || newData.getFacultad().trim().isEmpty()
				|| newData.getSemestreActual() < 1 || newData.getSemestreActual() > 9
				|| newData.getPromedioAcumulado() < 0.0 || newData.getPromedioAcumulado() > 5.0) {
			return 2;
		}

		Optional<Estudiante> porCorreo = estudianteRepo.findByCorreo(newData.getCorreo().trim());
		if (porCorreo.isPresent() && porCorreo.get().getCodigo() != id.intValue()) {
			return 1;
		}

		Estudiante entityToUpdate = foundOpt.get();
		entityToUpdate.setNombre(newData.getNombre().trim());
		entityToUpdate.setCorreo(newData.getCorreo().trim());
		entityToUpdate.setFacultad(newData.getFacultad().trim());
		entityToUpdate.setSemestreActual(newData.getSemestreActual());
		entityToUpdate.setEstado(newData.isEstado());
		entityToUpdate.setProgramaAcademico(newData.getProgramaAcademico());
		entityToUpdate.setModalidad(newData.getModalidad());
		entityToUpdate.setPromedioAcumulado(newData.getPromedioAcumulado());
		entityToUpdate.setTipoDocumento(newData.getTipoDocumento());
		entityToUpdate.setNumeroDocumento(newData.getNumeroDocumento());
		entityToUpdate.setResultadoSaber11(newData.getResultadoSaber11());
		entityToUpdate.setTieneTituloBachiller(newData.isTieneTituloBachiller());
		entityToUpdate.setDiplomaOActaBachiller(newData.getDiplomaOActaBachiller());
		entityToUpdate.setAfiliacionEPS(newData.getAfiliacionEPS());
		entityToUpdate.setEsquemaVacunacionCompleto(newData.isEsquemaVacunacionCompleto());
		entityToUpdate.setEsExtranjero(newData.isEsExtranjero());
		entityToUpdate.setTieneVisaVigente(newData.isTieneVisaVigente());
		entityToUpdate.setResolucionConvalidacion(newData.getResolucionConvalidacion());

		estudianteRepo.save(entityToUpdate);
		return 0;
	}

	/**
	 * Cuenta el total de estudiantes registrados.
	 *
	 * @return Cantidad total de estudiantes.
	 */
	@Override
	public long count() {
		return estudianteRepo.count();
	}

	/**
	 * Verifica si existe un estudiante con el ID (codigo) indicado.
	 *
	 * @param id Codigo del estudiante.
	 * @return true si existe, false en caso contrario.
	 */
	@Override
	public boolean exist(Integer id) {
		return id != null && estudianteRepo.existsById(id);
	}

	/**
	 * Busca un estudiante por su codigo unico.
	 *
	 * @param id Codigo del estudiante.
	 * @return {@link EstudianteDTO} si existe, o null si no se encuentra.
	 */
	public EstudianteDTO getById(Integer id) {
		if (id == null) {
			return null;
		}
		return estudianteRepo.findById(id).map(this::toDTO).orElse(null);
	}

	/**
	 * Busca un estudiante por su correo electronico.
	 *
	 * @param correo Correo institucional del estudiante.
	 * @return {@link EstudianteDTO} si existe, o null si no se encuentra.
	 */
	public EstudianteDTO getByCorreo(String correo) {
		if (correo == null || correo.trim().isEmpty()) {
			return null;
		}
		return estudianteRepo.findByCorreo(correo.trim()).map(this::toDTO).orElse(null);
	}

	/**
	 * Obtiene todos los estudiantes pertenecientes a una facultad especifica.
	 *
	 * @param facultad Nombre de la facultad.
	 * @return Lista de {@link EstudianteDTO} de la facultad.
	 */
	public List<EstudianteDTO> getByFacultad(String facultad) {
		List<EstudianteDTO> dtoList = new ArrayList<>();
		if (facultad == null || facultad.trim().isEmpty()) {
			return dtoList;
		}
		List<Estudiante> entities = estudianteRepo.findByFacultad(facultad.trim());
		for (Estudiante entity : entities) {
			dtoList.add(toDTO(entity));
		}
		return dtoList;
	}

	/**
	 * Obtiene todos los estudiantes inscritos en un programa academico especifico.
	 *
	 * @param programaAcademico Nombre del programa academico.
	 * @return Lista de {@link EstudianteDTO} del programa.
	 */
	public List<EstudianteDTO> getByProgramaAcademico(String programaAcademico) {
		List<EstudianteDTO> dtoList = new ArrayList<>();
		if (programaAcademico == null || programaAcademico.trim().isEmpty()) {
			return dtoList;
		}
		List<Estudiante> entities = estudianteRepo.findByProgramaAcademico(programaAcademico.trim());
		for (Estudiante entity : entities) {
			dtoList.add(toDTO(entity));
		}
		return dtoList;
	}

	/**
	 * Obtiene todos los estudiantes que cursan actualmente un semestre especifico (1 a 9).
	 *
	 * @param semestreActual Numero del semestre actual.
	 * @return Lista de {@link EstudianteDTO} del semestre indicado.
	 */
	public List<EstudianteDTO> getBySemestreActual(int semestreActual) {
		List<EstudianteDTO> dtoList = new ArrayList<>();
		List<Estudiante> entities = estudianteRepo.findBySemestreActual(semestreActual);
		for (Estudiante entity : entities) {
			dtoList.add(toDTO(entity));
		}
		return dtoList;
	}

	/**
	 * Obtiene los estudiantes filtrados por su estado academico (activo o inactivo).
	 *
	 * @param estado true para estudiantes activos, false para inactivos.
	 * @return Lista de {@link EstudianteDTO} filtrada.
	 */
	public List<EstudianteDTO> getByEstado(boolean estado) {
		List<EstudianteDTO> dtoList = new ArrayList<>();
		List<Estudiante> entities = estudianteRepo.findByEstado(estado);
		for (Estudiante entity : entities) {
			dtoList.add(toDTO(entity));
		}
		return dtoList;
	}

	/**
	 * Valida los datos obligatorios de un estudiante segun las reglas del plan de estudios
	 * de Ingenieria de Sistemas (SNIES 4952: 9 semestres).
	 *
	 * @param data DTO a validar.
	 * @return true si los datos son validos, false en caso contrario.
	 */
	private boolean esEstudianteValido(EstudianteDTO data) {
		if (data == null) {
			return false;
		}
		if (data.getCodigo() <= 0) {
			return false;
		}
		if (data.getNombre() == null || data.getNombre().trim().isEmpty()) {
			return false;
		}
		if (data.getCorreo() == null || data.getCorreo().trim().isEmpty()) {
			return false;
		}
		if (data.getFacultad() == null || data.getFacultad().trim().isEmpty()) {
			return false;
		}
		if (data.getSemestreActual() < 1 || data.getSemestreActual() > 9) {
			return false;
		}
		if (data.getPromedioAcumulado() < 0.0 || data.getPromedioAcumulado() > 5.0) {
			return false;
		}
		return true;
	}

	/**
	 * Convierte una entidad {@link Estudiante} a {@link EstudianteDTO}.
	 *
	 * @param entity Entidad Estudiante.
	 * @return Objeto EstudianteDTO.
	 */
	public EstudianteDTO toDTO(Estudiante entity) {
		if (entity == null) {
			return null;
		}
		return new EstudianteDTO(
				entity.getNombre(),
				entity.getCodigo(),
				entity.getCorreo(),
				entity.getFacultad(),
				entity.getSemestreActual(),
				entity.isEstado(),
				entity.getProgramaAcademico(),
				entity.getModalidad(),
				entity.getPromedioAcumulado(),
				entity.getTipoDocumento(),
				entity.getNumeroDocumento(),
				entity.getResultadoSaber11(),
				entity.isTieneTituloBachiller(),
				entity.getDiplomaOActaBachiller(),
				entity.getAfiliacionEPS(),
				entity.isEsquemaVacunacionCompleto(),
				entity.isEsExtranjero(),
				entity.isTieneVisaVigente(),
				entity.getResolucionConvalidacion()
		);
	}

	/**
	 * Convierte un {@link EstudianteDTO} a entidad {@link Estudiante}.
	 *
	 * @param dto Objeto EstudianteDTO.
	 * @return Entidad Estudiante.
	 */
	public Estudiante toEntity(EstudianteDTO dto) {
		if (dto == null) {
			return null;
		}
		Estudiante entity = new Estudiante(
				dto.getNombre() != null ? dto.getNombre().trim() : null,
				dto.getCodigo(),
				dto.getCorreo() != null ? dto.getCorreo().trim() : null,
				dto.getFacultad() != null ? dto.getFacultad().trim() : null,
				dto.getSemestreActual(),
				dto.isEstado()
		);
		entity.setProgramaAcademico(dto.getProgramaAcademico());
		entity.setModalidad(dto.getModalidad());
		entity.setPromedioAcumulado(dto.getPromedioAcumulado());
		entity.setTipoDocumento(dto.getTipoDocumento());
		entity.setNumeroDocumento(dto.getNumeroDocumento());
		entity.setResultadoSaber11(dto.getResultadoSaber11());
		entity.setTieneTituloBachiller(dto.isTieneTituloBachiller());
		entity.setDiplomaOActaBachiller(dto.getDiplomaOActaBachiller());
		entity.setAfiliacionEPS(dto.getAfiliacionEPS());
		entity.setEsquemaVacunacionCompleto(dto.isEsquemaVacunacionCompleto());
		entity.setEsExtranjero(dto.isEsExtranjero());
		entity.setTieneVisaVigente(dto.isTieneVisaVigente());
		entity.setResolucionConvalidacion(dto.getResolucionConvalidacion());
		return entity;
	}

}
