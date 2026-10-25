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
import co.edu.unbosque.sdgh.exception.EmptyFieldException;
import co.edu.unbosque.sdgh.exception.InvalidNumberException;
import co.edu.unbosque.sdgh.exception.LanzadorDeExcepcion;
import co.edu.unbosque.sdgh.exception.NullDataException;
import co.edu.unbosque.sdgh.exception.RecordAlreadyExistsException;
import co.edu.unbosque.sdgh.exception.SubjectNotFoundException;
import co.edu.unbosque.sdgh.repository.MateriaRepository;

/**
 * Servicio encargado de gestionar las operaciones CRUD de las materias del plan
 * de estudios (CU02 - RF01, RF26).
 */
@Service
public class MateriaService implements CRUDOperation<MateriaDTO> {

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
	 * Metodo que crea una nueva materia validando unicidad de codigo y nombre, asi
	 * como los datos obligatorios (CU02 / RF01).
	 * @param newData DTO con la informacion de la materia a crear.
	 * @return Retorna 0 si la materia fue creada correctamente, 1 si el
	 *         codigo o el nombre ya existen o 2 si los datos estan
	 *         incompletos o fuera de rango.
	 */
	@Override
	public int create(MateriaDTO newData) {

		if (!esMateriaValida(newData)) {
			return 2;
		}

		try {
			LanzadorDeExcepcion.verificarPositivo(newData.getCodigo());
		} catch (InvalidNumberException e) {
			System.out.println("Error de codigo: " + e.getMessage());
			return 2;
		}

		try {
			LanzadorDeExcepcion.revisarSiRegistroYaExiste(materiaRepo.existsById(newData.getCodigo()),
					"codigo " + newData.getCodigo());
		} catch (RecordAlreadyExistsException e) {
			System.out.println("Error de codigo: " + e.getMessage());
			return 1;
		}

		try {
			LanzadorDeExcepcion.revisarSiRegistroYaExiste(
					materiaRepo.findByNombre(newData.getNombre().trim()).isPresent(),
					"nombre " + newData.getNombre().trim());
		} catch (RecordAlreadyExistsException e) {
			System.out.println("Error de nombre: " + e.getMessage());
			return 1;
		}

		Materia entity = mapper.map(newData, Materia.class);
		entity.setNombre(newData.getNombre().trim());
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

	/**
	 * Metodo que elimina una materia mediante su identificador (codigo) recibido
	 * como Integer (RF26).
	 * @param id Codigo de la materia a eliminar.
	 * @return Retorna 0 si la materia fue eliminada correctamente o 1 si
	 *         la materia no existe o el identificador es nulo.
	 */
	@Override
	public int deleteById(Integer id) {

		if (id == null) {
			return 1;
		}

		return deleteByCodigo(id.longValue());
	}

	/**
	 * Metodo que elimina una materia mediante su codigo unico de tipo Long (RF26).
	 * @param codigo Codigo unico de la materia a eliminar.
	 * @return Retorna 0 si la materia fue eliminada correctamente o 1 si
	 *         la materia no existe o el codigo es nulo.
	 */
	public int deleteByCodigo(Long codigo) {

		if (codigo == null) {
			return 1;
		}

		Optional<Materia> encontrado = materiaRepo.findById(codigo);

		try {
			LanzadorDeExcepcion.revisarSiExisteMateria(encontrado);
		} catch (SubjectNotFoundException e) {
			System.out.println("Error de materia: " + e.getMessage());
			return 1;
		}

		materiaRepo.delete(encontrado.get());
		return 0;
	}

	/**
	 * Metodo que actualiza una materia mediante su identificador (codigo) recibido
	 * como Integer (CU02 / RF26).
	 * @param id Codigo de la materia a actualizar.
	 * @param newData DTO con la nueva informacion de la materia.
	 * @return Retorna 0 si la actualizacion fue exitosa, 1 si la materia no
	 *         existe o el nombre pertenece a otra materia, o 2 si los datos
	 *         nuevos son invalidos o estan incompletos.
	 */
	@Override
	public int updateById(Integer id, MateriaDTO newData) {

		if (id == null) {
			return 1;
		}

		return updateByCodigo(id.longValue(), newData);
	}

	/**
	 * Metodo que actualiza una materia mediante su codigo unico de tipo Long (CU02 / RF26).
	 * @param codigo Codigo de la materia a actualizar.
	 * @param newData DTO con la nueva informacion de la materia.
	 * @return Retorna 0 si la actualizacion fue exitosa, 1 si la materia no
	 *         existe o el nombre ya pertenece a otra materia, o 2 si los
	 *         datos nuevos son invalidos o estan incompletos.
	 */
	public int updateByCodigo(Long codigo, MateriaDTO newData) {

		if (codigo == null) {
			return 1;
		}

		Optional<Materia> encontrado = materiaRepo.findById(codigo);

		try {
			LanzadorDeExcepcion.revisarSiExisteMateria(encontrado);
		} catch (SubjectNotFoundException e) {
			System.out.println("Error de materia: " + e.getMessage());
			return 1;
		}

		if (!esMateriaValida(newData)) {
			return 2;
		}

		Optional<Materia> porNombre = materiaRepo.findByNombre(newData.getNombre().trim());

		try {
			LanzadorDeExcepcion.revisarSiRegistroYaExiste(
					porNombre.isPresent() && porNombre.get().getCodigo() != codigo.longValue(),
					"nombre " + newData.getNombre().trim());
		} catch (RecordAlreadyExistsException e) {
			System.out.println("Error de nombre: " + e.getMessage());
			return 1;
		}

		Materia temp = encontrado.get();
		temp.setNombre(newData.getNombre().trim());
		temp.setSemestre(newData.getSemestre());
		temp.setNcreditos(newData.getNcreditos());
		temp.setHorasDeLaMateria(newData.getHorasDeLaMateria());
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
	 * Metodo que verifica si una materia existe mediante su identificador (codigo)
	 * recibido como Integer.
	 * @param id Codigo de la materia a verificar.
	 * @return Retorna true si la materia existe o false en caso contrario.
	 */
	@Override
	public boolean exist(Integer id) {

		if (id == null) {
			return false;
		}

		return existByCodigo(id.longValue());
	}

	/**
	 * Metodo que verifica si una materia existe mediante su codigo de tipo Long.
	 * @param codigo Codigo unico de la materia a verificar.
	 * @return Retorna true si la materia existe o false en caso contrario.
	 */
	public boolean existByCodigo(Long codigo) {

		return codigo != null && materiaRepo.existsById(codigo);
	}

	/**
	 * Metodo que busca una materia mediante su codigo unico.
	 * @param codigo Codigo de la materia.
	 * @return Retorna la materia en formato DTO o null si no se encuentra.
	 */
	public MateriaDTO getByCodigo(long codigo) {

		return materiaRepo.findById(codigo).map((entidad) -> mapper.map(entidad, MateriaDTO.class)).orElse(null);
	}

	/**
	 * Metodo que busca una materia mediante su nombre exacto.
	 * @param nombre Nombre de la materia.
	 * @return Retorna la materia en formato DTO o null si no se encuentra.
	 */
	public MateriaDTO getByNombre(String nombre) {

		if (nombre == null || nombre.trim().isEmpty()) {
			return null;
		}

		return materiaRepo.findByNombre(nombre.trim()).map((entidad) -> mapper.map(entidad, MateriaDTO.class))
				.orElse(null);
	}

	/**
	 * Metodo que obtiene todas las materias correspondientes a un semestre especifico (1 a 9).
	 * @param semestre Numero de semestre.
	 * @return Retorna una lista de materias en formato DTO del semestre indicado.
	 */
	public List<MateriaDTO> getBySemestre(int semestre) {

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
	 * Metodo que valida las reglas basicas de integridad de una materia
	 * (CU02 - Excepcion: datos incompletos). Plan de estudios Ing. de Sistemas
	 * SNIES 4952: 9 semestres.
	 * @param data DTO de la materia a validar.
	 * @return Retorna true si todos los campos obligatorios son validos o false
	 *         en caso contrario.
	 */
	private boolean esMateriaValida(MateriaDTO data) {

		try {
			LanzadorDeExcepcion.verificarDatosNoNulos(data);
		} catch (NullDataException e) {
			System.out.println("Error de materia: " + e.getMessage());
			return false;
		}

		try {
			LanzadorDeExcepcion.verificarCampoVacio(data.getNombre());
		} catch (EmptyFieldException e) {
			System.out.println("Error de nombre: " + e.getMessage());
			return false;
		}

		try {
			LanzadorDeExcepcion.verificarRango(data.getSemestre(), 1, 9);
		} catch (InvalidNumberException e) {
			System.out.println("Error de semestre: " + e.getMessage());
			return false;
		}

		try {
			LanzadorDeExcepcion.verificarPositivo(data.getNcreditos());
		} catch (InvalidNumberException e) {
			System.out.println("Error de creditos: " + e.getMessage());
			return false;
		}

		try {
			LanzadorDeExcepcion.verificarPositivo(data.getHorasDeLaMateria());
		} catch (InvalidNumberException e) {
			System.out.println("Error de horas: " + e.getMessage());
			return false;
		}

		return true;
	}
}