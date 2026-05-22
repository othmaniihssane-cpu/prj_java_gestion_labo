package dao;

import model.TypeAnalyse;
import util.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;

public class TypeAnalyseDAO {

    public static void ensureTable() {
        try (Connection c = DBConnection.getConnection(); Statement s = c.createStatement()) {
            s.executeUpdate("CREATE TABLE IF NOT EXISTS type_analyse ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "nom VARCHAR(100) NOT NULL UNIQUE, "
                + "prix_defaut DOUBLE NOT NULL DEFAULT 0, "
                + "resultat_fixe VARCHAR(255) DEFAULT '')");
            // Migrate
            ResultSet rs = c.getMetaData().getColumns(null, null, "type_analyse", "resultat_fixe");
            if (!rs.next()) { s.executeUpdate("ALTER TABLE type_analyse ADD COLUMN resultat_fixe VARCHAR(255) DEFAULT ''"); }
            rs.close();
        } catch (SQLException e) { System.out.println("TypeAnalyseDAO ensureTable: " + e.getMessage()); }
    }

    public static ObservableList<TypeAnalyse> getAll() {
        ObservableList<TypeAnalyse> list = FXCollections.observableArrayList();
        try (Connection c = DBConnection.getConnection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM type_analyse ORDER BY nom")) {
            while (rs.next()) {
                String rf = "";
                try { rf = rs.getString("resultat_fixe"); } catch (SQLException ignored) {}
                list.add(new TypeAnalyse(rs.getInt("id"), rs.getString("nom"), rs.getDouble("prix_defaut"), rf != null ? rf : ""));
            }
        } catch (SQLException e) { System.out.println("TypeAnalyseDAO getAll: " + e.getMessage()); }
        return list;
    }

    public static void add(TypeAnalyse t) {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("INSERT INTO type_analyse (nom, prix_defaut, resultat_fixe) VALUES (?, ?, ?)")) {
            ps.setString(1, t.getNom()); ps.setDouble(2, t.getPrixDefaut()); ps.setString(3, t.getResultatFixe());
            ps.executeUpdate();
        } catch (SQLException e) { System.out.println("TypeAnalyseDAO add: " + e.getMessage()); }
    }

    public static void update(TypeAnalyse t) {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE type_analyse SET nom=?, prix_defaut=?, resultat_fixe=? WHERE id=?")) {
            ps.setString(1, t.getNom()); ps.setDouble(2, t.getPrixDefaut());
            ps.setString(3, t.getResultatFixe()); ps.setInt(4, t.getId());
            ps.executeUpdate();
        } catch (SQLException e) { System.out.println("TypeAnalyseDAO update: " + e.getMessage()); }
    }

    public static void delete(int id) {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM type_analyse WHERE id=?")) {
            ps.setInt(1, id); ps.executeUpdate();
        } catch (SQLException e) { System.out.println("TypeAnalyseDAO delete: " + e.getMessage()); }
    }

    public static void generateIfNeeded() {
        try (Connection c = DBConnection.getConnection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM type_analyse")) {
            rs.next(); if (rs.getInt(1) > 0) return;
        } catch (SQLException e) { return; }

        Object[][] data = {
            {"NFS", 120.0, ""}, {"Glycémie", 40.0, "Normal|Élevé|Bas"},
            {"Bilan", 200.0, ""}, {"Cholestérol", 80.0, "Normal|Élevé|Bas"},
            {"Triglycérides", 70.0, "Normal|Élevé"}, {"Acide Urique", 60.0, "Normal|Élevé"},
            {"TSH", 150.0, "Normal|Hypothyroïdie|Hyperthyroïdie"},
            {"Vitamine D", 180.0, "Suffisant|Insuffisant|Carence"},
            {"Hépatite B", 90.0, "Positif|Négatif"}, {"HIV", 100.0, "Positif|Négatif"}
        };
        for (Object[] d : data) add(new TypeAnalyse((String)d[0], (Double)d[1], (String)d[2]));
        System.out.println("TypeAnalyse test data added.");
    }
}
