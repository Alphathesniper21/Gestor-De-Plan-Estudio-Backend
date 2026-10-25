/**
 * Paquete de servicios del aplicativo.
 */
package co.edu.unbosque.sdgh.service;

import java.text.Normalizer;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.edu.unbosque.sdgh.dto.GrupoDTO;
import co.edu.unbosque.sdgh.entity.Aula;
import co.edu.unbosque.sdgh.entity.Grupo;
import co.edu.unbosque.sdgh.exception.CapacityExceededException;
import co.edu.unbosque.sdgh.exception.EmptyFieldException;
import co.edu.unbosque.sdgh.exception.GroupNotFoundException;
import co.edu.unbosque.sdgh.exception.InvalidNumberException;
import co.edu.unbosque.sdgh.exception.InvalidTimeSlotException;
import co.edu.unbosque.sdgh.exception.LanzadorDeExcepcion;
import co.edu.unbosque.sdgh.exception.NullDataException;
import co.edu.unbosque.sdgh.exception.RecordAlreadyExistsException;
import co.edu.unbosque.sdgh.exception.ScheduleConflictException;
import co.edu.unbosque.sdgh.exception.WeeklySessionLimitException;
import co.edu.unbosque.sdgh.repository.AulaRepository;
import co.edu.unbosque.sdgh.repository.GrupoRepository;

/**
 * Servicio encargado de gestionar las operaciones CRUD de los grupos y la
 * asignacion de horarios academicos (CU05, CU07, CU08, CU10 - RF05 a RF09,
 * RF15 a RF21).
 */
@Service
public class GrupoService implements CRUDOperation<GrupoDTO> {

	/**
	 * Cupo maximo institucional por grupo antes de aplicar tolerancia (RF07).
	 */
	private static final int CUPO_BASE_MAXIMO = 40;

	/**
	 * Porcentaje de tolerancia sobre el cupo (10 %) (RF07).
	 */
	private static final double TOLERANCIA_CUPO = 0.10;

	/**
	 * Minimo de estudiantes inscritos antes de senalar cierre de grupo (RF19).
	 */
	private static final int MINIMO_ESTUDIANTES_GRUPO = 10;

	/**
	 * Maximo de sesiones semanales por materia (RF15).
	 */
	private static final int MAXIMO_SESIONES_SEMANALES = 4;

	/**
	 * Duracion estandar de cada sesion de clase en minutos (2 horas) (RF15).
	 */
	private static final long DURACION_SESION_MINUTOS = 120;

	/**
	 * Hora de apertura institucional de lunes a sabado (07:00) (RF09).
	 */
	private static final LocalTime HORA_INICIO_JORNADA = LocalTime.of(7, 0);

	/**
	 * Hora de cierre institucional de lunes a viernes (22:00) (RF09).
	 */
	private static final LocalTime HORA_FIN_LUNES_VIERNES = LocalTime.of(22, 0);

	/**
	 * Hora de cierre institucional los sabados (13:00) (RF09).
	 */
	private static final LocalTime HORA_FIN_SABADO = LocalTime.of(13, 0);

	/**
	 * Inicio de la franja de almuerzo prohibida (12:00) (RF09).
	 */
	private static final LocalTime HORA_INICIO_ALMUERZO = LocalTime.of(12, 0);

	/**
	 * Fin de la franja de almuerzo prohibida (13:00) (RF09).
	 */
	private static final LocalTime HORA_FIN_ALMUERZO = LocalTime.of(13, 0);

	/**
	 * Repositorio encargado de las operaciones de persistencia de grupos.
	 */
	@Autowired
	private GrupoRepository grupoRepo;

