package co.edu.unbosque.sdgh.entity;

import java.util.Objects;

public class Estudiante extends Usuario {

    
    private int semestreActual;
    private boolean estado;                    
    private String programaAcademico;          
    private String modalidad;                 
    private double promedioAcumulado;


    private String tipoDocumento;              
    private String numeroDocumento;
    private String resultadoSaber11;           
    private boolean tieneTituloBachiller;      
    private String diplomaOActaBachiller;      
    private String afiliacionEPS;              
    private boolean esquemaVacunacionCompleto; 


    private boolean esExtranjero;
    private boolean tieneVisaVigente;
    private String resolucionConvalidacion;   

   
    public Estudiante() {
        super();
    }

    public Estudiante(String nombre, int codigo, String correo, String facultad) {
        super(nombre, codigo, correo, facultad);
    }

    public Estudiante(String nombre, int codigo, String correo, String facultad,
                      int semestreActual, boolean estado) {
        super(nombre, codigo, correo, facultad);
        this.semestreActual = semestreActual;
        this.estado = estado;
    }

    // Getters y Setters
    public int getSemestreActual() {
        return semestreActual;
    }

    public void setSemestreActual(int semestreActual) {
        this.semestreActual = semestreActual;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public String getProgramaAcademico() {
        return programaAcademico;
    }

    public void setProgramaAcademico(String programaAcademico) {
        this.programaAcademico = programaAcademico;
    }

    public String getModalidad() {
        return modalidad;
    }

    public void setModalidad(String modalidad) {
        this.modalidad = modalidad;
    }

    public double getPromedioAcumulado() {
        return promedioAcumulado;
    }

    public void setPromedioAcumulado(double promedioAcumulado) {
        this.promedioAcumulado = promedioAcumulado;
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

    public String getResultadoSaber11() {
        return resultadoSaber11;
    }

    public void setResultadoSaber11(String resultadoSaber11) {
        this.resultadoSaber11 = resultadoSaber11;
    }

    public boolean isTieneTituloBachiller() {
        return tieneTituloBachiller;
    }

    public void setTieneTituloBachiller(boolean tieneTituloBachiller) {
        this.tieneTituloBachiller = tieneTituloBachiller;
    }

    public String getDiplomaOActaBachiller() {
        return diplomaOActaBachiller;
    }

    public void setDiplomaOActaBachiller(String diplomaOActaBachiller) {
        this.diplomaOActaBachiller = diplomaOActaBachiller;
    }

    public String getAfiliacionEPS() {
        return afiliacionEPS;
    }

    public void setAfiliacionEPS(String afiliacionEPS) {
        this.afiliacionEPS = afiliacionEPS;
    }

    public boolean isEsquemaVacunacionCompleto() {
        return esquemaVacunacionCompleto;
    }

    public void setEsquemaVacunacionCompleto(boolean esquemaVacunacionCompleto) {
        this.esquemaVacunacionCompleto = esquemaVacunacionCompleto;
    }

    public boolean isEsExtranjero() {
        return esExtranjero;
    }

    public void setEsExtranjero(boolean esExtranjero) {
        this.esExtranjero = esExtranjero;
    }

    public boolean isTieneVisaVigente() {
        return tieneVisaVigente;
    }

    public void setTieneVisaVigente(boolean tieneVisaVigente) {
        this.tieneVisaVigente = tieneVisaVigente;
    }

    public String getResolucionConvalidacion() {
        return resolucionConvalidacion;
    }

    public void setResolucionConvalidacion(String resolucionConvalidacion) {
        this.resolucionConvalidacion = resolucionConvalidacion;
    }

    @Override
    public String toString() {
        return "Estudiante [" +
                "semestreActual=" + semestreActual +
                ", estado=" + estado +
                ", programaAcademico=" + programaAcademico +
                ", promedioAcumulado=" + promedioAcumulado +
                ", tipoDocumento=" + tipoDocumento +
                ", numeroDocumento=" + numeroDocumento +
                ", afiliacionEPS=" + afiliacionEPS +
                "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), semestreActual, estado, programaAcademico,
                modalidad, promedioAcumulado, tipoDocumento, numeroDocumento,
                resultadoSaber11, tieneTituloBachiller, diplomaOActaBachiller,
                afiliacionEPS, esquemaVacunacionCompleto, esExtranjero,
                tieneVisaVigente, resolucionConvalidacion);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        Estudiante other = (Estudiante) obj;
        return semestreActual == other.semestreActual &&
                estado == other.estado &&
                Double.compare(other.promedioAcumulado, promedioAcumulado) == 0 &&
                tieneTituloBachiller == other.tieneTituloBachiller &&
                esquemaVacunacionCompleto == other.esquemaVacunacionCompleto &&
                esExtranjero == other.esExtranjero &&
                tieneVisaVigente == other.tieneVisaVigente &&
                Objects.equals(programaAcademico, other.programaAcademico) &&
                Objects.equals(modalidad, other.modalidad) &&
                Objects.equals(tipoDocumento, other.tipoDocumento) &&
                Objects.equals(numeroDocumento, other.numeroDocumento) &&
                Objects.equals(resultadoSaber11, other.resultadoSaber11) &&
                Objects.equals(diplomaOActaBachiller, other.diplomaOActaBachiller) &&
                Objects.equals(afiliacionEPS, other.afiliacionEPS) &&
                Objects.equals(resolucionConvalidacion, other.resolucionConvalidacion);
    }
}