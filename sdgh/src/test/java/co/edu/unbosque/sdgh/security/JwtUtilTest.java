package co.edu.unbosque.sdgh.security;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import co.edu.unbosque.sdgh.entity.RolUsuario;
import co.edu.unbosque.sdgh.entity.Usuario;
import io.jsonwebtoken.JwtException;

/**
 * Pruebas unitarias para la clase utilitaria de seguridad {@link JwtUtil}.
 * <p>
 * Verifica la generación de tokens JWT firmados con HMAC-SHA256, la extracción
 * de claims (nombre de usuario, rol, fecha de expiración), el cumplimiento de
 * la vigencia de sesión de 60 minutos (RNF-4) y la validación del token frente
 * a distintos usuarios o cadenas malformadas.
 * </p>
 *
 * @author Martin Peña
 * @version 1.0
 * @since 1.0
 * @see JwtUtil
 */
class JwtUtilTest {

	/**
	 * Instancia del componente {@link JwtUtil} bajo prueba.
	 */
	private JwtUtil jwtUtil;

	/**
	 * Usuario de referencia utilizado para generar y validar tokens.
	 */
	private Usuario usuario;

	/**
	 * Inicializa {@link JwtUtil} e inyecta la clave secreta y el tiempo de
	 * expiración de 60 minutos (3.600.000 ms) antes de cada caso.
	 */
	@BeforeEach
	void setUp() {
		jwtUtil = new JwtUtil();
		ReflectionTestUtils.setField(jwtUtil, "secret",
				"404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
		ReflectionTestUtils.setField(jwtUtil, "jwtTokenValidity", 3600000L); // 60 minutos (RNF-4)

		usuario = new Usuario();
		usuario.setId(1L);
		usuario.setNombre("Gestor Academico");
		usuario.setCorreo("gestor@unbosque.edu.co");
		usuario.setUsername("gestor_academico");
		usuario.setPassword("passwordCodificado");
		usuario.setRol(RolUsuario.GESTOR);
	}

	// ─── GENERATE TOKEN & CLAIMS ──────────────────────────────────────────────

	/**
	 * Verifica que {@code generateToken} genera un token JWT no vacío, extrae el
	 * subject correctamente y lo valida de forma exitosa para el mismo usuario.
	 */
	@Test
	void testGenerateToken_Exitoso() {
		String token = jwtUtil.generateToken(usuario);

		assertNotNull(token);
		assertFalse(token.isBlank());
		assertEquals("gestor_academico", jwtUtil.extractUsername(token));
		assertTrue(jwtUtil.validateToken(token, usuario));
	}

	/**
	 * Verifica que el token generado para una instancia de {@link Usuario} incluye
	 * el claim {@code role} con el rol correspondiente.
	 */
	@Test
	void testExtractRole_IncluyeRolDelUsuario() {
		String token = jwtUtil.generateToken(usuario);

		assertEquals("GESTOR", jwtUtil.extractRole(token));
	}

	/**
	 * Verifica que {@code extractExpiration} retorna una fecha de vencimiento
	 * posterior a la fecha actual y acorde a los 60 minutos configurados (RNF-4).
	 */
	@Test
	void testExtractExpiration_VigenciaSesentaMinutos() {
		long antes = System.currentTimeMillis();
		String token = jwtUtil.generateToken(usuario);
		Date expiracion = jwtUtil.extractExpiration(token);

		assertNotNull(expiracion);
		assertTrue(expiracion.after(new Date()));
		long duracionMs = expiracion.getTime() - antes;
		assertTrue(duracionMs >= 3590000L && duracionMs <= 3610000L);
	}

	// ─── VALIDATE TOKEN ───────────────────────────────────────────────────────

	/**
	 * Verifica que {@code validateToken} retorna {@code false} cuando el nombre de
	 * usuario del token no coincide con el del {@link UserDetails} evaluado.
	 */
	@Test
	void testValidateToken_RechazaUsuarioDistinto() {
		String token = jwtUtil.generateToken(usuario);

		UserDetails otroUsuario = new User("otro_usuario", "password",
				List.of(new SimpleGrantedAuthority("ROLE_CONSULTANTE")));

		assertFalse(jwtUtil.validateToken(token, otroUsuario));
	}

	/**
	 * Verifica que al analizar un token malformado o con firma inválida se lanza
	 * una excepción {@link JwtException}.
	 */
	@Test
	void testExtractUsername_LanzaExcepcionTokenMalFormado() {
		assertThrows(JwtException.class, () -> jwtUtil.extractUsername("token.falso.invalido"));
	}
}
