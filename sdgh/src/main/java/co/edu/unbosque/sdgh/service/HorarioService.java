/**
 * Paquete de servicios del aplicativo.
 */
package co.edu.unbosque.sdgh.service;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.edu.unbosque.sdgh.dto.GrupoDTO;
import co.edu.unbosque.sdgh.entity.Grupo;
import co.edu.unbosque.sdgh.entity.Materia;
import co.edu.unbosque.sdgh.exception.DatosInvalidosException;
import co.edu.unbosque.sdgh.repository.GrupoRepository;
import co.edu.unbosque.sdgh.repository.MateriaRepository;
import co.edu.unbosque.sdgh.repository.SesionRepository;

/**
 * Servicio encargado del horario como un todo (CU08): la revision de los
 * grupos con sesiones pendientes (RF11) y la publicacion (RF12). El estado
 * publicado se guarda en los parametros con la clave horario.publicado.
 */
@Service
public class HorarioService {

	/**
	 * Repositorio de grupos.
	 */
	@Autowired
	private GrupoRepository grupoRepo;

	/**
	 * Repositorio de materias.
	 */
	@Autowired
	private MateriaRepository materiaRepo;

	/**
	 * Repositorio de sesiones.
	 */
	@Autowired
	private SesionRepository sesionRepo;

	/**
	 * Servicio de parametros, donde se guarda el estado de publicacion.
	 */
	@Autowired
	private ParametroService parametroServ;

	/**
	 * Mapper utilizado para convertir entidades en DTO y viceversa.
	 */
	@Autowired
	private ModelMapper mapper;

	/**
	 * Constructor vacio de la clase HorarioService.
	 */
	public HorarioService() {
	}

	/**
	 * Metodo que obtiene los grupos que todavia no tienen todas las sesiones de
	 * su curso (RF11). Un grupo con 1 de 2 sesiones aparece como pendiente.
	 * @return Retorna la lista de grupos pendientes en formato DTO.
	 */
	public List<GrupoDTO> getPendientes() {

		List<GrupoDTO> pendientes = new ArrayList<>();

		for (Grupo grupo : grupoRepo.findAll()) {
			Materia materia = materiaRepo.findById(grupo.getMateriaId()).orElse(null);
			if (materia == null) {
				continue;
			}
			int asignadas = sesionRepo.findByGrupoId(grupo.getId()).size();
			if (asignadas < materia.getSesionesSemanales()) {
				pendientes.add(mapper.map(grupo, GrupoDTO.class));
			}
		}

		return pendientes;
	}

	/**
	 * Metodo que publica el horario (RF12): lo marca como publicado y queda
	 * almacenado en la base de datos para su consulta. No es posible mientras
	 * haya grupos pendientes (CU08).
	 * @return Retorna 0 si el horario fue publicado.
	 * @throws DatosInvalidosException si hay grupos con sesiones pendientes.
	 */
	public int publicar() {

		int pendientes = getPendientes().size();

		if (pendientes > 0) {
			throw new DatosInvalidosException("No se puede publicar: hay " + pendientes
					+ " grupo(s) con sesiones pendientes");
		}

		parametroServ.marcarPublicado(true);
		return 0;
	}

	/**
	 * Metodo que indica si el horario esta publicado (RF12). Vuelve a false
	 * cuando se crea, mueve o quita una sesion.
	 * @return Retorna true si el horario esta publicado.
	 */
	public boolean estaPublicado() {

		return parametroServ.estaPublicado();
	}
}
