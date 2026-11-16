package co.edu.unbosque.sdgh.dto;

import java.time.LocalTime;
import java.util.Objects;

/**
 * DTO que transporta los datos de una sesión de clase de un grupo en la
 * grilla semanal.
 * <p>
 * Una sesión queda definida por el grupo al que pertenece, el día de la semana
 * (1 = lunes a 6 = sábado) y su hora de inicio y fin. Las validaciones de
 * franja institucional, pausa, máximo de sesiones y cruces de horario se
 * aplican en la capa de servicio.
 * </p>
 *
 * @author Laura Gomez
 * @version 1.0
 * @since 1.0
 * @see GrupoDTO
 * @see MateriaDTO
 */
public class SesionDTO {

	/**
	 * Identificador único de la sesión.
	 */
	private long id;

	/**
	 * Identificador del grupo al que pertenece la sesión.
	 */
	private long grupoId;

	/**
	 * Día de la semana, de 1 (lunes) a 6 (sábado).
	 */
	private int diaSemana;

	/**
	 * Hora de inicio de la sesión.
	 */
	private LocalTime horaInicio;

	/**
	 * Hora de fin de la sesión.
	 */
	private LocalTime horaFin;

	/**
	 * Constructor vacío requerido para la deserialización y el mapeo de datos.
	 */
	public SesionDTO() {
	}

	/**
	 * Construye una sesión con los datos necesarios para su creación.
	 *
	 * @param grupoId    identificador del grupo al que pertenece la sesión
	 * @param diaSemana  día de la semana, de 1 (lunes) a 6 (sábado)
	 * @param horaInicio hora de inicio de la sesión
	 * @param horaFin    hora de fin de la sesión
	 */
	public SesionDTO(long grupoId, int diaSemana, LocalTime horaInicio, LocalTime horaFin) {
		super();
		this.grupoId = grupoId;
		this.diaSemana = diaSemana;
		this.horaInicio = horaInicio;
		this.horaFin = horaFin;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getGrupoId() {
		return grupoId;
	}

	public void setGrupoId(long grupoId) {
		this.grupoId = grupoId;
	}

	public int getDiaSemana() {
		return diaSemana;
	}

	public void setDiaSemana(int diaSemana) {
		this.diaSemana = diaSemana;
	}

	public LocalTime getHoraInicio() {
		return horaInicio;
	}

	public void setHoraInicio(LocalTime horaInicio) {
		this.horaInicio = horaInicio;
	}

	public LocalTime getHoraFin() {
		return horaFin;
	}

	public void setHoraFin(LocalTime horaFin) {
		this.horaFin = horaFin;
	}

	/**
	 * Representación en texto de la sesión con todos sus atributos.
	 *
	 * @return cadena descriptiva de la sesión
	 */
	@Override
	public String toString() {
		return "SesionDTO [id=" + id + ", grupoId=" + grupoId + ", diaSemana=" + diaSemana + ", horaInicio="
				+ horaInicio + ", horaFin=" + horaFin + "]";
	}

	/**
	 * Calcula el código hash a partir de todos los atributos de la sesión.
	 *
	 * @return código hash del objeto
	 */
	@Override
	public int hashCode() {
		return Objects.hash(Integer.valueOf(diaSemana), Long.valueOf(grupoId), horaFin, horaInicio, Long.valueOf(id));
	}

	/**
	 * Compara esta sesión con otro objeto atributo por atributo.
	 *
	 * @param obj objeto a comparar
	 * @return {@code true} si ambos son {@link SesionDTO} con los mismos valores
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SesionDTO other = (SesionDTO) obj;
		return diaSemana == other.diaSemana && grupoId == other.grupoId && Objects.equals(horaFin, other.horaFin)
				&& Objects.equals(horaInicio, other.horaInicio) && id == other.id;
	}

}