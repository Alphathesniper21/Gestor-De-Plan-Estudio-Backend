package co.edu.unbosque.sdgh.dto;

import java.util.Objects;

/**
 * DTO que transporta los datos de un grupo de una materia (curso).
 * <p>
 * Cada grupo pertenece a una materia, se identifica por su número dentro de
 * ella (lo que define el bloque 01, 02...) y tiene un cupo planeado limitado
 * a 40 estudiantes más un 10 % de tolerancia (RF2). Incluye el nombre de la
 * materia para evitar consultas adicionales en el cliente.
 * </p>
 *
 * @author Laura Gomez
 * @version 1.0
 * @since 1.0
 * @see MateriaDTO
 * @see SesionDTO
 */
public class GrupoDTO {

	/**
	 * Identificador único del grupo.
	 */
	private Long id;

	/**
	 * Código de la materia (curso) a la que pertenece el grupo.
	 */
	private long materiaId;

	/**
	 * Nombre de la materia (curso) a la que pertenece el grupo.
	 */
	private String nombreMateria;

	/**
	 * Número del grupo dentro del curso. Define el bloque (01, 02...).
	 */
	private int numeroGrupo;

	/**
	 * Cupo planeado del grupo. Máximo 40 más 10 % de tolerancia (RF2).
	 */
	private int cupoPlaneado;

	/**
	 * Constructor vacío requerido para la deserialización y el mapeo de datos.
	 */
	public GrupoDTO() {
	}

	/**
	 * Construye un grupo con los datos necesarios para su creación.
	 *
	 * @param materiaId     código de la materia a la que pertenece el grupo
	 * @param nombreMateria nombre de la materia a la que pertenece el grupo
	 * @param numeroGrupo   número del grupo dentro del curso
	 * @param cupoPlaneado  cupo planeado del grupo
	 */
	public GrupoDTO(long materiaId, String nombreMateria, int numeroGrupo, int cupoPlaneado) {
		super();
		this.materiaId = materiaId;
		this.nombreMateria = nombreMateria;
		this.numeroGrupo = numeroGrupo;
		this.cupoPlaneado = cupoPlaneado;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public long getMateriaId() {
		return materiaId;
	}

	public void setMateriaId(long materiaId) {
		this.materiaId = materiaId;
	}

	public String getNombreMateria() {
		return nombreMateria;
	}

	public void setNombreMateria(String nombreMateria) {
		this.nombreMateria = nombreMateria;
	}

	public int getNumeroGrupo() {
		return numeroGrupo;
	}

	public void setNumeroGrupo(int numeroGrupo) {
		this.numeroGrupo = numeroGrupo;
	}

	public int getCupoPlaneado() {
		return cupoPlaneado;
	}

	public void setCupoPlaneado(int cupoPlaneado) {
		this.cupoPlaneado = cupoPlaneado;
	}

	/**
	 * Representación en texto del grupo con todos sus atributos.
	 *
	 * @return cadena descriptiva del grupo
	 */
	@Override
	public String toString() {
		return "GrupoDTO [id=" + id + ", materiaId=" + materiaId + ", nombreMateria=" + nombreMateria + ", numeroGrupo="
				+ numeroGrupo + ", cupoPlaneado=" + cupoPlaneado + "]";
	}

	/**
	 * Calcula el código hash a partir de todos los atributos del grupo.
	 *
	 * @return código hash del objeto
	 */
	@Override
	public int hashCode() {
		return Objects.hash(Integer.valueOf(cupoPlaneado), id, Long.valueOf(materiaId), nombreMateria,
				Integer.valueOf(numeroGrupo));
	}

	/**
	 * Compara este grupo con otro objeto atributo por atributo.
	 *
	 * @param obj objeto a comparar
	 * @return {@code true} si ambos son {@link GrupoDTO} con los mismos valores
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GrupoDTO other = (GrupoDTO) obj;
		return cupoPlaneado == other.cupoPlaneado && Objects.equals(id, other.id) && materiaId == other.materiaId
				&& Objects.equals(nombreMateria, other.nombreMateria) && numeroGrupo == other.numeroGrupo;
	}

}