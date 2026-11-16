package co.edu.unbosque.sdgh.controller;

import org.springframework.beans.factory.annotation.Autowired;
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

import co.edu.unbosque.sdgh.dto.MateriaDTO;
import co.edu.unbosque.sdgh.exception.DatosInvalidosException;
import co.edu.unbosque.sdgh.exception.LimiteSesionesExcedidoException;
import co.edu.unbosque.sdgh.exception.RecursoDuplicadoException;
import co.edu.unbosque.sdgh.exception.RecursoNoEncontradoException;
import co.edu.unbosque.sdgh.service.MateriaService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;

/**
 * Controlador REST encargado de gestionar las operaciones relacionadas con
 * las materias del plan de estudios.
 * 
 * Expone endpoints para crear, consultar, actualizar, eliminar y verificar
 * la existencia de materias, comunicándose con la capa de servicio
 * {@link MateriaService}.
 * 
 * Algunas operaciones requieren autenticación y autorización mediante el
 * rol GESTOR.
 * 
 * @author David Velasquez
 * @version 1.0
 * @since 1.0
 * @see MateriaService
 * @see MateriaDTO
 */
@RestController
@RequestMapping("/materia")
@CrossOrigin(origins = { "http://localhost:8080" })
@Transactional
@Tag(name = "Materias", description = "Endpoints para la gestion de materias del plan de estudios")
@SecurityRequirement(name = "bearerAuth")
public class MateriaController {

    /**
     * Servicio encargado de ejecutar la lógica de negocio relacionada con
     * la administración de las materias.
     */
    @Autowired
    private MateriaService materiaService;

