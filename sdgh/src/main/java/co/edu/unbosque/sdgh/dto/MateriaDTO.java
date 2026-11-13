package co.edu.unbosque.sdgh.dto;

import java.util.Objects;

public class MateriaDTO {

	private long id;

	/**
	 * Codigo institucional del curso (RF1).
	 */
	private long codigo;

	private String nombre;

	private int semestre;

	private int ncreditos;

	private int horasDeLaMateria;

	private boolean correlativa;

	public MateriaDTO() {
	}

	public MateriaDTO(long codigo, String nombre, int semestre, int ncreditos, int horasDeLaMateria,
			boolean correlativa) {
		super();
		this.codigo = codigo;
		this.nombre = nombre;
		this.semestre = semestre;
		this.ncreditos = ncreditos;
		this.horasDeLaMateria = horasDeLaMateria;
		this.correlativa = correlativa;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
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
	
	/**
	 * Cantidad de sesiones semanales del curso. Se calcula a partir de las horas
	 * de la materia, porque cada sesion dura 2 horas (RF3, RF7).
	 * @return Retorna el numero de sesiones semanales.
	 */
	public int getSesionesSemanales() {
		return horasDeLaMateria / 2;
	}

	@Override
	public String toString() {
		return "MateriaDTO [id=" + id + ", codigo=" + codigo + ", nombre=" + nombre + ", semestre=" + semestre + ", ncreditos=" + ncreditos
				+ ", horasDeLaMateria=" + horasDeLaMateria + ", correlativa=" + correlativa + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(Long.valueOf(codigo), Boolean.valueOf(correlativa), Integer.valueOf(horasDeLaMateria), Long.valueOf(id),
				Integer.valueOf(ncreditos), nombre, Integer.valueOf(semestre));
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MateriaDTO other = (MateriaDTO) obj;
		return codigo == other.codigo && correlativa == other.correlativa && horasDeLaMateria == other.horasDeLaMateria && id == other.id
				&& ncreditos == other.ncreditos && Objects.equals(nombre, other.nombre) && semestre == other.semestre;
	}

}