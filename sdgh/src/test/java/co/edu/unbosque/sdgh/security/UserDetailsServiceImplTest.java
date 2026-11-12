package co.edu.unbosque.sdgh.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import co.edu.unbosque.sdgh.entity.RolUsuario;
import co.edu.unbosque.sdgh.entity.Usuario;
import co.edu.unbosque.sdgh.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

	@Mock
	private UsuarioRepository usuarioRepo;

	@InjectMocks
	private UserDetailsServiceImpl userDetailsService;

	@Test
	@DisplayName("loadUserByUsername: Debe cargar usuario exitosamente cuando existe")
	void debeCargarUsuarioExistente() {
		Usuario usuario = new Usuario();
		usuario.setUsername("admin");
		usuario.setPassword("encodedPass");
		usuario.setRol(RolUsuario.GESTOR);

		when(usuarioRepo.findByUsername("admin")).thenReturn(Optional.of(usuario));

		UserDetails resultado = userDetailsService.loadUserByUsername("admin");

		assertNotNull(resultado);
		assertEquals("admin", resultado.getUsername());
		assertEquals("encodedPass", resultado.getPassword());
	}

	@Test
	@DisplayName("loadUserByUsername: Debe lanzar UsernameNotFoundException cuando no existe")
	void debeLanzarExcepcionCuandoUsuarioNoExiste() {
		when(usuarioRepo.findByUsername("desconocido")).thenReturn(Optional.empty());

		assertThrows(UsernameNotFoundException.class,
				() -> userDetailsService.loadUserByUsername("desconocido"));
	}
}
