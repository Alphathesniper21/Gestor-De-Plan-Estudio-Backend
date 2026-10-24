package co.edu.unbosque.sdgh.entity;

import java.util.Collection;
import java.util.Objects;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios")
@Inheritance(strategy = InheritanceType.JOINED)
public class Usuario implements UserDetails {

	@Id
	private int codigo;

	private String nombre;
	private String correo;
	private String facultad;
	private String username;

	public Usuario() {
		super();
	}

	public Usuario(String nombre, int codigo, String correo, String facultad) {
		super();
		this.nombre = nombre;
		this.codigo = codigo;
		this.correo = correo;
		this.facultad = facultad;
		this.username = correo;
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

	public String getUsernamePropio() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Override
	public String toString() {
		return "Usuario [nombre=" + nombre + ", codigo=" + codigo + ", correo=" + correo + ", facultad=" + facultad
				+ ", username=" + username + "]";
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
		return null;
	}

	@Override
	public @Nullable String getPassword() {
		return null;
	}

	@Override
	public String getUsername() {
		return this.username != null && !this.username.trim().isEmpty() ? this.username : this.correo;
	}

}
