package co.edu.unbosque.sdgh.entity;

/**
 * Claves de los parametros del sistema (RF9). Se usan como constantes para que
 * un error de escritura en una clave lo detecte el compilador.
 */
public final class ClaveParametro {

	/** Hora de apertura de lunes a sabado (RF4). */
	public static final String HORA_INICIO_JORNADA = "hora.inicio.jornada";

	/** Hora de cierre de lunes a viernes (RF4). */
	public static final String HORA_FIN_SEMANA = "hora.fin.semana";

	/** Hora de cierre del sabado (RF4). */
	public static final String HORA_FIN_SABADO = "hora.fin.sabado";

	/** Inicio de la pausa del mediodia (RF5). */
	public static final String PAUSA_INICIO = "pausa.inicio";

	/** Fin de la pausa del mediodia (RF5). */
	public static final String PAUSA_FIN = "pausa.fin";

	/** Cupo base de un grupo antes de la tolerancia (RF2). */
	public static final String CUPO_BASE = "cupo.base";

	/** Tolerancia sobre el cupo base, entre 0 y 1 (RF2). */
	public static final String CUPO_TOLERANCIA = "cupo.tolerancia";

	/** Maximo de sesiones semanales de un curso (RF1, RF7). */
	public static final String SESIONES_MAXIMAS = "sesiones.maximas";

	/** Reservada: solo la modifica la publicacion del horario (RF12). */
	public static final String HORARIO_PUBLICADO = "horario.publicado";

	private ClaveParametro() {
	}
}
