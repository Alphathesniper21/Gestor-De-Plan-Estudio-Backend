package co.edu.unbosque.sdgh.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.edu.unbosque.sdgh.dto.MateriaDTO;
import co.edu.unbosque.sdgh.entity.Materia;
import co.edu.unbosque.sdgh.repository.MateriaRepository;

/**
 * Servicio que gestiona la logica de negocio para las materias del plan de estudios
 * (CU02 - RF01, RF26).
 */
@Service
public class MateriaService implements CRUDOperation<MateriaDTO> {

	@Autowired
	private MateriaRepository materiaRepo;

	public MateriaService() {
	}

	/**
	 * Registra una nueva materia validando unicidad de codigo/nombre y datos obligatorios (CU02 / RF01).
	 *
	 * @param data DTO con los datos de la materia a registrar.
	 * @return 0 si se creo con exito;
	 *         1 si el codigo o nombre ya existe (duplicado);
	 *         2 si los datos estan incompletos o fuera de rango.
	 */
	@Override
	public int create(MateriaDTO data) {
		if (!esMateriaValida(data)) {
			return 2;
		}
		if (materiaRepo.existsById(data.getCodigo())) {
			return 1;
		}
		Optional<Materia> existentePorNombre = materiaRepo.findByNombre(data.getNombre().trim());
		if (existentePorNombre.isPresent()) {
			return 1;
		}

		Materia entity = toEntity(data);
		materiaRepo.save(entity);
		return 0;
	}

	/**
	 * Obtiene todas las materias registradas en el sistema (RF26).
	 *
	 * @return Lista de {@link MateriaDTO}.
	 */
	@Override
	public List<MateriaDTO> getAll() {
		List<MateriaDTO> dtoList = new ArrayList<>();
		Iterable<Materia> entities = materiaRepo.findAll();
		for (Materia entity : entities) {
			dtoList.add(toDTO(entity));
		}
		return dtoList;
	}

	/**
	 * Elimina una materia por su ID (codigo) recibido como Integer (RF26).
	 *
	 * @param id Codigo de la materia.
	 * @return 0 si se elimino correctamente; 1 si no existe o el id es nulo.
	 */
	@Override
	public int deleteById(Integer id) {
		if (id == null) {
			return 1;
		}
		return deleteByCodigo(id.longValue());
	}

	/**
	 * Elimina una materia por su codigo unico tipo Long (RF26).
	 *
	 * @param codigo Codigo unico de la materia.
	 * @return 0 si se elimino correctamente; 1 si no existe o el codigo es nulo.
	 */
	public int deleteByCodigo(Long codigo) {
		if (codigo == null || !materiaRepo.existsById(codigo)) {
			return 1;
		}
		materiaRepo.deleteById(codigo);
		return 0;
	}

	/**
	 * Actualiza una materia existente identificada por su ID (codigo) recibido como Integer (CU02 / RF26).
	 *
	 * @param id      Codigo de la materia a actualizar.
	 * @param newData Nuevos datos de la materia.
	 * @return 0 si se actualizo con exito;
	 *         1 si la materia no existe o el nombre entra en conflicto con otra materia;
	 *         2 si los datos nuevos son invalidos o incompletos.
	 */
	@Override
	public int updateById(Integer id, MateriaDTO newData) {
		if (id == null) {
			return 1;
		}
		return updateByCodigo(id.longValue(), newData);
	}

	/**
	 * Actualiza una materia existente identificada por su codigo unico tipo Long (CU02 / RF26).
	 *
	 * @param codigo  Codigo de la materia a actualizar.
	 * @param newData Nuevos datos de la materia.
	 * @return 0 si se actualizo con exito;
	 *         1 si la materia no existe o el nombre ya pertenece a otra materia;
	 *         2 si los datos nuevos son invalidos o incompletos.
	 */
	public int updateByCodigo(Long codigo, MateriaDTO newData) {
		if (codigo == null) {
			return 1;
		}
		Optional<Materia> foundOpt = materiaRepo.findById(codigo);
		if (foundOpt.isEmpty()) {
			return 1;
		}
		if (newData == null || newData.getNombre() == null || newData.getNombre().trim().isEmpty()
				|| newData.getSemestre() < 1 || newData.getSemestre() > 9
				|| newData.getNcreditos() <= 0 || newData.getHorasDeLaMateria() <= 0) {
			return 2;
		}

		Optional<Materia> porNombre = materiaRepo.findByNombre(newData.getNombre().trim());
		if (porNombre.isPresent() && porNombre.get().getCodigo() != codigo.longValue()) {
			return 1;
		}

		Materia entityToUpdate = foundOpt.get();
		entityToUpdate.setNombre(newData.getNombre().trim());
		entityToUpdate.setSemestre(newData.getSemestre());
		entityToUpdate.setNcreditos(newData.getNcreditos());
		entityToUpdate.setHorasDeLaMateria(newData.getHorasDeLaMateria());
		entityToUpdate.setCorrelativa(newData.isCorrelativa());

		materiaRepo.save(entityToUpdate);
		return 0;
	}

