package co.edu.unbosque.sdgh.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad que representa a un usuario del sistema. Implementa
 * {@link UserDetails} para la autenticacion con JWT. La contrasena se guarda
 * siempre con hash. El rol define los permisos: GESTOR arma el horario y
 * CONSULTA solo lo consulta.
 */
@Entity
@Table(name = "usuario")
public class Usuario implements UserDetails {

	/**
	 * Codigo unico del usuario.
	 */
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
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
	 * Contrasena con hash.
	 */
	private String password;

	/**
	 * Rol del usuario: GESTOR o CONSULTA.
	 */
	@Enumerated(EnumType.STRING)
	private RolUsuario rol;

	public Usuario() {
		super();
	}

	public Usuario(String nombre, String correo, String username, String password, RolUsuario rol) {
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

	public RolUsuario getRol() {
		return rol;
	}

	public void setRol(RolUsuario rol) {
		this.rol = rol;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Retorna los permisos del usuario a partir de su rol, con el prefijo
	 * ROLE_ que exige Spring Security.
	 *
	 * @return Lista con un unico permiso, o vacia si el rol es nulo.
	 */
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		List<GrantedAuthority> authorities = new ArrayList<>();
		if (rol != null) {
			authorities.add(new SimpleGrantedAuthority("ROLE_" + rol));
		}
		return authorities;
	}

	/**
	 * Retorna la contrasena con hash.
	 *
	 * @return Contrasena con hash.
	 */
	@Override
	public String getPassword() {
		return password;
	}

	/**
	 * Retorna el nombre de usuario usado para el login.
	 *
	 * @return Nombre de usuario.
	 */
	@Override
	public String getUsername() {
		return username;
	}

	/**
	 * Representacion en texto. Nunca incluye la contrasena.
	 */
	@Override
	public String toString() {
		return "Usuario [id=" + id + ", nombre=" + nombre + ", correo=" + correo + ", username=" + username
				+ ", password=" + password + ", rol=" + rol + "]";
	}
	

	@Override
	public int hashCode() {
		return Objects.hash(correo, id, nombre, password, rol, username);
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
		return Objects.equals(correo, other.correo) && Objects.equals(id, other.id)
				&& Objects.equals(nombre, other.nombre) && Objects.equals(password, other.password)
				&& Objects.equals(rol, other.rol) && Objects.equals(username, other.username);
	}

}