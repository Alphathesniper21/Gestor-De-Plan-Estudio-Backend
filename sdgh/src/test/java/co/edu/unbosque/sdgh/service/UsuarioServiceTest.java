package co.edu.unbosque.sdgh.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import co.edu.unbosque.sdgh.dto.UsuarioDTO;
import co.edu.unbosque.sdgh.entity.RolUsuario;
import co.edu.unbosque.sdgh.entity.Usuario;
import co.edu.unbosque.sdgh.exception.DatosInvalidosException;
import co.edu.unbosque.sdgh.exception.RecursoDuplicadoException;
import co.edu.unbosque.sdgh.exception.RecursoNoEncontradoException;
import co.edu.unbosque.sdgh.repository.UsuarioRepository;

/**
 * Pruebas unitarias para la clase {@link UsuarioService}.
 * <p>
 * Verifica la logica de negocio de los usuarios, incluyendo el registro con
 * cifrado/hash de contrasenas, validaciones de formato de nombre, correo
 * electronico y contrasena segura, politicas de privacidad para no exponer
 * credenciales en consultas (RNF-4 y RNF-7), y operaciones CRUD.
 * </p>
 *
 * @author Martin Peña
 * @version 1.0
 * @since 1.0
 * @see UsuarioService
 * @see CRUDOperation
 */
class UsuarioServiceTest {

	/**
	 * Mock del repositorio de usuarios.
	 */
	@Mock
	private UsuarioRepository usuarioRepo;

	/**
	 * Mock del codificador de contrasenas.
	 */
	@Mock
	private PasswordEncoder passwordEncoder;

	/**
	 * Mapper para la conversion entre entidades y DTOs.
	 */
	private ModelMapper mapper;

	/**
	 * Instancia del servicio bajo prueba.
	 */
	private UsuarioService service;

	/**
	 * Entidad de usuario de referencia.
	 */
	private Usuario usuario;

	/**
	 * DTO de usuario de referencia.
	 */
	private UsuarioDTO dto;

	/**
	 * Inicializa los mocks, inyecta las dependencias en el servicio y prepara los
	 * objetos de prueba antes de cada caso.
	 */
	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		mapper = new ModelMapper();
		service = new UsuarioService();

		try {
			var f1 = UsuarioService.class.getDeclaredField("usuarioRepo");
			f1.setAccessible(true);
			f1.set(service, usuarioRepo);

			var f2 = UsuarioService.class.getDeclaredField("passwordEncoder");
			f2.setAccessible(true);
			f2.set(service, passwordEncoder);

			var f3 = UsuarioService.class.getDeclaredField("mapper");
			f3.setAccessible(true);
			f3.set(service, mapper);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		usuario = new Usuario();
		usuario.setId(1L);
		usuario.setNombre("Martin Garcia");
		usuario.setCorreo("martin@unbosque.edu.co");
		usuario.setUsername("marting");
		usuario.setPassword("hashedPassword123");
		usuario.setRol(RolUsuario.GESTOR);

		dto = new UsuarioDTO();
		dto.setId(1L);
		dto.setNombre("Martin Garcia");
		dto.setCorreo("martin@unbosque.edu.co");
		dto.setUsername("marting");
		dto.setPassword("ClaveSegura123");
		dto.setRol(RolUsuario.GESTOR);
	}

	// ─── CREATE ──────────────────────────────────────────────────────────

	/**
	 * Verifica que la creacion de un usuario es exitosa y almacena la contrasena
	 * codificada con hash en la base de datos.
	 */
	@Test
	void testCreate_Exitoso() {
		when(usuarioRepo.existsByCorreo("martin@unbosque.edu.co")).thenReturn(false);
		when(usuarioRepo.existsByUsername("marting")).thenReturn(false);
		when(passwordEncoder.encode("ClaveSegura123")).thenReturn("hashedPassword123");
		when(usuarioRepo.save(any(Usuario.class))).thenReturn(usuario);

		int resultado = service.create(dto);

		assertEquals(0, resultado);
		verify(usuarioRepo, times(1)).save(any(Usuario.class));
		verify(passwordEncoder, times(1)).encode("ClaveSegura123");
	}

	/**
	 * Verifica que se lanza {@link DatosInvalidosException} cuando los datos del
	 * usuario o el nombre son nulos o contienen caracteres no permitidos.
	 */
	@Test
	void testCreate_LanzaExcepcionNombreInvalido() {
		assertThrows(DatosInvalidosException.class, () -> service.create(null));

		dto.setNombre("   ");
		assertThrows(DatosInvalidosException.class, () -> service.create(dto));

		dto.setNombre("Martin123"); // Contiene numeros en el nombre
		assertThrows(DatosInvalidosException.class, () -> service.create(dto));

		verify(usuarioRepo, never()).save(any());
	}

