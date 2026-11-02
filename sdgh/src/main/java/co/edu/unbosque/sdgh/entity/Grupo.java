package co.edu.unbosque.sdgh.entity;

import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Entidad que representa un grupo de un curso (RF2). El numero de grupo
 * define el bloque: el grupo 01 de todos los cursos de un semestre forma un
 * horario sin cruces (RF6). El dia y las horas de clase estan en las sesiones
 * del grupo (entidad Sesion).
 */
@Entity
@Table(name = "grupos", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "materiaId", "numeroGrupo" }) })
public class Grupo {

	/**
	 * Identificador unico del grupo.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	/**
	 * Codigo de la materia (curso) a la que pertenece el grupo.
	 */
	private long materiaId;

	/**
	 * Numero del grupo dentro del curso. Define el bloque (01, 02...).
	 */
	private int numeroGrupo;

	/**
	 * Cupo planeado del grupo. Maximo 40 mas 10 % de tolerancia (RF2).
	 */
	private int cupoPlaneado;

	public Grupo() {
		super();
	}

	public Grupo(long materiaId, int numeroGrupo, int cupoPlaneado) {
		super();
		this.materiaId = materiaId;
		this.numeroGrupo = numeroGrupo;
		this.cupoPlaneado = cupoPlaneado;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getMateriaId() {
		return materiaId;
	}

	public void setMateriaId(long materiaId) {
		this.materiaId = materiaId;
	}

	public int getNumeroGrupo() {
		return numeroGrupo;
	}

	public void setNumeroGrupo(int numeroGrupo) {
		this.numeroGrupo = numeroGrupo;
	}

	public int getCupoPlaneado() {
		return cupoPlaneado;
	}

	public void setCupoPlaneado(int cupoPlaneado) {
		this.cupoPlaneado = cupoPlaneado;
	}

	@Override
	public String toString() {
		return "Grupo [id=" + id + ", materiaId=" + materiaId + ", numeroGrupo=" + numeroGrupo + ", cupoPlaneado="
				+ cupoPlaneado + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(Integer.valueOf(cupoPlaneado), id, Long.valueOf(materiaId), Integer.valueOf(numeroGrupo));
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
		return cupoPlaneado == other.cupoPlaneado && Objects.equals(id, other.id) && materiaId == other.materiaId
				&& numeroGrupo == other.numeroGrupo;
	}

}