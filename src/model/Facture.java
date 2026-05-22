package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Facture {
    private String patient;
    private String cin;
    private LocalDate date;
    private List<Analyse> analyses;
    
    public Facture(String patient, String cin, LocalDate date) {
        this.patient = patient;
        this.cin = cin;
        this.date = date;
        this.analyses = new ArrayList<>();
    }

    public void addAnalyse(Analyse a) {
        this.analyses.add(a);
    }

    public String getPatient() { return patient; }
    public String getCin() { return cin; }
    public LocalDate getDate() { return date; }
    public List<Analyse> getAnalyses() { return analyses; }

    public int getNbAnalyses() {
        return analyses.size();
    }

    public double getTotal() {
        return analyses.stream()
            .filter(a -> !"Annulé".equals(a.getStatut()))
            .mapToDouble(Analyse::getPrix).sum();
    }

    public boolean isPaye() {
        if (analyses.isEmpty()) return false;
        boolean hasValid = false;
        for (Analyse a : analyses) {
            if ("Annulé".equals(a.getStatut())) continue;
            hasValid = true;
            if (!"Payé".equals(a.getPaiementStatut())) return false;
        }
        return hasValid;
    }

    public boolean isAnnule() {
        if (analyses.isEmpty()) return false;
        for (Analyse a : analyses) {
            if (!"Annulé".equals(a.getStatut())) return false;
        }
        return true;
    }
}
