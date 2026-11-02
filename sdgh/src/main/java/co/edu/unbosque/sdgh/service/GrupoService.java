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

import co.edu.unbosque.sdgh.dto.GrupoDTO;
import co.edu.unbosque.sdgh.entity.Grupo;
import co.edu.unbosque.sdgh.entity.Sesion;
import co.edu.unbosque.sdgh.exception.CupoExcedidoException;
import co.edu.unbosque.sdgh.exception.DatosInvalidosException;
import co.edu.unbosque.sdgh.exception.RecursoDuplicadoException;
import co.edu.unbosque.sdgh.exception.RecursoNoEncontradoException;
import co.edu.unbosque.sdgh.repository.GrupoRepository;
import co.edu.unbosque.sdgh.repository.MateriaRepository;
import co.edu.unbosque.sdgh.repository.SesionRepository;

/**
 * Servicio encargado de gestionar las operaciones CRUD de los grupos de un
 * curso (RF2). Valida el cupo planeado contra el limite institucional (40 mas
 * 10 % de tolerancia). Las reglas de horario (franja, pausa, cruce y limite de
 * sesiones) corresponden a SesionService. Las validaciones se hacen al inicio
 * de cada metodo y lanzan excepciones personalizadas que son capturadas en el
 * controlador.
 */
@Service
public class GrupoService implements CRUDOperation<GrupoDTO> {

	/**
	 * Cupo base por grupo antes de aplicar tolerancia (RF2).
	 */
	private static final int CUPO_BASE_MAXIMO = 40;

	/**
	 * Porcentaje de tolerancia sobre el cupo (10 %) (RF2).
	 */
	private static final double TOLERANCIA_CUPO = 0.10;

	/**
	 * Repositorio encargado de las operaciones de persistencia de grupos.
	 */
	@Autowired
	private GrupoRepository grupoRepo;

	/**
	 * Repositorio de materias, usado para verificar que el curso exista.
	 */
	@Autowired
	private MateriaRepository materiaRepo;

	/**
	 * Repositorio de sesiones, usado para borrar las sesiones de un grupo.
	 */
	@Autowired
	private SesionRepository sesionRepo;

	/**
	 * Mapper utilizado para convertir entidades en DTO y viceversa.
	 */
	@Autowired
	private ModelMapper mapper;

	/**
	 * Constructor vacio de la clase GrupoService.
	 */
	public GrupoService() {
	}

	/**
	 * Metodo que crea un nuevo grupo de un curso validando los datos
	 * obligatorios, que el curso exista, que el numero de grupo no se repita
	 * dentro del curso y que el cupo no supere el limite institucional (RF2).
	 * @param newData DTO con la informacion del grupo a crear.
	 * @return Retorna 0 si el grupo fue creado correctamente.
	 * @throws DatosInvalidosException si los datos son nulos, estan incompletos o
	 *         fuera de rango.
	 * @throws RecursoNoEncontradoException si el curso no existe.
	 * @throws RecursoDuplicadoException si el curso ya tiene un grupo con ese
	 *         numero.
	 * @throws CupoExcedidoException si el cupo planeado supera el limite
	 *         institucional.
	 */
	@Override
	public int create(GrupoDTO newData) {

		if (newData == null) {
			throw new DatosInvalidosException("Los datos del grupo no pueden ser nulos");
		}
		if (newData.getMateriaId() <= 0) {
			throw new DatosInvalidosException("El id de la materia debe ser un numero positivo");
		}
		if (newData.getNumeroGrupo() <= 0) {
			throw new DatosInvalidosException("El numero de grupo debe ser un numero positivo");
		}
		if (newData.getCupoPlaneado() <= 0) {
			throw new DatosInvalidosException("El cupo planeado debe ser un numero positivo");
		}

		int limiteCupo = (int) Math.floor(CUPO_BASE_MAXIMO * (1.0 + TOLERANCIA_CUPO));

		if (newData.getCupoPlaneado() > limiteCupo) {
			throw new CupoExcedidoException("El cupo del grupo (" + newData.getCupoPlaneado()
					+ ") supera el limite institucional permitido con tolerancia del 10 % (" + limiteCupo + ")");
		}
		if (!materiaRepo.existsById(newData.getMateriaId())) {
			throw new RecursoNoEncontradoException("No existe una materia con el codigo " + newData.getMateriaId());
		}
		if (grupoRepo.existsByMateriaIdAndNumeroGrupo(newData.getMateriaId(), newData.getNumeroGrupo())) {
			throw new RecursoDuplicadoException(
					"La materia " + newData.getMateriaId() + " ya tiene el grupo " + newData.getNumeroGrupo());
		}

		Grupo entity = mapper.map(newData, Grupo.class);
		grupoRepo.save(entity);
		return 0;
	}

	/**
	 * Metodo que obtiene todos los grupos registrados.
	 * @return Retorna una lista con todos los grupos en formato DTO.
	 */
	@Override
	public List<GrupoDTO> getAll() {

		List<Grupo> entityList = (List<Grupo>) grupoRepo.findAll();
		List<GrupoDTO> dtoList = new ArrayList<>();

		entityList.forEach((entidad) -> {

			GrupoDTO dto = mapper.map(entidad, GrupoDTO.class);
			dtoList.add(dto);
		});

		return dtoList;
	}

