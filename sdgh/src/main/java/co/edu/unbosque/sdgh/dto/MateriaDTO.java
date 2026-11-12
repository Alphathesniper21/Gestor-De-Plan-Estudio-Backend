package co.edu.unbosque.sdgh.dto;

import java.util.Objects;

public class MateriaDTO {

	private long id;

	private String nombre;

	private int semestre;

	private int ncreditos;

	private boolean correlativa;

	public MateriaDTO() {
	}

	public MateriaDTO(String nombre, int semestre, int ncreditos,  boolean correlativa) {
		super();
		this.nombre = nombre;
		this.semestre = semestre;
		this.ncreditos = ncreditos;
		this.correlativa = correlativa;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public int getSemestre() {
		return semestre;
	}

	public void setSemestre(int semestre) {
		this.semestre = semestre;
	}

	public int getNcreditos() {
		return ncreditos;
	}

	public void setNcreditos(int ncreditos) {
		this.ncreditos = ncreditos;
	}

	public boolean isCorrelativa() {
		return correlativa;
	}

	public void setCorrelativa(boolean correlativa) {
		this.correlativa = correlativa;
	}
	
	
	@Override
	public String toString() {
		return "MateriaDTO [id=" + id + ", nombre=" + nombre + ", semestre=" + semestre + ", ncreditos=" + ncreditos
				+ ", correlativa=" + correlativa + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(Boolean.valueOf(correlativa), Long.valueOf(id), Integer.valueOf(ncreditos), nombre,
				Integer.valueOf(semestre));
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MateriaDTO other = (MateriaDTO) obj;
		return correlativa == other.correlativa && id == other.id && ncreditos == other.ncreditos
				&& Objects.equals(nombre, other.nombre) && semestre == other.semestre;
	}

	

}