package co.edu.unbosque.sdgh.dto;

import java.time.LocalTime;
import java.util.Objects;

/**
 * DTO que transporta los datos de una sesion de clase de un grupo.
 */
public class SesionDTO {

	/**
	 * Identificador unico de la sesion.
	 */
	private long id;

	/**
	 * Identificador del grupo al que pertenece la sesion.
	 */
	private long grupoId;

	/**
	 * Dia de la semana, de 1 (lunes) a 6 (sabado).
	 */
	private int diaSemana;

	/**
	 * Hora de inicio de la sesion.
	 */
	private LocalTime horaInicio;

	/**
	 * Hora de fin de la sesion.
	 */
	private LocalTime horaFin;

	public SesionDTO() {
	}

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

	@Override
	public String toString() {
		return "SesionDTO [id=" + id + ", grupoId=" + grupoId + ", diaSemana=" + diaSemana + ", horaInicio="
				+ horaInicio + ", horaFin=" + horaFin + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(Integer.valueOf(diaSemana), Long.valueOf(grupoId), horaFin, horaInicio, Long.valueOf(id));
	}

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