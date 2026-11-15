/**
 * Paquete que contiene los servicios del aplicativo.
 */
package co.edu.unbosque.sdgh.service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.edu.unbosque.sdgh.dto.GrupoDTO;
import co.edu.unbosque.sdgh.dto.SesionDTO;
import co.edu.unbosque.sdgh.entity.ClaveParametro;
import co.edu.unbosque.sdgh.entity.Grupo;
import co.edu.unbosque.sdgh.entity.Materia;
import co.edu.unbosque.sdgh.entity.Sesion;
import co.edu.unbosque.sdgh.exception.DatosInvalidosException;
import co.edu.unbosque.sdgh.repository.GrupoRepository;
import co.edu.unbosque.sdgh.repository.MateriaRepository;
import co.edu.unbosque.sdgh.repository.SesionRepository;

/**
 * Servicio encargado de gestionar el horario como un todo (CU08), incluyendo
 * la revision de los grupos con sesiones pendientes (RF11) y la publicacion
 * del horario (RF12). El estado de publicacion se guarda en los parametros
 * mediante la clave horario.publicado.
 *
 * @author Sebastian Toyo
 */
@Service
public class HorarioService {

	/**
	 * Repositorio encargado de consultar los grupos registrados.
	 */
	@Autowired
	private GrupoRepository grupoRepo;

	/**
	 * Repositorio encargado de consultar las materias registradas.
	 */
	@Autowired
	private MateriaRepository materiaRepo;

	/**
	 * Repositorio encargado de consultar las sesiones programadas.
	 */
	@Autowired
	private SesionRepository sesionRepo;

	/**
	 * Servicio de parametros, donde se guarda y consulta el estado de
	 * publicacion del horario y se leen la franja y la pausa vigentes.
	 */
	@Autowired
	private ParametroService parametroServ;

	/**
	 * Mapper utilizado para convertir entidades en DTO y viceversa.
	 */
	@Autowired
	private ModelMapper mapper;

	/**
	 * Constructor vacio de la clase HorarioService.
	 */
	public HorarioService() {
	}

	/**
	 * Metodo que obtiene los grupos que todavia no tienen todas las sesiones
	 * correspondientes a su materia (RF11). Un grupo con una cantidad de
	 * sesiones inferior a la requerida se incluye en la lista de pendientes.
	 *
	 * @return Lista de grupos con sesiones pendientes en formato DTO.
	 */
	public List<GrupoDTO> getPendientes() {

		List<GrupoDTO> pendientes = new ArrayList<>();

		for (Grupo grupo : grupoRepo.findAll()) {
			Materia materia = materiaRepo.findById(grupo.getMateriaId()).orElse(null);
			if (materia == null) {
				continue;
			}
			int asignadas = sesionRepo.findByGrupoId(grupo.getId()).size();
			if (asignadas < materia.getSesionesSemanales()) {
				pendientes.add(mapper.map(grupo, GrupoDTO.class));
			}
		}

		return pendientes;
	}

	/**
	 * Metodo que publica el horario (RF12), siempre que todos los grupos tengan
	 * asignadas las sesiones correspondientes, exista al menos una sesion
	 * registrada y todas las sesiones cumplan la franja institucional y la
	 * pausa vigentes en los parametros (RF-4, RF-5, RF-9). Esta ultima
	 * revision es necesaria porque los parametros pueden cambiar despues de
	 * ubicar las sesiones. El estado de publicacion queda almacenado en la
	 * base de datos.
	 *
	 * @return Retorna 0 si el horario fue publicado correctamente.
	 * @throws DatosInvalidosException si existen grupos con sesiones pendientes,
	 *         si no hay sesiones registradas o si hay sesiones fuera del
	 *         horario configurado.
	 */
	public int publicar() {

		int pendientes = getPendientes().size();

		if (pendientes > 0) {
			throw new DatosInvalidosException("No se puede publicar: hay " + pendientes
					+ " grupo(s) con sesiones pendientes");
		}

		if (sesionRepo.count() == 0) {
			throw new DatosInvalidosException("No se puede publicar un horario sin sesiones");
		}

		Map<String, String> p = parametroServ.obtenerMapa();
		LocalTime horaInicioJornada = LocalTime.parse(p.get(ClaveParametro.HORA_INICIO_JORNADA));
		LocalTime horaFinSemana = LocalTime.parse(p.get(ClaveParametro.HORA_FIN_SEMANA));
		LocalTime horaFinSabado = LocalTime.parse(p.get(ClaveParametro.HORA_FIN_SABADO));
		LocalTime horaInicioPausa = LocalTime.parse(p.get(ClaveParametro.PAUSA_INICIO));
		LocalTime horaFinPausa = LocalTime.parse(p.get(ClaveParametro.PAUSA_FIN));

		int fueraDeHorario = 0;

		for (Sesion s : sesionRepo.findAll()) {
			LocalTime inicio = s.getHoraInicio();
			LocalTime fin = s.getHoraFin();
			int dia = s.getDiaSemana();

			boolean fuera = false;

			if (inicio.isBefore(horaInicioJornada)) {
				fuera = true;
			}
			if (dia <= 5 && fin.isAfter(horaFinSemana)) {
				fuera = true;
			}
			if (dia == 6 && fin.isAfter(horaFinSabado)) {
				fuera = true;
			}
			if (inicio.isBefore(horaFinPausa) && horaInicioPausa.isBefore(fin)) {
				fuera = true;
			}

			if (fuera) {
				fueraDeHorario++;
			}
		}

		if (fueraDeHorario > 0) {
			throw new DatosInvalidosException("No se puede publicar: hay " + fueraDeHorario
					+ " sesion(es) fuera del horario configurado");
		}

		parametroServ.marcarPublicado(true);
		return 0;
	}

	/**
	 * Metodo que consulta el estado de publicacion actual del horario (RF12).
	 *
	 * @return true si el horario esta publicado o false en caso contrario.
	 */
	public boolean estaPublicado() {

		return parametroServ.estaPublicado();
	}

	/**
	 * Metodo que obtiene todas las sesiones del horario publicado y las
	 * convierte al formato DTO. Si el horario no esta publicado, no permite
	 * realizar la consulta.
	 *
	 * @return Lista de sesiones publicadas en formato DTO.
	 * @throws DatosInvalidosException si el horario no esta publicado.
	 */
	public List<SesionDTO> getSesionesPublicadas() {

		if (!parametroServ.estaPublicado()) {
			throw new DatosInvalidosException("El horario no esta publicado");
		}
		List<SesionDTO> lista = new ArrayList<>();
		for (Sesion s : sesionRepo.findAll()) {
			lista.add(mapper.map(s, SesionDTO.class));
		}
		return lista;
	}
}