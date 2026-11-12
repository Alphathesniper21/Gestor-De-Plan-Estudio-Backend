package co.edu.unbosque.sdgh.dto;

import java.util.Objects;

import co.edu.unbosque.sdgh.entity.RolUsuario;

/**
 * DTO que transporta los datos de un usuario del sistema. La contrasena solo
 * se usa al recibir un registro o un cambio; nunca se devuelve al cliente.
 */
public class UsuarioDTO {

	/**
	 * Codigo unico del usuario.
	 */
	private long id;
	
	/**
	 * Nombre completo del usuario.
	 */
	private String nombre;


	/**
	 * Correo electronico del usuario.
	 */
	private String correo;

	/**
	 * Nombre de usuario usado como credencial de login.
	 */
	private String username;

	/**
	 * Contrasena en texto plano. Solo viaja del cliente al servidor.
	 */
	private String password;

	/**
	 * Rol del usuario: GESTOR o CONSULTA.
	 */
	private RolUsuario rol;

	public UsuarioDTO() {
		// TODO Auto-generated constructor stub
	}

	public UsuarioDTO(String nombre, String correo, String username, String password, RolUsuario rol) {
		super();
		this.nombre = nombre;
		this.correo = correo;
		this.username = username;
		this.password = password;
		this.rol = rol;
	}



	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getCorreo() {
		return correo;
	}

	public void setCorreo(String correo) {
		this.correo = correo;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public RolUsuario getRol() {
		return rol;
	}

	public void setRol(RolUsuario rol) {
		this.rol = rol;
	}

	@Override
	public String toString() {
		return "UsuarioDTO [id=" + id + ", nombre=" + nombre + ", correo=" + correo + ", username=" + username
				+ ", password=" + password + ", rol=" + rol + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(correo, Long.valueOf(id), nombre, password, rol, username);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UsuarioDTO other = (UsuarioDTO) obj;
		return Objects.equals(correo, other.correo) && id == other.id && Objects.equals(nombre, other.nombre)
				&& Objects.equals(password, other.password) && Objects.equals(rol, other.rol)
				&& Objects.equals(username, other.username);
	}

}