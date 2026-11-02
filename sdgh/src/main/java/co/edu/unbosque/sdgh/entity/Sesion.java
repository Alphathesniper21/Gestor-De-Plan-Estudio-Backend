package co.edu.unbosque.sdgh.entity;

import java.time.LocalTime;
import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad que representa una sesion de clase de un grupo en la grilla semanal
 * (RF3). Cada sesion dura 2 horas y se ubica en un dia (1 = lunes a 6 =
 * sabado) y una hora de inicio.
 */
@Entity
@Table(name = "sesion")
public class Sesion {

	/**
	 * Identificador unico de la sesion.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	/**
	 * Identificador del grupo al que pertenece la sesion.
	 */
	private int grupoId;

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

	public Sesion() {
		super();
	}

	public Sesion(int grupoId, int diaSemana, LocalTime horaInicio, LocalTime horaFin) {
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

	public int getGrupoId() {
		return grupoId;
	}

	public void setGrupoId(int grupoId) {
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
		return "Sesion [id=" + id + ", grupoId=" + grupoId + ", diaSemana=" + diaSemana + ", horaInicio=" + horaInicio
				+ ", horaFin=" + horaFin + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(Integer.valueOf(diaSemana), Integer.valueOf(grupoId), horaFin, horaInicio,
				Long.valueOf(id));
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Sesion other = (Sesion) obj;
		return diaSemana == other.diaSemana && grupoId == other.grupoId && Objects.equals(horaFin, other.horaFin)
				&& Objects.equals(horaInicio, other.horaInicio) && id == other.id;
	}

}