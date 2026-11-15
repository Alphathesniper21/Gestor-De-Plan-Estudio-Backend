
package co.edu.unbosque.sdgh.controller;

import java.util.List;

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

import co.edu.unbosque.sdgh.dto.UsuarioDTO;
import co.edu.unbosque.sdgh.entity.RolUsuario;
import co.edu.unbosque.sdgh.exception.DatosInvalidosException;
import co.edu.unbosque.sdgh.exception.RecursoDuplicadoException;
import co.edu.unbosque.sdgh.exception.RecursoNoEncontradoException;
import co.edu.unbosque.sdgh.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;

/**
 * Controlador REST para la gestion de usuarios del sistema.
 * <p>
 * Todas las rutas tienen el prefijo {@code /usuario} y requieren
 * autenticacion mediante JWT con el rol {@code GESTOR}.
 * Expone operaciones CRUD para crear, consultar, actualizar,
 * contar, verificar y eliminar usuarios registrados.
 * </p>
 *
 * @version 1.0
 * @since 1.0
 * @see co.edu.unbosque.sdgh.service.UsuarioService
 * @see co.edu.unbosque.sdgh.dto.UsuarioDTO
 * @see co.edu.unbosque.sdgh.entity.RolUsuario
 */
@RestController
@RequestMapping("/usuario")
@CrossOrigin(origins = { "http://localhost:8080" })
@Transactional
@Tag(name = "Usuarios", description = "Endpoints para la gestion de usuarios")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('GESTOR')")
public class UsuarioController {

    /**
     * Servicio encargado de ejecutar la logica de negocio
     * y las operaciones CRUD relacionadas con los usuarios.
     */
    @Autowired
    private UsuarioService usuarioServ;

