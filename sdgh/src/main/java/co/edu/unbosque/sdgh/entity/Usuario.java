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
 * Entidad que representa a un usuario registrado en el sistema.
 *
 * Almacena la información personal y las credenciales necesarias para
 * la autenticación. Implementa la interfaz UserDetails de Spring Security
 * para permitir su integración con el mecanismo de autenticación basado
 * en JWT.
 *
 * Cada usuario tiene un rol que determina los permisos disponibles
 * dentro del sistema. Los roles definidos son GESTOR y CONSULTANTE.
 *
 * @author Laura Gomez
 * @version 1.0
 * @since 1.0
 * @see UserDetails
 * @see RolUsuario
 */
@Entity
@Table(name = "usuario")
public class Usuario implements UserDetails {

    /**
     * Identificador único del usuario. Se genera automáticamente
     * al registrar un nuevo usuario en la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    /**
     * Nombre completo del usuario registrado en el sistema.
     */
    private String nombre;

    /**
     * Correo electrónico asociado a la cuenta del usuario.
     */
    private String correo;

    /**
     * Nombre de usuario utilizado como credencial para iniciar sesión.
     */
    private String username;

    /**
     * Contraseña del usuario. Debe almacenarse utilizando un mecanismo
     * de hash para proteger las credenciales.
     */
    private String password;

    /**
     * Rol asignado al usuario, que determina los permisos disponibles
     * dentro del sistema.
     *
     * Se almacena en la base de datos como una cadena de texto.
     */
    @Enumerated(EnumType.STRING)
    private RolUsuario rol;

    /**
     * Constructor vacío de la entidad Usuario.
     * Se utiliza para permitir la creación de instancias por parte de JPA.
     */
    public Usuario() {
        super();
    }

    /**
     * Construye un usuario con sus datos personales, credenciales y rol.
     *
     * @param nombre Nombre completo del usuario.
     * @param correo Correo electrónico del usuario.
     * @param username Nombre de usuario utilizado para iniciar sesión.
     * @param password Contraseña del usuario.
     * @param rol Rol que tendrá el usuario dentro del sistema.
     */
    public Usuario(String nombre, String correo, String username, String password, RolUsuario rol) {
        super();
        this.nombre = nombre;
        this.correo = correo;
        this.username = username;
        this.password = password;
        this.rol = rol;
    }

    /**
     * Obtiene el identificador único del usuario.
     *
     * @return Identificador del usuario.
     */
    public long getId() {
        return id;
    }

    /**
     * Establece el identificador del usuario.
     *
     * @param id Identificador que se asignará al usuario.
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Obtiene el nombre completo del usuario.
     *
     * @return Nombre del usuario.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Establece el nombre completo del usuario.
     *
     * @param nombre Nombre que se asignará al usuario.
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Obtiene el correo electrónico del usuario.
     *
     * @return Correo electrónico registrado.
     */
    public String getCorreo() {
        return correo;
    }

    /**
     * Establece el correo electrónico del usuario.
     *
     * @param correo Correo electrónico que se asignará al usuario.
     */
    public void setCorreo(String correo) {
        this.correo = correo;
    }

    /**
     * Obtiene el rol asignado al usuario.
     *
     * @return Rol del usuario.
     */
    public RolUsuario getRol() {
        return rol;
    }

    /**
     * Establece el rol del usuario.
     *
     * @param rol Rol que se asignará al usuario.
     */
    public void setRol(RolUsuario rol) {
        this.rol = rol;
    }

    /**
     * Establece el nombre de usuario utilizado para la autenticación.
     *
     * @param username Nombre de usuario que se asignará.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Establece la contraseña del usuario.
     *
     * @param password Contraseña que se asignará al usuario.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Obtiene los permisos asociados al usuario a partir de su rol.
     *
     * Spring Security requiere que las autoridades tengan el prefijo
     * ROLE_ para identificar los permisos definidos en el sistema.
     *
     * @return Colección de autoridades del usuario. Contiene el permiso
     *         correspondiente a su rol o una colección vacía si el rol es nulo.
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
     * Obtiene la contraseña almacenada para el proceso de autenticación.
     *
     * @return Contraseña del usuario.
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Obtiene el nombre de usuario utilizado para iniciar sesión.
     *
     * @return Nombre de usuario.
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * Genera una representación en texto del usuario con sus atributos.
     *
     * @return Cadena de texto con la información del usuario.
     */
    @Override
    public String toString() {
        return "Usuario [id=" + id + ", nombre=" + nombre + ", correo=" + correo + ", username=" + username
                + ", password=" + password + ", rol=" + rol + "]";
    }

    /**
     * Calcula el código hash del usuario utilizando sus atributos.
     *
     * @return Código hash generado a partir de los datos del usuario.
     */
    @Override
    public int hashCode() {
        return Objects.hash(correo, id, nombre, password, rol, username);
    }

    /**
     * Compara este usuario con otro objeto para determinar si son iguales.
     *
     * La comparación verifica que ambos objetos pertenezcan a la misma clase
     * y que sus atributos tengan los mismos valores.
     *
     * @param obj Objeto con el que se desea comparar el usuario.
     * @return TRUE si ambos objetos son iguales; FALSE en caso contrario.
     */
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