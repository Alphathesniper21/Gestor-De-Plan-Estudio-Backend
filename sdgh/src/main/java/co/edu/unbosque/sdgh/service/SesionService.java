/**
 * Paquete de servicios del aplicativo.
 */
package co.edu.unbosque.sdgh.service;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.edu.unbosque.sdgh.dto.SesionDTO;
import co.edu.unbosque.sdgh.entity.Grupo;
import co.edu.unbosque.sdgh.entity.Materia;
import co.edu.unbosque.sdgh.entity.Sesion;
import co.edu.unbosque.sdgh.exception.CruceHorarioException;
import co.edu.unbosque.sdgh.exception.DatosInvalidosException;
import co.edu.unbosque.sdgh.exception.FranjaInstitucionalInvalidaException;
import co.edu.unbosque.sdgh.exception.LimiteSesionesExcedidoException;
import co.edu.unbosque.sdgh.exception.RecursoNoEncontradoException;
import co.edu.unbosque.sdgh.repository.GrupoRepository;
import co.edu.unbosque.sdgh.repository.MateriaRepository;
import co.edu.unbosque.sdgh.repository.SesionRepository;

/**
 * Servicio encargado de ubicar, mover y quitar las sesiones de los grupos en la
 * grilla semanal (RF3). Valida la franja permitida (RF4), la pausa de 12:00 a
 * 13:00 (RF5), el cruce entre cursos del mismo semestre y bloque de grupos
 * (RF6) y el limite de sesiones del curso (RF7). La advertencia por dias
 * consecutivos (RF8) no rechaza la sesion y se consulta aparte. Las
 * validaciones se hacen al inicio de cada metodo y lanzan excepciones
 * personalizadas que son capturadas en el controlador.
 */
@Service
public class SesionService implements CRUDOperation<SesionDTO> {

	/**
	 * Duracion de cada sesion en minutos (2 horas) (RF3).
	 */
	private static final long DURACION_SESION_MINUTOS = 120;

	/**
	 * Hora de apertura de lunes a sabado (07:00) (RF4).
	 */
	private static final LocalTime HORA_INICIO_JORNADA = LocalTime.of(7, 0);

	/**
	 * Hora de cierre de lunes a viernes (22:00) (RF4).
	 */
	private static final LocalTime HORA_FIN_LUNES_VIERNES = LocalTime.of(22, 0);

	/**
	 * Hora de cierre los sabados (13:00) (RF4).
	 */
	private static final LocalTime HORA_FIN_SABADO = LocalTime.of(13, 0);

	/**
	 * Inicio de la pausa (12:00) (RF5).
	 */
	private static final LocalTime HORA_INICIO_PAUSA = LocalTime.of(12, 0);

	/**
	 * Fin de la pausa (13:00) (RF5).
	 */
	private static final LocalTime HORA_FIN_PAUSA = LocalTime.of(13, 0);

	/**
	 * Repositorio de sesiones.
	 */
	@Autowired
	private SesionRepository sesionRepo;

	/**
	 * Repositorio de grupos.
	 */
	@Autowired
	private GrupoRepository grupoRepo;

	/**
	 * Repositorio de materias.
	 */
	@Autowired
	private MateriaRepository materiaRepo;

	/**
	 * Mapper utilizado para convertir entidades en DTO y viceversa.
	 */
	@Autowired
	private ModelMapper mapper;

	/**
	 * Constructor vacio de la clase SesionService.
	 */
	public SesionService() {
	}

