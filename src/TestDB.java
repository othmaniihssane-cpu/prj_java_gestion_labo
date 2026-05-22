import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class TestDB {
    static class Analyse {
        int id;
        String patient;
        String typeAnalyse;
        LocalDate dateAnalyse;
        String statut;
        
        public String getTypeAnalyse() { return typeAnalyse; }
        public LocalDate getDateAnalyse() { return dateAnalyse; }
        public String getStatut() { return statut; }
    }

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/laboratoire?useSSL=false&allowPublicKeyRetrieval=true",
                "admin",
                "admin"
        )) {
            System.out.println("Connection successful!");
            List<Analyse> allAnalyses = new ArrayList<>();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM analyse")) {
                while (rs.next()) {
                    Analyse a = new Analyse();
                    a.id = rs.getInt("id");
                    a.patient = rs.getString("patient");
                    a.typeAnalyse = rs.getString("type_analyse");
                    java.sql.Date d = rs.getDate("date_analyse");
                    a.dateAnalyse = d != null ? d.toLocalDate() : null;
                    a.statut = rs.getString("statut");
                    allAnalyses.add(a);
                }
            }
            
            System.out.println("Total loaded analyses: " + allAnalyses.size());
            
            LocalDate today = LocalDate.now();
            LocalDate startOfWeek = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
            LocalDate endOfWeek = today.with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY));
            
            System.out.println("Today: " + today);
            System.out.println("Start of Week: " + startOfWeek);
            System.out.println("End of Week: " + endOfWeek);
            
            List<Analyse> filtered = allAnalyses.stream()
                .filter(a -> a.getDateAnalyse() != null && !a.getDateAnalyse().isBefore(startOfWeek) && !a.getDateAnalyse().isAfter(endOfWeek))
                .filter(a -> !"Annulé".equalsIgnoreCase(a.getStatut())).collect(Collectors.toList());
                
            System.out.println("Filtered analyses count for this week: " + filtered.size());
            for (Analyse a : filtered) {
                System.out.println(" - " + a.id + " | " + a.typeAnalyse + " | " + a.dateAnalyse + " | " + a.statut);
            }
            
            Map<String, Long> frequencies = filtered.stream()
                .map(Analyse::getTypeAnalyse)
                .filter(t -> t != null && !t.isEmpty())
                .collect(Collectors.groupingBy(t -> t, Collectors.counting()));
                
            List<String> sortedTypes = frequencies.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
                
            System.out.println("Frequencies: " + frequencies);
            
            int maxIndividualTypes = 5;
            List<String> topTypes = sortedTypes.stream().limit(maxIndividualTypes).collect(Collectors.toList());
            boolean hasOthers = sortedTypes.size() > maxIndividualTypes;
            
            List<String> displayCategories = new ArrayList<>(topTypes);
            if (hasOthers) {
                displayCategories.add("Autres");
            }
            
            String[] days = {"Lun","Mar","Mer","Jeu","Ven","Sam","Dim"};
            
            for (String type : displayCategories) {
                Map<java.time.DayOfWeek, Integer> c = new HashMap<>();
                for (java.time.DayOfWeek d : java.time.DayOfWeek.values()) c.put(d, 0);
                
                for (Analyse a : filtered) {
                    String aType = a.getTypeAnalyse();
                    if (aType == null || aType.isEmpty()) continue;
                    
                    boolean matches;
                    if ("Autres".equals(type)) {
                        matches = !topTypes.contains(aType);
                    } else {
                        matches = type.equals(aType);
                    }
                    
                    if (matches) {
                        java.time.DayOfWeek d = a.getDateAnalyse().getDayOfWeek();
                        c.put(d, c.get(d) + 1);
                    }
                }
                
                System.out.print("Series: " + type + " -> ");
                for (int i = 0; i < 7; i++) {
                    System.out.print(days[i] + "=" + c.get(java.time.DayOfWeek.of(i + 1)) + " ");
                }
                System.out.println();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
