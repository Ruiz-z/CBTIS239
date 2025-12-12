package cbtis239.model;

import javafx.beans.property.*;
import java.time.LocalDate;

public class AsistenciaHistorial {

    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty matricula = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> fecha = new SimpleObjectProperty<>();
    private final StringProperty estado = new SimpleStringProperty();
    private final StringProperty observacion = new SimpleStringProperty();
    private final BooleanProperty justificada = new SimpleBooleanProperty();

    public AsistenciaHistorial(int id,
                               String matricula,
                               LocalDate fecha,
                               String estado,
                               String observacion) {

        this.id.set(id);
        this.matricula.set(matricula);
        this.fecha.set(fecha);
        this.estado.set(estado);
        this.observacion.set(observacion == null ? "" : observacion);

        // Si el estado es "Justificada", marcar el checkbox
        this.justificada.set("Justificada".equalsIgnoreCase(estado));

        // Cuando cambia el checkbox, ajustamos el estado en memoria
        this.justificada.addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                this.estado.set("Justificada");
            } else {
                // Si se desmarca, lo regresamos a "Falta"
                if ("Justificada".equalsIgnoreCase(this.estado.get())) {
                    this.estado.set("Falta");
                }
            }
        });
    }

    // ===== PROPERTIES (JavaFX) =====
    public IntegerProperty idProperty() { return id; }
    public StringProperty matriculaProperty() { return matricula; }
    public ObjectProperty<LocalDate> fechaProperty() { return fecha; }
    public StringProperty estadoProperty() { return estado; }
    public StringProperty observacionProperty() { return observacion; }
    public BooleanProperty justificadaProperty() { return justificada; }

    // ===== GETTERS / SETTERS =====
    public int getId() { return id.get(); }
    public String getMatricula() { return matricula.get(); }
    public LocalDate getFecha() { return fecha.get(); }
    public String getEstado() { return estado.get(); }
    public String getObservacion() { return observacion.get(); }
    public boolean isJustificada() { return justificada.get(); }

    public void setObservacion(String obs) { this.observacion.set(obs); }
}
