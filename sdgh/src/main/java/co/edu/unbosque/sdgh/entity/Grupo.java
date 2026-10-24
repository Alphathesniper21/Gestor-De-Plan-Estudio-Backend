package co.edu.unbosque.sdgh.entity;

import java.time.LocalTime;
import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "grupos")
public class Grupo {

    @Id
    private int idGrupo;

    private long materiaId;
    private long docenteId;
    private long aulaId;

    private int cupoMaximo;
    private int estudiantesInscritos;

    private String diaSemana;          
    private LocalTime horaInicio;
    private LocalTime horaFin;

	public Grupo(int idGrupo, long materiaId, long docenteId, long aulaId, int cupoMaximo, int estudiantesInscritos,
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

	public Grupo() {
		super();
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
		return "Grupo [idGrupo=" + idGrupo + ", materiaId=" + materiaId + ", docenteId=" + docenteId + ", aulaId="
				+ aulaId + ", cupoMaximo=" + cupoMaximo + ", estudiantesInscritos=" + estudiantesInscritos
				+ ", diaSemana=" + diaSemana + ", horaInicio=" + horaInicio + ", horaFin=" + horaFin + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(Long.valueOf(aulaId), Integer.valueOf(cupoMaximo), diaSemana, Long.valueOf(docenteId),
				Integer.valueOf(estudiantesInscritos), horaFin, horaInicio, Integer.valueOf(idGrupo),
				Long.valueOf(materiaId));
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Grupo other = (Grupo) obj;
		return aulaId == other.aulaId && cupoMaximo == other.cupoMaximo && Objects.equals(diaSemana, other.diaSemana)
				&& docenteId == other.docenteId && estudiantesInscritos == other.estudiantesInscritos
				&& Objects.equals(horaFin, other.horaFin) && Objects.equals(horaInicio, other.horaInicio)
				&& idGrupo == other.idGrupo && materiaId == other.materiaId;
	}         
    
}
