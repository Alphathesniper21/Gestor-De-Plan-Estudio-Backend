
package co.edu.unbosque.sdgh.controller;

import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.sdgh.dto.SesionDTO;
import co.edu.unbosque.sdgh.exception.CruceHorarioException;
import co.edu.unbosque.sdgh.exception.DatosInvalidosException;
import co.edu.unbosque.sdgh.exception.FranjaInstitucionalInvalidaException;
import co.edu.unbosque.sdgh.exception.LimiteSesionesExcedidoException;
import co.edu.unbosque.sdgh.exception.RecursoNoEncontradoException;
import co.edu.unbosque.sdgh.service.SesionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;

/**
 * Controlador REST encargado de gestionar las sesiones academicas
 * de los grupos dentro de la grilla semanal del sistema.
 * <p>
 * Permite crear, consultar, actualizar y eliminar sesiones,
 * asi como consultar las sesiones asociadas a un grupo o semestre.
 * Tambien proporciona operaciones para contar y verificar
 * la existencia de sesiones registradas.
 * </p>
 * <p>
 * Las rutas se encuentran bajo el prefijo {@code /sesion}.
 * Las consultas estan disponibles para usuarios autenticados,
 * mientras que las operaciones de creacion, actualizacion y
 * eliminacion requieren el rol {@code GESTOR}.
 * </p>
 *
 * @version 1.0
 * @since 1.0
 * @see co.edu.unbosque.sdgh.service.SesionService
 * @see co.edu.unbosque.sdgh.dto.SesionDTO
 */
@RestController
@RequestMapping("/sesion")
@CrossOrigin(origins = { "http://localhost:8080" })
@Transactional
@Tag(name = "Sesiones", description = "Endpoints para la gestion de sesiones en la grilla semanal")
@SecurityRequirement(name = "bearerAuth")
public class SesionController {

    /**
     * Servicio encargado de ejecutar la logica de negocio
     * y las operaciones relacionadas con la gestion de sesiones.
     */
    @Autowired
    private SesionService sesionServ;

