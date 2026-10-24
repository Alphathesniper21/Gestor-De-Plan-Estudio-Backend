package co.edu.unbosque.sdgh.dto;

import java.util.Objects;

public class EspacioConocimientoDTO {

	private int id;

	private String nombre;

	private String descripcion;

	public EspacioConocimientoDTO() {
	}

	public EspacioConocimientoDTO(int id, String nombre, String descripcion) {
		super();
		this.id = id;
		this.nombre = nombre;
		this.descripcion = descripcion;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	@Override
	public String toString() {
		return "EspacioConocimientoDTO [" + "id=" + id + ", nombre=" + nombre + ", descripcion="
			+ descripcion + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(descripcion, id, nombre);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		EspacioConocimientoDTO other = (EspacioConocimientoDTO) obj;
		return id == other.id && Objects.equals(nombre, other.nombre)
				&& Objects.equals(descripcion, other.descripcion);
	}

}
