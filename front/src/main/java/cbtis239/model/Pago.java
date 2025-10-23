package cbtis239.model;

import javafx.beans.property.*;

public class Pago {

    private final IntegerProperty idPago = new SimpleIntegerProperty();
    private final IntegerProperty estatus = new SimpleIntegerProperty(); // 1=pagado, 0=pendiente
    private final DoubleProperty monto = new SimpleDoubleProperty();
    private final StringProperty alumnoMatricula = new SimpleStringProperty();
    private final IntegerProperty aspiranteFolio = new SimpleIntegerProperty();
    private final IntegerProperty periodoId = new SimpleIntegerProperty();

    private final StringProperty nombre = new SimpleStringProperty(); // "Matrícula: .." o "Folio: ..."
    private final StringProperty pagado = new SimpleStringProperty(); // "Sí" / "No"

    public Pago() {}

    public Pago(int idPago, int estatus, double monto,
                String alumnoMatricula, Integer aspiranteFolio, int periodoId) {
        setIdPago(idPago);
        setEstatus(estatus);
        setMonto(monto);
        setAlumnoMatricula(alumnoMatricula);
        setAspiranteFolio(aspiranteFolio != null ? aspiranteFolio : 0);
        setPeriodoId(periodoId);

        if (alumnoMatricula != null && !alumnoMatricula.isBlank()) {
            setNombre("Matrícula: " + alumnoMatricula);
        } else {
            setNombre("Folio: " + (aspiranteFolio != null ? aspiranteFolio : "-"));
        }
        setPagado(estatus == 1 ? "Sí" : "No");
    }


    public StringProperty nombreProperty() { return nombre; }
    public DoubleProperty montoProperty() { return monto; }
    public StringProperty pagadoProperty() { return pagado; }

    public int getIdPago() { return idPago.get(); }
    public void setIdPago(int v) { idPago.set(v); }
    public IntegerProperty idPagoProperty() { return idPago; }

    public int getEstatus() { return estatus.get(); }
    public void setEstatus(int v) { estatus.set(v); setPagado(v==1 ? "Sí":"No"); }
    public IntegerProperty estatusProperty() { return estatus; }

    public double getMonto() { return monto.get(); }
    public void setMonto(double v) { monto.set(v); }

    public String getAlumnoMatricula() { return alumnoMatricula.get(); }
    public void setAlumnoMatricula(String v) { alumnoMatricula.set(v); }
    public StringProperty alumnoMatriculaProperty() { return alumnoMatricula; }

    public int getAspiranteFolio() { return aspiranteFolio.get(); }
    public void setAspiranteFolio(int v) { aspiranteFolio.set(v); }
    public IntegerProperty aspiranteFolioProperty() { return aspiranteFolio; }

    public int getPeriodoId() { return periodoId.get(); }
    public void setPeriodoId(int v) { periodoId.set(v); }
    public IntegerProperty periodoIdProperty() { return periodoId; }

    public String getNombre() { return nombre.get(); }
    public void setNombre(String v) { nombre.set(v); }

    public String getPagado() { return pagado.get(); }
    public void setPagado(String v) { pagado.set(v); }
}