	/**
	 * Metodo que ubica una sesion en la grilla validando RF3 a RF7. El dia es un
	 * numero de 1 (lunes) a 6 (sabado).
	 * @param newData DTO con la sesion a crear.
	 * @return Retorna 0 si la sesion fue creada correctamente.
	 * @throws DatosInvalidosException si los datos son nulos o estan incompletos.
	 * @throws RecursoNoEncontradoException si el grupo o su curso no existen.
	 * @throws FranjaInstitucionalInvalidaException si incumple la duracion, la
	 *         franja o la pausa (RF3, RF4, RF5).
	 * @throws CruceHorarioException si se cruza con otra sesion del mismo
	 *         semestre y bloque (RF6).
	 * @throws LimiteSesionesExcedidoException si el grupo ya tiene todas las
	 *         sesiones de su curso (RF7).
	 */
	@Override
	public int create(SesionDTO newData) {

		if (newData == null) {
			throw new DatosInvalidosException("Los datos de la sesion no pueden ser nulos");
		}
		if (newData.getGrupoId() <= 0) {
			throw new DatosInvalidosException("El id del grupo debe ser un numero positivo");
		}
		if (newData.getHoraInicio() == null || newData.getHoraFin() == null) {
			throw new DatosInvalidosException("La hora de inicio y la hora de fin son obligatorias");
		}

		Grupo grupo = grupoRepo.findById(newData.getId()).orElseThrow(
				() -> new RecursoNoEncontradoException("No existe un grupo con el id " + newData.getGrupoId()));
		Materia materia = materiaRepo.findById(grupo.getMateriaId()).orElseThrow(
				() -> new RecursoNoEncontradoException("No existe una materia con el codigo " + grupo.getMateriaId()));

		LocalTime inicio = newData.getHoraInicio();
		LocalTime fin = newData.getHoraFin();
		int dia = newData.getDiaSemana();

		if (!inicio.isBefore(fin)) {
			throw new FranjaInstitucionalInvalidaException(
					"La hora de inicio (" + inicio + ") debe ser anterior a la hora de fin (" + fin + ")");
		}
		long minutos = Duration.between(inicio, fin).toMinutes();
		if (minutos != DURACION_SESION_MINUTOS) {
			throw new FranjaInstitucionalInvalidaException(
					"Cada sesion debe durar exactamente 2 horas y esta dura " + minutos + " minutos");
		}
		if (dia < 1 || dia > 6) {
			throw new FranjaInstitucionalInvalidaException(
					"El dia " + dia + " no es valido. Use de 1 (lunes) a 6 (sabado)");
		}
		if (inicio.isBefore(HORA_INICIO_JORNADA)) {
			throw new FranjaInstitucionalInvalidaException("Las clases inician a partir de las 07:00");
		}
		if (dia <= 5 && fin.isAfter(HORA_FIN_LUNES_VIERNES)) {
			throw new FranjaInstitucionalInvalidaException(
					"De lunes a viernes las clases deben estar entre las 07:00 y las 22:00");
		}
		if (dia == 6 && fin.isAfter(HORA_FIN_SABADO)) {
			throw new FranjaInstitucionalInvalidaException(
					"Los sabados las clases deben estar entre las 07:00 y las 13:00");
		}
		if (inicio.isBefore(HORA_FIN_PAUSA) && HORA_INICIO_PAUSA.isBefore(fin)) {
			throw new FranjaInstitucionalInvalidaException(
					"No se permite programar clases entre las 12:00 y las 13:00");
		}

		List<Sesion> sesionesGrupo = sesionRepo.findByGrupoId(grupo.getId());

		if (sesionesGrupo.size() >= materia.getSesionesSemanales()) {
			throw new LimiteSesionesExcedidoException("El grupo " + grupo.getId() + " ya tiene las "
					+ materia.getSesionesSemanales() + " sesiones semanales de su curso");
		}

		for (Sesion existente : sesionesGrupo) {
			if (existente.getDiaSemana() == dia && inicio.isBefore(existente.getHoraFin())
					&& existente.getHoraInicio().isBefore(fin)) {
				throw new CruceHorarioException("El grupo " + grupo.getId()
						+ " ya tiene una sesion que se cruza el dia " + dia + " (" + existente.getHoraInicio()
						+ " - " + existente.getHoraFin() + ")");
			}
		}

		List<Grupo> gruposBloque = grupoRepo.findByNumeroGrupo(grupo.getNumeroGrupo());

		for (Grupo otro : gruposBloque) {
			if (otro.getMateriaId() == grupo.getMateriaId()) {
				continue;
			}
			Materia materiaOtro = materiaRepo.findById(otro.getMateriaId()).orElse(null);
			if (materiaOtro == null || materiaOtro.getSemestre() != materia.getSemestre()) {
				continue;
			}
			for (Sesion existente : sesionRepo.findByGrupoId(otro.getId())) {
				if (existente.getDiaSemana() == dia && inicio.isBefore(existente.getHoraFin())
						&& existente.getHoraInicio().isBefore(fin)) {
					throw new CruceHorarioException("La sesion se cruza con el curso " + materiaOtro.getNombre()
							+ " (semestre " + materia.getSemestre() + ", bloque " + grupo.getNumeroGrupo()
							+ ") el dia " + dia + " (" + existente.getHoraInicio() + " - " + existente.getHoraFin()
							+ ")");
				}
			}
		}

		Sesion entity = mapper.map(newData, Sesion.class);
		sesionRepo.save(entity);
		return 0;
	}

	/**
	 * Metodo que obtiene todas las sesiones registradas.
	 * @return Retorna una lista con todas las sesiones en formato DTO.
	 */
	@Override
	public List<SesionDTO> getAll() {

		List<Sesion> entityList = (List<Sesion>) sesionRepo.findAll();
		List<SesionDTO> dtoList = new ArrayList<>();

		entityList.forEach((entidad) -> {

			SesionDTO dto = mapper.map(entidad, SesionDTO.class);
			dtoList.add(dto);
		});

		return dtoList;
	}

