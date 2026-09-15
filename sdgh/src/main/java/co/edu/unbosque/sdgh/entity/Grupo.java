package co.edu.unbosque.sdgh.entity;

import java.util.Objects;

public class Grupo {
	
	private int idGrupo;
	private long materiaId;
	private long docenteId;
	private long aulaId;
	private int cupoMaximo;
	private int estudiantesInscritos;
	
	
	
	
	public Grupo() {
		super();
		// TODO Auto-generated constructor stub
	}




	public Grupo(int idGrupo, long materiaId, long docenteId, long aulaId, int cupoMaximo, int estudiantesInscritos) {
		super();
		this.idGrupo = idGrupo;
		this.materiaId = materiaId;
		this.docenteId = docenteId;
		this.aulaId = aulaId;
		this.cupoMaximo = cupoMaximo;
		this.estudiantesInscritos = estudiantesInscritos;
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




	@Override
	public String toString() {
		return "Grupo [idGrupo=" + idGrupo + ", materiaId=" + materiaId + ", docenteId=" + docenteId + ", aulaId="
				+ aulaId + ", cupoMaximo=" + cupoMaximo + ", estudiantesInscritos=" + estudiantesInscritos + "]";
	}




	@Override
	public int hashCode() {
		return Objects.hash(Long.valueOf(aulaId), Integer.valueOf(cupoMaximo), Long.valueOf(docenteId),
				Integer.valueOf(estudiantesInscritos), Integer.valueOf(idGrupo), Long.valueOf(materiaId));
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
		return aulaId == other.aulaId && cupoMaximo == other.cupoMaximo && docenteId == other.docenteId
				&& estudiantesInscritos == other.estudiantesInscritos && idGrupo == other.idGrupo
				&& materiaId == other.materiaId;
	}
	
	

}
