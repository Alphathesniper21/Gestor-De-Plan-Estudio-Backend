package co.edu.unbosque.sdgh.dto;

import java.time.LocalTime;
import java.util.Objects;

public class GrupoDTO {

	private int idGrupo;

	private long materiaId;

	private long docenteId;

	private long aulaId;

	private int cupoMaximo;

	private int estudiantesInscritos;

	private String diaSemana;

	private LocalTime horaInicio;

	private LocalTime horaFin;

	public GrupoDTO() {
	}

	public GrupoDTO(int idGrupo, long materiaId, long docenteId,
			long aulaId, int cupoMaximo, int estudiantesInscritos,
			String diaSemana, LocalTime horaInicio, LocalTime horaFin) {
		super();
		this.idGrupo = idGrupo;
		this.materiaId = materiaId;
		this.docenteId = docenteId;
		this.aulaId = aulaId;
		this.cupoMaximo = cupoMaximo;
		this.estudiantesInscritos = estudiantesInscritos;
		this.diaSemana = diaSemana;
		this.horaInicio = horaInicio;
		this.horaFin = horaFin;
	}

	public int getIdGrupo() {
		return idGrupo;
	}

	public void setIdGrupo(int idGrupo) {
		this.idGrupo = idGrupo;
	}

	public long getMateriaId() {
		return materiaId;
	}

	public void setMateriaId(long materiaId) {
		this.materiaId = materiaId;
	}

	public long getDocenteId() {
		return docenteId;
	}

	public void setDocenteId(long docenteId) {
		this.docenteId = docenteId;
	}

	public long getAulaId() {
		return aulaId;
	}

	public void setAulaId(long aulaId) {
		this.aulaId = aulaId;
	}

	public int getCupoMaximo() {
		return cupoMaximo;
	}

	public void setCupoMaximo(int cupoMaximo) {
		this.cupoMaximo = cupoMaximo;
	}

	public int getEstudiantesInscritos() {
		return estudiantesInscritos;
	}

	public void setEstudiantesInscritos(int estudiantesInscritos) {
		this.estudiantesInscritos = estudiantesInscritos;
	}

	public String getDiaSemana() {
		return diaSemana;
	}

	public void setDiaSemana(String diaSemana) {
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
		return "GrupoDTO [" + "idGrupo=" + idGrupo + ", materiaId=" + materiaId + ", docenteId=" + docenteId
			+ ", aulaId=" + aulaId + ", cupoMaximo=" + cupoMaximo + ", estudiantesInscritos="
			+ estudiantesInscritos + ", diaSemana=" + diaSemana + ", horaInicio=" + horaInicio
			+ ", horaFin=" + horaFin + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(aulaId, cupoMaximo, diaSemana, docenteId, estudiantesInscritos, horaFin, horaInicio, idGrupo, materiaId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		GrupoDTO other = (GrupoDTO) obj;
		return idGrupo == other.idGrupo && materiaId == other.materiaId
				&& docenteId == other.docenteId && aulaId == other.aulaId
				&& cupoMaximo == other.cupoMaximo && estudiantesInscritos == other.estudiantesInscritos
				&& Objects.equals(diaSemana, other.diaSemana) && Objects.equals(horaInicio, other.horaInicio)
				&& Objects.equals(horaFin, other.horaFin);
	}

}