/**
 * Paquete de servicios del aplicativo.
 */
package co.edu.unbosque.sdgh.service;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
 * grilla semanal (RF-3 y RF-8). Valida la franja permitida (RF-4), la pausa
 * (RF-5), el cruce entre cursos del mismo semestre y bloque de grupos (RF-6) y
 * el limite de sesiones del curso (RF-7). Los valores de franja y pausa se leen
 * de los parametros en cada validacion (RF-9, RNF-3). Toda modificacion de una
 * sesion deja el horario como no publicado (RF-12).
 */
@Service
public class SesionService implements CRUDOperation<SesionDTO> {

	/**
	 * Duracion de cada sesion en minutos (2 horas) (RF-3).
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
	 * Servicio de parametros que entrega los valores de franja y pausa.
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
	 * Metodo que ubica una sesion en la grilla validando RF-3 a RF-7. El dia es
	 * un numero de 1 (lunes) a 6 (sabado).
	 * @param newData DTO con la sesion a crear.
	 * @return Retorna 0 si la sesion fue creada correctamente.
	 * @throws DatosInvalidosException si los datos son nulos o estan incompletos.
	 * @throws RecursoNoEncontradoException si el grupo o su curso no existen.
	 * @throws FranjaInstitucionalInvalidaException si incumple la duracion, la
	 *         franja o la pausa (RF-3, RF-4, RF-5).
	 * @throws CruceHorarioException si se cruza con otra sesion del mismo
	 *         semestre y bloque (RF-6).
	 * @throws LimiteSesionesExcedidoException si el grupo ya tiene todas las
	 *         sesiones de su curso (RF-7).
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
				() -> new RecursoNoEncontradoException("No existe una materia con el codigo " + grupo.getMateriaId()));

		LocalTime inicio = newData.getHoraInicio();
		LocalTime fin = newData.getHoraFin();
		int dia = newData.getDiaSemana();

		Map<String, String> p = parametroServ.obtenerMapa();
		LocalTime horaInicioJornada = LocalTime.parse(p.get(ClaveParametro.HORA_INICIO_JORNADA));
		LocalTime horaFinSemana = LocalTime.parse(p.get(ClaveParametro.HORA_FIN_SEMANA));
		LocalTime horaFinSabado = LocalTime.parse(p.get(ClaveParametro.HORA_FIN_SABADO));
		LocalTime horaInicioPausa = LocalTime.parse(p.get(ClaveParametro.PAUSA_INICIO));
		LocalTime horaFinPausa = LocalTime.parse(p.get(ClaveParametro.PAUSA_FIN));

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
		if (inicio.isBefore(horaInicioJornada)) {
			throw new FranjaInstitucionalInvalidaException("Las clases inician a partir de las " + horaInicioJornada);
		}
		if (dia <= 5 && fin.isAfter(horaFinSemana)) {
			throw new FranjaInstitucionalInvalidaException("De lunes a viernes las clases deben estar entre las "
					+ horaInicioJornada + " y las " + horaFinSemana);
		}
		if (dia == 6 && fin.isAfter(horaFinSabado)) {
			throw new FranjaInstitucionalInvalidaException(
					"Los sabados las clases deben estar entre las " + horaInicioJornada + " y las " + horaFinSabado);
		}
		if (inicio.isBefore(horaFinPausa) && horaInicioPausa.isBefore(fin)) {
			throw new FranjaInstitucionalInvalidaException(
					"No se permite programar clases entre las " + horaInicioPausa + " y las " + horaFinPausa);
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
							+ ") el dia " + dia + " (" + existente.getHoraInicio() + " - "
							+ existente.getHoraFin() + ")");
				}
			}
		}

		Sesion entity = mapper.map(newData, Sesion.class);
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
	 * Metodo que quita una sesion de la grilla, por lo que el grupo queda con una
	 * sesion pendiente y el horario deja de estar publicado (RF-8, RF-11, RF-12).
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
	 * Metodo que mueve una sesion a otro dia u hora, validando de nuevo RF-3 a
	 * RF-6. El grupo de la sesion no cambia, por lo que el limite de sesiones
	 * (RF-7) no se vuelve a evaluar.
	 * @param id Identificador de la sesion a mover.
	 * @param newData DTO con el nuevo dia y las nuevas horas.
	 * @return Retorna 0 si la actualizacion fue exitosa.
	 * @throws DatosInvalidosException si el id es nulo o los datos estan
	 *         incompletos.
	 * @throws RecursoNoEncontradoException si la sesion, su grupo o su curso no
	 *         existen.
	 * @throws FranjaInstitucionalInvalidaException si incumple la duracion, la
	 *         franja o la pausa (RF-3, RF-4, RF-5).
	 * @throws CruceHorarioException si se cruza con otra sesion del mismo
	 *         semestre y bloque (RF-6).
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
				() -> new RecursoNoEncontradoException("No existe una materia con el codigo " + grupo.getMateriaId()));

		LocalTime inicio = newData.getHoraInicio();
		LocalTime fin = newData.getHoraFin();
		int dia = newData.getDiaSemana();

		Map<String, String> p = parametroServ.obtenerMapa();
		LocalTime horaInicioJornada = LocalTime.parse(p.get(ClaveParametro.HORA_INICIO_JORNADA));
		LocalTime horaFinSemana = LocalTime.parse(p.get(ClaveParametro.HORA_FIN_SEMANA));
		LocalTime horaFinSabado = LocalTime.parse(p.get(ClaveParametro.HORA_FIN_SABADO));
		LocalTime horaInicioPausa = LocalTime.parse(p.get(ClaveParametro.PAUSA_INICIO));
		LocalTime horaFinPausa = LocalTime.parse(p.get(ClaveParametro.PAUSA_FIN));

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
		if (inicio.isBefore(horaInicioJornada)) {
			throw new FranjaInstitucionalInvalidaException("Las clases inician a partir de las " + horaInicioJornada);
		}
		if (dia <= 5 && fin.isAfter(horaFinSemana)) {
			throw new FranjaInstitucionalInvalidaException("De lunes a viernes las clases deben estar entre las "
					+ horaInicioJornada + " y las " + horaFinSemana);
		}
		if (dia == 6 && fin.isAfter(horaFinSabado)) {
			throw new FranjaInstitucionalInvalidaException(
					"Los sabados las clases deben estar entre las " + horaInicioJornada + " y las " + horaFinSabado);
		}
		if (inicio.isBefore(horaFinPausa) && horaInicioPausa.isBefore(fin)) {
			throw new FranjaInstitucionalInvalidaException(
					"No se permite programar clases entre las " + horaInicioPausa + " y las " + horaFinPausa);
		}

		for (Sesion existente : sesionRepo.findByGrupoId(grupo.getId())) {
			if (existente.getId() == id.longValue()) {
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
							+ ") el dia " + dia + " (" + existente.getHoraInicio() + " - "
							+ existente.getHoraFin() + ")");
				}
			}
		}

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
	 * Metodo que informa las advertencias de un grupo: no rechaza, solo avisa si
	 * el grupo tiene sesiones en dias consecutivos. Esta fuera del alcance del
	 * prototipo (trabajo futuro, seccion 2.5) y se conserva solo como consulta.
	 * @param grupoId Identificador del grupo.
	 * @return Retorna la lista de advertencias.
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
					advertencias.add("ADVERTENCIA - El grupo " + grupoId
							+ " tiene sesiones en dias consecutivos (" + sesiones.get(i).getDiaSemana() + " y "
							+ sesiones.get(j).getDiaSemana() + ")");
				}
			}
		}

		return advertencias;
	}

	/**
	 * Metodo que obtiene las sesiones de los cursos de un semestre (RF-10).
	 * @param semestre Semestre a consultar, de 1 a 9.
	 * @return Retorna una lista de sesiones en formato DTO del semestre indicado.
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
}