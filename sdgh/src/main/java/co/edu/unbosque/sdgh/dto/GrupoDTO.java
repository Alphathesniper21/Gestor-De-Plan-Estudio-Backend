package co.edu.unbosque.sdgh.dto;

import java.util.Objects;

public class GrupoDTO {

	private Long id;

	/**
	 * Codigo de la materia (curso) a la que pertenece el grupo.
	 */
	private long materiaId;
	
	/**
	 * Nombre de la materia (curso) a la que pertenece el grupo.
	 */
	private String nombreMateria;
	
	/**
	 * Numero del grupo dentro del curso. Define el bloque (01, 02...).
	 */
	private int numeroGrupo;

	/**
	 * Cupo planeado del grupo. Maximo 40 mas 10 % de tolerancia (RF2).
	 */
	private int cupoPlaneado;
	
	public GrupoDTO() {
		// TODO Auto-generated constructor stub
	}

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

	@Override
	public String toString() {
		return "GrupoDTO [id=" + id + ", materiaId=" + materiaId + ", nombreMateria=" + nombreMateria + ", numeroGrupo="
				+ numeroGrupo + ", cupoPlaneado=" + cupoPlaneado + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(Integer.valueOf(cupoPlaneado), id, Long.valueOf(materiaId), nombreMateria,
				Integer.valueOf(numeroGrupo));
	}

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