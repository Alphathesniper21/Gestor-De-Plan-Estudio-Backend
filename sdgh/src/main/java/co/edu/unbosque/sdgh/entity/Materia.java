package co.edu.unbosque.sdgh.entity;

import java.util.Objects;

public class Materia {
	

	private long codigo;
	private String nombre;
	private int semestre;
	private int ncreditos;
	private int horasDeLaMateria;
	private boolean correlativa;
	
		
		public Materia(long codigo, String nombre, int semestre, int ncreditos, int horasDeLaMateria, boolean correlativa) {
		super();
		this.codigo = codigo;
		this.nombre = nombre;
		this.semestre = semestre;
		this.ncreditos = ncreditos;
		this.horasDeLaMateria = horasDeLaMateria;
		this.correlativa = correlativa;
	}

		
	public Materia() {
		super();
	}


	public long getCodigo() {
		return codigo;
	}


	public void setCodigo(long codigo) {
		this.codigo = codigo;
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


	public int getHorasDeLaMateria() {
		return horasDeLaMateria;
	}


	public void setHorasDeLaMateria(int horasDeLaMateria) {
		this.horasDeLaMateria = horasDeLaMateria;
	}


	public boolean isCorrelativa() {
		return correlativa;
	}


	public void setCorrelativa(boolean correlativa) {
		this.correlativa = correlativa;
	}


	@Override
	public String toString() {
		return "Materia [codigo=" + codigo + ", nombre=" + nombre + ", semestre=" + semestre + ", ncreditos="
				+ ncreditos + ", horasDeLaMateria=" + horasDeLaMateria + ", correlativa=" + correlativa + "]";
	}


	@Override
	public int hashCode() {
		return Objects.hash(Long.valueOf(codigo), Boolean.valueOf(correlativa), Integer.valueOf(horasDeLaMateria),
				Integer.valueOf(ncreditos), nombre, Integer.valueOf(semestre));
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Materia other = (Materia) obj;
		return codigo == other.codigo && correlativa == other.correlativa && horasDeLaMateria == other.horasDeLaMateria
				&& ncreditos == other.ncreditos && Objects.equals(nombre, other.nombre) && semestre == other.semestre;
	}
	
	
	
	
	
	

}
