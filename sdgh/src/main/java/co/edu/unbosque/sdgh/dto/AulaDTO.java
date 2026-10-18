package co.edu.unbosque.sdgh.dto;

import java.util.Objects;

public class AulaDTO {

	private int capacidad;

	private boolean tieneSillasMovibles;

	private boolean tieneComputadores;

	public AulaDTO() {
	}

	public AulaDTO(int capacidad, boolean tieneSillasMovibles, boolean tieneComputadores) {
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
		return "AulaDTO [" + "capacidad=" + capacidad + ", tieneSillasMovibles=" + tieneSillasMovibles
			+ ", tieneComputadores=" + tieneComputadores + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(capacidad, tieneComputadores, tieneSillasMovibles);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		AulaDTO other = (AulaDTO) obj;
		return capacidad == other.capacidad && tieneSillasMovibles == other.tieneSillasMovibles
				&& tieneComputadores == other.tieneComputadores;
	}

}