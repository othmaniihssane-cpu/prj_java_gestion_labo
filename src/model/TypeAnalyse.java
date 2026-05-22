package model;

/**
 * Represents an analysis type with default price and optional fixed result.
 */
public class TypeAnalyse {
    private int id;
    private String nom;
    private double prixDefaut;
    private String resultatFixe; // e.g. "Positif/Négatif" or "" for manual input

    public TypeAnalyse(int id, String nom, double prixDefaut, String resultatFixe) {
        this.id = id; this.nom = nom; this.prixDefaut = prixDefaut; this.resultatFixe = resultatFixe;
    }
    public TypeAnalyse(String nom, double prixDefaut, String resultatFixe) {
        this.nom = nom; this.prixDefaut = prixDefaut; this.resultatFixe = resultatFixe;
    }
    // Backward-compat
    public TypeAnalyse(int id, String nom, double prixDefaut) { this(id, nom, prixDefaut, ""); }
    public TypeAnalyse(String nom, double prixDefaut) { this(nom, prixDefaut, ""); }

    public int getId() { return id; }
    public String getNom() { return nom; }
    public double getPrixDefaut() { return prixDefaut; }
    public String getResultatFixe() { return resultatFixe != null ? resultatFixe : ""; }

    @Override
    public String toString() { return nom; }
}
