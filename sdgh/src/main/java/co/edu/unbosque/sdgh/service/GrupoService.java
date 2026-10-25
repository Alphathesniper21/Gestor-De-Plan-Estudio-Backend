package co.edu.unbosque.sdgh.service;

import java.text.Normalizer;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.edu.unbosque.sdgh.dto.GrupoDTO;
import co.edu.unbosque.sdgh.entity.Aula;
import co.edu.unbosque.sdgh.entity.Grupo;
import co.edu.unbosque.sdgh.repository.AulaRepository;
import co.edu.unbosque.sdgh.repository.GrupoRepository;
import co.edu.unbosque.sdgh.repository.MateriaRepository;
import co.edu.unbosque.sdgh.repository.ProfesorRepository;

/**
 * Servicio que gestiona la logica de negocio para la creacion de grupos y asignacion
 * de horarios academicos (CU05, CU07, CU08, CU10 - RF05 a RF09, RF15 a RF21).
 */
@Service
public class GrupoService implements CRUDOperation<GrupoDTO> {

	/** Cupo maximo institucional por grupo antes de aplicar tolerancia (RF07). */
	private static final int CUPO_BASE_MAXIMO = 40;

	/** Porcentaje de tolerancia sobre el cupo (10 %) (RF07). */
	private static final double TOLERANCIA_CUPO = 0.10;

	/** Minimo de estudiantes inscritos antes de senalar cierre de grupo (RF19). */
	private static final int MINIMO_ESTUDIANTES_GRUPO = 10;

	/** Maximo de sesiones semanales por materia (RF15). */
	private static final int MAXIMO_SESIONES_SEMANALES = 4;

	/** Duracion estandar de cada sesion de clase en minutos (2 horas) (RF15). */
	private static final long DURACION_SESION_MINUTOS = 120;

	/** Hora de apertura institucional de lunes a sabado (07:00) (RF09). */
	private static final LocalTime HORA_INICIO_JORNADA = LocalTime.of(7, 0);

	/** Hora de cierre institucional de lunes a viernes (22:00) (RF09). */
	private static final LocalTime HORA_FIN_LUNES_VIERNES = LocalTime.of(22, 0);

	/** Hora de cierre institucional los sabados (13:00) (RF09). */
	private static final LocalTime HORA_FIN_SABADO = LocalTime.of(13, 0);

	/** Inicio de la franja de almuerzo prohibida (12:00) (RF09). */
	private static final LocalTime HORA_INICIO_ALMUERZO = LocalTime.of(12, 0);

	/** Fin de la franja de almuerzo prohibida (13:00) (RF09). */
	private static final LocalTime HORA_FIN_ALMUERZO = LocalTime.of(13, 0);

	@Autowired
	private GrupoRepository grupoRepo;

	@Autowired
	private MateriaRepository materiaRepo;

	@Autowired
	private ProfesorRepository profesorRepo;

	@Autowired
	private AulaRepository aulaRepo;

	public GrupoService() {
	}

	/**
	 * Crea un nuevo grupo y asigna su horario validando las restricciones duras definidas en CU05:
	 * ausencia de cruces de horario de docente y de aula (RF06), capacidad del aula con tolerancia
	 * del 10 % (RF07), franja dentro del horario institucional y fuera de 12:00 a 13:00 (RF09),
	 * y duracion de sesion de 2 horas con limite de sesiones semanales (RF15).
	 *
	 * @param data DTO con los datos del grupo a registrar.
	 * @return 0 si se creo con exito;
	 *         1 si el id de grupo ya existe o se incumple una restriccion dura de cruce/capacidad/franja;
	 *         2 si los datos son nulos o incompletos.
	 */
	@Override
	public int create(GrupoDTO data) {
		if (!esGrupoDatosBasicosValidos(data)) {
			return 2;
		}
		if (grupoRepo.existsById(data.getIdGrupo())) {
			return 1;
		}
		if (!cumpleRestriccionesDuras(data, null)) {
			return 1;
		}

		Grupo entity = toEntity(data);
		grupoRepo.save(entity);
		return 0;
	}