	/**
	 * Repositorio de aulas utilizado para validar la capacidad del aula asignada.
	 */
	@Autowired
	private AulaRepository aulaRepo;

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
	 * Metodo que crea un nuevo grupo y asigna su horario validando las restricciones
	 * duras definidas en CU05: ausencia de cruces de horario de docente y de aula
	 * (RF06), capacidad del aula con tolerancia del 10 % (RF07), franja dentro del
	 * horario institucional y fuera de 12:00 a 13:00 (RF09), y duracion de sesion de
	 * 2 horas con limite de sesiones semanales (RF15).
	 * @param newData DTO con la informacion del grupo a crear.
	 * @return Retorna 0 si el grupo fue creado correctamente, 1 si el
	 *         identificador ya existe o se incumple una restriccion dura de
	 *         cruce, capacidad o franja, o 2 si los datos son nulos o estan
	 *         incompletos.
	 */
	@Override
	public int create(GrupoDTO newData) {

		if (!esGrupoDatosBasicosValidos(newData)) {
			return 2;
		}

		try {
			LanzadorDeExcepcion.revisarSiRegistroYaExiste(grupoRepo.existsById(newData.getIdGrupo()),
					"grupo " + newData.getIdGrupo());
		} catch (RecordAlreadyExistsException e) {
			System.out.println("Error de grupo: " + e.getMessage());
			return 1;
		}

		if (!cumpleRestriccionesDuras(newData, null)) {
			return 1;
		}

		Grupo entity = mapper.map(newData, Grupo.class);
		entity.setDiaSemana(newData.getDiaSemana().trim());
		grupoRepo.save(entity);
		return 0;
	}

	/**
	 * Metodo que obtiene todos los grupos registrados (CU07, CU08 / RF13, RF14).
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
	 * Metodo que elimina un grupo mediante su identificador.
	 * @param id Identificador del grupo a eliminar.
	 * @return Retorna 0 si el grupo fue eliminado correctamente o 1 si el
	 *         grupo no existe o el identificador es nulo.
	 */
	@Override
	public int deleteById(Integer id) {

		if (id == null) {
			return 1;
		}

		Optional<Grupo> encontrado = grupoRepo.findById(id);

		try {
			LanzadorDeExcepcion.revisarSiExisteGrupo(encontrado);
		} catch (GroupNotFoundException e) {
			System.out.println("Error de grupo: " + e.getMessage());
			return 1;
		}

		grupoRepo.delete(encontrado.get());
		return 0;
	}

