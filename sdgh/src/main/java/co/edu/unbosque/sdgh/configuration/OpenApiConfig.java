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
						"  <li><b>ADMINISTRADOR</b>: acceso total</li>" +
						"  <li><b>USUARIO</b>: puede analizar código y ver sus propios análisis</li>" +
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
