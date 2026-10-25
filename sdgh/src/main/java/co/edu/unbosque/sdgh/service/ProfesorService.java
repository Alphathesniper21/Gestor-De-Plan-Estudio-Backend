package co.edu.unbosque.sdgh.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.edu.unbosque.sdgh.dto.ProfesorDTO;
import co.edu.unbosque.sdgh.entity.Profesor;
import co.edu.unbosque.sdgh.repository.ProfesorRepository;

@Service
public class ProfesorService implements CRUDOperation<ProfesorDTO> {

	@Autowired
	private ProfesorRepository profesorRepo;

	public ProfesorService() {
	}

	/**
	 * Registra un nuevo docente validando unicidad de codigo/correo y datos obligatorios (CU03 / RF03).
	 *
	 * @param data DTO con los datos del docente a registrar.
	 * @return 0 si se registro con exito;
	 *         1 si el codigo o el correo ya existen (duplicado);
	 *         2 si los datos son nulos, incompletos o invalidos.
	 */
	@Override
	public int create(ProfesorDTO data) {
		if (!esProfesorValido(data)) {
			return 2;
		}
		if (profesorRepo.existsById(data.getCodigo())) {
			return 1;
		}
		if (profesorRepo.existsByCorreo(data.getCorreo().trim())) {
			return 1;
		}

		Profesor entity = toEntity(data);
		profesorRepo.save(entity);
		return 0;
	}

	/**
	 * Obtiene todos los docentes registrados en el sistema (CU03 / RF03).
	 *
	 * @return Lista de {@link ProfesorDTO}.
	 */
	@Override
	public List<ProfesorDTO> getAll() {
		List<ProfesorDTO> dtoList = new ArrayList<>();
		Iterable<Profesor> entities = profesorRepo.findAll();
		for (Profesor entity : entities) {
			dtoList.add(toDTO(entity));
		}
		return dtoList;
	}

	/**
	 * Elimina un docente por su ID (codigo).
	 *
	 * @param id Codigo del docente a eliminar.
	 * @return 0 si se elimino correctamente; 1 si no existe o el id es nulo.
	 */
	@Override
	public int deleteById(Integer id) {
		if (id == null || !profesorRepo.existsById(id)) {
			return 1;
		}
		profesorRepo.deleteById(id);
		return 0;
	}

