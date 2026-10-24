package co.edu.unbosque.sdgh.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.edu.unbosque.sdgh.dto.EspacioConocimientoDTO;
import co.edu.unbosque.sdgh.dto.EtapaDeFormacionDTO;
import co.edu.unbosque.sdgh.dto.MateriaDTO;
import co.edu.unbosque.sdgh.dto.PlanDeEstudiosDTO;
import co.edu.unbosque.sdgh.entity.EspacioConocimiento;
import co.edu.unbosque.sdgh.entity.EtapaFormacion;
import co.edu.unbosque.sdgh.entity.Materia;
import co.edu.unbosque.sdgh.entity.PlanDeEstudios;
import co.edu.unbosque.sdgh.repository.PlanDeEstudiosRepository;

/**
 * Servicio que gestiona la logica de negocio para el plan de estudios del programa
 * (CU02 - RF01, RF02).
 */
@Service
public class PlanDeEstudiosService implements CRUDOperation<PlanDeEstudiosDTO> {

	@Autowired
	private PlanDeEstudiosRepository planRepo;

	public PlanDeEstudiosService() {
	}

	/**
	 * Registra un nuevo plan de estudios validando unicidad de ID y de nombre de programa (RF02).
	 *
	 * @param data DTO con los datos del plan de estudios a registrar.
	 * @return 0 si se registro con exito;
	 *         1 si el ID o el nombre de programa ya existen;
	 *         2 si los datos son nulos o incompletos.
	 */
	@Override
	public int create(PlanDeEstudiosDTO data) {
		if (!esPlanValido(data)) {
			return 2;
		}
		if (planRepo.existsById(data.getId().trim())) {
			return 1;
		}
		if (planRepo.existsByNombrePrograma(data.getNombrePrograma().trim())) {
			return 1;
		}

		PlanDeEstudios entity = toEntity(data);
		planRepo.save(entity);
		return 0;
	}

	/**
	 * Obtiene todos los planes de estudio registrados en el sistema.
	 *
	 * @return Lista de {@link PlanDeEstudiosDTO}.
	 */
	@Override
	public List<PlanDeEstudiosDTO> getAll() {
		List<PlanDeEstudiosDTO> dtoList = new ArrayList<>();
		Iterable<PlanDeEstudios> entities = planRepo.findAll();
		for (PlanDeEstudios entity : entities) {
			dtoList.add(toDTO(entity));
		}
		return dtoList;
	}

	/**
	 * Elimina un plan de estudios por su ID recibido como Integer (delegando a String).
	 *
	 * @param id Identificador numerico del plan de estudios.
	 * @return 0 si se elimino correctamente; 1 si no existe o el id es nulo.
	 */
	@Override
	public int deleteById(Integer id) {
		if (id == null) {
			return 1;
		}
		return deleteByStringId(String.valueOf(id));
	}

	/**
	 * Elimina un plan de estudios por su identificador tipo String.
	 *
	 * @param id Identificador del plan de estudios (ej. SNIES 4952).
	 * @return 0 si se elimino correctamente; 1 si no existe o el id es nulo/vacio.
	 */
	public int deleteByStringId(String id) {
		if (id == null || id.trim().isEmpty() || !planRepo.existsById(id.trim())) {
			return 1;
		}
		planRepo.deleteById(id.trim());
		return 0;
	}

	/**
	 * Actualiza un plan de estudios por su ID recibido como Integer (delegando a String).
	 *
	 * @param id      Identificador del plan a actualizar.
	 * @param newData Nuevos datos del plan de estudios.
	 * @return 0 si se actualizo con exito;
	 *         1 si no existe o hay conflicto con el nombre del programa;
	 *         2 si los datos nuevos son invalidos.
	 */
	@Override
	public int updateById(Integer id, PlanDeEstudiosDTO newData) {
		if (id == null) {
			return 1;
		}
		return updateByStringId(String.valueOf(id), newData);
	}

