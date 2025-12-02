package cbtis239.model;

import javafx.beans.property.*;

public class KardexFila {

    private StringProperty plantel        = new SimpleStringProperty();
    private StringProperty tipoUac       = new SimpleStringProperty();
    private StringProperty claveUac      = new SimpleStringProperty();
    private IntegerProperty semestre     = new SimpleIntegerProperty();
    private StringProperty nombreUac     = new SimpleStringProperty();
    private DoubleProperty calificacion  = new SimpleDoubleProperty();
    private IntegerProperty creditos     = new SimpleIntegerProperty();
    private StringProperty periodoEscolar = new SimpleStringProperty();

    public String getPlantel() { return plantel.get(); }
    public void setPlantel(String v) { plantel.set(v); }
    public StringProperty plantelProperty() { return plantel; }

    public String getTipoUac() { return tipoUac.get(); }
    public void setTipoUac(String v) { tipoUac.set(v); }
    public StringProperty tipoUacProperty() { return tipoUac; }

    public String getClaveUac() { return claveUac.get(); }
    public void setClaveUac(String v) { claveUac.set(v); }
    public StringProperty claveUacProperty() { return claveUac; }

    public int getSemestre() { return semestre.get(); }
    public void setSemestre(int v) { semestre.set(v); }
    public IntegerProperty semestreProperty() { return semestre; }

    public String getNombreUac() { return nombreUac.get(); }
    public void setNombreUac(String v) { nombreUac.set(v); }
    public StringProperty nombreUacProperty() { return nombreUac; }

    public double getCalificacion() { return calificacion.get(); }
    public void setCalificacion(double v) { calificacion.set(v); }
    public DoubleProperty calificacionProperty() { return calificacion; }

    public int getCreditos() { return creditos.get(); }
    public void setCreditos(int v) { creditos.set(v); }
    public IntegerProperty creditosProperty() { return creditos; }

    public String getPeriodoEscolar() { return periodoEscolar.get(); }
    public void setPeriodoEscolar(String v) { periodoEscolar.set(v); }
    public StringProperty periodoEscolarProperty() { return periodoEscolar; }
}
