package co.edu.unbosque.sdgh.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;

/**
 * Configuracion de la documentacion OpenAPI (Swagger) del aplicativo.
 * <p>
 * Define mediante anotaciones los metadatos de la API (titulo, version y guia
 * de uso con los roles {@code GESTOR} y {@code CONSULTANTE}) y el esquema de
 * seguridad {@code bearerAuth} basado en JWT. Adicionalmente registra el bean
 * {@link OpenAPI} con el esquema de seguridad y respuestas de error
 * reutilizables (401, 403 y 404). La documentacion es accesible desde la
 * interfaz de Swagger UI.
 * </p>
 *
 * @author Martín Peña
 * @version 1.0
 * @since 1.0
 * @see co.edu.unbosque.sdgh.controller.AuthController
 */
@Configuration
@OpenAPIDefinition(
		info = @Info(
				title       = "SkilIssue Analyser API",
				version     = "1.0",
				description =
				"<h3>Guía de uso</h3>" +
						"<ol>" +
						"  <li>Regístrate en <code>/auth/register</code></li>" +
						"  <li>Inicia sesión en <code>/auth/login</code> y copia el token JWT</li>" +
						"  <li>Haz clic en <b>Authorize</b> (arriba a la derecha)</li>" +
						"  <li>Escribe <code>Bearer &lt;tu_token&gt;</code> y confirma</li>" +
						"  <li>Ya puedes usar los endpoints protegidos</li>" +
						"</ol>" +
						"<h3>Roles</h3>" +
						"<ul>" +
						"  <li><b>GESTOR</b>: crea, modifica y publica el horario</li>" +
						"  <li><b>CONSULTANTE</b>: solo consulta el horario</li>" +
						"</ul>"
				)
		)
@SecurityScheme(
		name         = "bearerAuth",
		type         = SecuritySchemeType.HTTP,
		scheme       = "bearer",
		bearerFormat = "JWT",
		description  = "Pega aquí el token obtenido en /auth/login con el prefijo Bearer"
		)
public class OpenApiConfig {

	/**
	 * Construye y registra el bean {@link OpenAPI} con la configuracion de
	 * seguridad y las respuestas de error reutilizables de la documentacion.
	 * <p>
	 * Incluye el esquema de seguridad HTTP Bearer (JWT) {@code bearerAuth} y las
	 * respuestas {@code UnauthorizedError} (401), {@code ForbiddenError} (403) y
	 * {@code NotFoundError} (404), cada una con un ejemplo JSON.
	 * </p>
	 *
	 * @return instancia de {@link OpenAPI} configurada para su uso.
	 */
	@Bean
	public OpenAPI customOpenAPI() {

		io.swagger.v3.oas.models.security.SecurityScheme scheme =
				new io.swagger.v3.oas.models.security.SecurityScheme()
				.type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
				.scheme("bearer")
				.bearerFormat("JWT")
				.description("Token JWT obtenido en /auth/login");

		return new OpenAPI()
				.components(new Components()
						.addSecuritySchemes("bearerAuth", scheme)

						.addResponses("UnauthorizedError",
								new ApiResponse()
								.description("401 – Token ausente, inválido o expirado")
								.content(new Content().addMediaType("application/json",
										new MediaType().addExamples("ejemplo",
												new Example().value(
														"{\"error\":\"No autenticado\"," +
														"\"mensaje\":\"Token inválido o expirado\"}")))))

						.addResponses("ForbiddenError",
								new ApiResponse()
								.description("403 – Sin permisos para esta operación")
								.content(new Content().addMediaType("application/json",
										new MediaType().addExamples("ejemplo",
												new Example().value(
														"{\"error\":\"Acceso prohibido\"," +
														"\"mensaje\":\"No tienes permisos suficientes\"}")))))

						.addResponses("NotFoundError",
								new ApiResponse()
								.description("404 – Recurso no encontrado")
								.content(new Content().addMediaType("application/json",
										new MediaType().addExamples("ejemplo",
												new Example().value(
														"{\"error\":\"No encontrado\"," +
														"\"mensaje\":\"El recurso solicitado no existe\"}"))))));
	}
}