/**
 * Paquete que contiene los servicios del aplicativo.
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
import co.edu.unbosque.sdgh.entity.Grupo;
import co.edu.unbosque.sdgh.entity.Materia;
import co.edu.unbosque.sdgh.exception.DatosInvalidosException;
import co.edu.unbosque.sdgh.exception.LimiteSesionesExcedidoException;
import co.edu.unbosque.sdgh.exception.RecursoDuplicadoException;
import co.edu.unbosque.sdgh.exception.RecursoNoEncontradoException;
import co.edu.unbosque.sdgh.repository.GrupoRepository;
import co.edu.unbosque.sdgh.repository.MateriaRepository;
import co.edu.unbosque.sdgh.repository.SesionRepository;

/**
 * Servicio encargado de gestionar las operaciones CRUD de las materias del
 * plan de estudios (CU02 - RF01, RF26). Realiza validaciones sobre los datos
 * obligatorios, la unicidad de los registros, el semestre, los creditos y las
 * horas de cada materia. Las validaciones se realizan al inicio de cada metodo
 * y utilizan excepciones personalizadas que son capturadas en el controlador.
 *
 * @author Sebastian Toyo
 */
@Service
public class MateriaService implements CRUDOperation<MateriaDTO> {

	/**
	 * Semestre minimo permitido dentro del plan de estudios.
	 */
	private static final int SEMESTRE_MINIMO = 1;

	/**
	 * Semestre maximo permitido dentro del plan de estudios (Ing. de Sistemas SNIES 4952).
	 */
	private static final int SEMESTRE_MAXIMO = 9;

	/**
	 * Numero minimo de sesiones semanales que debe tener una materia.
	 */
	private static final int SESIONES_MINIMAS = 1;

	/**
	 * Cantidad de horas que debe durar cada sesion de una materia.
	 */
	private static final int HORAS_POR_SESION = 2;

	/**
	 * Repositorio encargado de las operaciones de persistencia de materias.
	 */
	@Autowired
	private MateriaRepository materiaRepo;

	/**
	 * Repositorio encargado de consultar y gestionar los grupos asociados
	 * a las materias.
	 */
	@Autowired
	private GrupoRepository grupoRepo;

	/**
	 * Servicio encargado de consultar y actualizar los parametros del sistema.
	 */
	@Autowired
	private ParametroService parametroServ;