	/**
	 * Actualiza un plan de estudios existente identificado por su ID tipo String.
	 *
	 * @param id      Identificador del plan de estudios.
	 * @param newData Nuevos datos del plan de estudios.
	 * @return 0 si se actualizo con exito;
	 *         1 si no existe o el nombre de programa entra en conflicto con otro plan;
	 *         2 si los datos nuevos son nulos o invalidos.
	 */
	public int updateByStringId(String id, PlanDeEstudiosDTO newData) {
		if (id == null || id.trim().isEmpty()) {
			return 1;
		}
		Optional<PlanDeEstudios> foundOpt = planRepo.findById(id.trim());
		if (foundOpt.isEmpty()) {
			return 1;
		}
		if (newData == null || newData.getNombrePrograma() == null || newData.getNombrePrograma().trim().isEmpty()
				|| newData.getTotalCreditos() <= 0 || newData.getNumeroSemestres() <= 0) {
			return 2;
		}

		Optional<PlanDeEstudios> porNombre = planRepo.findByNombrePrograma(newData.getNombrePrograma().trim());
		if (porNombre.isPresent() && !porNombre.get().getId().equals(id.trim())) {
			return 1;
		}

		PlanDeEstudios entityToUpdate = foundOpt.get();
		entityToUpdate.setNombrePrograma(newData.getNombrePrograma().trim());
		entityToUpdate.setVersion(newData.getVersion() != null ? newData.getVersion().trim() : null);
		entityToUpdate.setTotalCreditos(newData.getTotalCreditos());
		entityToUpdate.setNumeroSemestres(newData.getNumeroSemestres());
		entityToUpdate.setEtapas(toEtapaEntities(newData.getEtapas()));
		entityToUpdate.setEspaciosConocimiento(toEspacioEntities(newData.getEspaciosConocimiento()));
		entityToUpdate.setMaterias(toMateriaEntities(newData.getMaterias()));

		planRepo.save(entityToUpdate);
		return 0;
	}

	/**
	 * Asocia una materia a un plan de estudios existente validando que no este duplicada (CU02 / RF02).
	 *
	 * @param planId  Identificador del plan de estudios.
	 * @param materia DTO de la materia a asociar.
	 * @return 0 si se asocio con exito;
	 *         1 si el plan no existe o la materia ya estaba asociada al plan;
	 *         2 si los datos de la materia son nulos o invalidos.
	 */
	public int agregarMateriaAPlan(String planId, MateriaDTO materia) {
		if (planId == null || planId.trim().isEmpty()) {
			return 1;
		}
		if (materia == null || materia.getCodigo() <= 0 || materia.getNombre() == null
				|| materia.getNombre().trim().isEmpty()) {
			return 2;
		}
		Optional<PlanDeEstudios> foundOpt = planRepo.findById(planId.trim());
		if (foundOpt.isEmpty()) {
			return 1;
		}

		PlanDeEstudios plan = foundOpt.get();
		List<Materia> materias = plan.getMaterias() != null ? plan.getMaterias() : new ArrayList<>();
		for (Materia existente : materias) {
			if (existente.getCodigo() == materia.getCodigo()) {
				return 1;
			}
		}

		materias.add(new Materia(
				materia.getCodigo(),
				materia.getNombre().trim(),
				materia.getSemestre(),
				materia.getNcreditos(),
				materia.getHorasDeLaMateria(),
				materia.isCorrelativa()
		));
		plan.setMaterias(materias);
		planRepo.save(plan);
		return 0;
	}

	/**
	 * Cuenta el total de planes de estudio registrados.
	 *
	 * @return Cantidad total de planes de estudio.
	 */
	@Override
	public long count() {
		return planRepo.count();
	}

