package co.edu.unbosque.sdgh.entity;

import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "aulas")
public class Aula {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private int capacidad;
	private boolean tieneSillasMovibles;
	private boolean tieneComputadores;

	public Aula() {
		super();
	}

	public Aula(int capacidad, boolean tieneSillasMovibles, boolean tieneComputadores) {
		super();
		this.capacidad = capacidad;
		this.tieneSillasMovibles = tieneSillasMovibles;
		this.tieneComputadores = tieneComputadores;
	}

	public Aula(Integer id, int capacidad, boolean tieneSillasMovibles, boolean tieneComputadores) {
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
		return "Aula [id=" + id + ", capacidad=" + capacidad + ", tieneSillasMovibles=" + tieneSillasMovibles
				+ ", tieneComputadores=" + tieneComputadores + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, Integer.valueOf(capacidad), Boolean.valueOf(tieneComputadores),
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
		return Objects.equals(id, other.id) && capacidad == other.capacidad
				&& tieneComputadores == other.tieneComputadores
				&& tieneSillasMovibles == other.tieneSillasMovibles;
	}

}
