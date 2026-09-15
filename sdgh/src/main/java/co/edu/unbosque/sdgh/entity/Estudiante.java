package co.edu.unbosque.sdgh.entity;

import java.util.Objects;

public class Estudiante extends Usuario {
	
	private int semestreActual;
	private boolean estado;
	
	
	
	public Estudiante() {
		super();
		// TODO Auto-generated constructor stub
	}



	public Estudiante(String nombre, int codigo, String correo, String facultad) {
		super(nombre, codigo, correo, facultad);
		// TODO Auto-generated constructor stub
	}



	public Estudiante(String nombre, int codigo, String correo, String facultad, int semestreActual, boolean estado) {
		super(nombre, codigo, correo, facultad);
		this.semestreActual = semestreActual;
		this.estado = estado;
	}



	public Estudiante(int semestreActual, boolean estado) {
		super();
		this.semestreActual = semestreActual;
		this.estado = estado;
	}



	public int getSemestreActual() {
		return semestreActual;
	}



	public void setSemestreActual(int semestreActual) {
		this.semestreActual = semestreActual;
	}



	public boolean isEstado() {
		return estado;
	}



	public void setEstado(boolean estado) {
		this.estado = estado;
	}



	@Override
	public String toString() {
		return "Estudiante [semestreActual=" + semestreActual + ", estado=" + estado + "]";
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(estado, semestreActual);
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
		Estudiante other = (Estudiante) obj;
		return estado == other.estado && semestreActual == other.semestreActual;
	}
	
	

}
