package co.edu.unbosque.sdgh.dto;

import java.util.Objects;

/**
 * DTO que transporta los datos de una materia del plan de estudios.
 * <p>
 * Una materia se ubica en un semestre (1 a 9), tiene un número de créditos y
 * una intensidad horaria semanal. Cada sesión de clase dura dos horas, por lo
 * que el número de sesiones semanales se deriva de las horas de la materia.
 * </p>
 *
 * @author Equipo de desarrollo SDGH
 * @version 1.0
 * @since 1.0
 * @see GrupoDTO
 * @see SesionDTO
 */
public class MateriaDTO {

	/**
	 * Identificador interno único de la materia en la base de datos.
	 */
	private long id;

	/**
	 * Código institucional de la materia.
	 */
	private long codigo;

	/**
	 * Nombre de la materia.
	 */
	private String nombre;

	/**
	 * Semestre del plan de estudios en el que se ubica la materia (1 a 9).
	 */
	private int semestre;

	/**
	 * Número de créditos académicos de la materia.
	 */
	private int ncreditos;

	/**
	 * Horas semanales de la materia.
	 */
	private int horasDeLaMateria;

	/**
	 * Indica si la materia es correlativa.
	 */
	private boolean correlativa;

	/**
	 * Constructor vacío requerido para la deserialización y el mapeo de datos.
	 */
	public MateriaDTO() {
	}

	/**
	 * Construye una materia con los datos necesarios para su creación.
	 *
	 * @param codigo           código institucional de la materia
	 * @param nombre           nombre de la materia
	 * @param semestre         semestre en el que se ubica (1 a 9)
	 * @param ncreditos        número de créditos académicos
	 * @param horasDeLaMateria horas semanales de la materia
	 * @param correlativa      {@code true} si la materia es correlativa
	 */
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
	 * Representación en texto de la materia con todos sus atributos.
	 *
	 * @return cadena descriptiva de la materia
	 */
	@Override
	public String toString() {
		return "MateriaDTO [id=" + id + ", codigo=" + codigo + ", nombre=" + nombre + ", semestre=" + semestre
				+ ", ncreditos=" + ncreditos + ", horasDeLaMateria=" + horasDeLaMateria + ", correlativa=" + correlativa
				+ "]";
	}

	/**
	 * Calcula el código hash a partir de todos los atributos de la materia.
	 *
	 * @return código hash del objeto
	 */
	@Override
	public int hashCode() {
		return Objects.hash(Long.valueOf(codigo), Boolean.valueOf(correlativa), Integer.valueOf(horasDeLaMateria),
				Long.valueOf(id), Integer.valueOf(ncreditos), nombre, Integer.valueOf(semestre));
	}

	/**
	 * Compara esta materia con otro objeto atributo por atributo.
	 *
	 * @param obj objeto a comparar
	 * @return {@code true} si ambos son {@link MateriaDTO} con los mismos valores
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MateriaDTO other = (MateriaDTO) obj;
		return codigo == other.codigo && correlativa == other.correlativa && horasDeLaMateria == other.horasDeLaMateria
				&& id == other.id && ncreditos == other.ncreditos && Objects.equals(nombre, other.nombre)
				&& semestre == other.semestre;
	}

	/**
	 * Calcula el número de sesiones semanales que requiere la materia, dado que
	 * cada sesión dura dos horas.
	 *
	 * @return horas semanales de la materia divididas entre dos
	 */
	public int getSesionesSemanales() {
		return horasDeLaMateria / 2;
	}

}