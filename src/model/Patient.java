package model;

/**
 * Represents a patient in the lab system.
 */
public class Patient {
    private int id;
    private String nom;
    private String cin;
    private String telephone;

    public Patient(int id, String nom, String cin, String telephone) {
        this.id = id; this.nom = nom; this.cin = cin; this.telephone = telephone;
    }

    public Patient(String nom, String cin, String telephone) {
        this.nom = nom; this.cin = cin; this.telephone = telephone;
    }

    // Backward-compat
    public Patient(int id, String nom, String cin) {
        this(id, nom, cin, "");
    }
    public Patient(String nom, String cin) {
        this(nom, cin, "");
    }

    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getCin() { return cin; }
    public String getTelephone() { return telephone; }

    @Override
    public String toString() {
        if (cin != null && !cin.isEmpty()) return nom + " — " + cin;
        return nom;
    }
}