	/**
	 * Repositorio encargado de consultar las sesiones asociadas a los grupos.
	 */
	@Autowired
	private SesionRepository sesionRepo;

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
	 * Metodo que crea una nueva materia validando los datos obligatorios,
	 * la unicidad del codigo y del nombre, el semestre, los creditos y las
	 * horas permitidas (CU02 / RF01).
	 *
	 * @param newData DTO con la informacion de la materia que se desea crear.
	 * @return Retorna 0 si la materia fue creada correctamente.
	 * @throws DatosInvalidosException si los datos son nulos, estan incompletos
	 *         o se encuentran fuera de los rangos permitidos.
	 * @throws RecursoDuplicadoException si el codigo o el nombre ya existen.
	 */
	@Override
	public int create(MateriaDTO newData) {

		if (newData == null) {
			throw new DatosInvalidosException("Los datos de la materia no pueden ser nulos");
		}
		if (newData.getCodigo() <= 0 || newData.getCodigo() > 9999999999L) {
			throw new DatosInvalidosException("El codigo del curso debe ser un numero positivo de hasta 10 digitos");
		}
		if (newData.getNombre() == null || newData.getNombre().trim().isEmpty()) {
			throw new DatosInvalidosException("El nombre de la materia no puede estar vacio");
		}
		if (materiaRepo.existsByCodigo(newData.getCodigo())) {
			throw new RecursoDuplicadoException("Ya existe una materia con el codigo " + newData.getCodigo());
		}
		if (newData.getSemestre() < SEMESTRE_MINIMO || newData.getSemestre() > SEMESTRE_MAXIMO) {
			throw new DatosInvalidosException(
					"El semestre debe estar entre " + SEMESTRE_MINIMO + " y " + SEMESTRE_MAXIMO);
		}
		if (newData.getNcreditos() <= 0) {
			throw new DatosInvalidosException("El numero de creditos debe ser mayor a 0");
		}
		if (newData.getHorasDeLaMateria() <= 0) {
			throw new DatosInvalidosException("Las horas de la materia deben ser mayores a 0");
		}
		if (newData.getHorasDeLaMateria() % HORAS_POR_SESION != 0) {
			throw new DatosInvalidosException(
					"Las horas deben ser multiplo de " + HORAS_POR_SESION + " (sesiones de 2 horas)");
		}

		int sesionesMaximas = parametroServ.getEntero(ClaveParametro.SESIONES_MAXIMAS);
		int sesiones = newData.getHorasDeLaMateria() / HORAS_POR_SESION;
		if (sesiones < SESIONES_MINIMAS || sesiones > sesionesMaximas) {
			throw new DatosInvalidosException("El curso tendria " + sesiones
					+ " sesiones semanales y debe tener entre " + SESIONES_MINIMAS + " y " + sesionesMaximas);
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
	 * Metodo que obtiene todas las materias registradas y convierte sus
	 * datos al formato DTO (RF26).
	 *
	 * @return Lista con todas las materias registradas en formato DTO.
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



	/**
	 	 *
	 * @param id Identificador de la materia que se desea eliminar.
	 * @return Retorna 0 si la materia fue eliminada correctamente.
	 * @throws DatosInvalidosException si el identificador es nulo o la materia
	 *         tiene grupos asociados.
	 * @throws RecursoNoEncontradoException si la materia no existe.
	 */
	@Override
	public int deleteById(Long id) {

		if (!grupoRepo.findByMateriaId(id).isEmpty()) {
			throw new DatosInvalidosException("No se puede eliminar: el curso tiene grupos");
		}
		if (id == null) {
			throw new DatosInvalidosException("El id de la materia no puede ser nulo");
		}

		Materia encontrada = materiaRepo.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe una materia con el id " + id));

		materiaRepo.delete(encontrada);
		parametroServ.marcarPublicado(false);
		return 0;
	}

	/**
	 	 *
	 * @param id Identificador de la materia que se desea actualizar.
	 * @param newData DTO con la nueva informacion de la materia.
	 * @return Retorna 0 si la actualizacion fue exitosa.
	 * @throws DatosInvalidosException si el id es nulo, los datos son invalidos
	 *         o existen restricciones por grupos y sesiones asignadas.
	 * @throws RecursoNoEncontradoException si la materia no existe.
	 * @throws RecursoDuplicadoException si el codigo o nombre ya pertenece a
	 *         otra materia.
	 * @throws LimiteSesionesExcedidoException si algun grupo tiene mas sesiones
	 *         asignadas que las permitidas por las nuevas horas de la materia.
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
		if (newData.getCodigo() <= 0 || newData.getCodigo() > 9999999999L) {
			throw new DatosInvalidosException("El codigo del curso debe ser un numero positivo de hasta 10 digitos");
		}
		Optional<Materia> porCodigo = materiaRepo.findByCodigo(newData.getCodigo());
		if (porCodigo.isPresent() && porCodigo.get().getId() != id.longValue()) {
			throw new RecursoDuplicadoException("Ya existe otra materia con el codigo " + newData.getCodigo());
		}
		if (newData.getSemestre() < SEMESTRE_MINIMO || newData.getSemestre() > SEMESTRE_MAXIMO) {
			throw new DatosInvalidosException(
					"El semestre debe estar entre " + SEMESTRE_MINIMO + " y " + SEMESTRE_MAXIMO);
		}
		if (newData.getNcreditos() <= 0) {
			throw new DatosInvalidosException("El numero de creditos debe ser mayor a 0");
		}
		if (newData.getHorasDeLaMateria() <= 0) {
			throw new DatosInvalidosException("Las horas de la materia deben ser mayores a 0");
		}
		if (newData.getHorasDeLaMateria() % HORAS_POR_SESION != 0) {
			throw new DatosInvalidosException(
					"Las horas deben ser multiplo de " + HORAS_POR_SESION + " (sesiones de 2 horas)");
		}

		int sesionesMaximas = parametroServ.getEntero(ClaveParametro.SESIONES_MAXIMAS);
		int sesiones = newData.getHorasDeLaMateria() / HORAS_POR_SESION;
		if (sesiones < SESIONES_MINIMAS || sesiones > sesionesMaximas) {
			throw new DatosInvalidosException("El curso tendria " + sesiones
					+ " sesiones semanales y debe tener entre " + SESIONES_MINIMAS + " y " + sesionesMaximas);
		}

		String nombre = newData.getNombre().trim();
		Optional<Materia> porNombre = materiaRepo.findByNombre(nombre);

		if (porNombre.isPresent() && porNombre.get().getId() != id.longValue()) {
			throw new RecursoDuplicadoException("Ya existe otra materia con el nombre " + nombre);
		}

		if (newData.getSemestre() != temp.getSemestre()) {
			for (Grupo grupo : grupoRepo.findByMateriaId(id)) {
				if (!sesionRepo.findByGrupoId(grupo.getId()).isEmpty()) {
					throw new DatosInvalidosException("No se puede cambiar el semestre: el curso ya tiene sesiones asignadas");
				}
			}
		}

		for (Grupo grupo : grupoRepo.findByMateriaId(id)) {
			int asignadas = sesionRepo.findByGrupoId(grupo.getId()).size();
			if (asignadas > sesiones) {
				throw new LimiteSesionesExcedidoException("El grupo " + grupo.getNumeroGrupo() + " ya tiene "
						+ asignadas + " sesiones asignadas y el curso solo tendria " + sesiones
						+ ". Quite sesiones antes de reducir las horas");
			}
		}

		temp.setCodigo(newData.getCodigo());
		temp.setNombre(nombre);
		temp.setSemestre(newData.getSemestre());
		temp.setNcreditos(newData.getNcreditos());
		temp.setHorasDeLaMateria(newData.getHorasDeLaMateria());
		temp.setCorrelativa(newData.isCorrelativa());
		materiaRepo.save(temp);
		parametroServ.marcarPublicado(false);
		return 0;
	}

	/**
	 * Metodo que cuenta la cantidad total de materias registradas.
	 *
	 * @return Cantidad total de materias.
	 */
	@Override
	public long count() {

		return materiaRepo.count();
	}

	

	/**
	 * Metodo que verifica si una materia existe mediante su identificador
	 * de tipo Long.
	 *
	 * @param id Identificador de la materia que se desea verificar.
	 * @return true si la materia existe o false en caso contrario.
	 */
	@Override
	public boolean existById(Long id) {

		return id != null && materiaRepo.existsById(id);
	}

	/**
	 * Metodo que busca una materia mediante su identificador.
	 *
	 * @param id Identificador de la materia que se desea consultar.
	 * @return Informacion de la materia en formato DTO.
	 * @throws RecursoNoEncontradoException si la materia no existe.
	 */
	public MateriaDTO getById(long id) {

		Materia encontrada = materiaRepo.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe una materia con el codigo " + id));

		return mapper.map(encontrada, MateriaDTO.class);
	}

	/**
	 * Metodo que busca una materia mediante su nombre exacto.
	 *
	 * @param nombre Nombre de la materia que se desea consultar.
	 * @return Informacion de la materia en formato DTO.
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
	 * Metodo que obtiene todas las materias correspondientes a un semestre
	 * especifico dentro del rango permitido.
	 *
	 * @param semestre Numero del semestre que se desea consultar.
	 * @return Lista de materias del semestre indicado en formato DTO.
	 * @throws DatosInvalidosException si el semestre esta fuera del rango permitido.
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
	 * Metodo que obtiene las materias filtradas segun su condicion de
	 * correlativa, es decir, si tienen prerrequisito o no.
	 *
	 * @param correlativa true para consultar materias correlativas o false
	 *                    para consultar materias no correlativas.
	 * @return Lista de materias que cumplen el filtro en formato DTO.
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
	 * Metodo que busca una materia mediante su codigo institucional.
	 *
	 * @param codigo Codigo institucional de la materia que se desea consultar.
	 * @return Informacion de la materia en formato DTO.
	 * @throws RecursoNoEncontradoException si no existe una materia con ese codigo.
	 */
	public MateriaDTO getByCodigo(long codigo) {

		Materia encontrada = materiaRepo.findByCodigo(codigo).orElseThrow(
				() -> new RecursoNoEncontradoException("No existe una materia con el codigo " + codigo));

		return mapper.map(encontrada, MateriaDTO.class);
	}

	/**
	 * Metodo que elimina una materia mediante su codigo institucional.
	 * No permite eliminarla si tiene grupos asociados.
	 *
	 * @param codigo Codigo institucional de la materia que se desea eliminar.
	 * @return Retorna 0 si la materia fue eliminada correctamente.
	 * @throws RecursoNoEncontradoException si no existe una materia con ese codigo.
	 * @throws DatosInvalidosException si la materia tiene grupos asociados.
	 */
	public int deleteByCodigo(long codigo) {

		Materia encontrada = materiaRepo.findByCodigo(codigo).orElseThrow(
				() -> new RecursoNoEncontradoException("No existe una materia con el codigo " + codigo));

		if (!grupoRepo.findByMateriaId(encontrada.getId()).isEmpty()) {
			throw new DatosInvalidosException("No se puede eliminar: el curso tiene grupos");
		}

		materiaRepo.delete(encontrada);
		parametroServ.marcarPublicado(false);
		return 0;
	}
}