	/**
	 * Cuenta el total de materias registradas.
	 *
	 * @return Cantidad total de materias.
	 */
	@Override
	public long count() {
		return materiaRepo.count();
	}

	/**
	 * Verifica si existe una materia con el ID (codigo) indicado.
	 *
	 * @param id Codigo de la materia.
	 * @return true si existe, false en caso contrario.
	 */
	@Override
	public boolean exist(Integer id) {
		if (id == null) {
			return false;
		}
		return existByCodigo(id.longValue());
	}

	/**
	 * Verifica si existe una materia con el codigo tipo Long indicado.
	 *
	 * @param codigo Codigo unico de la materia.
	 * @return true si existe, false en caso contrario.
	 */
	public boolean existByCodigo(Long codigo) {
		return codigo != null && materiaRepo.existsById(codigo);
	}

	/**
	 * Busca una materia por su codigo unico.
	 *
	 * @param codigo Codigo de la materia.
	 * @return {@link MateriaDTO} si existe, o null si no se encuentra.
	 */
	public MateriaDTO getByCodigo(long codigo) {
		return materiaRepo.findById(codigo).map(this::toDTO).orElse(null);
	}

	/**
	 * Busca una materia por su nombre exacto.
	 *
	 * @param nombre Nombre de la materia.
	 * @return {@link MateriaDTO} si existe, o null si no se encuentra.
	 */
	public MateriaDTO getByNombre(String nombre) {
		if (nombre == null || nombre.trim().isEmpty()) {
			return null;
		}
		return materiaRepo.findByNombre(nombre.trim()).map(this::toDTO).orElse(null);
	}

	/**
	 * Obtiene todas las materias correspondientes a un semestre especifico (1 a 9).
	 *
	 * @param semestre Numero de semestre.
	 * @return Lista de {@link MateriaDTO} del semestre indicado.
	 */
	public List<MateriaDTO> getBySemestre(int semestre) {
		List<MateriaDTO> dtoList = new ArrayList<>();
		List<Materia> entities = materiaRepo.findBySemestre(semestre);
		for (Materia entity : entities) {
			dtoList.add(toDTO(entity));
		}
		return dtoList;
	}

	/**
	 * Obtiene las materias filtradas segun si son correlativas (tienen prerrequisito/correlatividad) o no.
	 *
	 * @param correlativa true para materias correlativas, false en caso contrario.
	 * @return Lista de {@link MateriaDTO} que cumplen el filtro.
	 */
	public List<MateriaDTO> getByCorrelativa(boolean correlativa) {
		List<MateriaDTO> dtoList = new ArrayList<>();
		List<Materia> entities = materiaRepo.findByCorrelativa(correlativa);
		for (Materia entity : entities) {
			dtoList.add(toDTO(entity));
		}
		return dtoList;
	}

	/**
	 * Valida las reglas basicas de integridad de una materia (CU02 - Excepcion: datos incompletos).
	 * Plan de estudios Ing. de Sistemas SNIES 4952: 9 semestres.
	 *
	 * @param data DTO a validar.
	 * @return true si todos los campos obligatorios son validos.
	 */
	private boolean esMateriaValida(MateriaDTO data) {
		if (data == null) {
			return false;
		}
		if (data.getCodigo() <= 0) {
			return false;
		}
		if (data.getNombre() == null || data.getNombre().trim().isEmpty()) {
			return false;
		}
		if (data.getSemestre() < 1 || data.getSemestre() > 9) {
			return false;
		}
		if (data.getNcreditos() <= 0 || data.getHorasDeLaMateria() <= 0) {
			return false;
		}
		return true;
	}

	/**
	 * Convierte una entidad {@link Materia} a {@link MateriaDTO}.
	 *
	 * @param entity Entidad Materia.
	 * @return Objeto MateriaDTO.
	 */
	public MateriaDTO toDTO(Materia entity) {
		if (entity == null) {
			return null;
		}
		return new MateriaDTO(
				entity.getCodigo(),
				entity.getNombre(),
				entity.getSemestre(),
				entity.getNcreditos(),
				entity.getHorasDeLaMateria(),
				entity.isCorrelativa()
		);
	}

	/**
	 * Convierte un {@link MateriaDTO} a entidad {@link Materia}.
	 *
	 * @param dto Objeto MateriaDTO.
	 * @return Entidad Materia.
	 */
	public Materia toEntity(MateriaDTO dto) {
		if (dto == null) {
			return null;
		}
		return new Materia(
				dto.getCodigo(),
				dto.getNombre() != null ? dto.getNombre().trim() : null,
				dto.getSemestre(),
				dto.getNcreditos(),
				dto.getHorasDeLaMateria(),
				dto.isCorrelativa()
		);
	}

}