	/**
	 * Verifica que se lanza {@link DatosInvalidosException} cuando el correo no
	 * cumple con el formato regular esperado.
	 */
	@Test
	void testCreate_LanzaExcepcionCorreoInvalido() {
		dto.setCorreo("correo-sin-formato");

		DatosInvalidosException ex = assertThrows(DatosInvalidosException.class, () -> service.create(dto));

		assertTrue(ex.getMessage().contains("El correo no tiene un formato valido"));
		verify(usuarioRepo, never()).save(any());
	}

	/**
	 * Verifica que se lanza {@link DatosInvalidosException} cuando la contrasena es
	 * menor a 8 caracteres o no cumple con la combinacion de mayusculas, minusculas
	 * y numeros.
	 */
	@Test
	void testCreate_LanzaExcepcionContrasenaDebil() {
		dto.setPassword("Ab1"); // Corta (< 8)
		assertThrows(DatosInvalidosException.class, () -> service.create(dto));

		dto.setPassword("solominuscula123"); // Sin mayuscula
		assertThrows(DatosInvalidosException.class, () -> service.create(dto));

		dto.setPassword("SOLOMAYUSCULA123"); // Sin minuscula
		assertThrows(DatosInvalidosException.class, () -> service.create(dto));

		dto.setPassword("SinNumeroValido"); // Sin numero
		assertThrows(DatosInvalidosException.class, () -> service.create(dto));
	}

	/**
	 * Verifica que se lanza {@link RecursoDuplicadoException} cuando el correo o el
	 * nombre de usuario ya existen en el repositorio.
	 */
	@Test
	void testCreate_LanzaExcepcionUsuarioDuplicado() {
		when(usuarioRepo.existsByCorreo("martin@unbosque.edu.co")).thenReturn(true);
		assertThrows(RecursoDuplicadoException.class, () -> service.create(dto));

		when(usuarioRepo.existsByCorreo("martin@unbosque.edu.co")).thenReturn(false);
		when(usuarioRepo.existsByUsername("marting")).thenReturn(true);
		assertThrows(RecursoDuplicadoException.class, () -> service.create(dto));

		verify(usuarioRepo, never()).save(any());
	}

	// ─── GET ALL ──────────────────────────────────────────────────────────

	/**
	 * Verifica que {@code getAll} retorna la lista de usuarios y garantiza que
	 * ningun DTO exponga la contrasena del usuario.
	 */
	@Test
	void testGetAll_ExitosoSinContrasena() {
		when(usuarioRepo.findAll()).thenReturn(List.of(usuario));

		List<UsuarioDTO> lista = service.getAll();

		assertNotNull(lista);
		assertEquals(1, lista.size());
		assertNull(lista.get(0).getPassword(), "La contrasena nunca debe exponerse en los DTOs");
	}

	// ─── UPDATE BY ID ─────────────────────────────────────────────────────

	/**
	 * Verifica que {@code updateById} actualiza correctamente el nombre y correo
	 * del usuario existente.
	 */
	@Test
	void testUpdateById_Exitoso() {
		when(usuarioRepo.findById(1L)).thenReturn(Optional.of(usuario));
		when(usuarioRepo.findByCorreo("martin@unbosque.edu.co")).thenReturn(Optional.of(usuario));
		when(usuarioRepo.save(any(Usuario.class))).thenReturn(usuario);

		UsuarioDTO actualizacion = new UsuarioDTO();
		actualizacion.setNombre("Martin Actualizado");
		actualizacion.setCorreo("martin@unbosque.edu.co");

		int resultado = service.updateById(1L, actualizacion);

		assertEquals(0, resultado);
		verify(usuarioRepo, times(1)).save(usuario);
	}

	// ─── DELETE BY ID ─────────────────────────────────────────────────────

	/**
	 * Verifica que {@code deleteById} elimina correctamente al usuario de la base
	 * de datos.
	 */
	@Test
	void testDeleteById_Exitoso() {
		when(usuarioRepo.findById(1L)).thenReturn(Optional.of(usuario));

		int resultado = service.deleteById(1L);

		assertEquals(0, resultado);
		verify(usuarioRepo, times(1)).delete(usuario);
	}

	/**
	 * Verifica que {@code deleteById} lanza {@link RecursoNoEncontradoException}
	 * cuando el usuario no existe.
	 */
	@Test
	void testDeleteById_LanzaExcepcionNoEncontrado() {
		when(usuarioRepo.findById(999L)).thenReturn(Optional.empty());

		assertThrows(RecursoNoEncontradoException.class, () -> service.deleteById(999L));
	}

	// ─── OPERACIONES DE CONTEO Y EXISTENCIA ────────────────────────────────

	/**
	 * Verifica que {@code count} y {@code existById} devuelven los resultados
	 * esperados.
	 */
	@Test
	void testCountYExistById_Exitoso() {
		when(usuarioRepo.count()).thenReturn(3L);
		assertEquals(3L, service.count());

		when(usuarioRepo.existsById(1L)).thenReturn(true);
		assertTrue(service.existById(1L));
		assertFalse(service.existById(null));
	}
}
