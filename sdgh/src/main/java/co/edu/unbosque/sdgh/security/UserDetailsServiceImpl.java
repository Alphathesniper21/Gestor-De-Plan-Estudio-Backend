package co.edu.unbosque.sdgh.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class UserDetailsServiceImpl {
	/**
	 * Repositorio de usuarios para la consulta en base de datos.
	 */
    private final UsuarioRepository usuarioRepo;

    /**
     * Constructor de la implementacion del servicio de detalles de usuario.
     * @param usuarioRepo
     */
    public UserDetailsServiceImpl(UsuarioRepository usuarioRepo) {
        this.usuarioRepo = usuarioRepo;
    }

    /**
     * Carga un usuario por su nombre de usuario para el proceso de autenticacion.
     *
     * @param username Nombre de usuario a buscar.
     * @return Detalles del usuario encontrado.
     * @throws UsernameNotFoundException si el usuario no existe en la base de datos.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return usuarioRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                    "Usuario no encontrado: " + username));
    }
}
