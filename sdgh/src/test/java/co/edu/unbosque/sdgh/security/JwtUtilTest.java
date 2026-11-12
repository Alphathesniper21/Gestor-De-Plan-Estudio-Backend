package co.edu.unbosque.sdgh.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.Base64;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import co.edu.unbosque.sdgh.entity.RolUsuario;
import co.edu.unbosque.sdgh.entity.Usuario;

class JwtUtilTest {

	private JwtUtil jwtUtil;
	private UserDetails userDetails;

	@BeforeEach
	void setUp() throws Exception {
		jwtUtil = new JwtUtil();

		// Clave segura de 32 bytes en Base64
		String secret = Base64.getEncoder().encodeToString("12345678901234567890123456789012".getBytes());

		setField(jwtUtil, "secretBase64", secret);
		setField(jwtUtil, "accessTokenExpirationMs", 3600000L); // 1 hora
		setField(jwtUtil, "refreshTokenExpirationMs", 86400000L); // 24 horas

		jwtUtil.init();

		Usuario usuario = new Usuario();
		usuario.setUsername("gestor_test");
		usuario.setPassword("password123");
		usuario.setRol(RolUsuario.GESTOR);
		userDetails = usuario;
	}

	private void setField(Object target, String fieldName, Object value) throws Exception {
		Field field = target.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(target, value);
	}

	@Test
	@DisplayName("generateToken: Debe generar un token valido con username y rol")
	void debeGenerarTokenValido() {
		String token = jwtUtil.generateToken(userDetails);

		assertNotNull(token);
		assertFalse(token.isBlank());
		assertEquals("gestor_test", jwtUtil.extractUsername(token));
		assertTrue(jwtUtil.isTokenValid(token, userDetails));
	}

	@Test
	@DisplayName("generateAccessToken: Debe incluir rol del usuario")
	void debeGenerarAccessTokenConRol() {
		String token = jwtUtil.generateAccessToken(userDetails);

		assertNotNull(token);
		assertEquals("gestor_test", jwtUtil.extractUsername(token));
		assertEquals("GESTOR", jwtUtil.extractRol(token));
	}

	@Test
	@DisplayName("generateRefreshToken: Debe generar token de refresco valido")
	void debeGenerarRefreshTokenValido() {
		String refreshToken = jwtUtil.generateRefreshToken(userDetails);

		assertNotNull(refreshToken);
		assertEquals("gestor_test", jwtUtil.extractUsername(refreshToken));
		assertTrue(jwtUtil.isTokenValid(refreshToken, userDetails));
	}

	@Test
	@DisplayName("isTokenValid: Debe retornar false si el username no coincide")
	void debeRechazarTokenConUsuarioDistinto() {
		String token = jwtUtil.generateToken(userDetails);

		UserDetails otroUsuario = new User("otro_usuario", "pass",
				List.of(new SimpleGrantedAuthority("ROLE_CONSULTA")));

		assertFalse(jwtUtil.isTokenValid(token, otroUsuario));
	}

	@Test
	@DisplayName("isTokenValid: Debe retornar false cuando el token esta corrupto o mal formado")
	void debeRechazarTokenMalFormado() {
		assertFalse(jwtUtil.isTokenValid("token.invalido.falso", userDetails));
		assertFalse(jwtUtil.isTokenValid("", userDetails));
	}
}