	/**
	 * Verifica si existe un plan de estudios con el ID recibido como Integer.
	 *
	 * @param id Identificador del plan.
	 * @return true si existe, false en caso contrario.
	 */
	@Override
	public boolean exist(Integer id) {
		if (id == null) {
			return false;
		}
		return existByStringId(String.valueOf(id));
	}

	/**
	 * Verifica si existe un plan de estudios con el ID tipo String indicado.
	 *
	 * @param id Identificador del plan de estudios.
	 * @return true si existe, false en caso contrario.
	 */
	public boolean existByStringId(String id) {
		return id != null && !id.trim().isEmpty() && planRepo.existsById(id.trim());
	}

	/**
	 * Busca un plan de estudios por su ID tipo String.
	 *
	 * @param id Identificador del plan de estudios.
	 * @return {@link PlanDeEstudiosDTO} si existe, o null si no se encuentra.
	 */
	public PlanDeEstudiosDTO getById(String id) {
		if (id == null || id.trim().isEmpty()) {
			return null;
		}
		return planRepo.findById(id.trim()).map(this::toDTO).orElse(null);
	}

	/**
	 * Busca un plan de estudios por el nombre del programa academico.
	 *
	 * @param nombrePrograma Nombre del programa.
	 * @return {@link PlanDeEstudiosDTO} si existe, o null si no se encuentra.
	 */
	public PlanDeEstudiosDTO getByNombrePrograma(String nombrePrograma) {
		if (nombrePrograma == null || nombrePrograma.trim().isEmpty()) {
			return null;
		}
		return planRepo.findByNombrePrograma(nombrePrograma.trim()).map(this::toDTO).orElse(null);
	}

	/**
	 * Busca un plan de estudios por el nombre del programa y su version.
	 *
	 * @param nombrePrograma Nombre del programa.
	 * @param version        Version del plan (ej. 2025).
	 * @return {@link PlanDeEstudiosDTO} si existe, o null si no se encuentra.
	 */
	public PlanDeEstudiosDTO getByNombreProgramaAndVersion(String nombrePrograma, String version) {
		if (nombrePrograma == null || nombrePrograma.trim().isEmpty()
				|| version == null || version.trim().isEmpty()) {
			return null;
		}
		return planRepo.findByNombreProgramaAndVersion(nombrePrograma.trim(), version.trim())
				.map(this::toDTO)
				.orElse(null);
	}

	/**
	 * Valida los datos obligatorios de un plan de estudios.
	 *
	 * @param data DTO a validar.
	 * @return true si es valido, false en caso contrario.
	 */
	private boolean esPlanValido(PlanDeEstudiosDTO data) {
		if (data == null) {
			return false;
		}
		if (data.getId() == null || data.getId().trim().isEmpty()) {
			return false;
		}
		if (data.getNombrePrograma() == null || data.getNombrePrograma().trim().isEmpty()) {
			return false;
		}
		if (data.getTotalCreditos() <= 0 || data.getNumeroSemestres() <= 0) {
			return false;
		}
		return true;
	}

	/**
	 * Convierte una entidad {@link PlanDeEstudios} a {@link PlanDeEstudiosDTO}.
	 *
	 * @param entity Entidad PlanDeEstudios.
	 * @return Objeto PlanDeEstudiosDTO.
	 */
	public PlanDeEstudiosDTO toDTO(PlanDeEstudios entity) {
		if (entity == null) {
			return null;
		}
		return new PlanDeEstudiosDTO(
				entity.getId(),
				entity.getNombrePrograma(),
				entity.getVersion(),
				entity.getTotalCreditos(),
				entity.getNumeroSemestres(),
				toEtapaDTOs(entity.getEtapas()),
				toEspacioDTOs(entity.getEspaciosConocimiento()),
				toMateriaDTOs(entity.getMaterias())
		);
	}

