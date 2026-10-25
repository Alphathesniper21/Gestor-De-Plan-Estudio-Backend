package co.edu.unbosque.sdgh.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "profesores")
@PrimaryKeyJoinColumn(name = "usuario_codigo")
public class Profesor extends Usuario {

    private String tituloPregrado;
    private String actaGradoPregrado;
    private String tituloPosgrado;          
    private String nivelPosgrado;           
    private boolean tituloExtranjero;
    private String resolucionConvalidacion; 
    private int horasFormacionPedagogica;  
    private String maximoNivelFormacion;   

    private String tipoDocumento;          
    private String numeroDocumento;
    private String tarjetaProfesional;     
    private String rut;
    private String eps;
    private String fondoPensiones;
    private boolean tieneVisaTrabajo;      

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "profesor_especialidades", joinColumns = @JoinColumn(name = "profesor_codigo"))
    @Column(name = "especialidad")
    private List<String> especialidades;

    private int aniosExperiencia;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "profesor_publicaciones", joinColumns = @JoinColumn(name = "profesor_codigo"))
    @Column(name = "publicacion")
    private List<String> publicaciones;    

    private String nivelIngles;            
    private String examenIngles;           
    private double puntajeIngles;
    private boolean hojaVidaActualizada;

    public Profesor() {
        super();
        this.especialidades = new ArrayList<>();
        this.publicaciones = new ArrayList<>();
    }

    public Profesor(String nombre, int codigo, String correo, String facultad) {
        super(nombre, codigo, correo, facultad);
        this.especialidades = new ArrayList<>();
        this.publicaciones = new ArrayList<>();
    }

    // Getters y Setters
    public String getTituloPregrado() {
        return tituloPregrado;
    }

    public void setTituloPregrado(String tituloPregrado) {
        this.tituloPregrado = tituloPregrado;
    }

    public String getActaGradoPregrado() {
        return actaGradoPregrado;
    }

    public void setActaGradoPregrado(String actaGradoPregrado) {
        this.actaGradoPregrado = actaGradoPregrado;
    }

    public String getTituloPosgrado() {
        return tituloPosgrado;
    }

    public void setTituloPosgrado(String tituloPosgrado) {
        this.tituloPosgrado = tituloPosgrado;
    }

    public String getNivelPosgrado() {
        return nivelPosgrado;
    }

    public void setNivelPosgrado(String nivelPosgrado) {
        this.nivelPosgrado = nivelPosgrado;
    }

    public boolean isTituloExtranjero() {
        return tituloExtranjero;
    }

    public void setTituloExtranjero(boolean tituloExtranjero) {
        this.tituloExtranjero = tituloExtranjero;
    }

    public String getResolucionConvalidacion() {
        return resolucionConvalidacion;
    }

    public void setResolucionConvalidacion(String resolucionConvalidacion) {
        this.resolucionConvalidacion = resolucionConvalidacion;
    }

    public int getHorasFormacionPedagogica() {
        return horasFormacionPedagogica;
    }

    public void setHorasFormacionPedagogica(int horasFormacionPedagogica) {
        this.horasFormacionPedagogica = horasFormacionPedagogica;
    }

    public String getMaximoNivelFormacion() {
        return maximoNivelFormacion;
    }

    public void setMaximoNivelFormacion(String maximoNivelFormacion) {
        this.maximoNivelFormacion = maximoNivelFormacion;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getTarjetaProfesional() {
        return tarjetaProfesional;
    }

    public void setTarjetaProfesional(String tarjetaProfesional) {
        this.tarjetaProfesional = tarjetaProfesional;
    }

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }

    public String getEps() {
        return eps;
    }

    public void setEps(String eps) {
        this.eps = eps;
    }

    public String getFondoPensiones() {
        return fondoPensiones;
    }

    public void setFondoPensiones(String fondoPensiones) {
        this.fondoPensiones = fondoPensiones;
    }

    public boolean isTieneVisaTrabajo() {
        return tieneVisaTrabajo;
    }

    public void setTieneVisaTrabajo(boolean tieneVisaTrabajo) {
        this.tieneVisaTrabajo = tieneVisaTrabajo;
    }

    public List<String> getEspecialidades() {
        return especialidades;
    }

    public void setEspecialidades(List<String> especialidades) {
        this.especialidades = especialidades;
    }

    public int getAniosExperiencia() {
        return aniosExperiencia;
    }

    public void setAniosExperiencia(int aniosExperiencia) {
        this.aniosExperiencia = aniosExperiencia;
    }

    public List<String> getPublicaciones() {
        return publicaciones;
    }

    public void setPublicaciones(List<String> publicaciones) {
        this.publicaciones = publicaciones;
    }

    public String getNivelIngles() {
        return nivelIngles;
    }

    public void setNivelIngles(String nivelIngles) {
        this.nivelIngles = nivelIngles;
    }

    public String getExamenIngles() {
        return examenIngles;
    }

    public void setExamenIngles(String examenIngles) {
        this.examenIngles = examenIngles;
    }

    public double getPuntajeIngles() {
        return puntajeIngles;
    }

    public void setPuntajeIngles(double puntajeIngles) {
        this.puntajeIngles = puntajeIngles;
    }

    public boolean isHojaVidaActualizada() {
        return hojaVidaActualizada;
    }

    public void setHojaVidaActualizada(boolean hojaVidaActualizada) {
        this.hojaVidaActualizada = hojaVidaActualizada;
    }

    @Override
    public String toString() {
        return "Profesor [" +
                "tituloPregrado=" + tituloPregrado +
                ", tituloPosgrado=" + tituloPosgrado +
                ", nivelPosgrado=" + nivelPosgrado +
                ", especialidades=" + especialidades +
                ", aniosExperiencia=" + aniosExperiencia +
                ", nivelIngles=" + nivelIngles +
                "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), tituloPregrado, tituloPosgrado, nivelPosgrado,
                tituloExtranjero, resolucionConvalidacion, horasFormacionPedagogica,
                maximoNivelFormacion, tipoDocumento, numeroDocumento, tarjetaProfesional,
                rut, eps, fondoPensiones, tieneVisaTrabajo, especialidades, aniosExperiencia,
                publicaciones, nivelIngles, examenIngles, puntajeIngles, hojaVidaActualizada);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        Profesor other = (Profesor) obj;
        return tituloExtranjero == other.tituloExtranjero &&
                horasFormacionPedagogica == other.horasFormacionPedagogica &&
                aniosExperiencia == other.aniosExperiencia &&
                tieneVisaTrabajo == other.tieneVisaTrabajo &&
                Double.compare(other.puntajeIngles, puntajeIngles) == 0 &&
                hojaVidaActualizada == other.hojaVidaActualizada &&
                Objects.equals(tituloPregrado, other.tituloPregrado) &&
                Objects.equals(actaGradoPregrado, other.actaGradoPregrado) &&
                Objects.equals(tituloPosgrado, other.tituloPosgrado) &&
                Objects.equals(nivelPosgrado, other.nivelPosgrado) &&
                Objects.equals(resolucionConvalidacion, other.resolucionConvalidacion) &&
                Objects.equals(maximoNivelFormacion, other.maximoNivelFormacion) &&
                Objects.equals(tipoDocumento, other.tipoDocumento) &&
                Objects.equals(numeroDocumento, other.numeroDocumento) &&
                Objects.equals(tarjetaProfesional, other.tarjetaProfesional) &&
                Objects.equals(rut, other.rut) &&
                Objects.equals(eps, other.eps) &&
                Objects.equals(fondoPensiones, other.fondoPensiones) &&
                Objects.equals(especialidades, other.especialidades) &&
                Objects.equals(publicaciones, other.publicaciones) &&
                Objects.equals(nivelIngles, other.nivelIngles) &&
                Objects.equals(examenIngles, other.examenIngles);
    }
}
