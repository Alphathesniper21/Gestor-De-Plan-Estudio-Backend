package co.edu.unbosque.sdgh.security;

import java.io.IOException;

import org.jspecify.annotations.NonNull;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import ch.qos.logback.classic.Logger;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@org.springframework.stereotype.Component
public class JwtFilter extends OncePerRequestFilter {
	/**
	 * Utilidad para la generacion y validacion de tokens JWT.
	 */
	private final JwtUtil jwtUtil;

	/**
	 * Servicio de carga de detalles de usuario para la autenticacion.
	 */
	private final UserDetailsService userDetailsService;

	/**
	 * Logger para el registro de eventos del filtro JWT.
	 */
	private static final Logger log = (Logger) LoggerFactory.getLogger(JwtFilter.class);

	/**
	 * Constructor del filtro JWT.
	 *
	 * @param jwtUtil            Utilidad JWT para extraccion y validacion de
	 *                           tokens.
	 * @param userDetailsService Servicio de carga de detalles de usuario.
	 */
	public JwtFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
		this.jwtUtil = jwtUtil;
		this.userDetailsService = userDetailsService;
	}

	/**
	 * Intercepta cada peticion HTTP, extrae el token JWT del header Authorization y
	 * establece la autenticacion en el SecurityContext si el token es valido.
	 *
	 * @param request     Peticion HTTP entrante.
	 * @param response    Respuesta HTTP saliente.
	 * @param filterChain Cadena de filtros a continuar.
	 * @throws ServletException si ocurre un error en el procesamiento del servlet.
	 * @throws IOException      si ocurre un error de entrada/salida.
	 */
	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain) throws ServletException, IOException {

		final String authHeader = request.getHeader("Authorization");

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		try {
			final String token = authHeader.substring(7);
			final String username = jwtUtil.extractUsername(token);

			if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

				UserDetails userDetails = userDetailsService.loadUserByUsername(username);

				if (jwtUtil.isTokenValid(token, userDetails)) {
					UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
							null, userDetails.getAuthorities());
					authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(authToken);
				}
			}
		} catch (ExpiredJwtException e) {
			log.warn("Token JWT expirado: {}", e.getMessage());
		} catch (UnsupportedJwtException e) {
			log.warn("Token JWT no soportado: {}", e.getMessage());
		} catch (MalformedJwtException e) {
			log.warn("Token JWT mal formado: {}", e.getMessage());
		} catch (SignatureException e) {
			log.warn("Firma del token JWT inválida: {}", e.getMessage());
		} catch (IllegalArgumentException e) {
			log.warn("Token JWT vacío o nulo: {}", e.getMessage());
		} catch (UsernameNotFoundException e) {
			log.warn("Usuario no encontrado al validar JWT: {}", e.getMessage());
		}

		filterChain.doFilter(request, response);
	}
}