	/**
	 * Actualiza la informacion de un docente existente por su ID (codigo) (CU03 / RF03).
	 *
	 * @param id      Codigo del docente a actualizar.
	 * @param newData Nuevos datos del docente.
	 * @return 0 si se actualizo con exito;
	 *         1 si el docente no existe o el nuevo correo ya pertenece a otro docente;
	 *         2 si los datos nuevos son invalidos o incompletos.
	 */
	@Override
	public int updateById(Integer id, ProfesorDTO newData) {
		if (id == null) {
			return 1;
		}
		Optional<Profesor> foundOpt = profesorRepo.findById(id);
		if (foundOpt.isEmpty()) {
			return 1;
		}
		if (newData == null || newData.getNombre() == null || newData.getNombre().trim().isEmpty()
				|| newData.getCorreo() == null || newData.getCorreo().trim().isEmpty()
				|| newData.getFacultad() == null || newData.getFacultad().trim().isEmpty()
				|| newData.getAniosExperiencia() < 0 || newData.getHorasFormacionPedagogica() < 0
				|| newData.getPuntajeIngles() < 0) {
			return 2;
		}

		Optional<Profesor> porCorreo = profesorRepo.findByCorreo(newData.getCorreo().trim());
		if (porCorreo.isPresent() && porCorreo.get().getCodigo() != id.intValue()) {
			return 1;
		}

		Profesor entityToUpdate = foundOpt.get();
		entityToUpdate.setNombre(newData.getNombre().trim());
		entityToUpdate.setCorreo(newData.getCorreo().trim());
		entityToUpdate.setFacultad(newData.getFacultad().trim());
		entityToUpdate.setTituloPregrado(newData.getTituloPregrado());
		entityToUpdate.setActaGradoPregrado(newData.getActaGradoPregrado());
		entityToUpdate.setTituloPosgrado(newData.getTituloPosgrado());
		entityToUpdate.setNivelPosgrado(newData.getNivelPosgrado());
		entityToUpdate.setTituloExtranjero(newData.isTituloExtranjero());
		entityToUpdate.setResolucionConvalidacion(newData.getResolucionConvalidacion());
		entityToUpdate.setHorasFormacionPedagogica(newData.getHorasFormacionPedagogica());
		entityToUpdate.setMaximoNivelFormacion(newData.getMaximoNivelFormacion());
		entityToUpdate.setTipoDocumento(newData.getTipoDocumento());
		entityToUpdate.setNumeroDocumento(newData.getNumeroDocumento());
		entityToUpdate.setTarjetaProfesional(newData.getTarjetaProfesional());
		entityToUpdate.setRut(newData.getRut());
		entityToUpdate.setEps(newData.getEps());
		entityToUpdate.setFondoPensiones(newData.getFondoPensiones());
		entityToUpdate.setTieneVisaTrabajo(newData.isTieneVisaTrabajo());
		entityToUpdate.setEspecialidades(newData.getEspecialidades() != null
				? new ArrayList<>(newData.getEspecialidades())
				: new ArrayList<>());
		entityToUpdate.setAniosExperiencia(newData.getAniosExperiencia());
		entityToUpdate.setPublicaciones(newData.getPublicaciones() != null
				? new ArrayList<>(newData.getPublicaciones())
				: new ArrayList<>());
		entityToUpdate.setNivelIngles(newData.getNivelIngles());
		entityToUpdate.setExamenIngles(newData.getExamenIngles());
		entityToUpdate.setPuntajeIngles(newData.getPuntajeIngles());
		entityToUpdate.setHojaVidaActualizada(newData.isHojaVidaActualizada());

		profesorRepo.save(entityToUpdate);
		return 0;
	}

	/**
	 * Cuenta el total de docentes registrados.
	 *
	 * @return Cantidad total de docentes.
	 */
	@Override
	public long count() {
		return profesorRepo.count();
	}

	/**
	 * Verifica si existe un docente con el ID (codigo) indicado.
	 *
	 * @param id Codigo del docente.
	 * @return true si existe, false en caso contrario.
	 */
	@Override
	public boolean exist(Integer id) {
		return id != null && profesorRepo.existsById(id);
	}

	/**
	 * Busca un docente por su codigo unico.
	 *
	 * @param id Codigo del docente.
	 * @return {@link ProfesorDTO} si existe, o null si no se encuentra.
	 */
	public ProfesorDTO getById(Integer id) {
		if (id == null) {
			return null;
		}
		return profesorRepo.findById(id).map(this::toDTO).orElse(null);
	}

	/**
	 * Busca un docente por su correo electronico.
	 *
	 * @param correo Correo institucional del docente.
	 * @return {@link ProfesorDTO} si existe, o null si no se encuentra.
	 */
	public ProfesorDTO getByCorreo(String correo) {
		if (correo == null || correo.trim().isEmpty()) {
			return null;
		}
		return profesorRepo.findByCorreo(correo.trim()).map(this::toDTO).orElse(null);
	}

	/**
	 * Obtiene todos los docentes adscritos a una facultad especifica.
	 *
	 * @param facultad Nombre de la facultad.
	 * @return Lista de {@link ProfesorDTO} de la facultad.
	 */
	public List<ProfesorDTO> getByFacultad(String facultad) {
		List<ProfesorDTO> dtoList = new ArrayList<>();
		if (facultad == null || facultad.trim().isEmpty()) {
			return dtoList;
		}
		List<Profesor> entities = profesorRepo.findByFacultad(facultad.trim());
		for (Profesor entity : entities) {
			dtoList.add(toDTO(entity));
		}
		return dtoList;
	}

