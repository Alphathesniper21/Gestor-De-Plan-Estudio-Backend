/**
 * Paquete de servicios del aplicativo.
 */
package co.edu.unbosque.sdgh.service;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.edu.unbosque.sdgh.dto.ParametroDTO;
import co.edu.unbosque.sdgh.entity.ClaveParametro;
import co.edu.unbosque.sdgh.entity.Parametro;
import co.edu.unbosque.sdgh.exception.DatosInvalidosException;
import co.edu.unbosque.sdgh.exception.RecursoNoEncontradoException;
import co.edu.unbosque.sdgh.repository.ParametroRepository;

/**
 * Servicio encargado de los parametros configurables del sistema (RF9, RNF3).
 * Los demas servicios leen los valores en cada validacion, de modo que un
 * cambio se aplica sin recompilar ni redesplegar. No implementa CRUDOperation
 * porque los parametros no se crean ni se eliminan: solo se consultan y se
 * actualizan.
 */
@Service
public class ParametroService {

	/**
	 * Unica fuente de los valores iniciales: la usa LoadDatabase para precargar
	 * la tabla y obtenerMapa() como respaldo si falta una clave.
	 */
	public static final Map<String, String> VALORES_POR_DEFECTO = Map.of(
			ClaveParametro.HORA_INICIO_JORNADA, "07:00",
			ClaveParametro.HORA_FIN_SEMANA, "22:00",
			ClaveParametro.HORA_FIN_SABADO, "13:00",
			ClaveParametro.PAUSA_INICIO, "12:00",
			ClaveParametro.PAUSA_FIN, "13:00",
			ClaveParametro.CUPO_BASE, "40",
			ClaveParametro.CUPO_TOLERANCIA, "0.10",
			ClaveParametro.SESIONES_MAXIMAS, "4",
			ClaveParametro.HORARIO_PUBLICADO, "false");

	/**
	 * Repositorio de los parametros.
	 */
	@Autowired
	private ParametroRepository parametroRepo;

	/**
	 * Constructor vacio de la clase ParametroService.
	 */
	public ParametroService() {
	}

	/**
	 * Retorna todos los parametros en un mapa clave-valor con una sola consulta.
	 * Parte de los valores por defecto y los sobrescribe con los de la base, de
	 * modo que nunca falta una clave (RF9, RNF3).
	 * @return Retorna el mapa ordenado por clave.
	 */
	public Map<String, String> obtenerMapa() {

		Map<String, String> mapa = new TreeMap<>(VALORES_POR_DEFECTO);
		for (Parametro p : parametroRepo.findAll()) {
			mapa.put(p.getClave(), p.getValor());
		}
		return mapa;
	}

	/**
	 * Lista los parametros editables. El estado de publicacion no se incluye.
	 * @return Retorna la lista de parametros en formato DTO.
	 */
	public List<ParametroDTO> getAll() {

		List<ParametroDTO> lista = new ArrayList<>();
		for (Map.Entry<String, String> e : obtenerMapa().entrySet()) {
			if (!ClaveParametro.HORARIO_PUBLICADO.equals(e.getKey())) {
				lista.add(new ParametroDTO(e.getKey(), e.getValue()));
			}
		}
		return lista;
	}

	/**
	 * Metodo que lee un parametro de tipo hora (HH:mm).
	 * @param clave Clave del parametro.
	 * @return Retorna la hora configurada.
	 */
	public LocalTime getHora(String clave) {
		return LocalTime.parse(obtenerMapa().get(clave));
	}

	/**
	 * Metodo que lee un parametro de tipo entero.
	 * @param clave Clave del parametro.
	 * @return Retorna el valor configurado.
	 */
	public int getEntero(String clave) {
		return Integer.parseInt(obtenerMapa().get(clave));
	}

	/**
	 * Metodo que lee un parametro de tipo decimal.
	 * @param clave Clave del parametro.
	 * @return Retorna el valor configurado.
	 */
	public double getDecimal(String clave) {
		return Double.parseDouble(obtenerMapa().get(clave));
	}

	/**
	 * Metodo que calcula el cupo maximo de un grupo: cupo base por (1 mas la
	 * tolerancia), redondeado hacia abajo (RF2). Con 40 y 0.10 da 44; con 30 da 33.
	 * @return Retorna el cupo maximo permitido.
	 */
	public int getCupoMaximo() {
		return (int) Math.floor(getEntero(ClaveParametro.CUPO_BASE) * (1.0 + getDecimal(ClaveParametro.CUPO_TOLERANCIA)) + 1e-9);
	}

	/**
	 * Metodo que indica si el horario esta publicado (RF12).
	 * @return Retorna true si el horario esta publicado.
	 */
	public boolean estaPublicado() {
		return Boolean.parseBoolean(obtenerMapa().get(ClaveParametro.HORARIO_PUBLICADO));
	}

	/**
	 * Metodo que marca el horario como publicado o no publicado (RF12). Solo lo
	 * usan HorarioService al publicar y SesionService cuando el horario cambia.
	 * @param publicado Nuevo estado.
	 */
	public void marcarPublicado(boolean publicado) {

		Parametro temp = parametroRepo.findById(ClaveParametro.HORARIO_PUBLICADO)
				.orElse(new Parametro(ClaveParametro.HORARIO_PUBLICADO, "false"));
		temp.setValor(String.valueOf(publicado));
		parametroRepo.save(temp);
	}