	/**
	 * Obtiene todos los grupos registrados en el sistema (CU07, CU08 / RF13, RF14).
	 *
	 * @return Lista de {@link GrupoDTO}.
	 */
	@Override
	public List<GrupoDTO> getAll() {
		List<GrupoDTO> dtoList = new ArrayList<>();
		Iterable<Grupo> entities = grupoRepo.findAll();
		for (Grupo entity : entities) {
			dtoList.add(toDTO(entity));
		}
		return dtoList;
	}

	/**
	 * Elimina un grupo por su ID.
	 *
	 * @param id Identificador del grupo a eliminar.
	 * @return 0 si se elimino correctamente; 1 si no existe o el id es nulo.
	 */
	@Override
	public int deleteById(Integer id) {
		if (id == null || !grupoRepo.existsById(id)) {
			return 1;
		}
		grupoRepo.deleteById(id);
		return 0;
	}

	/**
	 * Actualiza un grupo existente validando las restricciones duras de horario, aula y docente (CU05).
	 *
	 * @param id      Identificador del grupo a actualizar.
	 * @param newData Nuevos datos del grupo.
	 * @return 0 si se actualizo con exito;
	 *         1 si el grupo no existe o incumple cualquiera de las restricciones duras;
	 *         2 si los datos nuevos son nulos o incompletos.
	 */
	@Override
	public int updateById(Integer id, GrupoDTO newData) {
		if (id == null) {
			return 1;
		}
		Optional<Grupo> foundOpt = grupoRepo.findById(id);
		if (foundOpt.isEmpty()) {
			return 1;
		}
		if (!esGrupoDatosBasicosValidos(newData)) {
			return 2;
		}
		if (!cumpleRestriccionesDuras(newData, id)) {
			return 1;
		}

		Grupo entityToUpdate = foundOpt.get();
		entityToUpdate.setMateriaId(newData.getMateriaId());
		entityToUpdate.setDocenteId(newData.getDocenteId());
		entityToUpdate.setAulaId(newData.getAulaId());
		entityToUpdate.setCupoMaximo(newData.getCupoMaximo());
		entityToUpdate.setEstudiantesInscritos(newData.getEstudiantesInscritos());
		entityToUpdate.setDiaSemana(newData.getDiaSemana().trim());
		entityToUpdate.setHoraInicio(newData.getHoraInicio());
		entityToUpdate.setHoraFin(newData.getHoraFin());

		grupoRepo.save(entityToUpdate);
		return 0;
	}

	/**
	 * Cuenta el total de grupos registrados.
	 *
	 * @return Cantidad total de grupos.
	 */
	@Override
	public long count() {
		return grupoRepo.count();
	}

	/**
	 * Verifica si existe un grupo con el ID indicado.
	 *
	 * @param id Identificador del grupo.
	 * @return true si existe, false en caso contrario.
	 */
	@Override
	public boolean exist(Integer id) {
		return id != null && grupoRepo.existsById(id);
	}

	/**
	 * Busca un grupo por su ID.
	 *
	 * @param id Identificador del grupo.
	 * @return {@link GrupoDTO} si existe, o null si no se encuentra.
	 */
	public GrupoDTO getById(Integer id) {
		if (id == null) {
			return null;
		}
		return grupoRepo.findById(id).map(this::toDTO).orElse(null);
	}

	/**
	 * Obtiene todos los grupos asociados a una materia (CU08 / RF14).
	 *
	 * @param materiaId Codigo de la materia.
	 * @return Lista de {@link GrupoDTO} de la materia.
	 */
	public List<GrupoDTO> getByMateriaId(long materiaId) {
		List<GrupoDTO> dtoList = new ArrayList<>();
		List<Grupo> entities = grupoRepo.findByMateriaId(materiaId);
		for (Grupo entity : entities) {
			dtoList.add(toDTO(entity));
		}
		return dtoList;
	}

	/**
	 * Obtiene todos los grupos asignados a un docente (CU07 / RF13).
	 *
	 * @param docenteId Codigo del docente.
	 * @return Lista de {@link GrupoDTO} del docente.
	 */
	public List<GrupoDTO> getByDocenteId(long docenteId) {
		List<GrupoDTO> dtoList = new ArrayList<>();
		List<Grupo> entities = grupoRepo.findByDocenteId(docenteId);
		for (Grupo entity : entities) {
			dtoList.add(toDTO(entity));
		}
		return dtoList;
	}