    /**
     * Crea una nueva materia con la información proporcionada.
     * 
     * Requiere el rol GESTOR para ejecutar la operación.
     * 
     * @param codigo           Código institucional de la materia.
     * @param nombre           Nombre de la materia.
     * @param semestre         Semestre al que pertenece la materia.
     * @param ncreditos        Cantidad de créditos académicos de la materia.
     * @param horasDeLaMateria Cantidad de horas correspondientes a la materia.
     * @param correlativa      Indica si la materia tiene una correlativa.
     * @return ResponseEntity con un mensaje de confirmación y el estado HTTP
     *         CREATED si se crea correctamente, BAD_REQUEST si los datos son
     *         inválidos o CONFLICT si la materia ya existe.
     * @throws DatosInvalidosException Si los datos proporcionados no cumplen
     *         las validaciones establecidas.
     * @throws RecursoDuplicadoException Si ya existe una materia con la
     *         información registrada.
     * @see MateriaService#create(MateriaDTO)
     */
    @PreAuthorize("hasRole('GESTOR')")
    @PostMapping("/crear")
    @Operation(summary = "Crear materia", description = "Crea una nueva materia. Requiere rol GESTOR.")
    @ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Materia creada exitosamente"),
            @ApiResponse(responseCode = "406", description = "Error al crear materia (datos invalidos o duplicada)") })
    public ResponseEntity<String> crear(@RequestParam long codigo, @RequestParam String nombre,
            @RequestParam int semestre, @RequestParam int ncreditos, @RequestParam int horasDeLaMateria,
            @RequestParam boolean correlativa) {
        try {
            MateriaDTO nueva = new MateriaDTO(codigo, nombre, semestre, ncreditos, horasDeLaMateria, correlativa);
            materiaService.create(nueva);
            return new ResponseEntity<>("Materia creada exitosamente", HttpStatus.CREATED);
        } catch (DatosInvalidosException e) {
            return new ResponseEntity<>("Error al crear materia: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (RecursoDuplicadoException e) {
            return new ResponseEntity<>("Error al crear materia: " + e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    /**
     * Obtiene todas las materias registradas en el sistema.
     * 
     * @return ResponseEntity con la lista de materias y el estado HTTP ACCEPTED
     *         si existen registros, o NO_CONTENT si la lista está vacía.
     * @see MateriaService#getAll()
     */
    @GetMapping("/mostrartodo")
    @Operation(summary = "Obtener todas las materias", description = "Retorna la lista de todas las materias registradas.")
    @ApiResponses(value = { @ApiResponse(responseCode = "202", description = "Lista retornada exitosamente"),
            @ApiResponse(responseCode = "204", description = "No hay materias registradas") })
    public ResponseEntity<List<MateriaDTO>> obtenerTodo() {
        List<MateriaDTO> lista = materiaService.getAll();
        if (lista.isEmpty()) {
            return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(lista, HttpStatus.ACCEPTED);
        }
    }

    /**
     * Busca una materia utilizando su código institucional.
     * 
     * @param codigo Código institucional de la materia que se desea consultar.
     * @return ResponseEntity con los datos de la materia y el estado HTTP
     *         ACCEPTED si se encuentra, o NOT_FOUND si no existe.
     * @throws RecursoNoEncontradoException Si no existe una materia con el
     *         código proporcionado.
     * @see MateriaService#getByCodigo(long)
     */
    @GetMapping("/mostrarporcodigo/{codigo}")
    @Operation(summary = "Obtener materia por codigo", description = "Retorna una materia segun su codigo institucional.")
    @ApiResponses(value = { @ApiResponse(responseCode = "202", description = "Materia encontrada"),
            @ApiResponse(responseCode = "404", description = "Materia no encontrada") })
    public ResponseEntity<MateriaDTO> obtenerPorCodigo(@PathVariable long codigo) {
        try {
            return new ResponseEntity<>(materiaService.getByCodigo(codigo), HttpStatus.ACCEPTED);
        } catch (RecursoNoEncontradoException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Busca una materia utilizando su identificador único.
     * 
     * @param id Identificador de la materia que se desea consultar.
     * @return ResponseEntity con los datos de la materia y el estado HTTP
     *         ACCEPTED si se encuentra, o NOT_FOUND si no existe.
     * @throws RecursoNoEncontradoException Si no existe una materia con el
     *         identificador proporcionado.
     * @see MateriaService#getById(Long)
     */
    @GetMapping("/mostrar/{id}")
    @Operation(summary = "Obtener materia por id", description = "Retorna una materia segun su id.")
    @ApiResponses(value = { @ApiResponse(responseCode = "202", description = "Materia encontrada"),
            @ApiResponse(responseCode = "404", description = "Materia no encontrada") })
    public ResponseEntity<MateriaDTO> obtenerPorId(@PathVariable Long id) {
        try {
            return new ResponseEntity<>(materiaService.getById(id), HttpStatus.ACCEPTED);
        } catch (RecursoNoEncontradoException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Obtiene todas las materias pertenecientes a un semestre específico.
     * 
     * @param semestre Número del semestre que se desea consultar.
     * @return ResponseEntity con la lista de materias y el estado HTTP ACCEPTED
     *         si existen registros, o NO_CONTENT si no hay materias.
     * @throws DatosInvalidosException Si el semestre proporcionado no cumple
     *         con las validaciones establecidas.
     * @see MateriaService#getBySemestre(int)
     */
    @GetMapping("/semestre/{semestre}")
    @Operation(summary = "Obtener materias por semestre", description = "Retorna las materias de un semestre (1 a 9).")
    @ApiResponses(value = { @ApiResponse(responseCode = "202", description = "Lista retornada exitosamente"),
            @ApiResponse(responseCode = "204", description = "No hay materias en ese semestre") })
    public ResponseEntity<List<MateriaDTO>> obtenerPorSemestre(@PathVariable int semestre) {
        try {
            List<MateriaDTO> lista = materiaService.getBySemestre(semestre);
            if (lista.isEmpty()) {
                return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
            } else {
                return new ResponseEntity<>(lista, HttpStatus.ACCEPTED);
            }
        } catch (DatosInvalidosException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Obtiene las materias según su condición de correlatividad.
     * 
     * Permite consultar las materias que son correlativas o aquellas que
     * no tienen esta condición.
     * 
     * @param correlativa Valor booleano que determina el filtro de búsqueda.
     *                    TRUE para materias correlativas y FALSE para las que
     *                    no lo son.
     * @return ResponseEntity con la lista de materias y el estado HTTP ACCEPTED
     *         si existen registros, o NO_CONTENT si no hay coincidencias.
     * @see MateriaService#getByCorrelativa(boolean)
     */
    @GetMapping("/correlativa/{correlativa}")
    @Operation(summary = "Obtener materias por correlatividad", description = "Retorna las materias que son (o no) correlativas.")
    @ApiResponses(value = { @ApiResponse(responseCode = "202", description = "Lista retornada exitosamente"),
            @ApiResponse(responseCode = "204", description = "No hay materias con ese filtro") })
    public ResponseEntity<List<MateriaDTO>> obtenerPorCorrelativa(@PathVariable boolean correlativa) {
        List<MateriaDTO> lista = materiaService.getByCorrelativa(correlativa);
        if (lista.isEmpty()) {
            return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(lista, HttpStatus.ACCEPTED);
        }
    }

    /**
     * Consulta la cantidad total de materias registradas en el sistema.
     * 
     * @return ResponseEntity con el número total de materias y el estado HTTP
     *         ACCEPTED si existen registros, o NO_CONTENT si no hay materias.
     * @see MateriaService#count()
     */
    @GetMapping("/contar")
    @Operation(summary = "Contar materias", description = "Retorna el numero total de materias registradas.")
    @ApiResponses(value = { @ApiResponse(responseCode = "202", description = "Conteo retornado exitosamente"),
            @ApiResponse(responseCode = "204", description = "No hay materias registradas") })
    public ResponseEntity<Long> contar() {
        Long count = materiaService.count();
        if (count == 0) {
            return new ResponseEntity<>(count, HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(count, HttpStatus.ACCEPTED);
        }
    }

    /**
     * Verifica si existe una materia mediante su identificador único.
     * 
     * @param id Identificador de la materia que se desea verificar.
     * @return ResponseEntity con TRUE y el estado HTTP ACCEPTED si la materia
     *         existe, o FALSE y NO_CONTENT si no se encuentra.
     * @see MateriaService#existById(Long)
     */
    @GetMapping("/existe/{id}")
    @Operation(summary = "Verificar existencia de materia", description = "Verifica si existe una materia con el id proporcionado.")
    @ApiResponses(value = { @ApiResponse(responseCode = "202", description = "Materia encontrada"),
            @ApiResponse(responseCode = "204", description = "Materia no encontrada") })
    public ResponseEntity<Boolean> existe(@PathVariable Long id) {
        boolean encontrado = materiaService.existById(id);
        if (encontrado) {
            return new ResponseEntity<>(true, HttpStatus.ACCEPTED);
        } else {
            return new ResponseEntity<>(false, HttpStatus.NO_CONTENT);
        }
    }

    /**
     * Actualiza la información de una materia existente.
     * 
     * Requiere el rol GESTOR para ejecutar la operación.
     * 
     * @param id               Identificador de la materia que se desea actualizar.
     * @param codigo           Nuevo código institucional de la materia.
     * @param nombre           Nuevo nombre de la materia.
     * @param semestre         Nuevo semestre al que pertenece la materia.
     * @param ncreditos        Nueva cantidad de créditos académicos.
     * @param horasDeLaMateria Nueva cantidad de horas de la materia.
     * @param correlativa      Nueva condición de correlatividad de la materia.
     * @return ResponseEntity con un mensaje de confirmación y el estado HTTP
     *         ACCEPTED si se actualiza correctamente, NOT_FOUND si la materia
     *         no existe, CONFLICT si hay un recurso duplicado o se excede el
     *         límite de sesiones, y BAD_REQUEST si los datos son inválidos.
     * @throws RecursoNoEncontradoException Si la materia no existe.
     * @throws RecursoDuplicadoException Si la información genera un recurso
     *         duplicado.
     * @throws DatosInvalidosException Si los datos proporcionados no son válidos.
     * @throws LimiteSesionesExcedidoException Si se excede el límite de sesiones
     *         permitido.
     * @see MateriaService#updateById(Long, MateriaDTO)
     */
    @PreAuthorize("hasRole('GESTOR')")
    @PutMapping("/actualizar")
    @Operation(summary = "Actualizar materia", description = "Actualiza una materia existente por su id. Requiere rol GESTOR.")
    @ApiResponses(value = { @ApiResponse(responseCode = "202", description = "Materia actualizada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Materia no encontrada"),
            @ApiResponse(responseCode = "400", description = "Error al actualizar") })
    public ResponseEntity<String> actualizar(@RequestParam Long id, @RequestParam long codigo,
            @RequestParam String nombre, @RequestParam int semestre, @RequestParam int ncreditos,
            @RequestParam int horasDeLaMateria, @RequestParam boolean correlativa) {
        MateriaDTO nueva = new MateriaDTO(codigo, nombre, semestre, ncreditos, horasDeLaMateria, correlativa);
        if (!materiaService.existById(id)) {
            return new ResponseEntity<>("Materia no encontrada", HttpStatus.NOT_FOUND);
        }
        try {
            materiaService.updateById(id, nueva);
            return new ResponseEntity<>("Materia actualizada exitosamente", HttpStatus.ACCEPTED);
        } catch (RecursoNoEncontradoException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (RecursoDuplicadoException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (DatosInvalidosException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (LimiteSesionesExcedidoException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    /**
     * Elimina una materia utilizando su código institucional.
     * 
     * Requiere el rol GESTOR para ejecutar la operación.
     * 
     * @param codigo Código institucional de la materia que se desea eliminar.
     * @return ResponseEntity con un mensaje de confirmación y el estado HTTP
     *         ACCEPTED si se elimina correctamente, NOT_FOUND si no existe
     *         o BAD_REQUEST si no se cumplen las condiciones para eliminarla.
     * @throws RecursoNoEncontradoException Si no existe una materia con el
     *         código proporcionado.
     * @throws DatosInvalidosException Si la materia no puede eliminarse debido
     *         a las condiciones establecidas por el sistema.
     * @see MateriaService#deleteByCodigo(long)
     */
    @PreAuthorize("hasRole('GESTOR')")
    @DeleteMapping("/eliminarporcodigo/{codigo}")
    @Operation(summary = "Eliminar materia por codigo", description = "Elimina una materia por su codigo institucional. Requiere rol GESTOR.")
    @ApiResponses(value = { @ApiResponse(responseCode = "202", description = "Materia eliminada exitosamente"),
            @ApiResponse(responseCode = "400", description = "La materia tiene grupos"),
            @ApiResponse(responseCode = "404", description = "Materia no encontrada") })
    public ResponseEntity<String> eliminarPorCodigo(@PathVariable long codigo) {
        try {
            materiaService.deleteByCodigo(codigo);
            return new ResponseEntity<>("Materia eliminada exitosamente", HttpStatus.ACCEPTED);
        } catch (RecursoNoEncontradoException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (DatosInvalidosException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Elimina una materia utilizando su identificador único.
     * 
     * Requiere el rol GESTOR para ejecutar la operación.
     * 
     * @param id Identificador de la materia que se desea eliminar.
     * @return ResponseEntity con un mensaje de confirmación y el estado HTTP
     *         ACCEPTED si se elimina correctamente, NOT_FOUND si no existe
     *         o BAD_REQUEST si no se cumplen las condiciones para eliminarla.
     * @throws RecursoNoEncontradoException Si no existe una materia con el
     *         identificador proporcionado.
     * @throws DatosInvalidosException Si la materia no puede eliminarse debido
     *         a las condiciones establecidas por el sistema.
     * @see MateriaService#deleteById(Long)
     */
    @PreAuthorize("hasRole('GESTOR')")
    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<String> eliminarPorId(@PathVariable Long id) {
        try {
            materiaService.deleteById(id);
            return new ResponseEntity<>("Materia eliminada exitosamente", HttpStatus.ACCEPTED);
        } catch (RecursoNoEncontradoException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (DatosInvalidosException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}