package cbtis239.model;

public class PagoHist {
    private final String nombreCompleto;
    private final double monto;
    private final String periodo;
    private final Integer idPago;      // por si lo necesitas (detalles, recibo, etc.)
    private final String matricula;    // null si es aspirante
    private final Integer folio;       // null si es alumno

    public PagoHist(String nombreCompleto, double monto, String periodo,
                    Integer idPago, String matricula, Integer folio) {
        this.nombreCompleto = nombreCompleto;
        this.monto = monto;
        this.periodo = periodo;
        this.idPago = idPago;
        this.matricula = matricula;
        this.folio = folio;
    }

    public String getNombreCompleto() { return nombreCompleto; }
    public double getMonto() { return monto; }
    public String getPeriodo() { return periodo; }
    public Integer getIdPago() { return idPago; }
    public String getMatricula() { return matricula; }
    public Integer getFolio() { return folio; }
}