	/**
	 * Metodo que actualiza un grupo mediante su identificador validando las
	 * restricciones duras de horario, aula y docente (CU05).
	 * @param id Identificador del grupo a actualizar.
	 * @param newData DTO con la nueva informacion del grupo.
	 * @return Retorna 0 si la actualizacion fue exitosa, 1 si el grupo no
	 *         existe o incumple cualquiera de las restricciones duras, o 2 si
	 *         los datos nuevos son nulos o estan incompletos.
	 */
	@Override
	public int updateById(Integer id, GrupoDTO newData) {

		if (id == null) {
			return 1;
		}

		Optional<Grupo> encontrado = grupoRepo.findById(id);

		try {
			LanzadorDeExcepcion.revisarSiExisteGrupo(encontrado);
		} catch (GroupNotFoundException e) {
			System.out.println("Error de grupo: " + e.getMessage());
			return 1;
		}

		if (!esGrupoDatosBasicosValidos(newData)) {
			return 2;
		}

		if (!cumpleRestriccionesDuras(newData, id)) {
			return 1;
		}

		Grupo temp = encontrado.get();
		temp.setMateriaId(newData.getMateriaId());
		temp.setDocenteId(newData.getDocenteId());
		temp.setAulaId(newData.getAulaId());
		temp.setCupoMaximo(newData.getCupoMaximo());
		temp.setEstudiantesInscritos(newData.getEstudiantesInscritos());
		temp.setDiaSemana(newData.getDiaSemana().trim());
		temp.setHoraInicio(newData.getHoraInicio());
		temp.setHoraFin(newData.getHoraFin());
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
	public boolean exist(Integer id) {

		return id != null && grupoRepo.existsById(id);
	}

	/**
	 * Metodo que busca un grupo mediante su identificador.
	 * @param id Identificador del grupo.
	 * @return Retorna el grupo en formato DTO o null si no se encuentra.
	 */
	public GrupoDTO getById(Integer id) {

		if (id == null) {
			return null;
		}

		return grupoRepo.findById(id).map((entidad) -> mapper.map(entidad, GrupoDTO.class)).orElse(null);
	}

	/**
	 * Metodo que obtiene todos los grupos asociados a una materia (CU08 / RF14).
	 * @param materiaId Codigo de la materia.
	 * @return Retorna una lista de grupos en formato DTO de la materia.
	 */
	public List<GrupoDTO> getByMateriaId(long materiaId) {

		List<Grupo> entityList = grupoRepo.findByMateriaId(materiaId);
		List<GrupoDTO> dtoList = new ArrayList<>();

		entityList.forEach((entidad) -> {

			GrupoDTO dto = mapper.map(entidad, GrupoDTO.class);
			dtoList.add(dto);
		});

		return dtoList;
	}

	/**
	 * Metodo que obtiene todos los grupos asignados a un docente (CU07 / RF13).
	 * @param docenteId Codigo del docente.
	 * @return Retorna una lista de grupos en formato DTO del docente.
	 */
	public List<GrupoDTO> getByDocenteId(long docenteId) {

		List<Grupo> entityList = grupoRepo.findByDocenteId(docenteId);
		List<GrupoDTO> dtoList = new ArrayList<>();

		entityList.forEach((entidad) -> {

			GrupoDTO dto = mapper.map(entidad, GrupoDTO.class);
			dtoList.add(dto);
		});

		return dtoList;
	}

	/**
	 * Metodo que obtiene todos los grupos programados en un aula especifica.
	 * @param aulaId Identificador del aula.
	 * @return Retorna una lista de grupos en formato DTO del aula.
	 */
	public List<GrupoDTO> getByAulaId(long aulaId) {

		List<Grupo> entityList = grupoRepo.findByAulaId(aulaId);
		List<GrupoDTO> dtoList = new ArrayList<>();

		entityList.forEach((entidad) -> {

			GrupoDTO dto = mapper.map(entidad, GrupoDTO.class);
			dtoList.add(dto);
		});

		return dtoList;
	}

	/**
	 * Metodo que obtiene los grupos con menos de 10 estudiantes inscritos para su
	 * senalamiento y posible cierre (RF19).
	 * @return Retorna una lista de grupos en formato DTO con menos del minimo de
	 *         estudiantes requeridos.
	 */
	public List<GrupoDTO> getGruposParaCierre() {

		return getGruposParaCierre(MINIMO_ESTUDIANTES_GRUPO);
	}

	/**
	 * Metodo que obtiene los grupos cuyo numero de estudiantes inscritos es menor
	 * que el parametro indicado (RF19).
	 * @param minimoEstudiantes Cantidad minima requerida para mantener abierto el grupo.
	 * @return Retorna una lista de grupos en formato DTO por debajo del minimo.
	 */
	public List<GrupoDTO> getGruposParaCierre(int minimoEstudiantes) {

		List<Grupo> entityList = (List<Grupo>) grupoRepo.findAll();
		List<GrupoDTO> dtoList = new ArrayList<>();

		entityList.forEach((entidad) -> {

			if (entidad.getEstudiantesInscritos() < minimoEstudiantes) {
				dtoList.add(mapper.map(entidad, GrupoDTO.class));
			}
		});

		return dtoList;
	}

	/**
	 * Metodo que calcula el total de horas semanales asignadas a un docente (RF18).
	 * @param docenteId Codigo del docente.
	 * @return Retorna el total de horas semanales programadas para el docente.
	 */
	public long calcularHorasSemanalesDocente(long docenteId) {

		long totalMinutos = 0;
		List<Grupo> grupos = grupoRepo.findByDocenteId(docenteId);

		for (Grupo g : grupos) {

			if (g.getHoraInicio() != null && g.getHoraFin() != null && g.getHoraInicio().isBefore(g.getHoraFin())) {
				totalMinutos += Duration.between(g.getHoraInicio(), g.getHoraFin()).toMinutes();
			}
		}

		return totalMinutos / 60;
	}

	/**
	 * Metodo que verifica si existe cruce de horario para el docente o el aula en
	 * el dia y franja indicados (RF06).
	 * @param data DTO del grupo a evaluar.
	 * @param idGrupoExcluir Identificador del grupo a ignorar en caso de
	 *        actualizacion (o null si es creacion).
	 * @return Retorna true si hay cruce de horario o false si no hay conflicto.
	 */
	public boolean tieneCruceDeHorario(GrupoDTO data, Integer idGrupoExcluir) {

		String diaNuevo = normalizarDia(data.getDiaSemana());

		List<Grupo> gruposDocente = grupoRepo.findByDocenteId(data.getDocenteId());

		for (Grupo existente : gruposDocente) {

			if (idGrupoExcluir != null && existente.getIdGrupo() == idGrupoExcluir.intValue()) {
				continue;
			}

			if (diaNuevo.equals(normalizarDia(existente.getDiaSemana())) && seSolapan(data.getHoraInicio(),
					data.getHoraFin(), existente.getHoraInicio(), existente.getHoraFin())) {
				return true;
			}
		}

		List<Grupo> gruposAula = grupoRepo.findByAulaId(data.getAulaId());

		for (Grupo existente : gruposAula) {

			if (idGrupoExcluir != null && existente.getIdGrupo() == idGrupoExcluir.intValue()) {
				continue;
			}

			if (diaNuevo.equals(normalizarDia(existente.getDiaSemana())) && seSolapan(data.getHoraInicio(),
					data.getHoraFin(), existente.getHoraInicio(), existente.getHoraFin())) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Metodo que verifica si la franja del grupo respeta el horario institucional
	 * (L-V 07:00-22:00, Sab 07:00-13:00), excluye la hora de almuerzo (12:00-13:00)
	 * y dura 2 horas (RF09, RF15).
	 * @param diaSemana Dia de la semana.
	 * @param horaInicio Hora de inicio de la sesion.
	 * @param horaFin Hora de fin de la sesion.
	 * @return Retorna true si la franja es valida o false en caso contrario.
	 */
	public boolean esFranjaInstitucionalValida(String diaSemana, LocalTime horaInicio, LocalTime horaFin) {

		if (diaSemana == null || horaInicio == null || horaFin == null || !horaInicio.isBefore(horaFin)) {
			return false;
		}

		long minutos = Duration.between(horaInicio, horaFin).toMinutes();

		if (minutos != DURACION_SESION_MINUTOS) {
			return false;
		}

		if (seSolapan(horaInicio, horaFin, HORA_INICIO_ALMUERZO, HORA_FIN_ALMUERZO)) {
			return false;
		}

		String dia = normalizarDia(diaSemana);

		if (esLunesAViernes(dia)) {
			return !horaInicio.isBefore(HORA_INICIO_JORNADA) && !horaFin.isAfter(HORA_FIN_LUNES_VIERNES);
		} else if ("SABADO".equals(dia)) {
			return !horaInicio.isBefore(HORA_INICIO_JORNADA) && !horaFin.isAfter(HORA_FIN_SABADO);
		}

		return false;
	}

	/**
	 * Metodo que verifica si al programar un grupo en el dia indicado, la materia
	 * (cuando tiene 2 o 3 sesiones semanales) queda con sesiones en dias
	 * consecutivos (Restriccion blanda - RF16).
	 * @param data DTO del grupo a evaluar.
	 * @param idGrupoExcluir Identificador del grupo a ignorar en caso de
	 *        actualizacion (o null si es creacion).
	 * @return Retorna true si genera sesiones en dias consecutivos para una
	 *         materia de 2 o 3 sesiones o false en caso contrario.
	 */
	public boolean tieneSesionesEnDiasConsecutivos(GrupoDTO data, Integer idGrupoExcluir) {

		if (data == null || data.getDiaSemana() == null) {
			return false;
		}

		int indiceDiaNuevo = obtenerIndiceDia(normalizarDia(data.getDiaSemana()));

		if (indiceDiaNuevo == -1) {
			return false;
		}

		List<Grupo> sesionesMateria = grupoRepo.findByMateriaId(data.getMateriaId());
		List<Integer> diasExistentes = new ArrayList<>();

		for (Grupo g : sesionesMateria) {

			if (idGrupoExcluir != null && g.getIdGrupo() == idGrupoExcluir.intValue()) {
				continue;
			}

			int idx = obtenerIndiceDia(normalizarDia(g.getDiaSemana()));

			if (idx != -1) {
				diasExistentes.add(idx);
			}
		}

		int totalSesiones = diasExistentes.size() + 1;

		if (totalSesiones != 2 && totalSesiones != 3) {
			return false;
		}

		for (Integer diaExistente : diasExistentes) {

			if (Math.abs(indiceDiaNuevo - diaExistente) == 1) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Metodo que evalua de forma detallada un grupo e informa las reglas duras y
	 * blandas incumplidas junto con su descripcion (CU05 - Flujo de excepcion /
	 * RF16, RF20). Si la lista retornada esta vacia, el grupo no presenta ningun
	 * conflicto ni advertencia.
	 * @param data DTO del grupo a validar.
	 * @param idGrupoExcluir Identificador del grupo a excluir cuando se valida una
	 *        actualizacion (o null al crear).
	 * @return Retorna una lista de mensajes detallando cada regla incumplida o
	 *         advertencia detectada.
	 */
	public List<String> diagnosticarReglasGrupo(GrupoDTO data, Integer idGrupoExcluir) {

		List<String> conflictos = new ArrayList<>();

		if (!esGrupoDatosBasicosValidos(data)) {
			conflictos.add("RF05 - Datos incompletos o invalidos: verifique IDs, cupo maximo, inscritos, dia y horas.");
			return conflictos;
		}

		int limiteCupoInstitucional = (int) Math.floor(CUPO_BASE_MAXIMO * (1.0 + TOLERANCIA_CUPO));

		if (data.getCupoMaximo() > limiteCupoInstitucional) {
			conflictos.add("RF07 - El cupo del grupo (" + data.getCupoMaximo()
					+ ") supera el limite institucional permitido con tolerancia del 10 % ("
					+ limiteCupoInstitucional + ").");
		}

		Optional<Aula> aulaOpt = aulaRepo.findById((int) data.getAulaId());

		if (aulaOpt.isPresent()) {

			Aula aula = aulaOpt.get();
			int capacidadConTolerancia = (int) Math.floor(aula.getCapacidad() * (1.0 + TOLERANCIA_CUPO));

			if (data.getCupoMaximo() > capacidadConTolerancia) {
				conflictos.add("RF07 - El cupo del grupo (" + data.getCupoMaximo()
						+ ") supera la capacidad del aula con tolerancia del 10 % ("
						+ capacidadConTolerancia + ").");
			}
		}

		if (seSolapan(data.getHoraInicio(), data.getHoraFin(), HORA_INICIO_ALMUERZO, HORA_FIN_ALMUERZO)) {
			conflictos.add("RF09 - No se permite programar clases en la franja de almuerzo (12:00 a 13:00).");
		}

		if (!esFranjaInstitucionalValida(data.getDiaSemana(), data.getHoraInicio(), data.getHoraFin())) {
			conflictos.add("RF09/RF15 - La sesion debe durar 2 horas y ubicarse dentro de la franja institucional "
					+ "(Lunes a Viernes 07:00-22:00, Sabado 07:00-13:00, sin cruzar 12:00-13:00).");
		}

		String diaNuevo = normalizarDia(data.getDiaSemana());
		List<Grupo> gruposDocente = grupoRepo.findByDocenteId(data.getDocenteId());

		for (Grupo existente : gruposDocente) {

			if (idGrupoExcluir != null && existente.getIdGrupo() == idGrupoExcluir.intValue()) {
				continue;
			}

			if (diaNuevo.equals(normalizarDia(existente.getDiaSemana())) && seSolapan(data.getHoraInicio(),
					data.getHoraFin(), existente.getHoraInicio(), existente.getHoraFin())) {
				conflictos.add("RF06 - Cruce de horario del docente (ID " + data.getDocenteId()
						+ ") con el grupo " + existente.getIdGrupo() + " el dia " + existente.getDiaSemana()
						+ " (" + existente.getHoraInicio() + " - " + existente.getHoraFin() + ").");
			}
		}

		List<Grupo> gruposAula = grupoRepo.findByAulaId(data.getAulaId());

		for (Grupo existente : gruposAula) {

			if (idGrupoExcluir != null && existente.getIdGrupo() == idGrupoExcluir.intValue()) {
				continue;
			}

			if (diaNuevo.equals(normalizarDia(existente.getDiaSemana())) && seSolapan(data.getHoraInicio(),
					data.getHoraFin(), existente.getHoraInicio(), existente.getHoraFin())) {
				conflictos.add("RF06 - Cruce de horario del aula (ID " + data.getAulaId()
						+ ") con el grupo " + existente.getIdGrupo() + " el dia " + existente.getDiaSemana()
						+ " (" + existente.getHoraInicio() + " - " + existente.getHoraFin() + ").");
			}
		}

		int conteoSesiones = contarSesionesMateria(data.getMateriaId(), idGrupoExcluir);

		if (conteoSesiones >= MAXIMO_SESIONES_SEMANALES) {
			conflictos.add("RF15 - La materia (ID " + data.getMateriaId()
					+ ") ya alcanzo el limite maximo de " + MAXIMO_SESIONES_SEMANALES + " sesiones semanales.");
		}

		if (tieneSesionesEnDiasConsecutivos(data, idGrupoExcluir)) {
			conflictos.add("ADVERTENCIA RF16 - La materia (ID " + data.getMateriaId()
					+ ") queda con sesiones programadas en dias consecutivos.");
		}

		if (data.getEstudiantesInscritos() > 0 && data.getEstudiantesInscritos() < MINIMO_ESTUDIANTES_GRUPO) {
			conflictos.add("ADVERTENCIA RF19 - El grupo tiene menos de " + MINIMO_ESTUDIANTES_GRUPO
					+ " estudiantes inscritos (" + data.getEstudiantesInscritos()
					+ ") y queda senalado para posible cierre y reasignacion.");
		}

		return conflictos;
	}

	/**
	 * Metodo que genera el reporte consolidado de todos los conflictos y
	 * advertencias detectados en los grupos actualmente registrados en el sistema
	 * (CU05, CU10 / RF20, RF21, RF25).
	 * @return Retorna una lista con el detalle de los conflictos detectados en el
	 *         horario actual.
	 */
	public List<String> generarReporteConflictos() {

		List<String> reporte = new ArrayList<>();
		List<Grupo> entityList = (List<Grupo>) grupoRepo.findAll();

		entityList.forEach((entidad) -> {

			GrupoDTO dto = mapper.map(entidad, GrupoDTO.class);
			List<String> hallazgos = diagnosticarReglasGrupo(dto, entidad.getIdGrupo());

			for (String detalle : hallazgos) {
				reporte.add("Grupo " + entidad.getIdGrupo() + " (Materia " + entidad.getMateriaId() + "): " + detalle);
			}
		});

		return reporte;
	}

	/**
	 * Metodo que verifica si el horario general se encuentra libre de violaciones
	 * de restricciones duras y por tanto puede ser publicado (CU10 / RF25).
	 * @return Retorna true si no existen conflictos duros pendientes o false en
	 *         caso contrario.
	 */
	public boolean puedePublicarHorario() {

		List<Grupo> entityList = (List<Grupo>) grupoRepo.findAll();

		for (Grupo entidad : entityList) {

			GrupoDTO dto = mapper.map(entidad, GrupoDTO.class);

			if (!cumpleRestriccionesDuras(dto, entidad.getIdGrupo())) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Metodo que evalua todas las restricciones duras para la creacion o
	 * actualizacion de un grupo (CU05).
	 * @param data DTO del grupo a evaluar.
	 * @param idGrupoExcluir Identificador del grupo a excluir al actualizar (null al crear).
	 * @return Retorna true si cumple todas las restricciones duras o false en
	 *         caso contrario.
	 */
	private boolean cumpleRestriccionesDuras(GrupoDTO data, Integer idGrupoExcluir) {

		int limiteCupoInstitucional = (int) Math.floor(CUPO_BASE_MAXIMO * (1.0 + TOLERANCIA_CUPO));

		try {
			LanzadorDeExcepcion.verificarCupoMaximo(data.getCupoMaximo(), limiteCupoInstitucional);
		} catch (CapacityExceededException e) {
			System.out.println("Error de cupo institucional: " + e.getMessage());
			return false;
		}

		Optional<Aula> aulaOpt = aulaRepo.findById((int) data.getAulaId());

		if (aulaOpt.isPresent()) {

			int capacidadConTolerancia = (int) Math.floor(aulaOpt.get().getCapacidad() * (1.0 + TOLERANCIA_CUPO));

			try {
				LanzadorDeExcepcion.verificarCupoMaximo(data.getCupoMaximo(), capacidadConTolerancia);
			} catch (CapacityExceededException e) {
				System.out.println("Error de capacidad del aula: " + e.getMessage());
				return false;
			}
		}

		try {
			LanzadorDeExcepcion.verificarFranjaValida(
					esFranjaInstitucionalValida(data.getDiaSemana(), data.getHoraInicio(), data.getHoraFin()));
		} catch (InvalidTimeSlotException e) {
			System.out.println("Error de franja: " + e.getMessage());
			return false;
		}

		try {
			LanzadorDeExcepcion.verificarSinCruceDeHorario(tieneCruceDeHorario(data, idGrupoExcluir));
		} catch (ScheduleConflictException e) {
			System.out.println("Error de horario: " + e.getMessage());
			return false;
		}

		try {
			LanzadorDeExcepcion.verificarLimiteSesiones(contarSesionesMateria(data.getMateriaId(), idGrupoExcluir),
					MAXIMO_SESIONES_SEMANALES);
		} catch (WeeklySessionLimitException e) {
			System.out.println("Error de sesiones: " + e.getMessage());
			return false;
		}

		return true;
	}

	/**
	 * Metodo que cuenta las sesiones ya programadas para una materia, excluyendo
	 * opcionalmente un grupo (util al actualizar).
	 * @param materiaId Codigo de la materia.
	 * @param idGrupoExcluir Identificador del grupo a excluir (o null para contar todos).
	 * @return Retorna la cantidad de sesiones programadas para la materia.
	 */
	private int contarSesionesMateria(long materiaId, Integer idGrupoExcluir) {

		List<Grupo> sesionesMateria = grupoRepo.findByMateriaId(materiaId);
		int conteoSesiones = 0;

		for (Grupo g : sesionesMateria) {

			if (idGrupoExcluir == null || g.getIdGrupo() != idGrupoExcluir.intValue()) {
				conteoSesiones++;
			}
		}

		return conteoSesiones;
	}

	/**
	 * Metodo que valida los datos basicos de integridad del grupo antes de evaluar
	 * las reglas de negocio.
	 * @param data DTO del grupo a validar.
	 * @return Retorna true si los campos requeridos estan presentes y con valores
	 *         validos o false en caso contrario.
	 */
	private boolean esGrupoDatosBasicosValidos(GrupoDTO data) {

		try {
			LanzadorDeExcepcion.verificarDatosNoNulos(data);
		} catch (NullDataException e) {
			System.out.println("Error de grupo: " + e.getMessage());
			return false;
		}

		try {
			LanzadorDeExcepcion.verificarPositivo(data.getIdGrupo());
			LanzadorDeExcepcion.verificarPositivo(data.getMateriaId());
			LanzadorDeExcepcion.verificarPositivo(data.getDocenteId());
			LanzadorDeExcepcion.verificarPositivo(data.getAulaId());
		} catch (InvalidNumberException e) {
			System.out.println("Error de identificadores: " + e.getMessage());
			return false;
		}

		try {
			LanzadorDeExcepcion.verificarPositivo(data.getCupoMaximo());
			LanzadorDeExcepcion.verificarRango(data.getEstudiantesInscritos(), 0, data.getCupoMaximo());
		} catch (InvalidNumberException e) {
			System.out.println("Error de cupo: " + e.getMessage());
			return false;
		}

		try {
			LanzadorDeExcepcion.verificarCampoVacio(data.getDiaSemana());
		} catch (EmptyFieldException e) {
			System.out.println("Error de dia: " + e.getMessage());
			return false;
		}

		try {
			LanzadorDeExcepcion.verificarDatosNoNulos(data.getHoraInicio());
			LanzadorDeExcepcion.verificarDatosNoNulos(data.getHoraFin());
		} catch (NullDataException e) {
			System.out.println("Error de horas: " + e.getMessage());
			return false;
		}

		return true;
	}

	/**
	 * Metodo que determina si dos intervalos de tiempo [inicio1, fin1) y
	 * [inicio2, fin2) se solapan.
	 * @param inicio1 Hora de inicio del primer intervalo.
	 * @param fin1 Hora de fin del primer intervalo.
	 * @param inicio2 Hora de inicio del segundo intervalo.
	 * @param fin2 Hora de fin del segundo intervalo.
	 * @return Retorna true si los intervalos se solapan o false en caso contrario.
	 */
	private boolean seSolapan(LocalTime inicio1, LocalTime fin1, LocalTime inicio2, LocalTime fin2) {

		if (inicio1 == null || fin1 == null || inicio2 == null || fin2 == null) {
			return false;
		}

		return inicio1.isBefore(fin2) && inicio2.isBefore(fin1);
	}

	/**
	 * Metodo que normaliza el nombre del dia eliminando tildes y espacios y
	 * convirtiendolo a mayusculas.
	 * @param dia Nombre del dia a normalizar.
	 * @return Retorna el dia normalizado o una cadena vacia si el dia es nulo.
	 */
	private String normalizarDia(String dia) {

		if (dia == null) {
			return "";
		}

		String sinTildes = Normalizer.normalize(dia.trim(), Normalizer.Form.NFD)
				.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
		return sinTildes.toUpperCase();
	}

	/**
	 * Metodo que verifica si el dia normalizado corresponde a lunes, martes,
	 * miercoles, jueves o viernes.
	 * @param diaNormalizado Dia previamente normalizado.
	 * @return Retorna true si el dia es de lunes a viernes o false en caso contrario.
	 */
	private boolean esLunesAViernes(String diaNormalizado) {

		return "LUNES".equals(diaNormalizado) || "MARTES".equals(diaNormalizado)
				|| "MIERCOLES".equals(diaNormalizado) || "JUEVES".equals(diaNormalizado)
				|| "VIERNES".equals(diaNormalizado);
	}

	/**
	 * Metodo que convierte el dia normalizado en un indice numerico (Lunes = 1 ...
	 * Sabado = 6) para evaluar la consecutividad de dias entre sesiones (RF16).
	 * @param diaNormalizado Dia previamente normalizado.
	 * @return Retorna el indice del dia o -1 si el dia no es valido.
	 */
	private int obtenerIndiceDia(String diaNormalizado) {

		switch (diaNormalizado) {
		case "LUNES":
			return 1;
		case "MARTES":
			return 2;
		case "MIERCOLES":
			return 3;
		case "JUEVES":
			return 4;
		case "VIERNES":
			return 5;
		case "SABADO":
			return 6;
		default:
			return -1;
		}
	}
}