	/**
	 * Metodo que quita una sesion de la grilla, por lo que el grupo queda con una
	 * sesion pendiente (RF3, RF11).
	 * @param id Identificador de la sesion a eliminar.
	 * @return Retorna 0 si la sesion fue eliminada correctamente.
	 * @throws DatosInvalidosException si el identificador es nulo.
	 * @throws RecursoNoEncontradoException si la sesion no existe.
	 */
	@Override
	public int deleteById(Long id) {

		if (id == null) {
			throw new DatosInvalidosException("El id de la sesion no puede ser nulo");
		}

		Sesion encontrada = sesionRepo.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe una sesion con el id " + id));

		sesionRepo.delete(encontrada);
		return 0;
	}

	/**
	 * Metodo que mueve una sesion a otro dia u hora, validando de nuevo RF3 a
	 * RF6. El grupo de la sesion no cambia, por lo que el limite de sesiones
	 * (RF7) no se vuelve a evaluar.
	 * @param id Identificador de la sesion a mover.
	 * @param newData DTO con el nuevo dia y las nuevas horas.
	 * @return Retorna 0 si la actualizacion fue exitosa.
	 * @throws DatosInvalidosException si el id es nulo o los datos estan
	 *         incompletos.
	 * @throws RecursoNoEncontradoException si la sesion, su grupo o su curso no
	 *         existen.
	 * @throws FranjaInstitucionalInvalidaException si incumple la duracion, la
	 *         franja o la pausa (RF3, RF4, RF5).
	 * @throws CruceHorarioException si se cruza con otra sesion del mismo
	 *         semestre y bloque (RF6).
	 */
	@Override
	public int updateById(Long id, SesionDTO newData) {

		if (id == null) {
			throw new DatosInvalidosException("El id de la sesion no puede ser nulo");
		}

		Sesion temp = sesionRepo.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe una sesion con el id " + id));

		if (newData == null) {
			throw new DatosInvalidosException("Los datos de la sesion no pueden ser nulos");
		}
		if (newData.getHoraInicio() == null || newData.getHoraFin() == null) {
			throw new DatosInvalidosException("La hora de inicio y la hora de fin son obligatorias");
		}

		Grupo grupo = grupoRepo.findById(temp.getId()).orElseThrow(
				() -> new RecursoNoEncontradoException("No existe un grupo con el id " + temp.getGrupoId()));
		Materia materia = materiaRepo.findById(grupo.getMateriaId()).orElseThrow(
				() -> new RecursoNoEncontradoException("No existe una materia con el codigo " + grupo.getMateriaId()));

		LocalTime inicio = newData.getHoraInicio();
		LocalTime fin = newData.getHoraFin();
		int dia = newData.getDiaSemana();

		if (!inicio.isBefore(fin)) {
			throw new FranjaInstitucionalInvalidaException(
					"La hora de inicio (" + inicio + ") debe ser anterior a la hora de fin (" + fin + ")");
		}
		long minutos = Duration.between(inicio, fin).toMinutes();
		if (minutos != DURACION_SESION_MINUTOS) {
			throw new FranjaInstitucionalInvalidaException(
					"Cada sesion debe durar exactamente 2 horas y esta dura " + minutos + " minutos");
		}
		if (dia < 1 || dia > 6) {
			throw new FranjaInstitucionalInvalidaException(
					"El dia " + dia + " no es valido. Use de 1 (lunes) a 6 (sabado)");
		}
		if (inicio.isBefore(HORA_INICIO_JORNADA)) {
			throw new FranjaInstitucionalInvalidaException("Las clases inician a partir de las 07:00");
		}
		if (dia <= 5 && fin.isAfter(HORA_FIN_LUNES_VIERNES)) {
			throw new FranjaInstitucionalInvalidaException(
					"De lunes a viernes las clases deben estar entre las 07:00 y las 22:00");
		}
		if (dia == 6 && fin.isAfter(HORA_FIN_SABADO)) {
			throw new FranjaInstitucionalInvalidaException(
					"Los sabados las clases deben estar entre las 07:00 y las 13:00");
		}
		if (inicio.isBefore(HORA_FIN_PAUSA) && HORA_INICIO_PAUSA.isBefore(fin)) {
			throw new FranjaInstitucionalInvalidaException(
					"No se permite programar clases entre las 12:00 y las 13:00");
		}

		for (Sesion existente : sesionRepo.findByGrupoId(grupo.getId())) {
			if (existente.getId() == id.longValue()) { // CAMBIO: intValue() -> longValue()
				continue;
			}
			if (existente.getDiaSemana() == dia && inicio.isBefore(existente.getHoraFin())
					&& existente.getHoraInicio().isBefore(fin)) {
				throw new CruceHorarioException("El grupo " + grupo.getId()
						+ " ya tiene una sesion que se cruza el dia " + dia + " (" + existente.getHoraInicio()
						+ " - " + existente.getHoraFin() + ")");
			}
		}

		List<Grupo> gruposBloque = grupoRepo.findByNumeroGrupo(grupo.getNumeroGrupo());

		for (Grupo otro : gruposBloque) {
			if (otro.getMateriaId() == grupo.getMateriaId()) {
				continue;
			}
			Materia materiaOtro = materiaRepo.findById(otro.getMateriaId()).orElse(null);
			if (materiaOtro == null || materiaOtro.getSemestre() != materia.getSemestre()) {
				continue;
			}
			for (Sesion existente : sesionRepo.findByGrupoId(otro.getId())) {
				if (existente.getDiaSemana() == dia && inicio.isBefore(existente.getHoraFin())
						&& existente.getHoraInicio().isBefore(fin)) {
					throw new CruceHorarioException("La sesion se cruza con el curso " + materiaOtro.getNombre()
							+ " (semestre " + materia.getSemestre() + ", bloque " + grupo.getNumeroGrupo()
							+ ") el dia " + dia + " (" + existente.getHoraInicio() + " - " + existente.getHoraFin()
							+ ")");
				}
			}
		}

		temp.setDiaSemana(dia);
		temp.setHoraInicio(inicio);
		temp.setHoraFin(fin);
		sesionRepo.save(temp);
		return 0;
	}