    /**
     * Registra una nueva sesion academica en la grilla semanal.
     * <p>
     * Construye un objeto SesionDTO con la informacion recibida
     * y lo envia al servicio para realizar las validaciones
     * correspondientes, como la franja institucional, los cruces
     * de horario y el limite de sesiones permitido.
     * </p>
     *
     * @param grupoId identificador del grupo al que pertenece la sesion.
     * @param diaSemana dia de la semana en el que se programara la sesion.
     * @param horaInicio hora de inicio de la sesion en formato HH:mm.
     * @param horaFin hora de finalizacion de la sesion en formato HH:mm.
     * @return respuesta HTTP 201 si la sesion fue creada exitosamente,
     *         o el codigo correspondiente si ocurre un error de validacion
     *         o no se encuentra el grupo.
     */
    @PreAuthorize("hasRole('GESTOR')")
    @PostMapping("/crear")
    @Operation(summary = "Ubicar sesion", description = "Ubica una sesion de dos horas validando franja, pausa, maximo de sesiones y cruces. Requiere rol GESTOR.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Sesion creada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Regla incumplida o datos invalidos"),
        @ApiResponse(responseCode = "404", description = "El grupo o su curso no existen")
    })
    public ResponseEntity<String> crear(
            @RequestParam long grupoId,
            @RequestParam int diaSemana,
            @RequestParam @DateTimeFormat(pattern = "HH:mm")
            @Parameter(schema = @Schema(type = "string", example = "14:00")) LocalTime horaInicio,
            @RequestParam @DateTimeFormat(pattern = "HH:mm")
            @Parameter(schema = @Schema(type = "string", example = "16:00")) LocalTime horaFin) {

        try {
            SesionDTO nueva = new SesionDTO();
            nueva.setGrupoId(grupoId);
            nueva.setDiaSemana(diaSemana);
            nueva.setHoraInicio(horaInicio);
            nueva.setHoraFin(horaFin);

            sesionServ.create(nueva);

            return new ResponseEntity<>("Sesion creada exitosamente", HttpStatus.CREATED);

        } catch (DatosInvalidosException | RecursoNoEncontradoException
                | FranjaInstitucionalInvalidaException | CruceHorarioException
                | LimiteSesionesExcedidoException e) {

            return respuestaError("Error al crear sesion: ", e);
        }
    }

    /**
     * Obtiene todas las sesiones academicas registradas en el sistema.
     *
     * @return respuesta HTTP 202 con la lista de sesiones,
     *         o 204 cuando no existen sesiones registradas.
     */
    @GetMapping("/mostrartodo")
    @Operation(summary = "Obtener todas las sesiones", description = "Retorna la lista de todas las sesiones registradas.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "Lista retornada exitosamente"),
        @ApiResponse(responseCode = "204", description = "No hay sesiones registradas")
    })
    public ResponseEntity<List<SesionDTO>> obtenerTodo() {

        List<SesionDTO> lista = sesionServ.getAll();

        if (lista.isEmpty()) {
            return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(lista, HttpStatus.ACCEPTED);
    }

    /**
     * Busca una sesion academica mediante su identificador.
     *
     * @param id identificador de la sesion que se desea consultar.
     * @return respuesta HTTP 202 con la informacion de la sesion,
     *         o 404 si no se encuentra o el identificador es invalido.
     */
    @GetMapping("/mostrar/{id}")
    @Operation(summary = "Obtener sesion por id", description = "Retorna una sesion segun su identificador.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "Sesion encontrada"),
        @ApiResponse(responseCode = "404", description = "Sesion no encontrada")
    })
    public ResponseEntity<SesionDTO> obtenerPorId(@PathVariable Long id) {

        try {
            return new ResponseEntity<>(sesionServ.getById(id), HttpStatus.ACCEPTED);

        } catch (DatosInvalidosException | RecursoNoEncontradoException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Obtiene todas las sesiones asociadas a un grupo especifico.
     *
     * @param grupoId identificador del grupo cuyas sesiones se desean consultar.
     * @return respuesta HTTP 202 con la lista de sesiones,
     *         204 si el grupo no tiene sesiones o 400 si el identificador
     *         proporcionado es invalido.
     */
    @GetMapping("/mostrarporgrupo/{grupoId}")
    @Operation(summary = "Obtener sesiones por grupo", description = "Retorna las sesiones asociadas a un grupo.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "Lista retornada exitosamente"),
        @ApiResponse(responseCode = "204", description = "El grupo no tiene sesiones"),
        @ApiResponse(responseCode = "400", description = "Id de grupo invalido")
    })
    public ResponseEntity<List<SesionDTO>> obtenerPorGrupo(@PathVariable long grupoId) {

        try {
            List<SesionDTO> lista = sesionServ.getByGrupoId(grupoId);

            if (lista.isEmpty()) {
                return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
            }

            return new ResponseEntity<>(lista, HttpStatus.ACCEPTED);

        } catch (DatosInvalidosException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Consulta las sesiones academicas correspondientes a los cursos
     * de un semestre determinado.
     *
     * @param semestre numero del semestre que se desea consultar.
     *                 Debe encontrarse dentro del rango de 1 a 9.
     * @return respuesta HTTP 202 con las sesiones encontradas,
     *         204 si no existen sesiones para el semestre o 400
     *         si el semestre proporcionado esta fuera del rango permitido.
     */
    @GetMapping("/mostrarporsemestre/{semestre}")
    @Operation(summary = "Obtener sesiones por semestre", description = "Retorna las sesiones de los cursos de un semestre (1 a 9).")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "Lista retornada exitosamente"),
        @ApiResponse(responseCode = "204", description = "No hay sesiones en ese semestre"),
        @ApiResponse(responseCode = "400", description = "Semestre fuera de rango")
    })
    public ResponseEntity<List<SesionDTO>> obtenerPorSemestre(@PathVariable int semestre) {

        try {
            List<SesionDTO> lista = sesionServ.getBySemestre(semestre);

            if (lista.isEmpty()) {
                return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
            }

            return new ResponseEntity<>(lista, HttpStatus.ACCEPTED);

        } catch (DatosInvalidosException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Consulta la cantidad total de sesiones registradas en el sistema.
     *
     * @return respuesta HTTP 202 con el numero total de sesiones,
     *         o 204 cuando no existen registros.
     */
    @GetMapping("/contar")
    @Operation(summary = "Contar sesiones", description = "Retorna el numero total de sesiones registradas.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "Conteo retornado exitosamente"),
        @ApiResponse(responseCode = "204", description = "No hay sesiones registradas")
    })
    public ResponseEntity<Long> contar() {

        long count = sesionServ.count();

        if (count == 0) {
            return new ResponseEntity<>(count, HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(count, HttpStatus.ACCEPTED);
    }

    /**
     * Verifica si existe una sesion academica mediante su identificador.
     *
     * @param id identificador de la sesion que se desea verificar.
     * @return respuesta HTTP 202 con true si la sesion existe,
     *         o 204 con false si no se encuentra.
     */
    @GetMapping("/existe/{id}")
    @Operation(summary = "Verificar existencia de sesion", description = "Verifica si existe una sesion con el id proporcionado.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "Sesion encontrada"),
        @ApiResponse(responseCode = "204", description = "Sesion no encontrada")
    })
    public ResponseEntity<Boolean> existe(@PathVariable Long id) {

        if (sesionServ.existById(id)) {
            return new ResponseEntity<>(true, HttpStatus.ACCEPTED);
        }

        return new ResponseEntity<>(false, HttpStatus.NO_CONTENT);
    }

    /**
     * Actualiza la ubicacion de una sesion existente dentro de la grilla semanal.
     * <p>
     * Permite modificar el dia de la semana y las horas de inicio
     * y finalizacion. El servicio vuelve a ejecutar las validaciones
     * correspondientes antes de aplicar los cambios.
     * </p>
     *
     * @param id identificador de la sesion que se desea actualizar.
     * @param diaSemana nuevo dia de la semana de la sesion.
     * @param horaInicio nueva hora de inicio en formato HH:mm.
     * @param horaFin nueva hora de finalizacion en formato HH:mm.
     * @return respuesta HTTP 202 si la sesion fue actualizada,
     *         o el codigo correspondiente si ocurre un error de validacion
     *         o no se encuentra el recurso.
     */
    @PreAuthorize("hasRole('GESTOR')")
    @PutMapping("/actualizar")
    @Operation(summary = "Mover sesion", description = "Mueve una sesion aplicando de nuevo las validaciones. Requiere rol GESTOR.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "Sesion actualizada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Regla incumplida o datos invalidos"),
        @ApiResponse(responseCode = "404", description = "La sesion, su grupo o su curso no existen")
    })
    public ResponseEntity<String> actualizar(
            @RequestParam Long id,
            @RequestParam int diaSemana,
            @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime horaInicio,
            @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime horaFin) {

        try {
            SesionDTO nueva = new SesionDTO();
            nueva.setDiaSemana(diaSemana);
            nueva.setHoraInicio(horaInicio);
            nueva.setHoraFin(horaFin);

            sesionServ.updateById(id, nueva);

            return new ResponseEntity<>("Sesion actualizada exitosamente", HttpStatus.ACCEPTED);

        } catch (DatosInvalidosException | RecursoNoEncontradoException
                | FranjaInstitucionalInvalidaException | CruceHorarioException
                | LimiteSesionesExcedidoException e) {

            return respuestaError("Error al actualizar sesion: ", e);
        }
    }

    /**
     * Elimina una sesion de la grilla semanal mediante su identificador.
     * <p>
     * La eliminacion permite que el grupo correspondiente vuelva a tener
     * una sesion pendiente de ubicacion.
     * </p>
     *
     * @param id identificador de la sesion que se desea eliminar.
     * @return respuesta HTTP 202 si la sesion fue eliminada exitosamente,
     *         o el codigo correspondiente si ocurre un error
     *         o no se encuentra el recurso.
     */
    @PreAuthorize("hasRole('GESTOR')")
    @DeleteMapping("/eliminar/{id}")
    @Operation(summary = "Quitar sesion", description = "Elimina una sesion de la grilla. Requiere rol GESTOR.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "Sesion eliminada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Sesion no encontrada")
    })
    public ResponseEntity<String> eliminarPorId(@PathVariable Long id) {

        try {
            sesionServ.deleteById(id);

            return new ResponseEntity<>("Sesion eliminada exitosamente", HttpStatus.ACCEPTED);

        } catch (DatosInvalidosException | RecursoNoEncontradoException e) {
            return respuestaError("Error al eliminar sesion: ", e);
        }
    }

    /**
     * Construye una respuesta HTTP de error a partir de una excepcion
     * generada durante la ejecucion de una operacion del servicio.
     * <p>
     * Si el error corresponde a un recurso no encontrado, retorna
     * el estado HTTP 404. Para las demas excepciones, retorna
     * el estado HTTP 400.
     * </p>
     *
     * @param prefijo texto que se agrega antes del mensaje de la excepcion.
     * @param e excepcion capturada durante la operacion.
     * @return respuesta HTTP con el mensaje del error y su codigo correspondiente.
     */
    private ResponseEntity<String> respuestaError(String prefijo, RuntimeException e) {

        HttpStatus estado = (e instanceof RecursoNoEncontradoException)
                ? HttpStatus.NOT_FOUND
                : HttpStatus.BAD_REQUEST;

        return new ResponseEntity<>(prefijo + e.getMessage(), estado);
    }
}
