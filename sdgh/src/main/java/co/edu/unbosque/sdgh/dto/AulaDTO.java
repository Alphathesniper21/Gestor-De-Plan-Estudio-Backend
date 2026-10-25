package co.edu.unbosque.sdgh.dto;

import java.util.Objects;

public class AulaDTO {

	private Integer id;

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

	public AulaDTO(Integer id, int capacidad, boolean tieneSillasMovibles, boolean tieneComputadores) {
		super();
		this.id = id;
		this.capacidad = capacidad;
		this.tieneSillasMovibles = tieneSillasMovibles;
		this.tieneComputadores = tieneComputadores;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
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
		return "AulaDTO [id=" + id + ", capacidad=" + capacidad + ", tieneSillasMovibles=" + tieneSillasMovibles
				+ ", tieneComputadores=" + tieneComputadores + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, capacidad, tieneComputadores, tieneSillasMovibles);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		AulaDTO other = (AulaDTO) obj;
		return Objects.equals(id, other.id) && capacidad == other.capacidad
				&& tieneSillasMovibles == other.tieneSillasMovibles
				&& tieneComputadores == other.tieneComputadores;
	}

}
