package cbtis239.dao;

import cbtis239.model.Catalogo;
import cbtis239.util.DB;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CatalogoDAO {
    public List<Catalogo> edoCivil() throws SQLException {
        return fetch("SELECT idEdoCivil, Nombre FROM EdoCivil ORDER BY Nombre");
    }

    public List<Catalogo> generos() throws SQLException {
        return fetch("SELECT idGenero, Nombre FROM generos ORDER BY Nombre");
    }

    public List<Catalogo> periodos() throws SQLException {
        return fetch("SELECT idPeriodo, Nombre FROM Periodo ORDER BY idPeriodo DESC");
    }

    public List<Catalogo> especialidades() throws SQLException {
        return fetch("SELECT Clave, Nombre FROM Especialidad ORDER BY Nombre");
    }

    private List<Catalogo> fetch(String sql) throws SQLException {
        List<Catalogo> list = new ArrayList<>();
        try (Connection cn = DB.get();
             Statement st = cn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Catalogo(rs.getInt(1), rs.getString(2)));
            }
        }
        return list;
    }
}