	/**
	 * Metodo que cuenta la cantidad total de sesiones registradas.
	 * @return Retorna la cantidad total de sesiones.
	 */
	@Override
	public long count() {

		return sesionRepo.count();
	}

	/**
	 * Metodo que verifica si una sesion existe mediante su identificador.
	 * @param id Identificador de la sesion a verificar.
	 * @return Retorna true si la sesion existe o false en caso contrario.
	 */
	@Override
	public boolean exist(Long id) {

		return id != null && sesionRepo.existsById(id);
	}

	/**
	 * Metodo que busca una sesion mediante su identificador.
	 * @param id Identificador de la sesion.
	 * @return Retorna la sesion en formato DTO.
	 * @throws DatosInvalidosException si el identificador es nulo.
	 * @throws RecursoNoEncontradoException si la sesion no existe.
	 */
	public SesionDTO getById(Long id) {

		if (id == null) {
			throw new DatosInvalidosException("El id de la sesion no puede ser nulo");
		}

		Sesion encontrada = sesionRepo.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe una sesion con el id " + id));

		return mapper.map(encontrada, SesionDTO.class);
	}

	/**
	 * Metodo que obtiene las sesiones de un grupo.
	 * @param grupoId Identificador del grupo.
	 * @return Retorna una lista de sesiones en formato DTO del grupo.
	 * @throws DatosInvalidosException si el id del grupo no es positivo.
	 */
	public List<SesionDTO> getByGrupoId(long grupoId) { // CAMBIO: int -> long

		if (grupoId <= 0) {
			throw new DatosInvalidosException("El id del grupo debe ser un numero positivo");
		}

		List<Sesion> entityList = sesionRepo.findByGrupoId(grupoId);
		List<SesionDTO> dtoList = new ArrayList<>();

		entityList.forEach((entidad) -> {

			SesionDTO dto = mapper.map(entidad, SesionDTO.class);
			dtoList.add(dto);
		});

		return dtoList;
	}

	/**
	 * Metodo que informa las advertencias de un grupo (RF8): no rechaza, solo
	 * avisa si el grupo tiene sesiones en dias consecutivos. Se llama despues de
	 * crear o mover una sesion. Si la lista esta vacia, no hay advertencias.
	 * @param grupoId Identificador del grupo.
	 * @return Retorna la lista de advertencias.
	 * @throws DatosInvalidosException si el id del grupo no es positivo.
	 */
	public List<String> obtenerAdvertencias(long grupoId) { // CAMBIO: int -> long

		if (grupoId <= 0) {
			throw new DatosInvalidosException("El id del grupo debe ser un numero positivo");
		}

		List<String> advertencias = new ArrayList<>();
		List<Sesion> sesiones = sesionRepo.findByGrupoId(grupoId);

		for (int i = 0; i < sesiones.size(); i++) {
			for (int j = i + 1; j < sesiones.size(); j++) {
				if (Math.abs(sesiones.get(i).getDiaSemana() - sesiones.get(j).getDiaSemana()) == 1) {
					advertencias.add("ADVERTENCIA RF8 - El grupo " + grupoId
							+ " tiene sesiones en dias consecutivos (" + sesiones.get(i).getDiaSemana() + " y "
							+ sesiones.get(j).getDiaSemana() + ")");
				}
			}
		}

		return advertencias;
	}
}