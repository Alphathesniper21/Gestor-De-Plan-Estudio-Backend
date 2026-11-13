package co.edu.unbosque.sdgh.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import co.edu.unbosque.sdgh.entity.RolUsuario;
import co.edu.unbosque.sdgh.entity.Usuario;
import co.edu.unbosque.sdgh.repository.UsuarioRepository;

/**
 * Pruebas unitarias para la clase {@link UserDetailsServiceImpl}.
 * <p>
 * Verifica la carga de detalles de usuario para la autenticacion en Spring
 * Security y el lanzamiento de {@link UsernameNotFoundException} cuando el
 * usuario no existe en la base de datos.
 * </p>
 *
 * @author Martin Peña
 * @version 1.0
 * @since 1.0
 * @see UserDetailsServiceImpl
 */
class UserDetailsServiceImplTest {

	/** Mock del repositorio de usuarios. */
	@Mock
	private UsuarioRepository usuarioRepo;

	/** Instancia del servicio bajo prueba. */
	private UserDetailsServiceImpl service;

	/**
	 * Prepara los mocks e instancia el servicio antes de cada prueba.
	 */
	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		service = new UserDetailsServiceImpl(usuarioRepo);
	}

	// ─── LOAD USER BY USERNAME ────────────────────────────────────────────

	/**
	 * Verifica que {@code loadUserByUsername} retorna los detalles del usuario
	 * cuando este existe en la base de datos.
	 */
	@Test
	void testLoadUserByUsername_Exitoso() {
		Usuario usuario = new Usuario();
		usuario.setUsername("gestor");
		usuario.setPassword("passwordCodificado");
		usuario.setRol(RolUsuario.GESTOR);

		when(usuarioRepo.findByUsername("gestor")).thenReturn(Optional.of(usuario));

		UserDetails resultado = service.loadUserByUsername("gestor");

		assertNotNull(resultado);
		assertEquals("gestor", resultado.getUsername());
		assertEquals("passwordCodificado", resultado.getPassword());
		verify(usuarioRepo, times(1)).findByUsername("gestor");
	}

	/**
	 * Verifica que {@code loadUserByUsername} lanza
	 * {@link UsernameNotFoundException} con el mensaje exacto cuando el usuario
	 * solicitado no existe.
	 */
	@Test
	void testLoadUserByUsername_LanzaExcepcionUsuarioNoEncontrado() {
		when(usuarioRepo.findByUsername("desconocido")).thenReturn(Optional.empty());

		UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
				() -> service.loadUserByUsername("desconocido"));

		assertEquals("Usuario no encontrado: desconocido", ex.getMessage());
	}
}