	/**
	 * Obtiene los docentes con una cantidad de anios de experiencia igual o superior a la indicada (RF03).
	 *
	 * @param anios Anios minimos de experiencia.
	 * @return Lista de {@link ProfesorDTO} que cumplen el criterio.
	 */
	public List<ProfesorDTO> getByAniosExperienciaMinima(int anios) {
		List<ProfesorDTO> dtoList = new ArrayList<>();
		List<Profesor> entities = profesorRepo.findByAniosExperienciaGreaterThanEqual(anios);
		for (Profesor entity : entities) {
			dtoList.add(toDTO(entity));
		}
		return dtoList;
	}

	/**
	 * Obtiene los docentes filtrados por su nivel de ingles.
	 *
	 * @param nivelIngles Nivel de ingles (ej. A2, B1, B2, C1).
	 * @return Lista de {@link ProfesorDTO} con ese nivel.
	 */
	public List<ProfesorDTO> getByNivelIngles(String nivelIngles) {
		List<ProfesorDTO> dtoList = new ArrayList<>();
		if (nivelIngles == null || nivelIngles.trim().isEmpty()) {
			return dtoList;
		}
		List<Profesor> entities = profesorRepo.findByNivelIngles(nivelIngles.trim());
		for (Profesor entity : entities) {
			dtoList.add(toDTO(entity));
		}
		return dtoList;
	}

	/**
	 * Obtiene los docentes segun si tienen su hoja de vida actualizada o no.
	 *
	 * @param hojaVidaActualizada true para hojas de vida actualizadas, false en caso contrario.
	 * @return Lista de {@link ProfesorDTO} filtrada.
	 */
	public List<ProfesorDTO> getByHojaVidaActualizada(boolean hojaVidaActualizada) {
		List<ProfesorDTO> dtoList = new ArrayList<>();
		List<Profesor> entities = profesorRepo.findByHojaVidaActualizada(hojaVidaActualizada);
		for (Profesor entity : entities) {
			dtoList.add(toDTO(entity));
		}
		return dtoList;
	}

	/**
	 * Obtiene los docentes que cuentan con una especialidad especifica y pueden orientar cursos afines (RF17).
	 *
	 * @param especialidad Nombre de la especialidad o area de conocimiento.
	 * @return Lista de {@link ProfesorDTO} que poseen la especialidad.
	 */
	public List<ProfesorDTO> getByEspecialidad(String especialidad) {
		List<ProfesorDTO> dtoList = new ArrayList<>();
		if (especialidad == null || especialidad.trim().isEmpty()) {
			return dtoList;
		}
		String buscada = especialidad.trim().toLowerCase();
		Iterable<Profesor> entities = profesorRepo.findAll();
		for (Profesor entity : entities) {
			if (entity.getEspecialidades() != null) {
				for (String esp : entity.getEspecialidades()) {
					if (esp != null && esp.trim().toLowerCase().contains(buscada)) {
						dtoList.add(toDTO(entity));
						break;
					}
				}
			}
		}
		return dtoList;
	}

