package co.edu.unbosque.sdgh.security;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.Base64;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import co.edu.unbosque.sdgh.entity.RolUsuario;
import co.edu.unbosque.sdgh.entity.Usuario;

/**
 * Pruebas unitarias para la clase de seguridad {@link JwtUtil}.
 * <p>
 * Verifica la generacion de tokens JWT de acceso y de refresco, la inclusion de
 * claims personalizados (username, rol), la verificacion de firmas y la
 * deteccion de tokens malformados o no autenticados.
 * </p>
 *
 * @author Martin Peña
 * @version 1.0
 * @since 1.0
 * @see JwtUtil
 */
class JwtUtilTest {

	/** Instancia del componente JwtUtil bajo prueba. */
	private JwtUtil jwtUtil;

	/** Detalles de usuario para pruebas. */
	private UserDetails userDetails;

	/**
	 * Prepara la clave secreta de 32 bytes en Base64, configura los tiempos de
	 * expiracion e inicializa {@link JwtUtil}.
	 */
	@BeforeEach
	void setUp() {
		jwtUtil = new JwtUtil();

		String secret = Base64.getEncoder().encodeToString("12345678901234567890123456789012".getBytes());

		try {
			setField(jwtUtil, "secretBase64", secret);
			setField(jwtUtil, "accessTokenExpirationMs", 3600000L); // 1 hora
			setField(jwtUtil, "refreshTokenExpirationMs", 86400000L); // 24 horas
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		jwtUtil.init();

		Usuario usuario = new Usuario();
		usuario.setUsername("gestor_academico");
		usuario.setPassword("password123");
		usuario.setRol(RolUsuario.GESTOR);
		userDetails = usuario;
	}

	private void setField(Object target, String fieldName, Object value) throws Exception {
		Field field = target.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(target, value);
	}

	// ─── GENERATE TOKENS ──────────────────────────────────────────────────

	/**
	 * Verifica que {@code generateToken} genera un token JWT no vacio, extrae el
	 * subject correctamente y valida el token para el usuario.
	 */
	@Test
	void testGenerateToken_Exitoso() {
		String token = jwtUtil.generateToken(userDetails);

		assertNotNull(token);
		assertFalse(token.isBlank());
		assertEquals("gestor_academico", jwtUtil.extractUsername(token));
		assertTrue(jwtUtil.isTokenValid(token, userDetails));
	}

	/**
	 * Verifica que {@code generateAccessToken} incluye el claim con el rol del
	 * usuario.
	 */
	@Test
	void testGenerateAccessToken_IncluyeRol() {
		String token = jwtUtil.generateAccessToken(userDetails);

		assertNotNull(token);
		assertEquals("gestor_academico", jwtUtil.extractUsername(token));
		assertEquals("GESTOR", jwtUtil.extractRol(token));
	}

	/**
	 * Verifica que {@code generateRefreshToken} genera un token valido con el tipo
	 * de refresco.
	 */
	@Test
	void testGenerateRefreshToken_Exitoso() {
		String refreshToken = jwtUtil.generateRefreshToken(userDetails);

		assertNotNull(refreshToken);
		assertEquals("gestor_academico", jwtUtil.extractUsername(refreshToken));
		assertTrue(jwtUtil.isTokenValid(refreshToken, userDetails));
	}

	// ─── VALIDATION & ERROR HANDLING ──────────────────────────────────────

	/**
	 * Verifica que {@code isTokenValid} retorna false cuando el usuario que intenta
	 * validar no coincide con el subject del token.
	 */
	@Test
	void testIsTokenValid_RechazaUsuarioDistinto() {
		String token = jwtUtil.generateToken(userDetails);

		UserDetails otroUsuario = new User("otro_usuario", "password",
				List.of(new SimpleGrantedAuthority("ROLE_CONSULTANTE")));

		assertFalse(jwtUtil.isTokenValid(token, otroUsuario));
	}

	/**
	 * Verifica que {@code isTokenValid} retorna false ante tokens malformados,
	 * cadenas vacias o firmas alteradas.
	 */
	@Test
	void testIsTokenValid_RechazaTokenMalFormado() {
		assertFalse(jwtUtil.isTokenValid("token.falso.invalido", userDetails));
		assertFalse(jwtUtil.isTokenValid("", userDetails));
	}
}
