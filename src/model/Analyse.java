package model;

import java.time.LocalDate;

public class Analyse {

    private int id;
    private String patient;
    private String cin;       // National ID card number
    private String typeAnalyse;
    private LocalDate dateAnalyse;
    private String resultat;
    private double prix;
    private String statut;   // "En cours", "Terminé", "Annulé"
    private String paiementStatut; // "Payé", "Non payé", "Annulé"

    // Full constructor (with id, for DB reads)
    public Analyse(int id, String patient, String cin, String typeAnalyse, LocalDate dateAnalyse,
                   String resultat, double prix, String statut, String paiementStatut) {
        this.id = id;
        this.patient = patient;
        this.cin = cin;
        this.typeAnalyse = typeAnalyse;
        this.dateAnalyse = dateAnalyse;
        this.resultat = resultat;
        this.prix = prix;
        this.statut = statut;
        this.paiementStatut = ("Annulé".equals(statut)) ? "Annulé" : paiementStatut;
    }

    // Constructor for new analyses (no id)
    public Analyse(String patient, String cin, String typeAnalyse, LocalDate dateAnalyse,
                   String resultat, double prix, String statut, String paiementStatut) {
        this.patient = patient;
        this.cin = cin;
        this.typeAnalyse = typeAnalyse;
        this.dateAnalyse = dateAnalyse;
        this.resultat = resultat;
        this.prix = prix;
        this.statut = statut;
        this.paiementStatut = ("Annulé".equals(statut)) ? "Annulé" : paiementStatut;
    }

    // === Getters ===
    public int getId() { return id; }
    public String getPatient() { return patient; }
    public String getCin() { return cin; }
    public String getTypeAnalyse() { return typeAnalyse; }
    public LocalDate getDateAnalyse() { return dateAnalyse; }
    public String getResultat() { return resultat; }
    public double getPrix() { return prix; }
    public String getStatut() { return statut; }
    public String getPaiementStatut() { return paiementStatut; }

    // === Setters ===
    public void setStatut(String statut) { 
        this.statut = statut; 
        if ("Annulé".equals(statut)) {
            this.paiementStatut = "Annulé";
        }
    }
    public void setPaiementStatut(String paiementStatut) { this.paiementStatut = paiementStatut; }
    public void setResultat(String resultat) { this.resultat = resultat; }
}
