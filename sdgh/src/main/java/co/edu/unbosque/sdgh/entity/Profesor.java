package co.edu.unbosque.sdgh.entity;

import java.util.Objects;

public class Profesor extends Usuario {
	
	
	private String especialidad;
	private int aniosExperiencia;
	
	public Profesor() {
		super();
		// TODO Auto-generated constructor stub
	}
	

	public Profesor(String especialidad, int aniosExperiencia) {
		super();
		this.especialidad = especialidad;
		this.aniosExperiencia = aniosExperiencia;
	}


	public Profesor(String nombre, int codigo, String correo, String facultad) {
		super(nombre, codigo, correo, facultad);
		// TODO Auto-generated constructor stub
	}


	public Profesor(String nombre, int codigo, String correo, String facultad, String especialidad,
			int aniosExperiencia) {
		super(nombre, codigo, correo, facultad);
		this.especialidad = especialidad;
		this.aniosExperiencia = aniosExperiencia;
	}


	public String getEspecialidad() {
		return especialidad;
	}


	public void setEspecialidad(String especialidad) {
		this.especialidad = especialidad;
	}


	public int getAniosExperiencia() {
		return aniosExperiencia;
	}


	public void setAniosExperiencia(int aniosExperiencia) {
		this.aniosExperiencia = aniosExperiencia;
	}


	@Override
	public String toString() {
		return "Profesor [especialidad=" + especialidad + ", aniosExperiencia=" + aniosExperiencia + "]";
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(aniosExperiencia, especialidad);
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Profesor other = (Profesor) obj;
		return aniosExperiencia == other.aniosExperiencia && Objects.equals(especialidad, other.especialidad);
	}

	
	
	
	

}