	/**
	 * Obtiene todos los grupos programados en un aula especifica.
	 *
	 * @param aulaId Identificador del aula.
	 * @return Lista de {@link GrupoDTO} del aula.
	 */
	public List<GrupoDTO> getByAulaId(long aulaId) {
		List<GrupoDTO> dtoList = new ArrayList<>();
		List<Grupo> entities = grupoRepo.findByAulaId(aulaId);
		for (Grupo entity : entities) {
			dtoList.add(toDTO(entity));
		}
		return dtoList;
	}

	/**
	 * Obtiene los grupos con menos de 10 estudiantes inscritos para su senalamiento y posible cierre (RF19).
	 *
	 * @return Lista de {@link GrupoDTO} con menos del minimo de estudiantes requeridos.
	 */
	public List<GrupoDTO> getGruposParaCierre() {
		return getGruposParaCierre(MINIMO_ESTUDIANTES_GRUPO);
	}

	/**
	 * Obtiene los grupos cuyo numero de estudiantes inscritos es menor que el parametro indicado (RF19).
	 *
	 * @param minimoEstudiantes Cantidad minima requerida para mantener abierto el grupo.
	 * @return Lista de {@link GrupoDTO} por debajo del minimo.
	 */
	public List<GrupoDTO> getGruposParaCierre(int minimoEstudiantes) {
		List<GrupoDTO> dtoList = new ArrayList<>();
		Iterable<Grupo> entities = grupoRepo.findAll();
		for (Grupo entity : entities) {
			if (entity.getEstudiantesInscritos() < minimoEstudiantes) {
				dtoList.add(toDTO(entity));
			}
		}
		return dtoList;
	}

