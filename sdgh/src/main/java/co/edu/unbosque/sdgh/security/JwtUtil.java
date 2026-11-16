package co.edu.unbosque.sdgh.security;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import co.edu.unbosque.sdgh.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

/**
 * Clase utilitaria encargada de realizar las operaciones relacionadas
 * con los JSON Web Tokens (JWT) utilizados para la autenticación.
 *
 * Permite generar tokens, extraer información de sus atributos,
 * consultar fechas de expiración y comprobar su validez.
 *
 * @author David Velasquez
 * @version 1.0
 * @since 1.0
 * @see Usuario
 * @see UserDetails
 * @see Claims
 */
@Component
public class JwtUtil {

    /**
     * Tiempo de validez de un token JWT expresado en milisegundos.
     * Corresponde a una duración de 60 minutos.
     */
	@Value("${jwt.access-token-expiration-ms:3600000}")
	private long jwtTokenValidity;

    /**
     * Clave secreta utilizada para firmar y verificar los tokens JWT.
     * Su valor se obtiene de la configuración de la aplicación.
     */
    @Value("${jwt.secret:defaultSecretKeyWhichShouldBeAtLeast32CharactersLong}")
    private String secret;

    /**
     * Obtiene la clave criptográfica utilizada para firmar y verificar
     * los tokens mediante el algoritmo HMAC.
     *
     * @return clave criptográfica generada a partir del secreto configurado.
     */
    private Key getSigningKey() {
        byte[] keyBytes = secret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extrae el nombre de usuario almacenado en el atributo subject
     * del token JWT.
     *
     * @param token token JWT del cual se desea obtener el usuario.
     * @return nombre de usuario contenido en el token.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrae la fecha de expiración de un token JWT.
     *
     * @param token token JWT del cual se desea consultar la expiración.
     * @return fecha y hora de vencimiento del token.
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrae el rol almacenado en los atributos del token JWT.
     *
     * @param token token JWT del cual se desea obtener el rol.
     * @return rol asociado al token.
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    /**
     * Obtiene un atributo específico del conjunto de declaraciones
     * almacenadas en el token JWT.
     *
     * @param token token JWT del cual se desea extraer la información.
     * @param claimsResolver función encargada de obtener el atributo
     *                       solicitado a partir de las declaraciones.
     * @param <T> tipo de dato del atributo que se desea obtener.
     * @return valor del atributo extraído del token.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extrae todas las declaraciones almacenadas en un token JWT
     * después de verificar su firma utilizando la clave configurada.
     *
     * @param token token JWT del cual se desean obtener las declaraciones.
     * @return conjunto de declaraciones contenidas en el token.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
    }

    /**
     * Comprueba si un token JWT ha superado su fecha de expiración.
     *
     * @param token token JWT que se desea verificar.
     * @return true si el token está vencido, false en caso contrario.
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Genera un token JWT para el usuario proporcionado.
     *
     * Incluye las autoridades del usuario y, cuando corresponde,
     * su rol. El nombre de usuario se utiliza como subject del token.
     *
     * @param userDetails información del usuario para el cual se genera
     *                    el token.
     * @return token JWT generado y firmado.
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("authorities", userDetails.getAuthorities());

        if (userDetails instanceof Usuario) {
            Usuario usuario = (Usuario) userDetails;
            claims.put("role", usuario.getRol());
        }

        return createToken(claims, userDetails.getUsername());
    }

    /**
     * Construye y firma un token JWT con las declaraciones y el subject
     * proporcionados.
     *
     * También establece las fechas de emisión y expiración del token,
     * utilizando el algoritmo de firma HS256.
     *
     * @param claims conjunto de atributos que se incluirán en el token.
     * @param subject identificador del usuario asociado al token.
     * @return token JWT firmado y serializado.
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder().setClaims(claims).setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtTokenValidity))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256).compact();
    }

    /**
     * Valida un token JWT comprobando que el nombre de usuario contenido
     * en él coincida con el del usuario proporcionado y que el token
     * no haya expirado.
     *
     * @param token token JWT que se desea validar.
     * @param userDetails información del usuario contra la cual se compara
     *                    el contenido del token.
     * @return true si el nombre de usuario coincide y el token no está
     *         vencido, false en caso contrario.
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

}