	/**
	 * Cambia el valor de un parametro validando su formato y su rango (CU06).
	 * @param clave Clave del parametro.
	 * @param valor Nuevo valor.
	 * @return Retorna 0 si la actualizacion fue exitosa.
	 * @throws DatosInvalidosException si el valor es vacio, no tiene el formato
	 *         esperado o esta fuera de rango, o si la clave es la de publicacion.
	 * @throws RecursoNoEncontradoException si la clave no existe.
	 */
	public int updateByClave(String clave, String valor) {

		if (clave == null || clave.trim().isEmpty()) {
			throw new DatosInvalidosException("La clave del parametro no puede estar vacia");
		}
		if (valor == null || valor.trim().isEmpty()) {
			throw new DatosInvalidosException("El valor del parametro no puede estar vacio");
		}

		final String claveLimpia = clave.trim();
		final String valorLimpio = valor.trim();

		if (ClaveParametro.HORARIO_PUBLICADO.equals(claveLimpia)) {
			throw new DatosInvalidosException(
					"El estado de publicacion no es un parametro editable: solo cambia al publicar el horario");
		}
		if (!VALORES_POR_DEFECTO.containsKey(claveLimpia)) {
			throw new RecursoNoEncontradoException("No existe el parametro " + claveLimpia);
		}

		Map<String, String> p = obtenerMapa();
		p.put(claveLimpia, valorLimpio);

		switch (claveLimpia) {
		case ClaveParametro.HORA_INICIO_JORNADA, ClaveParametro.HORA_FIN_SEMANA, ClaveParametro.HORA_FIN_SABADO,
				ClaveParametro.PAUSA_INICIO, ClaveParametro.PAUSA_FIN -> {
			LocalTime inicioJornada;
			LocalTime finSemana;
			LocalTime finSabado;
			LocalTime inicioPausa;
			LocalTime finPausa;
			try {
				inicioJornada = LocalTime.parse(p.get(ClaveParametro.HORA_INICIO_JORNADA));
				finSemana = LocalTime.parse(p.get(ClaveParametro.HORA_FIN_SEMANA));
				finSabado = LocalTime.parse(p.get(ClaveParametro.HORA_FIN_SABADO));
				inicioPausa = LocalTime.parse(p.get(ClaveParametro.PAUSA_INICIO));
				finPausa = LocalTime.parse(p.get(ClaveParametro.PAUSA_FIN));
			} catch (DateTimeParseException e) {
				throw new DatosInvalidosException("El valor debe tener formato HH:mm (por ejemplo 07:00)");
			}
			if (!inicioJornada.isBefore(finSemana) || !inicioJornada.isBefore(finSabado)) {
				throw new DatosInvalidosException(
						"Valor fuera de rango: el inicio de la jornada debe ser anterior a los cierres");
			}
			if (!inicioPausa.isBefore(finPausa)) {
				throw new DatosInvalidosException(
						"Valor fuera de rango: el inicio de la pausa debe ser anterior a su fin");
			}
			if (inicioPausa.isBefore(inicioJornada) || finPausa.isAfter(finSemana)) {
				throw new DatosInvalidosException(
						"Valor fuera de rango: la pausa debe estar dentro de la franja de lunes a viernes");
			}
		}
		case ClaveParametro.CUPO_BASE -> {
			int cupo;
			try {
				cupo = Integer.parseInt(valorLimpio);
			} catch (NumberFormatException e) {
				throw new DatosInvalidosException("El cupo base debe ser un numero entero");
			}
			if (cupo < 1 || cupo > 200) {
				throw new DatosInvalidosException("Valor fuera de rango: el cupo base debe estar entre 1 y 200");
			}
		}
		case ClaveParametro.CUPO_TOLERANCIA -> {
			double tolerancia;
			try {
				tolerancia = Double.parseDouble(valorLimpio);
			} catch (NumberFormatException e) {
				throw new DatosInvalidosException("La tolerancia debe ser un numero (por ejemplo 0.10)");
			}
			if (tolerancia < 0 || tolerancia > 1) {
				throw new DatosInvalidosException("Valor fuera de rango: la tolerancia debe estar entre 0 y 1");
			}
		}
		case ClaveParametro.SESIONES_MAXIMAS -> {
			int maximo;
			try {
				maximo = Integer.parseInt(valorLimpio);
			} catch (NumberFormatException e) {
				throw new DatosInvalidosException("El maximo de sesiones debe ser un numero entero");
			}
			if (maximo < 1 || maximo > 10) {
				throw new DatosInvalidosException(
						"Valor fuera de rango: el maximo de sesiones debe estar entre 1 y 10");
			}
		}
		default -> throw new DatosInvalidosException("Parametro desconocido: " + claveLimpia);
		}

		Parametro temp = parametroRepo.findById(claveLimpia).orElse(new Parametro(claveLimpia, valorLimpio));
		temp.setValor(valorLimpio);
		parametroRepo.save(temp);

		return 0;
	}
}