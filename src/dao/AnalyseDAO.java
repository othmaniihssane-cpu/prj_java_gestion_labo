package dao;

import model.Analyse;
import util.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class AnalyseDAO {

    /**
     * Ensure new columns exist (statut, paye, cin).
     * Called once at startup so old databases are migrated automatically.
     */
    public static void ensureSchema() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            ResultSet rs = conn.getMetaData().getColumns(null, null, "analyse", "statut");
            if (!rs.next()) {
                stmt.executeUpdate("ALTER TABLE analyse ADD COLUMN statut VARCHAR(20) NOT NULL DEFAULT 'En cours'");
                System.out.println("Column 'statut' added.");
            }
            rs.close();

            rs = conn.getMetaData().getColumns(null, null, "analyse", "paye");
            if (!rs.next()) {
                stmt.executeUpdate("ALTER TABLE analyse ADD COLUMN paye VARCHAR(20) NOT NULL DEFAULT 'Non payé'");
            } else {
                // If it already exists, ensure it's a VARCHAR (safe for legacy databases)
                stmt.executeUpdate("ALTER TABLE analyse MODIFY paye VARCHAR(20) NOT NULL DEFAULT 'Non payé'");
            }
            rs.close();

            rs = conn.getMetaData().getColumns(null, null, "analyse", "cin");
            if (!rs.next()) {
                stmt.executeUpdate("ALTER TABLE analyse ADD COLUMN cin VARCHAR(30) DEFAULT ''");
                System.out.println("Column 'cin' added.");
            }
            rs.close();

        } catch (SQLException e) {
            System.out.println("Schema migration error: " + e.getMessage());
        }
    }

    public static void addAnalyse(Analyse analyse) {
        String sql = "INSERT INTO analyse (patient, cin, type_analyse, date_analyse, resultat, prix, statut, paye) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, analyse.getPatient());
            ps.setString(2, analyse.getCin());
            ps.setString(3, analyse.getTypeAnalyse());
            ps.setDate(4, Date.valueOf(analyse.getDateAnalyse()));
            ps.setString(5, analyse.getResultat());
            ps.setDouble(6, analyse.getPrix());
            ps.setString(7, analyse.getStatut());
            ps.setString(8, analyse.getPaiementStatut());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur SQL addAnalyse: " + e.getMessage());
        }
    }

    public static ObservableList<Analyse> getAllAnalyses() {
        ObservableList<Analyse> list = FXCollections.observableArrayList();
        String sql = "SELECT * FROM analyse";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("Erreur SQL getAllAnalyses: " + e.getMessage());
        }
        return list;
    }

    public static void updateAnalyse(Analyse a) {
        String sql = "UPDATE analyse SET patient=?, cin=?, type_analyse=?, date_analyse=?, resultat=?, prix=?, statut=?, paye=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, a.getPatient());
            ps.setString(2, a.getCin());
            ps.setString(3, a.getTypeAnalyse());
            ps.setDate(4, Date.valueOf(a.getDateAnalyse()));
            ps.setString(5, a.getResultat());
            ps.setDouble(6, a.getPrix());
            ps.setString(7, a.getStatut());
            ps.setString(8, a.getPaiementStatut());
            ps.setInt(9, a.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur SQL updateAnalyse: " + e.getMessage());
        }
    }

    public static void updateStatut(int id, String statut, String resultat) {
        String sql = "UPDATE analyse SET statut=?, resultat=? ";
        if ("Annulé".equals(statut)) sql += ", paye='Annulé' ";
        sql += "WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, statut);
            ps.setString(2, resultat);
            ps.setInt(3, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur SQL updateStatut: " + e.getMessage());
        }
    }

    public static void updatePaye(int id, String paiementStatut) {
        String sql = "UPDATE analyse SET paye=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, paiementStatut);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur SQL updatePaye: " + e.getMessage());
        }
    }

    public static void deleteAnalyse(int id) {
        String sql = "DELETE FROM analyse WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur SQL deleteAnalyse: " + e.getMessage());
        }
    }

    /** Read one row safely, with fallback for missing columns */
    private static Analyse mapRow(ResultSet rs) throws SQLException {
        String statut = "En cours";
        String payeRaw = "Non payé";
        String cin = "";
        try { statut = rs.getString("statut"); } catch (SQLException ignored) {}
        try { payeRaw = rs.getString("paye"); } catch (SQLException ignored) {}
        try { cin = rs.getString("cin"); } catch (SQLException ignored) {}
        
        // Handle legacy booleans from previous DB version
        if (payeRaw == null || payeRaw.equals("0") || payeRaw.equalsIgnoreCase("false")) payeRaw = "Non payé";
        else if (payeRaw.equals("1") || payeRaw.equalsIgnoreCase("true")) payeRaw = "Payé";

        // Heal corrupted DB state
        if ("Annulé".equals(statut)) {
            payeRaw = "Annulé";
        }


        return new Analyse(
            rs.getInt("id"),
            rs.getString("patient"),
            cin != null ? cin : "",
            rs.getString("type_analyse"),
            rs.getDate("date_analyse").toLocalDate(),
            rs.getString("resultat"),
            rs.getDouble("prix"),
            statut != null ? statut : "En cours",
            payeRaw
        );
    }

    public static void generateTestDataIfNeeded() {
        if (!getAllAnalyses().isEmpty()) return;

        System.out.println("Génération de données de test...");
        String[] patients = {"Ahmed Ben Ali", "Sarah Mansouri", "Mohamed Tounsi", "Youssef Filali", "Fatima Zahra", "Karim Alaoui", "Ihssane Otmani", "Nabil Drissi", "Imane Chafik", "Rachid Rami"};
        String[] cins     = {"AB123456", "CD789012", "EF345678", "GH901234", "IJ567890", "KL123098", "MN456321", "OP789654", "QR012987", "ST345210"};
        String[] types = {"NFS", "Glycémie", "Bilan", "Cholestérol", "Triglycérides", "Acide Urique", "TSH", "Vitamine D"};
        String[] statuts = {"En cours", "Terminé", "Terminé", "En cours", "Annulé", "Terminé", "Terminé"};
        String[] resultatsTermine = {"Normal", "Élevé", "1.10 g/L", "Bas", "Normal", "0.85 g/L"};

        java.time.LocalDate today = java.time.LocalDate.now();
        java.util.Random rand = new java.util.Random();

        for (int i = 0; i < 45; i++) {
            int idx = rand.nextInt(patients.length);
            String p = patients[idx];
            String cin = cins[idx];
            String t = types[rand.nextInt(types.length)];
            java.time.LocalDate date = today.minusDays(rand.nextInt(10));
            String statut = statuts[rand.nextInt(statuts.length)];
            String resultat = "";
            if ("Terminé".equals(statut)) resultat = resultatsTermine[rand.nextInt(resultatsTermine.length)];
            double prix = 50.0 + (rand.nextInt(15) * 10);
            String paiementStatut = "Non payé";
            if ("Terminé".equals(statut) && rand.nextBoolean()) paiementStatut = "Payé";
            else if ("Annulé".equals(statut)) paiementStatut = "Annulé";

            addAnalyse(new Analyse(p, cin, t, date, resultat, prix, statut, paiementStatut));
        }
        System.out.println("Données de test ajoutées.");
    }
}
