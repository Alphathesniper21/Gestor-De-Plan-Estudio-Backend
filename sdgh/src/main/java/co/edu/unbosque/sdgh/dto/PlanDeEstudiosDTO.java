package co.edu.unbosque.sdgh.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PlanDeEstudiosDTO {

	private String id;

	private String nombrePrograma;

	private String version;

	private int totalCreditos;

	private int numeroSemestres;

	private List<EtapaDeFormacionDTO> etapas;

	private List<EspacioConocimientoDTO> espaciosConocimiento;

	private List<MateriaDTO> materias;

	public PlanDeEstudiosDTO() {
		this.etapas = new ArrayList<>();
		this.espaciosConocimiento = new ArrayList<>();
		this.materias = new ArrayList<>();
	}

	public PlanDeEstudiosDTO(String id, String nombrePrograma) {
		this();
		this.id = id;
		this.nombrePrograma = nombrePrograma;
	}

	public PlanDeEstudiosDTO(String id, String nombrePrograma, String version,
			int totalCreditos, int numeroSemestres, List<EtapaDeFormacionDTO> etapas,
			List<EspacioConocimientoDTO> espaciosConocimiento, List<MateriaDTO> materias) {
		this.id = id;
		this.nombrePrograma = nombrePrograma;
		this.version = version;
		this.totalCreditos = totalCreditos;
		this.numeroSemestres = numeroSemestres;
		this.etapas = etapas != null ? new ArrayList<>(etapas) : new ArrayList<>();
		this.espaciosConocimiento = espaciosConocimiento != null ? new ArrayList<>(espaciosConocimiento) : new ArrayList<>();
		this.materias = materias != null ? new ArrayList<>(materias) : new ArrayList<>();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNombrePrograma() {
		return nombrePrograma;
	}

	public void setNombrePrograma(String nombrePrograma) {
		this.nombrePrograma = nombrePrograma;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public int getTotalCreditos() {
		return totalCreditos;
	}

	public void setTotalCreditos(int totalCreditos) {
		this.totalCreditos = totalCreditos;
	}

	public int getNumeroSemestres() {
		return numeroSemestres;
	}

	public void setNumeroSemestres(int numeroSemestres) {
		this.numeroSemestres = numeroSemestres;
	}

	public List<EtapaDeFormacionDTO> getEtapas() {
		return etapas;
	}

	public void setEtapas(List<EtapaDeFormacionDTO> etapas) {
		this.etapas = etapas;
	}

	public List<EspacioConocimientoDTO> getEspaciosConocimiento() {
		return espaciosConocimiento;
	}

	public void setEspaciosConocimiento(List<EspacioConocimientoDTO> espaciosConocimiento) {
		this.espaciosConocimiento = espaciosConocimiento;
	}

	public List<MateriaDTO> getMaterias() {
		return materias;
	}

	public void setMaterias(List<MateriaDTO> materias) {
		this.materias = materias;
	}

	@Override
	public String toString() {
		return "PlanDeEstudiosDTO [id=" + id + ", nombrePrograma=" + nombrePrograma + ", version="
			+ version + ", totalCreditos=" + totalCreditos + ", numeroSemestres=" + numeroSemestres
			+ ", materias=" + (materias != null ? materias.size() : 0) + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(espaciosConocimiento, etapas, id, materias, nombrePrograma, numeroSemestres, totalCreditos, version);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		PlanDeEstudiosDTO other = (PlanDeEstudiosDTO) obj;
		return Objects.equals(id, other.id)
				&& Objects.equals(nombrePrograma, other.nombrePrograma)
				&& Objects.equals(version, other.version)
				&& totalCreditos == other.totalCreditos
				&& numeroSemestres == other.numeroSemestres
				&& Objects.equals(etapas, other.etapas)
				&& Objects.equals(espaciosConocimiento, other.espaciosConocimiento)
				&& Objects.equals(materias, other.materias);
	}

}
