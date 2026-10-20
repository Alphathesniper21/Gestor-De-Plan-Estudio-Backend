package co.edu.unbosque.sdgh.entity;

import java.util.Objects;

public class Aula {
	private int capacidad;
	private boolean tieneSillasMovibles;
	private boolean tieneComputadores;
	
	
	
	
	public Aula() {
		super();
		// TODO Auto-generated constructor stub
	}


	public Aula(int capacidad, boolean tieneSillasMovibles, boolean tieneComputadores) {
		super();
		this.capacidad = capacidad;
		this.tieneSillasMovibles = tieneSillasMovibles;
		this.tieneComputadores = tieneComputadores;
	}


	public int getCapacidad() {
		return capacidad;
	}


	public void setCapacidad(int capacidad) {
		this.capacidad = capacidad;
	}


	public boolean isTieneSillasMovibles() {
		return tieneSillasMovibles;
	}


	public void setTieneSillasMovibles(boolean tieneSillasMovibles) {
		this.tieneSillasMovibles = tieneSillasMovibles;
	}


	public boolean isTieneComputadores() {
		return tieneComputadores;
	}


	public void setTieneComputadores(boolean tieneComputadores) {
		this.tieneComputadores = tieneComputadores;
	}


	@Override
	public String toString() {
		return "Aula [capacidad=" + capacidad + ", tieneSillasMovibles=" + tieneSillasMovibles + ", tieneComputadores="
				+ tieneComputadores + "]";
	}


	@Override
	public int hashCode() {
		return Objects.hash(Integer.valueOf(capacidad), Boolean.valueOf(tieneComputadores),
				Boolean.valueOf(tieneSillasMovibles));
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Aula other = (Aula) obj;
		return capacidad == other.capacidad && tieneComputadores == other.tieneComputadores
				&& tieneSillasMovibles == other.tieneSillasMovibles;
	}
	
	
	
}
