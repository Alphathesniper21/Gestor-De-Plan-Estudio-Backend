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
import co.edu.unbosque.sdgh.entity.ClaveParametro;
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
 * grilla semanal (RF3, RF8). Valida la franja permitida (RF4), la pausa (RF5),
 * el cruce entre cursos del mismo semestre y bloque de grupos (RF6) y el maximo
 * de sesiones del curso (RF7). Los valores de la franja y la pausa se leen de
 * los parametros en cada validacion (RF9, RNF3). Las validaciones lanzan
 * excepciones personalizadas que son capturadas en el controlador.
 */
@Service
public class SesionService implements CRUDOperation<SesionDTO> {

	/**
	 * Duracion de cada sesion en minutos (2 horas) (RF3). Es una regla del
	 * enunciado, no un parametro.
	 */
	private static final long DURACION_SESION_MINUTOS = 120;

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
	 * Servicio de parametros: franja, pausa y estado de publicacion (RF9, RF12).
	 */
	@Autowired
	private ParametroService parametroServ;

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
	 * Metodo que ubica una sesion en la grilla (RF3) validando, en este orden:
	 * datos completos, que el grupo y su curso existan, duracion de 2 horas, dia
	 * de 1 (lunes) a 6 (sabado), franja (RF4), pausa (RF5), maximo de sesiones
	 * del curso (RF7), cruce dentro del grupo y cruce con cursos del mismo
	 * semestre y bloque (RF6).
	 * @param newData DTO con la sesion a crear.
	 * @return Retorna 0 si la sesion fue creada correctamente.
	 * @throws DatosInvalidosException si los datos son nulos o estan incompletos.
	 * @throws RecursoNoEncontradoException si el grupo o su curso no existen.
	 * @throws FranjaInstitucionalInvalidaException si incumple la duracion, el
	 *         dia, la franja o la pausa (RF3, RF4, RF5).
	 * @throws LimiteSesionesExcedidoException si el grupo ya tiene todas las
	 *         sesiones de su curso (RF7).
	 * @throws CruceHorarioException si se cruza con otra sesion del grupo o de
	 *         otro curso del mismo semestre y bloque (RF6).
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

		Grupo grupo = grupoRepo.findById(newData.getGrupoId()).orElseThrow(
				() -> new RecursoNoEncontradoException("No existe un grupo con el id " + newData.getGrupoId()));
		Materia materia = materiaRepo.findById(grupo.getMateriaId()).orElseThrow(
				() -> new RecursoNoEncontradoException("No existe una materia con el id " + grupo.getMateriaId()));

		LocalTime inicio = newData.getHoraInicio();
		LocalTime fin = newData.getHoraFin();
		int dia = newData.getDiaSemana();

		validarFranja(inicio, fin, dia);

		List<Sesion> sesionesGrupo = sesionRepo.findByGrupoId(grupo.getId());

		if (sesionesGrupo.size() >= materia.getSesionesSemanales()) {
			throw new LimiteSesionesExcedidoException("El grupo " + grupo.getId() + " ya tiene las "
					+ materia.getSesionesSemanales() + " sesiones semanales de su curso");
		}

		validarCruces(grupo, materia, dia, inicio, fin, null);

