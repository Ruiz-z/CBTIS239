package cbtis239.bo;

import cbtis239.dao.AspiranteDAO;
import cbtis239.model.Aspirante;

import java.sql.SQLException;
import java.util.List;

public class AspiranteBO {

    private final AspiranteDAO dao = new AspiranteDAO();

    public void guardar(Aspirante a) throws SQLException {
        if (a.getFolio() == null)
            throw new IllegalArgumentException("El folio es obligatorio");
        if (a.getCurp() == null || a.getCurp().isBlank())
            throw new IllegalArgumentException("La CURP es obligatoria");
        if (a.getFechaRegistro() == null)
            throw new IllegalArgumentException("La fecha de registro es obligatoria");

        if (dao.existe(a.getFolio())) dao.update(a);
        else dao.insert(a);
    }

    public void eliminar(int folio) throws SQLException {
        dao.deleteByFolio(folio);
    }

    public Aspirante buscar(int folio) throws SQLException {
        return dao.findByFolio(folio);
    }

    public List<Aspirante> listarBreve() throws SQLException {
        return dao.listBreve();
    }
}

