package cbtis239.bo;

import cbtis239.dao.PagoDAO;
import cbtis239.model.Pago;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.SQLException;
import java.util.List;

public class PagoBO {

    private final PagoDAO dao = new PagoDAO();

    // Carga pagos para mostrar en tu TableView (usa tus columnas: Nombre, Monto, Pagado)
    public ObservableList<Pago> buscarPagos(String matriculaOFolio) {
        ObservableList<Pago> datos = FXCollections.observableArrayList();
        try {
            List<Pago> lista = dao.findByMatriculaOrFolio(matriculaOFolio);
            datos.addAll(lista);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return datos;
    }

    // Registra un pago (por defecto lo marca como pagado=1); regresa el Pago listo para agregar a la tabla
    public Pago registrarPago(String matriculaOFolio, double monto, int periodoId) throws SQLException {
        int id = dao.insertPago(matriculaOFolio, monto, periodoId, 1);
        // Construimos el objeto para la UI
        boolean esNumero = matriculaOFolio != null && matriculaOFolio.matches("\\d+");
        String matricula = esNumero ? null : matriculaOFolio;
        Integer folio = esNumero ? Integer.parseInt(matriculaOFolio) : null;

        return new Pago(id, 1, monto, matricula, folio, periodoId);
    }

    public boolean marcarComoPagado(int idPago) throws SQLException {
        return dao.updateEstatus(idPago, 1);
    }

    public boolean marcarComoPendiente(int idPago) throws SQLException {
        return dao.updateEstatus(idPago, 0);
    }

    public boolean eliminarPago(int idPago) throws SQLException {
        return dao.deleteById(idPago);
    }
}