		Sesion entity = mapper.map(newData, Sesion.class);
		entity.setId(0);
		sesionRepo.save(entity);
		parametroServ.marcarPublicado(false);
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
	 * Metodo que quita una sesion de la grilla (RF8), por lo que el grupo queda
	 * con una sesion pendiente (RF11).
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
		parametroServ.marcarPublicado(false);
		return 0;
	}

	/**
	 * Metodo que mueve una sesion a otro dia u hora (RF8), aplicando de nuevo las
	 * validaciones de RF3 a RF6. El grupo de la sesion no cambia, por lo que el
	 * maximo de sesiones (RF7) no se vuelve a evaluar. Si una regla falla, la
	 * sesion conserva su posicion.
	 * @param id Identificador de la sesion a mover.
	 * @param newData DTO con el nuevo dia y las nuevas horas.
	 * @return Retorna 0 si la actualizacion fue exitosa.
	 * @throws DatosInvalidosException si el id es nulo o los datos estan
	 *         incompletos.
	 * @throws RecursoNoEncontradoException si la sesion, su grupo o su curso no
	 *         existen.
	 * @throws FranjaInstitucionalInvalidaException si incumple la duracion, el
	 *         dia, la franja o la pausa (RF3, RF4, RF5).
	 * @throws CruceHorarioException si se cruza con otra sesion del grupo o de
	 *         otro curso del mismo semestre y bloque (RF6).
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

		Grupo grupo = grupoRepo.findById(temp.getGrupoId()).orElseThrow(
				() -> new RecursoNoEncontradoException("No existe un grupo con el id " + temp.getGrupoId()));
		Materia materia = materiaRepo.findById(grupo.getMateriaId()).orElseThrow(
				() -> new RecursoNoEncontradoException("No existe una materia con el id " + grupo.getMateriaId()));

		LocalTime inicio = newData.getHoraInicio();
		LocalTime fin = newData.getHoraFin();
		int dia = newData.getDiaSemana();

		validarFranja(inicio, fin, dia);
		validarCruces(grupo, materia, dia, inicio, fin, id);

		temp.setDiaSemana(dia);
		temp.setHoraInicio(inicio);
		temp.setHoraFin(fin);
		sesionRepo.save(temp);
		parametroServ.marcarPublicado(false);
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
	public boolean existById(Long id) {

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
	public List<SesionDTO> getByGrupoId(long grupoId) {

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
	 * Metodo que obtiene el horario de un semestre: las sesiones de todos los
	 * grupos de los cursos de ese semestre (RF10).
	 * @param semestre Semestre del plan de estudios (1 a 9).
	 * @return Retorna una lista de sesiones en formato DTO.
	 * @throws DatosInvalidosException si el semestre esta fuera del rango 1 a 9.
	 */
	public List<SesionDTO> getBySemestre(int semestre) {

		if (semestre < 1 || semestre > 9) {
			throw new DatosInvalidosException("El semestre debe estar entre 1 y 9");
		}

		List<SesionDTO> dtoList = new ArrayList<>();

		for (Materia materia : materiaRepo.findBySemestre(semestre)) {
			for (Grupo grupo : grupoRepo.findByMateriaId(materia.getId())) {
				for (Sesion sesion : sesionRepo.findByGrupoId(grupo.getId())) {
					dtoList.add(mapper.map(sesion, SesionDTO.class));
				}
			}
		}

		return dtoList;
	}

	/**
	 * Metodo que informa si un grupo tiene sesiones en dias consecutivos. No
	 * rechaza la sesion: el enunciado lo plantea como deseable y queda fuera de
	 * los requisitos del prototipo (seccion 5.6.3 del informe).
	 * @param grupoId Identificador del grupo.
	 * @return Retorna la lista de advertencias; vacia si no hay ninguna.
	 * @throws DatosInvalidosException si el id del grupo no es positivo.
	 */
	public List<String> obtenerAdvertencias(long grupoId) {

		if (grupoId <= 0) {
			throw new DatosInvalidosException("El id del grupo debe ser un numero positivo");
		}

		List<String> advertencias = new ArrayList<>();
		List<Sesion> sesiones = sesionRepo.findByGrupoId(grupoId);

		for (int i = 0; i < sesiones.size(); i++) {
			for (int j = i + 1; j < sesiones.size(); j++) {
				if (Math.abs(sesiones.get(i).getDiaSemana() - sesiones.get(j).getDiaSemana()) == 1) {
					advertencias.add("El grupo " + grupoId + " tiene sesiones en dias consecutivos ("
							+ sesiones.get(i).getDiaSemana() + " y " + sesiones.get(j).getDiaSemana() + ")");
				}
			}
		}

		return advertencias;
	}

	/**
	 * Metodo que valida la duracion, el dia, la franja (RF4) y la pausa (RF5) de
	 * una sesion con los valores vigentes de los parametros.
	 * @param inicio Hora de inicio.
	 * @param fin Hora de fin.
	 * @param dia Dia de la semana, de 1 (lunes) a 6 (sabado).
	 * @throws FranjaInstitucionalInvalidaException si alguna regla no se cumple.
	 */
	private void validarFranja(LocalTime inicio, LocalTime fin, int dia) {

		LocalTime inicioJornada = parametroServ.getHora(ClaveParametro.HORA_INICIO_JORNADA);
		LocalTime finSemana = parametroServ.getHora(ClaveParametro.HORA_FIN_SEMANA);
		LocalTime finSabado = parametroServ.getHora(ClaveParametro.HORA_FIN_SABADO);
		LocalTime inicioPausa = parametroServ.getHora(ClaveParametro.PAUSA_INICIO);
		LocalTime finPausa = parametroServ.getHora(ClaveParametro.PAUSA_FIN);

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
		if (inicio.isBefore(inicioJornada)) {
			throw new FranjaInstitucionalInvalidaException("Las clases inician a partir de las " + inicioJornada);
		}
		if (dia <= 5 && fin.isAfter(finSemana)) {
			throw new FranjaInstitucionalInvalidaException(
					"De lunes a viernes las clases deben estar entre las " + inicioJornada + " y las " + finSemana);
		}
		if (dia == 6 && fin.isAfter(finSabado)) {
			throw new FranjaInstitucionalInvalidaException(
					"Los sabados las clases deben estar entre las " + inicioJornada + " y las " + finSabado);
		}
		if (inicio.isBefore(finPausa) && inicioPausa.isBefore(fin)) {
			throw new FranjaInstitucionalInvalidaException(
					"No se permite programar clases entre las " + inicioPausa + " y las " + finPausa);
		}
	}

	/**
	 * Metodo que valida que la sesion no se cruce con otra del mismo grupo ni con
	 * las de otros cursos del mismo semestre y del mismo bloque de grupos (RF6).
	 * @param grupo Grupo de la sesion.
	 * @param materia Curso del grupo.
	 * @param dia Dia de la semana.
	 * @param inicio Hora de inicio.
	 * @param fin Hora de fin.
	 * @param excluirId Id de la sesion que se esta moviendo, para no compararla
	 *        consigo misma; null al crear.
	 * @throws CruceHorarioException si hay un cruce.
	 */
	private void validarCruces(Grupo grupo, Materia materia, int dia, LocalTime inicio, LocalTime fin,
			Long excluirId) {

		for (Sesion existente : sesionRepo.findByGrupoId(grupo.getId())) {
			if (excluirId != null && existente.getId() == excluirId.longValue()) {
				continue;
			}
			if (existente.getDiaSemana() == dia && inicio.isBefore(existente.getHoraFin())
					&& existente.getHoraInicio().isBefore(fin)) {
				throw new CruceHorarioException("El grupo " + grupo.getId()
						+ " ya tiene una sesion que se cruza el dia " + dia + " (" + existente.getHoraInicio()
						+ " - " + existente.getHoraFin() + ")");
			}
		}

		for (Grupo otro : grupoRepo.findByNumeroGrupo(grupo.getNumeroGrupo())) {
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
	}
}