	/**
	 * Calcula el total de horas semanales asignadas a un docente (RF18).
	 *
	 * @param docenteId Codigo del docente.
	 * @return Total de horas semanales programadas para el docente.
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
	 * Verifica si existe cruce de horario para el docente o el aula en el dia y franja indicados (RF06).
	 *
	 * @param data           DTO del grupo a evaluar.
	 * @param idGrupoExcluir ID del grupo a ignorar en caso de actualizacion (o null si es creacion).
	 * @return true si hay cruce de horario, false si no hay conflicto.
	 */
	public boolean tieneCruceDeHorario(GrupoDTO data, Integer idGrupoExcluir) {
		String diaNuevo = normalizarDia(data.getDiaSemana());

		List<Grupo> gruposDocente = grupoRepo.findByDocenteId(data.getDocenteId());
		for (Grupo existente : gruposDocente) {
			if (idGrupoExcluir != null && existente.getIdGrupo() == idGrupoExcluir.intValue()) {
				continue;
			}
			if (diaNuevo.equals(normalizarDia(existente.getDiaSemana()))
					&& seSolapan(data.getHoraInicio(), data.getHoraFin(), existente.getHoraInicio(), existente.getHoraFin())) {
				return true;
			}
		}

		List<Grupo> gruposAula = grupoRepo.findByAulaId(data.getAulaId());
		for (Grupo existente : gruposAula) {
			if (idGrupoExcluir != null && existente.getIdGrupo() == idGrupoExcluir.intValue()) {
				continue;
			}
			if (diaNuevo.equals(normalizarDia(existente.getDiaSemana()))
					&& seSolapan(data.getHoraInicio(), data.getHoraFin(), existente.getHoraInicio(), existente.getHoraFin())) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Verifica si la franja del grupo respeta el horario institucional (L-V 07:00-22:00, Sab 07:00-13:00),
	 * excluye la hora de almuerzo (12:00-13:00) y dura 2 horas (RF09, RF15).
	 *
	 * @param diaSemana  Dia de la semana.
	 * @param horaInicio Hora de inicio de la sesion.
	 * @param horaFin    Hora de fin de la sesion.
	 * @return true si la franja es valida, false en caso contrario.
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
	 * Verifica si al programar un grupo en el dia indicado, la materia (cuando tiene 2 o 3 sesiones
	 * semanales) queda con sesiones en dias consecutivos (Restriccion blanda - RF16).
	 *
	 * @param data           DTO del grupo a evaluar.
	 * @param idGrupoExcluir ID del grupo a ignorar en caso de actualizacion (o null si es creacion).
	 * @return true si genera sesiones en dias consecutivos para una materia de 2 o 3 sesiones, false en caso contrario.
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
	 * Evalua de forma detallada un grupo e informa las reglas duras y blandas incumplidas
	 * junto con su descripcion (CU05 - Flujo de excepcion / RF16, RF20).
	 * Si la lista retornada esta vacia, el grupo no presenta ningun conflicto ni advertencia.
	 *
	 * @param data           DTO del grupo a validar.
	 * @param idGrupoExcluir ID del grupo a excluir cuando se valida una actualizacion (o null al crear).
	 * @return Lista de mensajes detallando cada regla incumplida o advertencia detectada.
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
			if (diaNuevo.equals(normalizarDia(existente.getDiaSemana()))
					&& seSolapan(data.getHoraInicio(), data.getHoraFin(), existente.getHoraInicio(), existente.getHoraFin())) {
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
			if (diaNuevo.equals(normalizarDia(existente.getDiaSemana()))
					&& seSolapan(data.getHoraInicio(), data.getHoraFin(), existente.getHoraInicio(), existente.getHoraFin())) {
				conflictos.add("RF06 - Cruce de horario del aula (ID " + data.getAulaId()
						+ ") con el grupo " + existente.getIdGrupo() + " el dia " + existente.getDiaSemana()
						+ " (" + existente.getHoraInicio() + " - " + existente.getHoraFin() + ").");
			}
		}

		List<Grupo> sesionesMateria = grupoRepo.findByMateriaId(data.getMateriaId());
		int conteoSesiones = 0;
		for (Grupo g : sesionesMateria) {
			if (idGrupoExcluir == null || g.getIdGrupo() != idGrupoExcluir.intValue()) {
				conteoSesiones++;
			}
		}
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
	 * Genera el reporte consolidado de todos los conflictos y advertencias detectados
	 * en los grupos actualmente registrados en el sistema (CU05, CU10 / RF20, RF21, RF25).
	 *
	 * @return Lista con el detalle de los conflictos detectados en el horario actual.
	 */
	public List<String> generarReporteConflictos() {
		List<String> reporte = new ArrayList<>();
		Iterable<Grupo> todos = grupoRepo.findAll();
		for (Grupo entity : todos) {
			GrupoDTO dto = toDTO(entity);
			List<String> hallazgos = diagnosticarReglasGrupo(dto, entity.getIdGrupo());
			for (String detalle : hallazgos) {
				reporte.add("Grupo " + entity.getIdGrupo() + " (Materia " + entity.getMateriaId() + "): " + detalle);
			}
		}
		return reporte;
	}

	/**
	 * Verifica si el horario general se encuentra libre de violaciones de restricciones duras
	 * y por tanto puede ser publicado (CU10 / RF25).
	 *
	 * @return true si no existen conflictos duros pendientes, false en caso contrario.
	 */
	public boolean puedePublicarHorario() {
		Iterable<Grupo> todos = grupoRepo.findAll();
		for (Grupo entity : todos) {
			GrupoDTO dto = toDTO(entity);
			if (!cumpleRestriccionesDuras(dto, entity.getIdGrupo())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Evalua todas las restricciones duras para la creacion o actualizacion de un grupo (CU05).
	 *
	 * @param data           Datos del grupo.
	 * @param idGrupoExcluir ID del grupo a excluir al actualizar (null al crear).
	 * @return true si cumple todas las restricciones duras, false en caso contrario.
	 */
	private boolean cumpleRestriccionesDuras(GrupoDTO data, Integer idGrupoExcluir) {
		int limiteCupoInstitucional = (int) Math.floor(CUPO_BASE_MAXIMO * (1.0 + TOLERANCIA_CUPO));
		if (data.getCupoMaximo() > limiteCupoInstitucional) {
			return false;
		}

		Optional<Aula> aulaOpt = aulaRepo.findById((int) data.getAulaId());
		if (aulaOpt.isPresent()) {
			Aula aula = aulaOpt.get();
			int capacidadConTolerancia = (int) Math.floor(aula.getCapacidad() * (1.0 + TOLERANCIA_CUPO));
			if (data.getCupoMaximo() > capacidadConTolerancia) {
				return false;
			}
		}

		if (!esFranjaInstitucionalValida(data.getDiaSemana(), data.getHoraInicio(), data.getHoraFin())) {
			return false;
		}

		if (tieneCruceDeHorario(data, idGrupoExcluir)) {
			return false;
		}

		List<Grupo> sesionesMateria = grupoRepo.findByMateriaId(data.getMateriaId());
		int conteoSesiones = 0;
		for (Grupo g : sesionesMateria) {
			if (idGrupoExcluir == null || g.getIdGrupo() != idGrupoExcluir.intValue()) {
				conteoSesiones++;
			}
		}
		if (conteoSesiones >= MAXIMO_SESIONES_SEMANALES) {
			return false;
		}

		return true;
	}

	/**
	 * Valida los datos basicos de integridad del grupo antes de evaluar las reglas de negocio.
	 *
	 * @param data DTO del grupo.
	 * @return true si los campos requeridos estan presentes y con valores validos.
	 */
	private boolean esGrupoDatosBasicosValidos(GrupoDTO data) {
		if (data == null) {
			return false;
		}
		if (data.getIdGrupo() <= 0 || data.getMateriaId() <= 0 || data.getDocenteId() <= 0 || data.getAulaId() <= 0) {
			return false;
		}
		if (data.getCupoMaximo() <= 0 || data.getEstudiantesInscritos() < 0
				|| data.getEstudiantesInscritos() > data.getCupoMaximo()) {
			return false;
		}
		if (data.getDiaSemana() == null || data.getDiaSemana().trim().isEmpty()
				|| data.getHoraInicio() == null || data.getHoraFin() == null) {
			return false;
		}
		return true;
	}

	/**
	 * Determina si dos intervalos de tiempo [inicio1, fin1) y [inicio2, fin2) se solapan.
	 */
	private boolean seSolapan(LocalTime inicio1, LocalTime fin1, LocalTime inicio2, LocalTime fin2) {
		if (inicio1 == null || fin1 == null || inicio2 == null || fin2 == null) {
			return false;
		}
		return inicio1.isBefore(fin2) && inicio2.isBefore(fin1);
	}

	/**
	 * Normaliza el nombre del dia eliminando tildes y espacios y convirtiendolo a mayusculas.
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
	 * Verifica si el dia normalizado corresponde a lunes, martes, miercoles, jueves o viernes.
	 */
	private boolean esLunesAViernes(String diaNormalizado) {
		return "LUNES".equals(diaNormalizado)
				|| "MARTES".equals(diaNormalizado)
				|| "MIERCOLES".equals(diaNormalizado)
				|| "JUEVES".equals(diaNormalizado)
				|| "VIERNES".equals(diaNormalizado);
	}

	/**
	 * Convierte el dia normalizado en un indice numerico (Lunes = 1 ... Sabado = 6)
	 * para evaluar la consecutividad de dias entre sesiones (RF16).
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

	/**
	 * Convierte una entidad {@link Grupo} a {@link GrupoDTO}.
	 *
	 * @param entity Entidad Grupo.
	 * @return Objeto GrupoDTO.
	 */
	public GrupoDTO toDTO(Grupo entity) {
		if (entity == null) {
			return null;
		}
		return new GrupoDTO(
				entity.getIdGrupo(),
				entity.getMateriaId(),
				entity.getDocenteId(),
				entity.getAulaId(),
				entity.getCupoMaximo(),
				entity.getEstudiantesInscritos(),
				entity.getDiaSemana(),
				entity.getHoraInicio(),
				entity.getHoraFin()
		);
	}

	/**
	 * Convierte un {@link GrupoDTO} a entidad {@link Grupo}.
	 *
	 * @param dto Objeto GrupoDTO.
	 * @return Entidad Grupo.
	 */
	public Grupo toEntity(GrupoDTO dto) {
		if (dto == null) {
			return null;
		}
		return new Grupo(
				dto.getIdGrupo(),
				dto.getMateriaId(),
				dto.getDocenteId(),
				dto.getAulaId(),
				dto.getCupoMaximo(),
				dto.getEstudiantesInscritos(),
				dto.getDiaSemana() != null ? dto.getDiaSemana().trim() : null,
				dto.getHoraInicio(),
				dto.getHoraFin()
		);
	}

}
