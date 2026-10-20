package co.edu.unbosque.sdgh.security;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;

import co.edu.unbosque.skillissueanalyzer.security.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

public class JwtUtil {
	private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

	@Value("${jwt.secret}")
	private String secretBase64;

	@Value("${jwt.access-token-expiration-ms}")
	private long accessTokenExpirationMs;

	@Value("${jwt.refresh-token-expiration-ms}")
	private long refreshTokenExpirationMs;

	private SecretKey signingKey;

	/**
	 * Inicializa la clave de firma al arrancar el contexto de Spring.
	 * Lanza IllegalStateException si el secreto es menor a 32 bytes.
	 */
	@PostConstruct
	public void init() {
		byte[] keyBytes = Decoders.BASE64.decode(secretBase64);
		if (keyBytes.length < 32) {
			throw new IllegalStateException(
					"JWT_SECRET debe tener al menos 32 bytes. Genera uno con: openssl rand -base64 32");
		}
		signingKey = Keys.hmacShaKeyFor(keyBytes);
		log.info("JwtUtil inicializado — accessMs={} refreshMs={}",
				accessTokenExpirationMs, refreshTokenExpirationMs);
	}


	/**
	 * Genera un access token con el username y el rol del usuario como claims.
	 *
	 * @param userDetails El usuario autenticado.
	 * @return Token JWT firmado.
	 */
	public String generateAccessToken(UserDetails userDetails) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("type", "access");

		String rol = userDetails.getAuthorities().stream()
				.findFirst()
				.map(a -> a.getAuthority().replace("ROLE_", ""))
				.orElse("USUARIO");
		claims.put("rol", rol);

		return buildToken(claims, userDetails.getUsername(), accessTokenExpirationMs);
	}

	/**
	 * Genera un refresh token para renovar el access token.
	 *
	 * @param userDetails El usuario autenticado.
	 * @return Refresh token JWT firmado.
	 */
	public String generateRefreshToken(UserDetails userDetails) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("type", "refresh");
		return buildToken(claims, userDetails.getUsername(), refreshTokenExpirationMs);
	}

	private String buildToken(Map<String, Object> extraClaims,
			String subject, long expirationMs) {
		long now = System.currentTimeMillis();
		return Jwts.builder()
				.setClaims(extraClaims)
				.setSubject(subject)
				.setIssuedAt(new Date(now))
				.setExpiration(new Date(now + expirationMs))
				.signWith(signingKey)
				.compact();
	}


	/**
	 * Verifica que el token sea valido para el usuario dado.
	 *
	 * @param token       Token JWT a validar.
	 * @param userDetails Usuario contra el que se valida.
	 * @return true si el token es valido y no ha expirado.
	 */
	public boolean isTokenValid(String token, UserDetails userDetails) {
		try {
			final String username = extractUsername(token);
			return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
		} catch (JwtException | IllegalArgumentException e) {
			log.warn("Token JWT invalido: {}", e.getMessage());
			return false;
		}
	}

	/**
	 * Extrae el username (subject) del token.
	 *
	 * @param token Token JWT.
	 * @return El username del usuario.
	 */
	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	/**
	 * Extrae el rol del claim personalizado del token.
	 *
	 * @param token Token JWT.
	 * @return El rol del usuario (sin prefijo ROLE_).
	 */
	public String extractRol(String token) {
		return extractClaim(token, c -> c.get("rol", String.class));
	}

	/**
	 * Extrae un claim especifico del token.
	 *
	 * @param <T>            Tipo del claim.
	 * @param token          Token JWT.
	 * @param claimsResolver Funcion que extrae el claim deseado.
	 * @return El valor del claim.
	 */
	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		return claimsResolver.apply(getClaims(token));
	}

	private Claims getClaims(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(signingKey)
				.build()
				.parseClaimsJws(token)
				.getBody();
	}

	private boolean isTokenExpired(String token) {
		return extractClaim(token, Claims::getExpiration).before(new Date());
	}
}
