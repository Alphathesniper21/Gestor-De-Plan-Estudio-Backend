package co.edu.unbosque.sdgh.dto;

import java.util.Objects;

public class MateriaDTO {

	private long codigo;

	private String nombre;

	private int semestre;

	private int ncreditos;

	private int horasDeLaMateria;

	private boolean correlativa;

	public MateriaDTO() {
	}

	public MateriaDTO(long codigo, String nombre, int semestre,
			int ncreditos, int horasDeLaMateria, boolean correlativa) {
		super();
		this.codigo = codigo;
		this.nombre = nombre;
		this.semestre = semestre;
		this.ncreditos = ncreditos;
		this.horasDeLaMateria = horasDeLaMateria;
		this.correlativa = correlativa;
	}

	public long getCodigo() {
		return codigo;
	}

	public void setCodigo(long codigo) {
		this.codigo = codigo;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public int getSemestre() {
		return semestre;
	}

	public void setSemestre(int semestre) {
		this.semestre = semestre;
	}

	public int getNcreditos() {
		return ncreditos;
	}

	public void setNcreditos(int ncreditos) {
		this.ncreditos = ncreditos;
	}

	public int getHorasDeLaMateria() {
		return horasDeLaMateria;
	}

	public void setHorasDeLaMateria(int horasDeLaMateria) {
		this.horasDeLaMateria = horasDeLaMateria;
	}

	public boolean isCorrelativa() {
		return correlativa;
	}

	public void setCorrelativa(boolean correlativa) {
		this.correlativa = correlativa;
	}

	@Override
	public String toString() {
		return "MateriaDTO [" + "codigo=" + codigo + ", nombre=" + nombre + ", semestre=" + semestre
			+ ", ncreditos=" + ncreditos + ", horasDeLaMateria=" + horasDeLaMateria + ", correlativa="
			+ correlativa + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(codigo, correlativa, horasDeLaMateria, ncreditos, nombre, semestre);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		MateriaDTO other = (MateriaDTO) obj;
		return codigo == other.codigo && Objects.equals(nombre, other.nombre)
				&& semestre == other.semestre && ncreditos == other.ncreditos
				&& horasDeLaMateria == other.horasDeLaMateria && correlativa == other.correlativa;
	}

}