	/**
	 * Convierte un {@link PlanDeEstudiosDTO} a entidad {@link PlanDeEstudios}.
	 *
	 * @param dto Objeto PlanDeEstudiosDTO.
	 * @return Entidad PlanDeEstudios.
	 */
	public PlanDeEstudios toEntity(PlanDeEstudiosDTO dto) {
		if (dto == null) {
			return null;
		}
		PlanDeEstudios entity = new PlanDeEstudios(
				dto.getId() != null ? dto.getId().trim() : null,
				dto.getNombrePrograma() != null ? dto.getNombrePrograma().trim() : null
		);
		entity.setVersion(dto.getVersion() != null ? dto.getVersion().trim() : null);
		entity.setTotalCreditos(dto.getTotalCreditos());
		entity.setNumeroSemestres(dto.getNumeroSemestres());
		entity.setEtapas(toEtapaEntities(dto.getEtapas()));
		entity.setEspaciosConocimiento(toEspacioEntities(dto.getEspaciosConocimiento()));
		entity.setMaterias(toMateriaEntities(dto.getMaterias()));
		return entity;
	}

	private List<EtapaDeFormacionDTO> toEtapaDTOs(List<EtapaFormacion> etapas) {
		List<EtapaDeFormacionDTO> list = new ArrayList<>();
		if (etapas != null) {
			for (EtapaFormacion e : etapas) {
				if (e != null) {
					list.add(new EtapaDeFormacionDTO(
							e.getNumero(),
							e.getNombre(),
							e.getMeta(),
							e.getSemestreInicio(),
							e.getSemestreFin()
					));
				}
			}
		}
		return list;
	}

	private List<EtapaFormacion> toEtapaEntities(List<EtapaDeFormacionDTO> etapas) {
		List<EtapaFormacion> list = new ArrayList<>();
		if (etapas != null) {
			for (EtapaDeFormacionDTO d : etapas) {
				if (d != null) {
					list.add(new EtapaFormacion(
							d.getNumero(),
							d.getNombre(),
							d.getMeta(),
							d.getSemestreInicio(),
							d.getSemestreFin()
					));
				}
			}
		}
		return list;
	}

	private List<EspacioConocimientoDTO> toEspacioDTOs(List<EspacioConocimiento> espacios) {
		List<EspacioConocimientoDTO> list = new ArrayList<>();
		if (espacios != null) {
			for (EspacioConocimiento e : espacios) {
				if (e != null) {
					list.add(new EspacioConocimientoDTO(e.getId(), e.getNombre(), e.getDescripcion()));
				}
			}
		}
		return list;
	}

	private List<EspacioConocimiento> toEspacioEntities(List<EspacioConocimientoDTO> espacios) {
		List<EspacioConocimiento> list = new ArrayList<>();
		if (espacios != null) {
			for (EspacioConocimientoDTO d : espacios) {
				if (d != null) {
					list.add(new EspacioConocimiento(d.getId(), d.getNombre(), d.getDescripcion()));
				}
			}
		}
		return list;
	}

	private List<MateriaDTO> toMateriaDTOs(List<Materia> materias) {
		List<MateriaDTO> list = new ArrayList<>();
		if (materias != null) {
			for (Materia m : materias) {
				if (m != null) {
					list.add(new MateriaDTO(
							m.getCodigo(),
							m.getNombre(),
							m.getSemestre(),
							m.getNcreditos(),
							m.getHorasDeLaMateria(),
							m.isCorrelativa()
					));
				}
			}
		}
		return list;
	}

	private List<Materia> toMateriaEntities(List<MateriaDTO> materias) {
		List<Materia> list = new ArrayList<>();
		if (materias != null) {
			for (MateriaDTO d : materias) {
				if (d != null) {
					list.add(new Materia(
							d.getCodigo(),
							d.getNombre() != null ? d.getNombre().trim() : null,
							d.getSemestre(),
							d.getNcreditos(),
							d.getHorasDeLaMateria(),
							d.isCorrelativa()
					));
				}
			}
		}
		return list;
	}

}
