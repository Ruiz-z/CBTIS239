package cbtis239.bo;

import cbtis239.dao.AspiranteDAO;
import cbtis239.dao.PagoDAO;
import cbtis239.util.DB;

import java.sql.Connection;

public class InscripcionBO {

    private final PagoDAO pagoDAO = new PagoDAO();
    private final AspiranteDAO aspiranteDAO = new AspiranteDAO();

    /** Elimina pagos del aspirante y luego elimina el aspirante (en transacción). */
    public void eliminarPagosYAspirante(int folioAspirante) throws Exception {
        try (Connection cn = DB.get()) {
            cn.setAutoCommit(false);
            try {
                pagoDAO.deleteByAspiranteFolio(cn, folioAspirante);   // hijos
                int rows = aspiranteDAO.deleteByFolio(cn, folioAspirante); // padre
                if (rows == 0) throw new Exception("No existe aspirante con folio " + folioAspirante);
                cn.commit();
            } catch (Exception ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }
}