    /**
     * Registra un nuevo usuario en el sistema.
     * <p>
     * Recibe los datos del usuario mediante parametros de solicitud
     * y los encapsula en un objeto UsuarioDTO antes de enviarlos
     * al servicio para su validacion y almacenamiento.
     * </p>
     *
     * @param nombre nombre completo del usuario.
     * @param correo correo electronico del usuario.
     * @param username nombre de usuario para iniciar sesion.
     * @param password contrasena de acceso.
     * @param rol rol que tendra el usuario dentro del sistema.
     * @return respuesta HTTP 201 si el usuario fue creado,
     *         o 400 si los datos son invalidos o existe un duplicado.
     */
    @PostMapping("/crear")
    @Operation(summary = "Crear usuario", description = "Registra un nuevo usuario. Requiere rol GESTOR.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos invalidos"),
        @ApiResponse(responseCode = "409", description = "Codigo, correo o username duplicado")
    })
    public ResponseEntity<String> crear(
            @RequestParam String nombre,
            @RequestParam String correo,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam RolUsuario rol) {

        try {
            UsuarioDTO nuevo = new UsuarioDTO(nombre, correo, username, password, rol);
            usuarioServ.create(nuevo);
            return new ResponseEntity<>("Usuario creado exitosamente", HttpStatus.CREATED);

        } catch (DatosInvalidosException | RecursoDuplicadoException e) {
            return new ResponseEntity<>("Error al crear usuario: " + e.getMessage(),
                    HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Obtiene todos los usuarios registrados en el sistema.
     *
     * @return respuesta HTTP 202 con la lista de usuarios,
     *         o 204 si no existen usuarios registrados.
     */
    @GetMapping("/mostrartodo")
    @Operation(summary = "Obtener todos los usuarios", description = "Retorna la lista de todos los usuarios registrados.")
    public ResponseEntity<List<UsuarioDTO>> obtenerTodo() {

        List<UsuarioDTO> lista = usuarioServ.getAll();

        if (lista.isEmpty()) {
            return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(lista, HttpStatus.ACCEPTED);
    }

    /**
     * Busca un usuario registrado mediante su identificador.
     *
     * @param id identificador del usuario que se desea consultar.
     * @return respuesta HTTP 202 con los datos del usuario,
     *         o 404 si el usuario no existe o el identificador es invalido.
     */
    @GetMapping("/mostrar/{id}")
    @Operation(summary = "Obtener usuario por codigo", description = "Retorna un usuario segun su codigo.")
    public ResponseEntity<UsuarioDTO> obtenerPorCodigo(@PathVariable Long id) {

        try {
            return new ResponseEntity<>(usuarioServ.getById(id), HttpStatus.ACCEPTED);

        } catch (DatosInvalidosException | RecursoNoEncontradoException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Busca un usuario mediante su correo electronico institucional.
     *
     * @param correo correo electronico del usuario que se desea consultar.
     * @return respuesta HTTP 202 con los datos del usuario,
     *         o 404 si no se encuentra el usuario o el correo es invalido.
     */
    @GetMapping("/mostrarporcorreo")
    @Operation(summary = "Obtener usuario por correo", description = "Retorna un usuario segun su correo institucional.")
    public ResponseEntity<UsuarioDTO> obtenerPorCorreo(@RequestParam String correo) {

        try {
            return new ResponseEntity<>(usuarioServ.getByCorreo(correo), HttpStatus.ACCEPTED);

        } catch (DatosInvalidosException | RecursoNoEncontradoException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Busca un usuario mediante su nombre de usuario utilizado para iniciar sesion.
     *
     * @param username nombre de usuario que se desea consultar.
     * @return respuesta HTTP 202 con los datos del usuario,
     *         o 404 si no se encuentra el usuario o el username es invalido.
     */
    @GetMapping("/mostrarporusername")
    @Operation(summary = "Obtener usuario por username", description = "Retorna un usuario segun su nombre de usuario (login).")
    public ResponseEntity<UsuarioDTO> obtenerPorUsername(@RequestParam String username) {

        try {
            return new ResponseEntity<>(usuarioServ.getByUsername(username), HttpStatus.ACCEPTED);

        } catch (DatosInvalidosException | RecursoNoEncontradoException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Consulta la cantidad total de usuarios registrados en el sistema.
     *
     * @return respuesta HTTP 202 con el numero de usuarios,
     *         o 204 cuando no existen registros.
     */
    @GetMapping("/contar")
    @Operation(summary = "Contar usuarios", description = "Retorna el numero total de usuarios registrados.")
    public ResponseEntity<Long> contar() {

        long count = usuarioServ.count();

        if (count == 0) {
            return new ResponseEntity<>(count, HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(count, HttpStatus.ACCEPTED);
    }

    /**
     * Verifica si existe un usuario con el identificador proporcionado.
     *
     * @param id identificador del usuario que se desea verificar.
     * @return respuesta HTTP 202 con true si existe el usuario,
     *         o 204 con false si no se encuentra.
     */
    @GetMapping("/existe/{id}")
    @Operation(summary = "Verificar existencia de usuario", description = "Verifica si existe un usuario con el codigo proporcionado.")
    public ResponseEntity<Boolean> existe(@PathVariable Long id) {

        if (usuarioServ.existById(id)) {
            return new ResponseEntity<>(true, HttpStatus.ACCEPTED);
        }

        return new ResponseEntity<>(false, HttpStatus.NO_CONTENT);
    }

    /**
     * Actualiza la informacion de un usuario existente.
     * <p>
     * Permite modificar el nombre, correo electronico y rol.
     * La contrasena es opcional y se envia al servicio cuando
     * se proporciona.
     * </p>
     *
     * @param id identificador del usuario que se desea actualizar.
     * @param nombre nuevo nombre completo del usuario.
     * @param correo nuevo correo electronico del usuario.
     * @param rol nuevo rol asignado al usuario.
     * @param password nueva contrasena del usuario (opcional).
     * @return respuesta HTTP 202 si la actualizacion fue exitosa,
     *         o 400 si ocurre un error de validacion o duplicidad.
     */
    @PutMapping("/actualizar")
    @Operation(summary = "Actualizar usuario", description = "Actualiza nombre, correo y rol de un usuario por su codigo. La contrasena es opcional.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "Usuario actualizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos invalidos"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
        @ApiResponse(responseCode = "409", description = "Correo de otro usuario")
    })
    public ResponseEntity<String> actualizar(
            @RequestParam Long id,
            @RequestParam String nombre,
            @RequestParam String correo,
            @RequestParam RolUsuario rol,
            @RequestParam(required = false) String password) {

        try {
            UsuarioDTO nuevo = new UsuarioDTO();
            nuevo.setId(id);
            nuevo.setNombre(nombre);
            nuevo.setCorreo(correo);
            nuevo.setRol(rol);
            nuevo.setPassword(password);

            usuarioServ.updateById(id, nuevo);

            return new ResponseEntity<>("Usuario actualizado exitosamente", HttpStatus.ACCEPTED);

        } catch (DatosInvalidosException | RecursoNoEncontradoException | RecursoDuplicadoException e) {
            return new ResponseEntity<>("Error al actualizar usuario: " + e.getMessage(),
                    HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Elimina un usuario del sistema mediante su identificador.
     *
     * @param id identificador del usuario que se desea eliminar.
     * @return respuesta HTTP 202 si el usuario fue eliminado,
     *         o 400 si el identificador es invalido o el usuario no existe.
     */
    @DeleteMapping("/eliminar/{id}")
    @Operation(summary = "Eliminar usuario", description = "Elimina un usuario por su codigo.")
    public ResponseEntity<String> eliminarPorCodigo(@PathVariable Long id) {

        try {
            usuarioServ.deleteById(id);
            return new ResponseEntity<>("Usuario eliminado exitosamente", HttpStatus.ACCEPTED);

        } catch (DatosInvalidosException | RecursoNoEncontradoException e) {
            return new ResponseEntity<>("Error al eliminar: " + e.getMessage(),
                    HttpStatus.BAD_REQUEST);
        }
    }
}
