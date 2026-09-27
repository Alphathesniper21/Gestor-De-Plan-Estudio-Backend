package co.edu.unbosque.sdgh.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PlanDeEstudios {

    private String id;
    private String nombrePrograma;          
    private String version;                 
    private int totalCreditos;              
    private int numeroSemestres;           

   
    private List<EtapaFormacion> etapas;    
    private List<EspacioConocimiento> espaciosConocimiento; 
    
    private List<Materia> materias;

    public PlanDeEstudios() {
        this.etapas = new ArrayList<>();
        this.espaciosConocimiento = new ArrayList<>();
        this.materias = new ArrayList<>();
    }

    public PlanDeEstudios(String id, String nombrePrograma) {
        this();
        this.id = id;
        this.nombrePrograma = nombrePrograma;
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

	public List<EtapaFormacion> getEtapas() {
		return etapas;
	}

	public void setEtapas(List<EtapaFormacion> etapas) {
		this.etapas = etapas;
	}

	public List<EspacioConocimiento> getEspaciosConocimiento() {
		return espaciosConocimiento;
	}

	public void setEspaciosConocimiento(List<EspacioConocimiento> espaciosConocimiento) {
		this.espaciosConocimiento = espaciosConocimiento;
	}

	public List<Materia> getMaterias() {
		return materias;
	}

	public void setMaterias(List<Materia> materias) {
		this.materias = materias;
	}

	@Override
    public String toString() {
        return "PlanDeEstudios [id=" + id + 
               ", nombrePrograma=" + nombrePrograma + 
               ", version=" + version + 
               ", totalCreditos=" + totalCreditos + 
               ", materias=" + (materias != null ? materias.size() : 0) + "]";
    }

	@Override
	public int hashCode() {
		return Objects.hash(espaciosConocimiento, etapas, id, materias, nombrePrograma,
				Integer.valueOf(numeroSemestres), Integer.valueOf(totalCreditos), version);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PlanDeEstudios other = (PlanDeEstudios) obj;
		return Objects.equals(espaciosConocimiento, other.espaciosConocimiento) && Objects.equals(etapas, other.etapas)
				&& Objects.equals(id, other.id) && Objects.equals(materias, other.materias)
				&& Objects.equals(nombrePrograma, other.nombrePrograma) && numeroSemestres == other.numeroSemestres
				&& totalCreditos == other.totalCreditos && Objects.equals(version, other.version);
	}

 
}