package co.edu.unbosque.sdgh.entity;

import java.util.Collection;
import java.util.Objects;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class Usuario implements UserDetails{
	
	private String nombre;
	private int codigo;
	private String correo;
	private String facultad;
	
	public Usuario() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Usuario(String nombre, int codigo, String correo, String facultad) {
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
		return "Usuario [nombre=" + nombre + ", codigo=" + codigo + ", correo=" + correo + ", facultad=" + facultad
				+ "]";
	}
	@Override
	public int hashCode() {
		return Objects.hash(Integer.valueOf(codigo), correo, facultad, nombre);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Usuario other = (Usuario) obj;
		return codigo == other.codigo && Objects.equals(correo, other.correo)
				&& Objects.equals(facultad, other.facultad) && Objects.equals(nombre, other.nombre);
	}
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public @Nullable String getPassword() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return null;
	}	
	

}


