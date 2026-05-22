package dao;

import model.Patient;
import util.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;

public class PatientDAO {

    public static void ensureTable() {
        try (Connection c = DBConnection.getConnection(); Statement s = c.createStatement()) {
            s.executeUpdate("CREATE TABLE IF NOT EXISTS patient ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "nom VARCHAR(100) NOT NULL, "
                + "cin VARCHAR(30) DEFAULT '', "
                + "telephone VARCHAR(30) DEFAULT '', "
                + "date_ajout TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "UNIQUE KEY uk_cin (cin))");

            // Migrate: add telephone if missing
            ResultSet rs = c.getMetaData().getColumns(null, null, "patient", "telephone");
            if (!rs.next()) { s.executeUpdate("ALTER TABLE patient ADD COLUMN telephone VARCHAR(30) DEFAULT ''"); }
            rs.close();
            rs = c.getMetaData().getColumns(null, null, "patient", "date_ajout");
            if (!rs.next()) { s.executeUpdate("ALTER TABLE patient ADD COLUMN date_ajout TIMESTAMP DEFAULT CURRENT_TIMESTAMP"); }
            rs.close();
        } catch (SQLException e) {
            System.out.println("PatientDAO ensureTable: " + e.getMessage());
        }
    }

    public static ObservableList<Patient> getAll() {
        ObservableList<Patient> list = FXCollections.observableArrayList();
        try (Connection c = DBConnection.getConnection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM patient ORDER BY id DESC")) {
            while (rs.next()) {
                String tel = "";
                try { tel = rs.getString("telephone"); } catch (SQLException ignored) {}
                list.add(new Patient(rs.getInt("id"), rs.getString("nom"), rs.getString("cin"), tel != null ? tel : ""));
            }
        } catch (SQLException e) { System.out.println("PatientDAO getAll: " + e.getMessage()); }
        return list;
    }

    public static void add(Patient p) {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("INSERT INTO patient (nom, cin, telephone) VALUES (?, ?, ?)")) {
            ps.setString(1, p.getNom()); ps.setString(2, p.getCin()); ps.setString(3, p.getTelephone());
            ps.executeUpdate();
        } catch (SQLException e) { System.out.println("PatientDAO add: " + e.getMessage()); }
    }

    public static void update(Patient p) {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE patient SET nom=?, cin=?, telephone=? WHERE id=?")) {
            ps.setString(1, p.getNom()); ps.setString(2, p.getCin());
            ps.setString(3, p.getTelephone()); ps.setInt(4, p.getId());
            ps.executeUpdate();
        } catch (SQLException e) { System.out.println("PatientDAO update: " + e.getMessage()); }
    }

    public static void delete(int id) {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM patient WHERE id=?")) {
            ps.setInt(1, id); ps.executeUpdate();
        } catch (SQLException e) { System.out.println("PatientDAO delete: " + e.getMessage()); }
    }

    public static void generateIfNeeded() {
        try (Connection c = DBConnection.getConnection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM patient")) {
            rs.next(); if (rs.getInt(1) > 0) return;
        } catch (SQLException e) { return; }

        String[][] data = {
            {"Ahmed Ben Ali", "AB123456", "0612345678"}, {"Sarah Mansouri", "CD789012", "0698765432"},
            {"Mohamed Tounsi", "EF345678", "0654321098"}, {"Youssef Filali", "GH901234", "0667890123"},
            {"Fatima Zahra", "IJ567890", "0645678901"}, {"Karim Alaoui", "KL123098", "0623456789"},
            {"Ihssane Otmani", "MN456321", "0634567890"}, {"Nabil Drissi", "OP789654", "0656789012"},
            {"Imane Chafik", "QR012987", "0678901234"}, {"Rachid Rami", "ST345210", "0690123456"}
        };
        for (String[] d : data) add(new Patient(d[0], d[1], d[2]));
        System.out.println("Patient test data added.");
    }
}
