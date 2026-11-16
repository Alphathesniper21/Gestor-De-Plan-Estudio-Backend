package co.edu.unbosque.sdgh.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import co.edu.unbosque.sdgh.repository.UsuarioRepository;

/**
 * Implementación del servicio UserDetailsService de Spring Security.
 *
 * Se encarga de consultar los usuarios registrados en el sistema
 * mediante el repositorio de usuarios y proporcionar sus datos
 * para los procesos de autenticación.
 *
 * @author David Velasquez
 * @version 1.0
 * @since 1.0
 * @see UserDetailsService
 * @see UsuarioRepository
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    /**
     * Repositorio encargado de consultar los usuarios
     * almacenados en la base de datos.
     */
    private final UsuarioRepository usuarioRepo;

    /**
     * Construye el servicio de detalles de usuario e inicializa
     * el repositorio necesario para realizar las consultas.
     *
     * @param usuarioRepo repositorio utilizado para consultar
     *                    los usuarios registrados.
     */
    public UserDetailsServiceImpl(UsuarioRepository usuarioRepo) {
        this.usuarioRepo = usuarioRepo;
    }

    /**
     * Busca un usuario registrado mediante su nombre de usuario
     * para proporcionar sus datos durante el proceso de autenticación.
     *
     * Si no se encuentra un registro que coincida con el nombre
     * proporcionado, se genera una excepción indicando que el usuario
     * no existe.
     *
     * @param username nombre de usuario que se desea buscar.
     * @return detalles del usuario encontrado, utilizados por
     *         Spring Security durante la autenticación.
     * @throws UsernameNotFoundException si no existe un usuario
     *                                   con el nombre proporcionado.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return usuarioRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                    "Usuario no encontrado: " + username));
    }
}