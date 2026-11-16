/**
 * Paquete que contiene los servicios del aplicativo.
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
 * Servicio encargado de gestionar los parametros configurables del sistema
 * (RF9, RNF3). Permite consultar los valores registrados, obtener valores
 * predeterminados y actualizar los parametros existentes. Los demas servicios
 * consultan estos valores durante sus validaciones, por lo que los cambios se
 * aplican sin necesidad de recompilar ni redesplegar el aplicativo.
 *
 * @author Sebastian Toyo
 */
@Service
public class ParametroService {

	/**
	 * Mapa que contiene los valores iniciales de los parametros del sistema.
	 * Es utilizado por LoadDatabase para precargar la tabla y por obtenerMapa()
	 * como respaldo cuando falta alguna clave.
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
	 * Repositorio encargado de las operaciones de persistencia de los parametros.
	 */
	@Autowired
	private ParametroRepository parametroRepo;

	/**
	 * Constructor vacio de la clase ParametroService.
	 */
	public ParametroService() {
	}

	/**
	 * Metodo que obtiene todos los parametros en un mapa de clave y valor.
	 * Parte de los valores predeterminados y los sobrescribe con los datos
	 * almacenados en la base de datos, garantizando que las claves tengan
	 * un valor disponible (RF9, RNF3).
	 *
	 * @return Mapa ordenado que contiene las claves y los valores de los parametros.
	 */
	public Map<String, String> obtenerMapa() {

		Map<String, String> mapa = new TreeMap<>(VALORES_POR_DEFECTO);
		for (Parametro p : parametroRepo.findAll()) {
			mapa.put(p.getClave(), p.getValor());
		}
		return mapa;
	}

	/**
	 * Metodo que obtiene la lista de parametros editables del sistema.
	 * Excluye el parametro correspondiente al estado de publicacion del horario.
	 *
	 * @return Lista de parametros en formato DTO.
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
	 * Metodo que consulta un parametro de tipo hora y convierte su valor
	 * al formato LocalTime.
	 *
	 * @param clave Clave del parametro que se desea consultar.
	 * @return Hora configurada para el parametro.
	 */
	public LocalTime getHora(String clave) {
		return LocalTime.parse(obtenerMapa().get(clave));
	}

	/**
	 * Metodo que consulta un parametro de tipo entero y convierte su valor
	 * a un numero entero.
	 *
	 * @param clave Clave del parametro que se desea consultar.
	 * @return Valor entero configurado para el parametro.
	 */
	public int getEntero(String clave) {
		return Integer.parseInt(obtenerMapa().get(clave));
	}

	/**
	 * Metodo que consulta un parametro de tipo decimal y convierte su valor
	 * a un numero de tipo double.
	 *
	 * @param clave Clave del parametro que se desea consultar.
	 * @return Valor decimal configurado para el parametro.
	 */
	public double getDecimal(String clave) {
		return Double.parseDouble(obtenerMapa().get(clave));
	}

	/**
	 * Metodo que calcula el cupo maximo permitido para un grupo a partir del
	 * cupo base y la tolerancia configurada. El resultado se redondea hacia
	 * abajo (RF2).
	 *
	 * @return Cupo maximo permitido para un grupo.
	 */
	public int getCupoMaximo() {
		return (int) Math.floor(getEntero(ClaveParametro.CUPO_BASE) * (1.0 + getDecimal(ClaveParametro.CUPO_TOLERANCIA)) + 1e-9);
	}

	/**
	 * Metodo que consulta el estado de publicacion actual del horario (RF12).
	 *
	 * @return true si el horario esta publicado o false en caso contrario.
	 */
	public boolean estaPublicado() {
		return Boolean.parseBoolean(obtenerMapa().get(ClaveParametro.HORARIO_PUBLICADO));
	}

	/**
	 * Metodo que actualiza el estado de publicacion del horario y almacena
	 * el nuevo valor en la base de datos (RF12).
	 *
	 * @param publicado Estado de publicacion que se desea establecer.
	 */
	public void marcarPublicado(boolean publicado) {

		Parametro temp = parametroRepo.findById(ClaveParametro.HORARIO_PUBLICADO)
				.orElse(new Parametro(ClaveParametro.HORARIO_PUBLICADO, "false"));
		temp.setValor(String.valueOf(publicado));
		parametroRepo.save(temp);
	}

	/**
	 * Metodo que actualiza el valor de un parametro despues de validar su
	 * formato y rango permitido (CU06). No permite modificar directamente
	 * el parametro correspondiente al estado de publicacion.
	 *
	 * @param clave Clave del parametro que se desea actualizar.
	 * @param valor Nuevo valor que se asignara al parametro.
	 * @return Retorna 0 si la actualizacion fue exitosa.
	 * @throws DatosInvalidosException si la clave o el valor estan vacios,
	 *         el formato es incorrecto, el valor esta fuera de rango o se
	 *         intenta modificar el estado de publicacion.
	 * @throws RecursoNoEncontradoException si la clave del parametro no existe.
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
		marcarPublicado(false);
		return 0;
	}
}