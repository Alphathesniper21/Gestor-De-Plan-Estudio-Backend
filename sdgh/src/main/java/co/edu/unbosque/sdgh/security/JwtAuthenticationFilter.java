package co.edu.unbosque.sdgh.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filtro de autenticación que intercepta las solicitudes HTTP para
 * verificar los tokens JWT enviados en el encabezado Authorization.
 *
 * Cuando el token es válido, obtiene los datos del usuario y establece
 * su autenticación en el contexto de seguridad de Spring Security.
 *
 * @author David Velasquez
 * @version 1.0
 * @since 1.0
 * @see JwtUtil
 * @see UserDetailsService
 * @see OncePerRequestFilter
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * Utilidad encargada de extraer información y validar los tokens JWT.
     */
    private final JwtUtil jwtUtil;

    /**
     * Servicio encargado de cargar los datos del usuario a partir
     * de su nombre de usuario.
     */
    private final UserDetailsService userDetailsService;

    /**
     * Construye el filtro de autenticación JWT e inicializa
     * las dependencias necesarias para validar los tokens.
     *
     * @param jwtUtil utilidad para gestionar y validar los tokens JWT.
     * @param userDetailsService servicio para consultar los detalles del usuario.
     */
    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Intercepta cada solicitud HTTP para comprobar si contiene un token JWT
     * en el encabezado Authorization.
     *
     * Si encuentra un token con el formato Bearer, extrae el nombre
     * de usuario, consulta sus datos y valida el token. Cuando la validación
     * es exitosa, establece la autenticación correspondiente en el contexto
     * de seguridad de Spring.
     *
     * Finalmente, continúa con la ejecución de la cadena de filtros.
     *
     * @param request solicitud HTTP recibida.
     * @param response respuesta HTTP asociada a la solicitud.
     * @param filterChain cadena de filtros que debe continuar ejecutándose.
     * @throws ServletException si ocurre un error durante el procesamiento
     *         de la solicitud.
     * @throws IOException si ocurre un error de entrada o salida.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                logger.error("Error extracting username from token", e);
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}