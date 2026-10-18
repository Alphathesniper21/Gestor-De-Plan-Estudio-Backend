package co.edu.unbosque.sdgh.dto;

import java.util.Objects;

public class UsuarioDTO {

	private String nombre;

	private int codigo;

	private String correo;

	private String facultad;

	public UsuarioDTO() {
	}

	public UsuarioDTO(String nombre, int codigo, String correo,
			String facultad) {
		super();
		this.nombre = nombre;
		this.codigo = codigo;
		this.correo = correo;
		this.facultad = facultad;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public int getCodigo() {
		return codigo;
	}

	public void setCodigo(int codigo) {
		this.codigo = codigo;
	}

	public String getCorreo() {
		return correo;
	}

	public void setCorreo(String correo) {
		this.correo = correo;
	}

	public String getFacultad() {
		return facultad;
	}

	public void setFacultad(String facultad) {
		this.facultad = facultad;
	}

	@Override
	public String toString() {
		return "UsuarioDTO [" + "nombre=" + nombre + ", codigo=" + codigo + ", correo=" + correo
			+ ", facultad=" + facultad + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(codigo, correo, facultad, nombre);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		UsuarioDTO other = (UsuarioDTO) obj;
		return Objects.equals(nombre, other.nombre) && codigo == other.codigo
				&& Objects.equals(correo, other.correo) && Objects.equals(facultad, other.facultad);
	}

}