	/**
	 * Metodo que elimina un grupo junto con sus sesiones.
	 * @param id Identificador del grupo a eliminar.
	 * @return Retorna 0 si el grupo fue eliminado correctamente.
	 * @throws DatosInvalidosException si el identificador es nulo.
	 * @throws RecursoNoEncontradoException si el grupo no existe.
	 */
	@Override
	public int deleteById(Long id) {

		if (id == null) {
			throw new DatosInvalidosException("El id del grupo no puede ser nulo");
		}

		Grupo encontrado = grupoRepo.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe un grupo con el id " + id));

		List<Sesion> sesiones = sesionRepo.findByGrupoId(id);
		sesionRepo.deleteAll(sesiones);
		grupoRepo.delete(encontrado);
		return 0;
	}

	/**
	 * Metodo que actualiza el numero de grupo y el cupo planeado de un grupo
	 * existente. El curso del grupo no se modifica (RF2).
	 * @param id Identificador del grupo a actualizar.
	 * @param newData DTO con la nueva informacion del grupo.
	 * @return Retorna 0 si la actualizacion fue exitosa.
	 * @throws DatosInvalidosException si el id es nulo o los datos nuevos son
	 *         nulos, estan incompletos o fuera de rango.
	 * @throws RecursoNoEncontradoException si el grupo no existe.
	 * @throws RecursoDuplicadoException si el curso ya tiene otro grupo con ese
	 *         numero.
	 * @throws CupoExcedidoException si el cupo planeado supera el limite
	 *         institucional.
	 */
	@Override
	public int updateById(Long id, GrupoDTO newData) {

		if (id == null) {
			throw new DatosInvalidosException("El id del grupo no puede ser nulo");
		}

		Grupo temp = grupoRepo.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe un grupo con el id " + id));

		if (newData == null) {
			throw new DatosInvalidosException("Los datos del grupo no pueden ser nulos");
		}
		if (newData.getNumeroGrupo() <= 0) {
			throw new DatosInvalidosException("El numero de grupo debe ser un numero positivo");
		}
		if (newData.getCupoPlaneado() <= 0) {
			throw new DatosInvalidosException("El cupo planeado debe ser un numero positivo");
		}

		int limiteCupo = (int) Math.floor(CUPO_BASE_MAXIMO * (1.0 + TOLERANCIA_CUPO));

		if (newData.getCupoPlaneado() > limiteCupo) {
			throw new CupoExcedidoException("El cupo del grupo (" + newData.getCupoPlaneado()
					+ ") supera el limite institucional permitido con tolerancia del 10 % (" + limiteCupo + ")");
		}

		Optional<Grupo> mismoNumero = grupoRepo.findByMateriaIdAndNumeroGrupo(temp.getMateriaId(),
				newData.getNumeroGrupo());

		if (mismoNumero.isPresent() && mismoNumero.get().getId() != id.longValue()) {
			throw new RecursoDuplicadoException(
					"La materia " + temp.getMateriaId() + " ya tiene el grupo " + newData.getNumeroGrupo());
		}

		temp.setNumeroGrupo(newData.getNumeroGrupo());
		temp.setCupoPlaneado(newData.getCupoPlaneado());
		grupoRepo.save(temp);
		return 0;
	}

	/**
	 * Metodo que cuenta la cantidad total de grupos registrados.
	 * @return Retorna la cantidad total de grupos.
	 */
	@Override
	public long count() {

		return grupoRepo.count();
	}

	/**
	 * Metodo que verifica si un grupo existe mediante su identificador.
	 * @param id Identificador del grupo a verificar.
	 * @return Retorna true si el grupo existe o false en caso contrario.
	 */
	@Override
	public boolean exist(Long id) {

		return id != null && grupoRepo.existsById(id);
	}

	/**
	 * Metodo que busca un grupo mediante su identificador.
	 * @param id Identificador del grupo.
	 * @return Retorna el grupo en formato DTO.
	 * @throws DatosInvalidosException si el identificador es nulo.
	 * @throws RecursoNoEncontradoException si el grupo no existe.
	 */
	public GrupoDTO getById(Long id) {

		if (id == null) {
			throw new DatosInvalidosException("El id del grupo no puede ser nulo");
		}

		Grupo encontrado = grupoRepo.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe un grupo con el id " + id));

		return mapper.map(encontrado, GrupoDTO.class);
	}

	/**
	 * Metodo que obtiene todos los grupos asociados a una materia.
	 * @param materiaId Codigo de la materia.
	 * @return Retorna una lista de grupos en formato DTO de la materia.
	 * @throws DatosInvalidosException si el codigo de la materia no es positivo.
	 */
	public List<GrupoDTO> getByMateriaId(long materiaId) {

		if (materiaId <= 0) {
			throw new DatosInvalidosException("El id de la materia debe ser un numero positivo");
		}

		List<Grupo> entityList = grupoRepo.findByMateriaId(materiaId);
		List<GrupoDTO> dtoList = new ArrayList<>();

		entityList.forEach((entidad) -> {

			GrupoDTO dto = mapper.map(entidad, GrupoDTO.class);
			dtoList.add(dto);
		});

		return dtoList;
	}
}