	/**
	 * Verifica si un docente especifico tiene una especialidad que le permita orientar una materia (RF17).
	 *
	 * @param idDocente    Codigo del docente.
	 * @param especialidad Especialidad o nombre de la materia a verificar.
	 * @return true si el docente posee la especialidad, false en caso contrario.
	 */
	public boolean puedeOrientarEspecialidad(Integer idDocente, String especialidad) {
		if (idDocente == null || especialidad == null || especialidad.trim().isEmpty()) {
			return false;
		}
		Optional<Profesor> foundOpt = profesorRepo.findById(idDocente);
		if (foundOpt.isEmpty() || foundOpt.get().getEspecialidades() == null) {
			return false;
		}
		String buscada = especialidad.trim().toLowerCase();
		for (String esp : foundOpt.get().getEspecialidades()) {
			if (esp != null && esp.trim().toLowerCase().contains(buscada)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Valida que los datos obligatorios del docente sean consistentes (CU03 / RF03).
	 *
	 * @param data DTO a validar.
	 * @return true si los datos son validos, false en caso contrario.
	 */
	private boolean esProfesorValido(ProfesorDTO data) {
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
		if (data.getAniosExperiencia() < 0 || data.getHorasFormacionPedagogica() < 0
				|| data.getPuntajeIngles() < 0) {
			return false;
		}
		return true;
	}

	/**
	 * Convierte una entidad {@link Profesor} a {@link ProfesorDTO}.
	 *
	 * @param entity Entidad Profesor.
	 * @return Objeto ProfesorDTO.
	 */
	public ProfesorDTO toDTO(Profesor entity) {
		if (entity == null) {
			return null;
		}
		return new ProfesorDTO(
				entity.getNombre(),
				entity.getCodigo(),
				entity.getCorreo(),
				entity.getFacultad(),
				entity.getTituloPregrado(),
				entity.getActaGradoPregrado(),
				entity.getTituloPosgrado(),
				entity.getNivelPosgrado(),
				entity.isTituloExtranjero(),
				entity.getResolucionConvalidacion(),
				entity.getHorasFormacionPedagogica(),
				entity.getMaximoNivelFormacion(),
				entity.getTipoDocumento(),
				entity.getNumeroDocumento(),
				entity.getTarjetaProfesional(),
				entity.getRut(),
				entity.getEps(),
				entity.getFondoPensiones(),
				entity.isTieneVisaTrabajo(),
				entity.getEspecialidades() != null ? new ArrayList<>(entity.getEspecialidades()) : new ArrayList<>(),
				entity.getAniosExperiencia(),
				entity.getPublicaciones() != null ? new ArrayList<>(entity.getPublicaciones()) : new ArrayList<>(),
				entity.getNivelIngles(),
				entity.getExamenIngles(),
				entity.getPuntajeIngles(),
				entity.isHojaVidaActualizada()
		);
	}

	/**
	 * Convierte un {@link ProfesorDTO} a entidad {@link Profesor}.
	 *
	 * @param dto Objeto ProfesorDTO.
	 * @return Entidad Profesor.
	 */
	public Profesor toEntity(ProfesorDTO dto) {
		if (dto == null) {
			return null;
		}
		Profesor entity = new Profesor(
				dto.getNombre() != null ? dto.getNombre().trim() : null,
				dto.getCodigo(),
				dto.getCorreo() != null ? dto.getCorreo().trim() : null,
				dto.getFacultad() != null ? dto.getFacultad().trim() : null
		);
		entity.setTituloPregrado(dto.getTituloPregrado());
		entity.setActaGradoPregrado(dto.getActaGradoPregrado());
		entity.setTituloPosgrado(dto.getTituloPosgrado());
		entity.setNivelPosgrado(dto.getNivelPosgrado());
		entity.setTituloExtranjero(dto.isTituloExtranjero());
		entity.setResolucionConvalidacion(dto.getResolucionConvalidacion());
		entity.setHorasFormacionPedagogica(dto.getHorasFormacionPedagogica());
		entity.setMaximoNivelFormacion(dto.getMaximoNivelFormacion());
		entity.setTipoDocumento(dto.getTipoDocumento());
		entity.setNumeroDocumento(dto.getNumeroDocumento());
		entity.setTarjetaProfesional(dto.getTarjetaProfesional());
		entity.setRut(dto.getRut());
		entity.setEps(dto.getEps());
		entity.setFondoPensiones(dto.getFondoPensiones());
		entity.setTieneVisaTrabajo(dto.isTieneVisaTrabajo());
		entity.setEspecialidades(dto.getEspecialidades() != null
				? new ArrayList<>(dto.getEspecialidades())
				: new ArrayList<>());
		entity.setAniosExperiencia(dto.getAniosExperiencia());
		entity.setPublicaciones(dto.getPublicaciones() != null
				? new ArrayList<>(dto.getPublicaciones())
				: new ArrayList<>());
		entity.setNivelIngles(dto.getNivelIngles());
		entity.setExamenIngles(dto.getExamenIngles());
		entity.setPuntajeIngles(dto.getPuntajeIngles());
		entity.setHojaVidaActualizada(dto.isHojaVidaActualizada());
		return entity;
	}

}
