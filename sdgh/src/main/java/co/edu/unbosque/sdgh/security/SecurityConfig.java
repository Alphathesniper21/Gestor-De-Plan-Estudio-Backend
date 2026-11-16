package co.edu.unbosque.sdgh.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Clase de configuración principal de seguridad del aplicativo.
 *
 * Define las reglas de autenticación y autorización, la administración
 * de sesiones, la configuración CORS y los componentes encargados
 * de gestionar la autenticación mediante Spring Security y JWT.
 *
 * @author David Velasquez
 * @version 1.0
 * @since 1.0
 * @see JwtAuthenticationFilter
 * @see UserDetailsServiceImpl
 * @see SecurityFilterChain
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Implementación del servicio encargado de cargar los detalles
     * de los usuarios registrados para el proceso de autenticación.
     */
    private final UserDetailsServiceImpl userDetailsService;

    /**
     * Filtro encargado de interceptar las solicitudes y validar
     * los tokens JWT enviados por los clientes.
     */
    private final JwtAuthenticationFilter jwtFilter;

    /**
     * Construye la configuración de seguridad e inicializa
     * los componentes necesarios para la autenticación.
     *
     * @param userDetailsService servicio encargado de consultar
     *                           los detalles de los usuarios.
     * @param jwtFilter filtro encargado de validar los tokens JWT.
     */
    public SecurityConfig(UserDetailsServiceImpl userDetailsService, JwtAuthenticationFilter jwtFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtFilter = jwtFilter;
    }

    /**
     * Configura la cadena principal de filtros de seguridad HTTP.
     *
     * Establece la desactivación de CSRF, la configuración CORS,
     * la política de sesiones sin estado, el proveedor de autenticación,
     * las reglas de autorización para las rutas y la incorporación
     * del filtro JWT antes del filtro de autenticación por usuario
     * y contraseña.
     *
     * @param http objeto utilizado para configurar la seguridad HTTP.
     * @return cadena de filtros de seguridad configurada.
     * @throws Exception si ocurre un error durante la configuración
     *                   de la seguridad HTTP.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())

            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authenticationProvider(authenticationProvider())

            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/v3/api-docs.yaml").permitAll()

                .requestMatchers("/admin/**").hasRole("GESTOR")

                .anyRequest().authenticated()
            )

            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configura las reglas de intercambio de recursos de origen cruzado
     * (CORS) para las solicitudes que recibe el aplicativo.
     *
     * Permite los orígenes, encabezados y métodos HTTP definidos
     * en la configuración, y registra estas reglas para todas las rutas.
     *
     * @return fuente de configuración CORS registrada para todas las rutas.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * Configura el proveedor de autenticación basado en la consulta
     * de usuarios mediante UserDetailsService y la validación
     * de contraseñas con BCrypt.
     *
     * @return proveedor de autenticación configurado.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Expone el administrador de autenticación de Spring Security
     * como un componente disponible para su inyección en otras clases.
     *
     * @param config configuración de autenticación proporcionada
     *               por Spring Security.
     * @return administrador de autenticación configurado.
     * @throws Exception si ocurre un error al obtener el administrador
     *                   de autenticación.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Configura el codificador de contraseñas utilizando el algoritmo BCrypt.
     *
     * @return instancia de BCryptPasswordEncoder para codificar
     *         y verificar contraseñas.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}