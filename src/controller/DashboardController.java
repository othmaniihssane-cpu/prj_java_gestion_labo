package controller;

import dao.AnalyseDAO;
import model.Analyse;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class DashboardController {

    @FXML private Label lblTotalAnalyses, lblEnCours, lblAnnule, lblRevenue;
    @FXML private Label lblTodayCount, lblLastAnalyse1, lblLastAnalyse2, lblPaidCount, lblUnpaidCount, lblCancelledCount;
    @FXML private StackedBarChart<String, Number> weeklyBarChart;
    @FXML private PieChart typePieChart;
    @FXML private CategoryAxis barXAxis;
    @FXML private NumberAxis barYAxis;
    @FXML private TableView<Analyse> tableAnalyse;
    @FXML private TableColumn<Analyse, Integer> colId;
    @FXML private TableColumn<Analyse, String> colCin, colPatient, colType, colResultat, colStatut;
    @FXML private TableColumn<Analyse, LocalDate> colDate;
    @FXML private TableColumn<Analyse, Double> colPrix;
    @FXML private TableColumn<Analyse, String> colPaye;
    @FXML private TableColumn<Analyse, Void> colActions;
    @FXML private TextField searchField;
    @FXML private Label lblTableInfo;
    @FXML private ComboBox<String> filterStatutCombo, filterPayeCombo;

    // Navigation & panels
    @FXML private StackPane contentStack;
    @FXML private ScrollPane panelDashboard, panelPatients, panelAnalyses, panelFacturation, panelParametres;
    @FXML private Button navDashboard, navPatients, navAnalyses, navFacturation, navParametres;
    @FXML private VBox sidebarVBox;
    @FXML private Label lblLogoText;
    @FXML private Button btnPin;
    @FXML private Region iconPin;

    // Patients panel
    @FXML private TextField patientSearchField;
    @FXML private Label lblPatientInfo;
    @FXML private TableView<model.Patient> tablePatients;
    @FXML private TableColumn<model.Patient, Integer> colPatId;
    @FXML private TableColumn<model.Patient, String> colPatNom, colPatCin, colPatTel;
    @FXML private TableColumn<model.Patient, Void> colPatNbAnalyses, colPatActions;
    @FXML private VBox patientHistoryBox;
    @FXML private Label lblPatientHistoryTitle;
    @FXML private TableView<Analyse> tablePatientHistory;
    @FXML private TableColumn<Analyse, Integer> colHistId;
    @FXML private TableColumn<Analyse, String> colHistType, colHistStatut, colHistResultat;
    @FXML private TableColumn<Analyse, LocalDate> colHistDate;
    @FXML private TableColumn<Analyse, Double> colHistPrix;
    @FXML private TableColumn<Analyse, String> colHistPaye;

    // Analyses panel
    @FXML private TextField analyseSearchField;
    @FXML private ComboBox<String> analyseFilterStatut;
    @FXML private Label lblAllAnalysesInfo;
    @FXML private TableView<Analyse> tableAllAnalyses;
    @FXML private TableColumn<Analyse, Integer> colAaId;
    @FXML private TableColumn<Analyse, String> colAaCin, colAaPatient, colAaType, colAaStatut, colAaResultat;
    @FXML private TableColumn<Analyse, LocalDate> colAaDate;
    @FXML private TableColumn<Analyse, Double> colAaPrix;
    @FXML private TableColumn<Analyse, String> colAaPaye;
    @FXML private TableColumn<Analyse, Void> colAaActions;
    @FXML private TableView<model.TypeAnalyse> tableTypes;
    @FXML private TableColumn<model.TypeAnalyse, Integer> colTyId;
    @FXML private TableColumn<model.TypeAnalyse, String> colTyNom, colTyResultat;
    @FXML private TableColumn<model.TypeAnalyse, Double> colTyPrix;
    @FXML private TableColumn<model.TypeAnalyse, Void> colTyActions;

    // Facturation panel
    @FXML private Label lblFactPaye, lblFactImpaye, lblFactTotal;
    @FXML private TableView<model.Facture> tableFacturation;
    @FXML private TableColumn<model.Facture, String> colFaPatient, colFaCin;
    @FXML private TableColumn<model.Facture, LocalDate> colFaDate;
    @FXML private TableColumn<model.Facture, Integer> colFaNbAnalyses;
    @FXML private TableColumn<model.Facture, Double> colFaPrix;
    @FXML private TableColumn<model.Facture, Void> colFaStatut, colFaPaiement, colFaImpression;
    @FXML private TextField factureSearchField;
    @FXML private ComboBox<String> factureFilterPaye;

    // Paramètres panel
    @FXML private TextField paramLabName, paramLabPhone, paramLabAddress, paramLabEmail, paramDoctorName, paramSpeciality;
    @FXML private Label paramTotalPatients, paramTotalAnalyses, paramTotalTypes;
    @FXML private ComboBox<String> paramTheme;

    private ObservableList<Analyse> allAnalyses;
    private ObservableList<model.Patient> allPatientsList;
    private boolean isSidebarPinned = true;
    private Timeline sidebarTimeline = new Timeline();

    @FXML
    public void initialize() {
        setupSidebar();
        setupFilters();
        setupTable();
        setupRealTimeSearch();
        setupPatientsPanel();
        setupAnalysesPanel();
        setupFacturationPanel();
        setupThemeCombo();
        refreshAll();

        // Application du thème et chargement des paramètres dès le démarrage
        javafx.application.Platform.runLater(() -> {
            loadParametresPanel();
        });
    }

    // === NAVIGATION ===
    @FXML private void handleNavDashboard(ActionEvent ev) { switchPanel(0); }
    @FXML private void handleNavPatients(ActionEvent ev) { switchPanel(1); loadPatients(); }
    @FXML private void handleNavAnalyses(ActionEvent ev) { switchPanel(2); loadAnalysesPanel(); }
    @FXML private void handleNavFacturation(ActionEvent ev) { switchPanel(3); loadFacturationPanel(); }
    @FXML private void handleNavParametres(ActionEvent ev) { switchPanel(4); loadParametresPanel(); }

    private void setupSidebar() {
        sidebarVBox.setOnMouseEntered(e -> {
            if (!isSidebarPinned) animateSidebar(220, 1.0);
        });
        sidebarVBox.setOnMouseExited(e -> {
            if (!isSidebarPinned) animateSidebar(65, 0.0);
        });
    }

    @FXML private void handleTogglePin(ActionEvent ev) {
        isSidebarPinned = !isSidebarPinned;
        if (isSidebarPinned) {
            iconPin.getStyleClass().setAll("flat-icon", "icon-pin-white");
            animateSidebar(220, 1.0);
        } else {
            iconPin.getStyleClass().setAll("flat-icon", "icon-unpin-white");
            if (!sidebarVBox.isHover()) {
                animateSidebar(65, 0.0);
            }
        }
    }

    private void animateSidebar(double width, double opacity) {
        sidebarTimeline.stop();
        sidebarTimeline.getKeyFrames().clear();
        sidebarTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(250),
            new KeyValue(sidebarVBox.minWidthProperty(), width),
            new KeyValue(sidebarVBox.prefWidthProperty(), width),
            new KeyValue(sidebarVBox.maxWidthProperty(), width),
            new KeyValue(lblLogoText.opacityProperty(), opacity)
        ));
        sidebarTimeline.play();

        sidebarVBox.lookupAll(".nav-label").forEach(node -> {
            Timeline t = new Timeline(new KeyFrame(Duration.millis(250), new KeyValue(node.opacityProperty(), opacity)));
            t.play();
        });
    }

    private void switchPanel(int idx) {
        ScrollPane[] panels = {panelDashboard, panelPatients, panelAnalyses, panelFacturation, panelParametres};
        Button[] navs = {navDashboard, navPatients, navAnalyses, navFacturation, navParametres};
        for (int i = 0; i < panels.length; i++) {
            panels[i].setVisible(i == idx);
            navs[i].getStyleClass().setAll(i == idx ? "nav-btn-active" : "nav-btn");
        }
    }

    private void setupFilters() {
        filterStatutCombo.setItems(FXCollections.observableArrayList("Tous", "En cours", "Terminé", "Annulé"));
        filterPayeCombo.setItems(FXCollections.observableArrayList("Tous", "Payé", "Non payé"));
        filterStatutCombo.setValue("Tous");
        filterPayeCombo.setValue("Tous");
    }

    /** Real-time search: filters as user types by ID, CIN, or patient name */
    private void setupRealTimeSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            applyFiltersAndSearch();
        });
    }

    /** Apply both filter combos AND search text together */
    private void applyFiltersAndSearch() {
        if (allAnalyses == null) return;
        String keyword = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();
        String statutFilter = filterStatutCombo.getValue();
        String payeFilter = filterPayeCombo.getValue();
        LocalDate today = LocalDate.now();

        ObservableList<Analyse> result = FXCollections.observableArrayList();
        for (Analyse a : allAnalyses) {
            // Filter by today's date for Dashboard analyses
            if (a.getDateAnalyse() == null || !a.getDateAnalyse().isEqual(today)) {
                continue;
            }
            // Filter by statut
            if (statutFilter != null && !"Tous".equals(statutFilter)) {
                if (!statutFilter.equalsIgnoreCase(a.getStatut())) continue;
            }
            // Filter by paye
            if (payeFilter != null && !"Tous".equals(payeFilter)) {
                if ("Payé".equals(payeFilter) && !"Payé".equals(a.getPaiementStatut())) continue;
                if ("Non payé".equals(payeFilter) && "Payé".equals(a.getPaiementStatut())) continue;
            }
            // Search by keyword (ID, CIN, or patient name)
            if (!keyword.isEmpty()) {
                boolean matchId = String.valueOf(a.getId()).contains(keyword);
                boolean matchCin = a.getCin() != null && a.getCin().toLowerCase().contains(keyword);
                boolean matchPatient = a.getPatient() != null && a.getPatient().toLowerCase().contains(keyword);
                if (!matchId && !matchCin && !matchPatient) continue;
            }
            result.add(a);
        }
        tableAnalyse.setItems(result);
        updateTableInfo();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCin.setCellValueFactory(new PropertyValueFactory<>("cin"));
        colPatient.setCellValueFactory(new PropertyValueFactory<>("patient"));
        colType.setCellValueFactory(new PropertyValueFactory<>("typeAnalyse"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateAnalyse"));
        colResultat.setCellValueFactory(new PropertyValueFactory<>("resultat"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colPaye.setCellValueFactory(new PropertyValueFactory<>("paiementStatut"));

        colDate.setCellFactory(col -> new TableCell<>() {
            protected void updateItem(LocalDate d, boolean empty) {
                super.updateItem(d, empty);
                setText(empty || d == null ? null : d.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
            }
        });

        colStatut.setCellFactory(col -> new TableCell<>() {
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(statut);
                badge.getStyleClass().add(getStatusStyleClass(statut));
                if ("En cours".equalsIgnoreCase(statut))
                    badge.setOnMouseClicked(e -> showChangeStatusMenu(getTableRow().getItem()));
                setGraphic(badge); setText(null); setAlignment(Pos.CENTER);
            }
        });

        colResultat.setCellFactory(col -> new TableCell<>() {
            protected void updateItem(String r, boolean empty) {
                super.updateItem(r, empty);
                if (empty || r == null || r.isEmpty()) { setText("—"); setStyle("-fx-text-fill: -color-text-placeholder;"); setGraphic(null); }
                else { setText(r); setStyle("-fx-text-fill: -color-text-title;"); setGraphic(null); }
            }
        });

        colPrix.setCellFactory(col -> new TableCell<>() {
            protected void updateItem(Double p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? null : String.format("%.2f DH", p));
            }
        });

        colPaye.setCellFactory(col -> new TableCell<>() {
            protected void updateItem(String paye, boolean empty) {
                super.updateItem(paye, empty);
                if (empty || paye == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(paye);
                if("Payé".equals(paye)) badge.getStyleClass().add("badge-paye"); else if("Annulé".equals(paye)) badge.getStyleClass().add("badge-annule"); else badge.getStyleClass().add("badge-non-paye");
                badge.setOnMouseClicked(e -> {
                    Analyse a = getTableRow().getItem();
                    if (a != null) togglePaye(a);
                });
                setGraphic(badge); setText(null); setAlignment(Pos.CENTER);
            }
        });

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = new Button("Modifier");
            private final Button btnDel = new Button("Suppr.");
            private final HBox box = new HBox(6, btnEdit, btnDel);
            { box.setAlignment(Pos.CENTER);
              btnEdit.setStyle("-fx-background-color:#2563EB;-fx-text-fill:white;-fx-font-size:11;-fx-background-radius:6;-fx-padding:3 10;-fx-cursor:hand;");
              btnDel.setStyle("-fx-background-color:#DC2626;-fx-text-fill:white;-fx-font-size:11;-fx-background-radius:6;-fx-padding:3 10;-fx-cursor:hand;");
              btnEdit.setOnAction(e -> showAnalyseDialog(getTableRow().getItem(), true));
              btnDel.setOnAction(e -> handleDeleteAnalyse(getTableRow().getItem()));
            }
            protected void updateItem(Void v, boolean e) { super.updateItem(v,e); setGraphic(e?null:box); }
        });

        tableAnalyse.setRowFactory(tv -> {
            TableRow<Analyse> row = new TableRow<>();
            ContextMenu ctx = new ContextMenu();
            MenuItem edit = new MenuItem("Modifier");
            edit.setOnAction(e -> showAnalyseDialog(row.getItem(), true));
            MenuItem del = new MenuItem("Supprimer");
            del.setOnAction(e -> handleDeleteAnalyse(row.getItem()));
            MenuItem chg = new MenuItem("Changer statut");
            chg.setOnAction(e -> showChangeStatusMenu(row.getItem()));
            MenuItem pay = new MenuItem("Basculer paiement");
            pay.setOnAction(e -> togglePaye(row.getItem()));
            ctx.getItems().addAll(edit, chg, pay, new SeparatorMenuItem(), del);
            row.contextMenuProperty().bind(Bindings.when(row.emptyProperty()).then((ContextMenu)null).otherwise(ctx));
            row.setOnMouseClicked(ev -> { if (ev.getClickCount()==2 && !row.isEmpty()) showAnalyseDialog(row.getItem(), true); });
            return row;
        });

        tableAnalyse.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableAnalyse.setPlaceholder(new Label("Aucune analyse trouvée"));
    }

    private String getStatusStyleClass(String s) {
        if (s == null) return "badge-default";
        String l = s.toLowerCase().trim();
        if (l.contains("en cours")) return "badge-en-cours";
        if (l.contains("termin")) return "badge-termine";
        if (l.contains("annul")) return "badge-annule";
        return "badge-default";
    }

    private void showChangeStatusMenu(Analyse analyse) {
        if (analyse == null) return;
        List<String> choices = new ArrayList<>();
        if (!"Terminé".equalsIgnoreCase(analyse.getStatut())) choices.add("Terminé");
        if (!"Annulé".equalsIgnoreCase(analyse.getStatut())) choices.add("Annulé");
        if (!"En cours".equalsIgnoreCase(analyse.getStatut())) choices.add("En cours");
        if (choices.isEmpty()) return;

        ChoiceDialog<String> dlg = new ChoiceDialog<>(choices.get(0), choices);
        dlg.setTitle("Changer le statut");
        dlg.setHeaderText("Analyse de « " + analyse.getPatient() + " »");
        dlg.setContentText("Nouveau statut :");
        applyThemeToDialogPane(dlg.getDialogPane());

        dlg.showAndWait().ifPresent(newStatut -> {
            if ("Terminé".equalsIgnoreCase(newStatut)) {
                String res = showResultatInputDialog(analyse);
                if (res == null) return;
                analyse.setStatut("Terminé"); analyse.setResultat(res);
                AnalyseDAO.updateStatut(analyse.getId(), "Terminé", res);
            } else if ("Annulé".equalsIgnoreCase(newStatut)) {
                analyse.setStatut("Annulé"); analyse.setResultat("");
                AnalyseDAO.updateStatut(analyse.getId(), "Annulé", "");
            } else {
                analyse.setStatut("En cours"); analyse.setResultat("");
                AnalyseDAO.updateStatut(analyse.getId(), "En cours", "");
            }
            refreshAll();
        });
    }

    private String showResultatInputDialog(Analyse analyse) {
        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle("Résultat de l'analyse");
        dlg.setHeaderText("Analyse : " + analyse.getTypeAnalyse() + " — " + analyse.getPatient());
        dlg.setContentText("Résultat (obligatoire) :");
        applyThemeToDialogPane(dlg.getDialogPane());
        Button okBtn = (Button) dlg.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.setDisable(true);
        dlg.getEditor().textProperty().addListener((o, a, n) -> okBtn.setDisable(n.trim().isEmpty()));
        Optional<String> r = dlg.showAndWait();
        return (r.isPresent() && !r.get().trim().isEmpty()) ? r.get().trim() : null;
    }

    private void togglePaye(Analyse a) {
        if (a == null) return;
        if ("Annulé".equals(a.getStatut())) return;
        a.setPaiementStatut("Payé".equals(a.getPaiementStatut()) ? "Non payé" : "Payé");
        AnalyseDAO.updatePaye(a.getId(), a.getPaiementStatut());
        refreshAll();
    }

    private void refreshAll() { loadTable(); updateKPIs(); updateSummary(); populateCharts(); }

    private void loadTable() {
        allAnalyses = AnalyseDAO.getAllAnalyses();
        applyFiltersAndSearch(); // re-apply current filters/search
    }

    private void updateTableInfo() {
        int shown = tableAnalyse.getItems().size();
        LocalDate today = LocalDate.now();
        long totalToday = allAnalyses != null ? allAnalyses.stream().filter(a -> a.getDateAnalyse() != null && a.getDateAnalyse().isEqual(today)).count() : 0;
        lblTableInfo.setText(String.format("Affichage %d sur %d résultats", shown, totalToday));
    }

    private void updateKPIs() {
        if (allAnalyses == null) return;
        LocalDate today = LocalDate.now();
        List<Analyse> todayAnalyses = allAnalyses.stream()
            .filter(a -> a.getDateAnalyse() != null && a.getDateAnalyse().isEqual(today))
            .collect(Collectors.toList());

        lblTotalAnalyses.setText(String.valueOf(todayAnalyses.size()));
        lblEnCours.setText(String.valueOf(todayAnalyses.stream().filter(a -> "En cours".equalsIgnoreCase(a.getStatut())).count()));
        lblAnnule.setText(String.valueOf(todayAnalyses.stream().filter(a -> "Annulé".equalsIgnoreCase(a.getStatut())).count()));
        lblRevenue.setText(String.format("%.2f DH", todayAnalyses.stream().filter(a -> "Payé".equals(a.getPaiementStatut())).mapToDouble(Analyse::getPrix).sum()));
    }

    private void updateSummary() {
        if (allAnalyses == null || allAnalyses.isEmpty()) return;
        LocalDate today = LocalDate.now();
        lblTodayCount.setText(allAnalyses.stream().filter(a -> a.getDateAnalyse() != null && a.getDateAnalyse().isEqual(today)).count() + " analyses");

        List<Analyse> todaySorted = allAnalyses.stream()
            .filter(a -> a.getDateAnalyse() != null && a.getDateAnalyse().isEqual(today))
            .sorted((a, b) -> Integer.compare(b.getId(), a.getId()))
            .collect(Collectors.toList());
        lblLastAnalyse1.setText("—");
        lblLastAnalyse2.setText("—");
        if (todaySorted.size() > 0) { Analyse a = todaySorted.get(0); lblLastAnalyse1.setText(a.getPatient()+" | "+a.getTypeAnalyse()+" | "+a.getStatut()); }
        if (todaySorted.size() > 1) { Analyse a = todaySorted.get(1); lblLastAnalyse2.setText(a.getPatient()+" | "+a.getTypeAnalyse()+" | "+a.getStatut()); }

        List<Analyse> todayAnalyses = allAnalyses.stream()
            .filter(a -> a.getDateAnalyse() != null && a.getDateAnalyse().isEqual(today))
            .collect(Collectors.toList());

        long paid = todayAnalyses.stream().filter(a -> "Payé".equals(a.getPaiementStatut())).count();
        long cancelled = todayAnalyses.stream().filter(a -> "Annulé".equals(a.getStatut())).count();
        long unpaid = todayAnalyses.size() - paid - cancelled;
        lblPaidCount.setText(String.valueOf(paid));
        lblUnpaidCount.setText(String.valueOf(unpaid));
        lblCancelledCount.setText(String.valueOf(cancelled));
    }

    private void populateCharts() { populateBarChart(); populateTypePieChart(); }

    private void populateBarChart() {
        weeklyBarChart.getData().clear();
        String[] days = {"Lun","Mar","Mer","Jeu","Ven","Sam","Dim"};
        barXAxis.setCategories(FXCollections.observableArrayList(days));
        if (allAnalyses == null || allAnalyses.isEmpty()) {
            System.out.println("[CHART] No analyses loaded, skipping bar chart.");
            return;
        }
        
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate endOfWeek = today.with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY));
        System.out.println("[CHART] Week: " + startOfWeek + " to " + endOfWeek + " (today=" + today + ")");
        
        List<Analyse> filtered = allAnalyses.stream()
            .filter(a -> a.getDateAnalyse() != null && !a.getDateAnalyse().isBefore(startOfWeek) && !a.getDateAnalyse().isAfter(endOfWeek))
            .filter(a -> !"Annulé".equalsIgnoreCase(a.getStatut())).collect(Collectors.toList());
        System.out.println("[CHART] Filtered analyses for this week: " + filtered.size());
            
        // Count frequencies of each analysis type this week to identify the top types
        Map<String, Long> frequencies = filtered.stream()
            .map(Analyse::getTypeAnalyse)
            .filter(t -> t != null && !t.isEmpty())
            .collect(Collectors.groupingBy(t -> t, Collectors.counting()));
        System.out.println("[CHART] Type frequencies: " + frequencies);
            
        // Sort types by frequency descending
        List<String> sortedTypes = frequencies.entrySet().stream()
            .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
            
        // We will show at most the top 5 types individually, and group the rest under "Autres"
        int maxIndividualTypes = 5;
        List<String> topTypes = sortedTypes.stream().limit(maxIndividualTypes).collect(Collectors.toList());
        boolean hasOthers = sortedTypes.size() > maxIndividualTypes;
        
        // Prepare list of series categories to display
        List<String> displayCategories = new ArrayList<>(topTypes);
        if (hasOthers) {
            displayCategories.add("Autres");
        }
        System.out.println("[CHART] Display categories: " + displayCategories);
        
        for (String type : displayCategories) {
            XYChart.Series<String, Number> s = new XYChart.Series<>();
            s.setName(type);
            
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
            
            StringBuilder sb = new StringBuilder("[CHART]   Series '" + type + "': ");
            for (int i = 0; i < 7; i++) {
                int val = c.get(java.time.DayOfWeek.of(i + 1));
                s.getData().add(new XYChart.Data<>(days[i], val));
                sb.append(days[i]).append("=").append(val).append(" ");
            }
            System.out.println(sb.toString());
            
            weeklyBarChart.getData().add(s);
        }
        System.out.println("[CHART] Total series added: " + weeklyBarChart.getData().size());
        
        // Install tooltips and hover labels on bar segments after rendering
        javafx.application.Platform.runLater(() -> {
            for (XYChart.Series<String, Number> series : weeklyBarChart.getData()) {
                for (XYChart.Data<String, Number> data : series.getData()) {
                    if (data.getNode() != null) {
                        int val = data.getYValue().intValue();
                        String dayName = data.getXValue();
                        
                        // Create rich tooltip
                        Tooltip tip = new Tooltip(
                            series.getName() + "\n" +
                            dayName + "\n" +
                            "Nb d'Analyses: " + val
                        );
                        tip.setStyle(
                            "-fx-background-color: #1E293B;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 12px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 8;" +
                            "-fx-padding: 8 12;"
                        );
                        tip.setShowDelay(javafx.util.Duration.millis(100));
                        Tooltip.install(data.getNode(), tip);
                        
                        // Show count label on hover inside the bar segment
                        javafx.scene.Node node = data.getNode();
                        Label hoverLabel = new Label(String.valueOf(val));
                        hoverLabel.setStyle(
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 10px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 2, 0, 0, 1);"
                        );
                        hoverLabel.setMouseTransparent(true);
                        hoverLabel.setVisible(false);
                        
                        if (node instanceof StackPane) {
                            ((StackPane) node).getChildren().add(hoverLabel);
                            ((StackPane) node).setAlignment(Pos.CENTER);
                        }
                        
                        node.setOnMouseEntered(e -> hoverLabel.setVisible(true));
                        node.setOnMouseExited(e -> hoverLabel.setVisible(false));
                    }
                }
            }
        });
    }

    private void populateTypePieChart() {
        typePieChart.getData().clear();
        if (allAnalyses == null || allAnalyses.isEmpty()) return;
        LocalDate today = LocalDate.now();
        List<Analyse> f = allAnalyses.stream()
            .filter(a -> a.getDateAnalyse() != null && a.getDateAnalyse().isEqual(today))
            .filter(a -> !"Annulé".equalsIgnoreCase(a.getStatut())).collect(Collectors.toList());
        Map<String, Long> tc = f.stream().map(Analyse::getTypeAnalyse).filter(t -> t!=null && !t.isEmpty())
            .collect(Collectors.groupingBy(t -> t, Collectors.counting()));
        for (Map.Entry<String, Long> e : tc.entrySet())
            typePieChart.getData().add(new PieChart.Data(e.getKey()+" ("+e.getValue()+")", e.getValue()));
    }

    // === EVENT HANDLERS ===
    @FXML private void handleAddAnalyse(ActionEvent ev) { showAnalyseDialog(null, false); }
    
    @FXML private void showAdvancedSearchDialog(ActionEvent ev) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Recherche Avancée");
        dialog.setHeaderText("Rechercher des analyses par critères");
        DialogPane dp = dialog.getDialogPane();
        applyThemeToDialogPane(dp);
        dp.setMinWidth(500);

        GridPane grid = new GridPane();
        grid.setHgap(15); grid.setVgap(15); grid.setPadding(new Insets(20));

        TextField fPatient = new TextField(); fPatient.setPromptText("Nom, CIN..."); fPatient.getStyleClass().add("search-field");
        TextField fType = new TextField(); fType.setPromptText("Type d'analyse..."); fType.getStyleClass().add("search-field");
        ComboBox<String> cStatut = new ComboBox<>(FXCollections.observableArrayList("Tous", "En cours", "Terminé", "Annulé")); cStatut.setValue("Tous");
        ComboBox<String> cPaye = new ComboBox<>(FXCollections.observableArrayList("Tous", "Payé", "Non payé")); cPaye.setValue("Tous");
        DatePicker dDebut = new DatePicker(); dDebut.setPromptText("Date début");
        DatePicker dFin = new DatePicker(); dFin.setPromptText("Date fin");

        grid.add(createFieldLabel("Patient / CIN :"), 0, 0); grid.add(fPatient, 1, 0);
        grid.add(createFieldLabel("Type :"), 0, 1); grid.add(fType, 1, 1);
        grid.add(createFieldLabel("Statut :"), 0, 2); grid.add(cStatut, 1, 2);
        grid.add(createFieldLabel("Paiement :"), 0, 3); grid.add(cPaye, 1, 3);
        grid.add(createFieldLabel("Date de début :"), 0, 4); grid.add(dDebut, 1, 4);
        grid.add(createFieldLabel("Date de fin :"), 0, 5); grid.add(dFin, 1, 5);

        dp.setContent(grid);
        ButtonType btnRecherche = new ButtonType("Rechercher", ButtonBar.ButtonData.OK_DONE);
        dp.getButtonTypes().addAll(btnRecherche, ButtonType.CANCEL);
        ((Button)dp.lookupButton(btnRecherche)).setStyle("-fx-background-color:#2563EB;-fx-text-fill:white;-fx-font-weight:bold;-fx-background-radius:8;-fx-padding:8 22;");

        dialog.setResultConverter(b -> b);
        dialog.showAndWait().ifPresent(b -> {
            if (b == btnRecherche) {
                // Switch to Analyses panel and filter it
                switchPanel(2);
                if (allAnalyses == null) allAnalyses = AnalyseDAO.getAllAnalyses();
                ObservableList<Analyse> result = FXCollections.observableArrayList();
                
                String kwP = fPatient.getText().trim().toLowerCase();
                String kwT = fType.getText().trim().toLowerCase();
                String st = cStatut.getValue();
                String py = cPaye.getValue();
                LocalDate ld = dDebut.getValue();
                LocalDate lf = dFin.getValue();

                for (Analyse a : allAnalyses) {
                    if (!"Tous".equals(st) && !st.equals(a.getStatut())) continue;
                    if ("Payé".equals(py) && !"Payé".equals(a.getPaiementStatut())) continue;
                    if ("Non payé".equals(py) && "Payé".equals(a.getPaiementStatut())) continue;
                    if (ld != null && a.getDateAnalyse().isBefore(ld)) continue;
                    if (lf != null && a.getDateAnalyse().isAfter(lf)) continue;
                    if (!kwP.isEmpty() && !a.getPatient().toLowerCase().contains(kwP) && (a.getCin()==null || !a.getCin().toLowerCase().contains(kwP))) continue;
                    if (!kwT.isEmpty() && !a.getTypeAnalyse().toLowerCase().contains(kwT)) continue;
                    result.add(a);
                }
                tableAllAnalyses.setItems(result);
                lblAllAnalysesInfo.setText("Résultat recherche avancée : " + result.size() + " analyses");
            }
        });
    }

    @FXML private void handleShowAll(ActionEvent ev) {
        searchField.clear();
        filterStatutCombo.setValue("Tous");
        filterPayeCombo.setValue("Tous");
        applyFiltersAndSearch();
    }

    @FXML private void handleFilter(ActionEvent ev) { applyFiltersAndSearch(); }
    @FXML private void handleResetFilter(ActionEvent ev) { handleShowAll(null); }

    // === CRUD DIALOG ===
    private void showAnalyseDialog(Analyse existing, boolean isEdit) {
        Dialog<Analyse> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Modifier l'analyse" : "Nouvelle analyse");
        dialog.setHeaderText(isEdit ? "Modifier les détails" : "Remplir les informations");
        DialogPane dp = dialog.getDialogPane();
        applyThemeToDialogPane(dp);
        dp.setMinWidth(520);

        GridPane grid = new GridPane();
        grid.setHgap(15); grid.setVgap(14); grid.setPadding(new Insets(25, 25, 15, 25));

        // --- Load data from DB ---
        ObservableList<model.Patient> allPatientObjs = dao.PatientDAO.getAll();
        ObservableList<model.TypeAnalyse> allTypeObjs = dao.TypeAnalyseDAO.getAll();

        // Build display strings and lookup maps
        ObservableList<String> patientStrings = FXCollections.observableArrayList();
        Map<String, model.Patient> patientMap = new LinkedHashMap<>();
        for (model.Patient p : allPatientObjs) {
            String display = p.getNom() + " — " + p.getCin();
            patientStrings.add(display);
            patientMap.put(display, p);
        }

        ObservableList<String> typeStrings = FXCollections.observableArrayList();
        Map<String, model.TypeAnalyse> typeMap = new LinkedHashMap<>();
        for (model.TypeAnalyse t : allTypeObjs) {
            typeStrings.add(t.getNom());
            typeMap.put(t.getNom(), t);
        }

        // --- Patient ComboBox (String-based, searchable) ---
        ComboBox<String> patientCombo = new ComboBox<>(patientStrings);
        patientCombo.setEditable(true);
        patientCombo.setPromptText("Chercher par nom ou CIN...");
        patientCombo.setPrefWidth(300);
        patientCombo.getStyleClass().add("filter-combo");

        Label cinDisplay = new Label("");
        cinDisplay.setStyle("-fx-text-fill:#2563EB; -fx-font-weight:bold; -fx-font-size:13px;");

        final boolean[] patientUpdating = {false};

        patientCombo.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            if (patientUpdating[0]) return;
            if (newVal == null || newVal.isEmpty()) {
                javafx.application.Platform.runLater(() -> { patientUpdating[0] = true; patientCombo.setItems(patientStrings); patientCombo.show(); patientUpdating[0] = false; });
                cinDisplay.setText("");
                return;
            }
            String kw = newVal.toLowerCase();
            ObservableList<String> filtered = FXCollections.observableArrayList();
            for (String s : patientStrings) {
                if (s.toLowerCase().contains(kw)) filtered.add(s);
            }
            javafx.application.Platform.runLater(() -> { patientUpdating[0] = true; patientCombo.setItems(filtered); if (!filtered.isEmpty()) patientCombo.show(); patientUpdating[0] = false; });
        });

        patientCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && patientMap.containsKey(newVal)) {
                model.Patient p = patientMap.get(newVal);
                cinDisplay.setText("CIN: " + p.getCin());
            }
        });

        // --- Type ComboBox (String-based, searchable) ---
        ComboBox<String> typeCombo = new ComboBox<>(typeStrings);
        typeCombo.setEditable(true);
        typeCombo.setPromptText("Chercher un type d'analyse...");
        typeCombo.setPrefWidth(300);
        typeCombo.getStyleClass().add("filter-combo");

        TextField prixField = new TextField();
        prixField.setPromptText("Prix en DH");
        prixField.getStyleClass().add("search-field");

        final boolean[] typeUpdating = {false};

        typeCombo.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            if (typeUpdating[0]) return;
            if (newVal == null || newVal.isEmpty()) {
                javafx.application.Platform.runLater(() -> { typeUpdating[0] = true; typeCombo.setItems(typeStrings); typeCombo.show(); typeUpdating[0] = false; });
                return;
            }
            String kw = newVal.toLowerCase();
            ObservableList<String> filtered = FXCollections.observableArrayList();
            for (String s : typeStrings) {
                if (s.toLowerCase().contains(kw)) filtered.add(s);
            }
            javafx.application.Platform.runLater(() -> { typeUpdating[0] = true; typeCombo.setItems(filtered); if (!filtered.isEmpty()) typeCombo.show(); typeUpdating[0] = false; });
        });

        typeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && typeMap.containsKey(newVal)) {
                prixField.setText(String.valueOf(typeMap.get(newVal).getPrixDefaut()));
            }
        });

        DatePicker datePicker = new DatePicker(LocalDate.now());

        ComboBox<String> statutCombo = new ComboBox<>(FXCollections.observableArrayList("En cours", "Terminé", "Annulé"));
        statutCombo.setValue("En cours"); statutCombo.getStyleClass().add("filter-combo"); statutCombo.setPrefWidth(300);

        TextField resultatField = new TextField(); resultatField.setPromptText("Résultat de l'analyse"); resultatField.getStyleClass().add("search-field");
        Label resultatLabel = createFieldLabel("Résultat :");
        resultatField.setVisible(false); resultatField.setManaged(false);
        resultatLabel.setVisible(false); resultatLabel.setManaged(false);

        statutCombo.valueProperty().addListener((o, a, n) -> {
            boolean show = "Terminé".equals(n);
            resultatField.setVisible(show); resultatField.setManaged(show);
            resultatLabel.setVisible(show); resultatLabel.setManaged(show);
        });

        // Pre-fill for edit mode
        if (existing != null) {
            // Find matching patient display string
            for (Map.Entry<String, model.Patient> entry : patientMap.entrySet()) {
                if (entry.getValue().getNom().equals(existing.getPatient())) {
                    patientCombo.setValue(entry.getKey());
                    break;
                }
            }
            if (patientCombo.getValue() == null) patientCombo.getEditor().setText(existing.getPatient());
            cinDisplay.setText("CIN: " + existing.getCin());
            typeCombo.setValue(existing.getTypeAnalyse());
            datePicker.setValue(existing.getDateAnalyse());
            prixField.setText(String.valueOf(existing.getPrix()));
            statutCombo.setValue(existing.getStatut());
            resultatField.setText(existing.getResultat());
        }

        // Layout
        grid.add(createFieldLabel("Patient :"), 0, 0);        grid.add(patientCombo, 1, 0);
        grid.add(createFieldLabel(""), 0, 1);                  grid.add(cinDisplay, 1, 1);
        grid.add(createFieldLabel("Type d'analyse :"), 0, 2);  grid.add(typeCombo, 1, 2);
        grid.add(createFieldLabel("Date :"), 0, 3);            grid.add(datePicker, 1, 3);
        grid.add(createFieldLabel("Prix (DH) :"), 0, 4);       grid.add(prixField, 1, 4);
        grid.add(createFieldLabel("Statut :"), 0, 5);          grid.add(statutCombo, 1, 5);
        grid.add(resultatLabel, 0, 6);                          grid.add(resultatField, 1, 6);

        dp.setContent(grid);
        ButtonType saveType = new ButtonType(isEdit ? "Enregistrer" : "Ajouter", ButtonBar.ButtonData.OK_DONE);
        dp.getButtonTypes().addAll(saveType, ButtonType.CANCEL);
        ((Button) dp.lookupButton(saveType)).setStyle("-fx-background-color:#2563EB;-fx-text-fill:white;-fx-font-weight:bold;-fx-background-radius:8;-fx-padding:8 22;");

        dialog.setResultConverter(bt -> {
            if (bt == saveType) {
                try {
                    // Resolve patient
                    String patientName, cin;
                    String selectedPatientStr = patientCombo.getValue();
                    if (selectedPatientStr == null) selectedPatientStr = patientCombo.getEditor().getText().trim();
                    if (patientMap.containsKey(selectedPatientStr)) {
                        model.Patient p = patientMap.get(selectedPatientStr);
                        patientName = p.getNom(); cin = p.getCin();
                    } else {
                        patientName = selectedPatientStr; cin = "";
                    }

                    // Resolve type
                    String typeName = typeCombo.getValue();
                    if (typeName == null) typeName = typeCombo.getEditor().getText().trim();

                    String statut = statutCombo.getValue();
                    String resultat = "Terminé".equals(statut) ? resultatField.getText().trim() : "";
                    LocalDate date = datePicker.getValue();
                    double prix = Double.parseDouble(prixField.getText().trim());

                    if (patientName.isEmpty() || typeName.isEmpty() || date == null) { showAlert("Erreur", "Patient, type et date sont obligatoires."); return null; }
                    if (prix < 0) { showAlert("Erreur", "Le prix ne peut pas être négatif."); return null; }
                    if ("Terminé".equals(statut) && resultat.isEmpty()) { showAlert("Erreur", "Le résultat est obligatoire pour Terminé."); return null; }

                    if (existing != null) return new Analyse(existing.getId(), patientName, cin, typeName, date, resultat, prix, statut, existing.getPaiementStatut());
                    else return new Analyse(patientName, cin, typeName, date, resultat, prix, statut, "Non payé");
                } catch (NumberFormatException e) { showAlert("Erreur", "Prix invalide."); return null; }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(a -> { if (isEdit) AnalyseDAO.updateAnalyse(a); else AnalyseDAO.addAnalyse(a); refreshAll(); });
    }

    private void handleDeleteAnalyse(Analyse a) {
        if (a == null) return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation"); alert.setHeaderText("Supprimer l'analyse");
        alert.setContentText("Supprimer l'analyse de « " + a.getPatient() + " » ?");
        alert.showAndWait().ifPresent(r -> { if (r == ButtonType.OK) { AnalyseDAO.deleteAnalyse(a.getId()); refreshAll(); } });
    }

    private Label createFieldLabel(String text) {
        Label l = new Label(text); l.setStyle("-fx-text-fill: -color-text-normal;-fx-font-weight:bold;-fx-font-size:13px;"); l.setMinWidth(130); return l;
    }

    private void showAlert(String title, String content) {
        Alert a = new Alert(Alert.AlertType.WARNING); a.setTitle(title); a.setHeaderText(null); a.setContentText(content); a.showAndWait();
    }

    // ================================================================
    //  PATIENTS PANEL
    // ================================================================
    private void setupPatientsPanel() {
        colPatId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPatNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPatCin.setCellValueFactory(new PropertyValueFactory<>("cin"));
        colPatTel.setCellValueFactory(new PropertyValueFactory<>("telephone"));

        // Nb Analyses column (computed)
        colPatNbAnalyses.setCellFactory(col -> new TableCell<>() {
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) { setText(null); return; }
                model.Patient p = getTableRow().getItem();
                long count = allAnalyses == null ? 0 : allAnalyses.stream()
                    .filter(a -> p.getNom().equals(a.getPatient()) || (p.getCin() != null && p.getCin().equals(a.getCin())))
                    .count();
                setText(String.valueOf(count));
                setAlignment(Pos.CENTER);
            }
        });

        // Actions column (View History + Edit + Delete)
        colPatActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnView = new Button("Historique");
            private final Button btnEdit = new Button("Modifier");
            private final Button btnDel = new Button("Suppr.");
            private final HBox box = new HBox(6, btnView, btnEdit, btnDel);
            { box.setAlignment(Pos.CENTER);
              btnView.setStyle("-fx-background-color:#2563EB;-fx-text-fill:white;-fx-font-size:11;-fx-background-radius:6;-fx-padding:3 10;-fx-cursor:hand;");
              btnEdit.setStyle("-fx-background-color:#059669;-fx-text-fill:white;-fx-font-size:11;-fx-background-radius:6;-fx-padding:3 10;-fx-cursor:hand;");
              btnDel.setStyle("-fx-background-color:#DC2626;-fx-text-fill:white;-fx-font-size:11;-fx-background-radius:6;-fx-padding:3 10;-fx-cursor:hand;");
              btnView.setOnAction(e -> showPatientHistory(getTableRow().getItem()));
              btnEdit.setOnAction(e -> showPatientDialog(getTableRow().getItem()));
              btnDel.setOnAction(e -> handleDeletePatient(getTableRow().getItem()));
            }
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
            }
        });

        tablePatients.setRowFactory(tv -> {
            TableRow<model.Patient> row = new TableRow<>();
            row.setOnMouseClicked(ev -> { if (ev.getClickCount() == 2 && !row.isEmpty()) showPatientDialog(row.getItem()); });
            return row;
        });

        tablePatients.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablePatients.setPlaceholder(new Label("Aucun patient trouvé"));

        // Setup history table
        colHistId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colHistType.setCellValueFactory(new PropertyValueFactory<>("typeAnalyse"));
        colHistDate.setCellValueFactory(new PropertyValueFactory<>("dateAnalyse"));
        colHistStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colHistResultat.setCellValueFactory(new PropertyValueFactory<>("resultat"));
        colHistPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        colHistPaye.setCellValueFactory(new PropertyValueFactory<>("paiementStatut"));
        colHistDate.setCellFactory(c -> new TableCell<>() {
            protected void updateItem(LocalDate d, boolean empty) { super.updateItem(d, empty); setText(empty||d==null?null:d.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))); }
        });
        colHistPrix.setCellFactory(c -> new TableCell<>() {
            protected void updateItem(Double p, boolean empty) { super.updateItem(p, empty); setText(empty||p==null?null:String.format("%.2f DH",p)); }
        });
        colHistPaye.setCellFactory(c -> new TableCell<>() {
            protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty); if(empty||s==null){setGraphic(null);return;}
                Label l=new Label(s); l.getStyleClass().add("Payé".equals(s)?"badge-paye":"Annulé".equals(s)?"badge-annule":"badge-non-paye"); setGraphic(l); setText(null); setAlignment(Pos.CENTER);
            }
        });
        tablePatientHistory.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Real-time patient search
        patientSearchField.textProperty().addListener((o,a,n) -> filterPatients());
    }

    private void loadPatients() {
        allPatientsList = dao.PatientDAO.getAll();
        filterPatients();
    }

    private void filterPatients() {
        if (allPatientsList == null) return;
        String kw = patientSearchField.getText() == null ? "" : patientSearchField.getText().toLowerCase().trim();
        ObservableList<model.Patient> result = FXCollections.observableArrayList();
        for (model.Patient p : allPatientsList) {
            if (!kw.isEmpty()) {
                boolean m = p.getNom().toLowerCase().contains(kw)
                    || (p.getCin()!=null && p.getCin().toLowerCase().contains(kw))
                    || (p.getTelephone()!=null && p.getTelephone().contains(kw));
                if (!m) continue;
            }
            result.add(p);
        }
        tablePatients.setItems(result);
        lblPatientInfo.setText("Affichage " + result.size() + " patients sur " + allPatientsList.size());
    }

    @FXML private void handlePatientShowAll(ActionEvent ev) { patientSearchField.clear(); filterPatients(); }

    @FXML private void handleAddPatient(ActionEvent ev) { showPatientDialog(null); }

    private void showPatientDialog(model.Patient existing) {
        boolean isEdit = existing != null;
        Dialog<model.Patient> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Modifier le patient" : "Nouveau patient");
        dialog.setHeaderText(isEdit ? "Modifier les informations" : "Remplir les informations du patient");
        DialogPane dp = dialog.getDialogPane();
        applyThemeToDialogPane(dp);
        dp.setMinWidth(450);

        GridPane grid = new GridPane();
        grid.setHgap(15); grid.setVgap(14); grid.setPadding(new Insets(25,25,15,25));

        TextField nomField = new TextField(); nomField.setPromptText("Nom complet"); nomField.setPrefWidth(280); nomField.getStyleClass().add("search-field");
        TextField cinField = new TextField(); cinField.setPromptText("Ex: AB123456"); cinField.getStyleClass().add("search-field");
        TextField telField = new TextField(); telField.setPromptText("Ex: 0612345678"); telField.getStyleClass().add("search-field");

        if (isEdit) { nomField.setText(existing.getNom()); cinField.setText(existing.getCin()); telField.setText(existing.getTelephone()); }

        grid.add(createFieldLabel("Nom complet :"),0,0); grid.add(nomField,1,0);
        grid.add(createFieldLabel("CIN :"),0,1); grid.add(cinField,1,1);
        grid.add(createFieldLabel("Téléphone :"),0,2); grid.add(telField,1,2);

        dp.setContent(grid);
        ButtonType saveType = new ButtonType(isEdit?"Enregistrer":"Ajouter", ButtonBar.ButtonData.OK_DONE);
        dp.getButtonTypes().addAll(saveType, ButtonType.CANCEL);
        ((Button)dp.lookupButton(saveType)).setStyle("-fx-background-color:#2563EB;-fx-text-fill:white;-fx-font-weight:bold;-fx-background-radius:8;-fx-padding:8 22;");

        dialog.setResultConverter(bt -> {
            if (bt == saveType) {
                String nom=nomField.getText().trim(), cin=cinField.getText().trim(), tel=telField.getText().trim();
                if (nom.isEmpty()||cin.isEmpty()) { showAlert("Erreur","Nom et CIN sont obligatoires."); return null; }
                if (isEdit) return new model.Patient(existing.getId(), nom, cin, tel);
                return new model.Patient(nom, cin, tel);
            }
            return null;
        });

        dialog.showAndWait().ifPresent(p -> {
            if (isEdit) dao.PatientDAO.update(p); else dao.PatientDAO.add(p);
            loadPatients();
        });
    }

    private void handleDeletePatient(model.Patient p) {
        if (p==null) return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation"); alert.setHeaderText("Supprimer le patient");
        alert.setContentText("Supprimer « " + p.getNom() + " » ?");
        alert.showAndWait().ifPresent(r -> { if(r==ButtonType.OK){ dao.PatientDAO.delete(p.getId()); loadPatients(); } });
    }

    private void showPatientHistory(model.Patient p) {
        if (p==null) return;
        lblPatientHistoryTitle.setText("Historique — " + p.getNom() + " (" + p.getCin() + ")");
        patientHistoryBox.setVisible(true); patientHistoryBox.setManaged(true);
        ObservableList<Analyse> history = FXCollections.observableArrayList();
        if (allAnalyses != null) {
            for (Analyse a : allAnalyses) {
                if (p.getNom().equals(a.getPatient()) || (p.getCin()!=null && !p.getCin().isEmpty() && p.getCin().equals(a.getCin())))
                    history.add(a);
            }
        }
        tablePatientHistory.setItems(history);
    }

    @FXML private void handleCloseHistory(ActionEvent ev) {
        patientHistoryBox.setVisible(false); patientHistoryBox.setManaged(false);
    }

    // ================================================================
    //  ANALYSES PANEL
    // ================================================================
    private void setupAnalysesPanel() {
        colAaId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colAaCin.setCellValueFactory(new PropertyValueFactory<>("cin"));
        colAaPatient.setCellValueFactory(new PropertyValueFactory<>("patient"));
        colAaType.setCellValueFactory(new PropertyValueFactory<>("typeAnalyse"));
        colAaDate.setCellValueFactory(new PropertyValueFactory<>("dateAnalyse"));
        colAaStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colAaResultat.setCellValueFactory(new PropertyValueFactory<>("resultat"));
        colAaPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        colAaPaye.setCellValueFactory(new PropertyValueFactory<>("paiementStatut"));
        colAaDate.setCellFactory(c -> new TableCell<>() {
            protected void updateItem(LocalDate d, boolean e) { super.updateItem(d,e); setText(e||d==null?null:d.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))); }
        });
        colAaPrix.setCellFactory(c -> new TableCell<>() {
            protected void updateItem(Double p, boolean e) { super.updateItem(p,e); setText(e||p==null?null:String.format("%.2f DH",p)); }
        });
        colAaStatut.setCellFactory(c -> new TableCell<>() {
            protected void updateItem(String s, boolean e) {
                super.updateItem(s,e); if(e||s==null){setGraphic(null);return;}
                Label l=new Label(s); l.getStyleClass().add("En cours".equals(s)?"badge-en-cours":"Terminé".equals(s)?"badge-termine":"badge-annule"); setGraphic(l); setText(null); setAlignment(Pos.CENTER);
            }
        });
        colAaPaye.setCellFactory(c -> new TableCell<>() {
            protected void updateItem(String s, boolean e) {
                super.updateItem(s,e); if(e||s==null){setGraphic(null);return;}
                Label l=new Label(s); l.getStyleClass().add("Payé".equals(s)?"badge-paye":"Annulé".equals(s)?"badge-annule":"badge-non-paye"); setGraphic(l); setText(null); setAlignment(Pos.CENTER);
            }
        });

        colAaActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = new Button("Modifier");
            private final Button btnDel = new Button("Suppr.");
            private final HBox box = new HBox(6, btnEdit, btnDel);
            { box.setAlignment(Pos.CENTER);
              btnEdit.setStyle("-fx-background-color:#2563EB;-fx-text-fill:white;-fx-font-size:11;-fx-background-radius:6;-fx-padding:3 10;-fx-cursor:hand;");
              btnDel.setStyle("-fx-background-color:#DC2626;-fx-text-fill:white;-fx-font-size:11;-fx-background-radius:6;-fx-padding:3 10;-fx-cursor:hand;");
              btnEdit.setOnAction(e -> showAnalyseDialog(getTableRow().getItem(), true));
              btnDel.setOnAction(e -> handleDeleteAnalyse(getTableRow().getItem()));
            }
            protected void updateItem(Void v, boolean e) { super.updateItem(v,e); setGraphic(e?null:box); }
        });

        tableAllAnalyses.setRowFactory(tv -> {
            TableRow<Analyse> row = new TableRow<>();
            row.setOnMouseClicked(ev -> { if (ev.getClickCount() == 2 && !row.isEmpty()) showAnalyseDialog(row.getItem(), true); });
            return row;
        });

        tableAllAnalyses.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Types table
        colTyId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTyNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colTyPrix.setCellValueFactory(new PropertyValueFactory<>("prixDefaut"));
        colTyResultat.setCellValueFactory(new PropertyValueFactory<>("resultatFixe"));
        colTyPrix.setCellFactory(c -> new TableCell<>() {
            protected void updateItem(Double p, boolean e) { super.updateItem(p,e); setText(e||p==null?null:String.format("%.2f DH",p)); }
        });
        colTyActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = new Button("Modifier");
            private final Button btnDel = new Button("Suppr.");
            private final HBox box = new HBox(6, btnEdit, btnDel);
            { box.setAlignment(Pos.CENTER);
              btnEdit.setStyle("-fx-background-color:#2563EB;-fx-text-fill:white;-fx-font-size:11;-fx-background-radius:6;-fx-padding:3 10;-fx-cursor:hand;");
              btnDel.setStyle("-fx-background-color:#DC2626;-fx-text-fill:white;-fx-font-size:11;-fx-background-radius:6;-fx-padding:3 10;-fx-cursor:hand;");
              btnEdit.setOnAction(e -> showTypeDialog(getTableRow().getItem()));
              btnDel.setOnAction(e -> { model.TypeAnalyse t=getTableRow().getItem(); if(t!=null){dao.TypeAnalyseDAO.delete(t.getId()); loadAnalysesPanel();} });
            }
            protected void updateItem(Void v, boolean e) { super.updateItem(v,e); setGraphic(e?null:box); }
        });
        tableTypes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableTypes.setRowFactory(tv -> {
            TableRow<model.TypeAnalyse> row = new TableRow<>();
            row.setOnMouseClicked(ev -> { if (ev.getClickCount() == 2 && !row.isEmpty()) showTypeDialog(row.getItem()); });
            return row;
        });

        analyseFilterStatut.setItems(FXCollections.observableArrayList("Tous","En cours","Terminé","Annulé"));
        analyseFilterStatut.setValue("Tous");
        analyseSearchField.textProperty().addListener((o,a,n) -> filterAnalysesPanel());
        analyseFilterStatut.valueProperty().addListener((o,a,n) -> filterAnalysesPanel());
    }

    private void loadAnalysesPanel() {
        allAnalyses = AnalyseDAO.getAllAnalyses();
        filterAnalysesPanel();
        tableTypes.setItems(dao.TypeAnalyseDAO.getAll());
    }

    private void filterAnalysesPanel() {
        if (allAnalyses == null) return;
        String kw = analyseSearchField.getText()==null?"":analyseSearchField.getText().toLowerCase().trim();
        String st = analyseFilterStatut.getValue();
        ObservableList<Analyse> result = FXCollections.observableArrayList();
        for (Analyse a : allAnalyses) {
            if (st!=null && !"Tous".equals(st) && !st.equals(a.getStatut())) continue;
            if (!kw.isEmpty()) {
                boolean m = String.valueOf(a.getId()).contains(kw) || a.getPatient().toLowerCase().contains(kw)
                    || (a.getCin()!=null && a.getCin().toLowerCase().contains(kw))
                    || a.getTypeAnalyse().toLowerCase().contains(kw);
                if (!m) continue;
            }
            result.add(a);
        }
        tableAllAnalyses.setItems(result);
        lblAllAnalysesInfo.setText("Affichage " + result.size() + " sur " + allAnalyses.size() + " analyses");
    }

    @FXML private void handleAnalyseShowAll(ActionEvent ev) {
        analyseSearchField.clear(); analyseFilterStatut.setValue("Tous");
    }

    @FXML private void handleAddType(ActionEvent ev) { showTypeDialog(null); }

    private void showTypeDialog(model.TypeAnalyse existing) {
        boolean isEdit = existing != null;
        Dialog<model.TypeAnalyse> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Modifier le type" : "Nouveau type d'analyse");
        dialog.setHeaderText(isEdit ? "Modifier les informations" : "Ajouter un type d'analyse");
        DialogPane dp = dialog.getDialogPane();
        applyThemeToDialogPane(dp);
        dp.setMinWidth(450);

        GridPane grid = new GridPane();
        grid.setHgap(15); grid.setVgap(14); grid.setPadding(new Insets(25,25,15,25));

        TextField nomField = new TextField(); nomField.setPromptText("Ex: Glycémie"); nomField.setPrefWidth(280); nomField.getStyleClass().add("search-field");
        TextField prixField = new TextField(); prixField.setPromptText("Ex: 80.00"); prixField.getStyleClass().add("search-field");
        TextField resField = new TextField(); resField.setPromptText("Ex: Normal|Élevé|Bas (vide = saisie libre)"); resField.getStyleClass().add("search-field");

        if (isEdit) { nomField.setText(existing.getNom()); prixField.setText(String.valueOf(existing.getPrixDefaut())); resField.setText(existing.getResultatFixe()); }

        grid.add(createFieldLabel("Nom :"),0,0); grid.add(nomField,1,0);
        grid.add(createFieldLabel("Prix défaut :"),0,1); grid.add(prixField,1,1);
        grid.add(createFieldLabel("Résultat fixe :"),0,2); grid.add(resField,1,2);

        dp.setContent(grid);
        ButtonType saveType = new ButtonType(isEdit?"Enregistrer":"Ajouter", ButtonBar.ButtonData.OK_DONE);
        dp.getButtonTypes().addAll(saveType, ButtonType.CANCEL);
        ((Button)dp.lookupButton(saveType)).setStyle("-fx-background-color:#2563EB;-fx-text-fill:white;-fx-font-weight:bold;-fx-background-radius:8;-fx-padding:8 22;");

        dialog.setResultConverter(bt -> {
            if (bt == saveType) {
                String nom=nomField.getText().trim(), res=resField.getText().trim();
                if (nom.isEmpty()) { showAlert("Erreur","Nom obligatoire."); return null; }
                try {
                    double prix = Double.parseDouble(prixField.getText().trim());
                    if (isEdit) return new model.TypeAnalyse(existing.getId(), nom, prix, res);
                    return new model.TypeAnalyse(nom, prix, res);
                } catch (NumberFormatException e) { showAlert("Erreur","Prix invalide."); return null; }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(t -> {
            if (isEdit) dao.TypeAnalyseDAO.update(t); else dao.TypeAnalyseDAO.add(t);
            loadAnalysesPanel();
        });
    }

    // ================================================================
    //  FACTURATION PANEL
    // ================================================================

    private void setupFacturationPanel() {
        // Change columns to Facture model
        colFaPatient.setCellValueFactory(new PropertyValueFactory<>("patient"));
        colFaCin.setCellValueFactory(new PropertyValueFactory<>("cin"));
        colFaDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colFaNbAnalyses.setCellValueFactory(new PropertyValueFactory<>("nbAnalyses"));
        colFaPrix.setCellValueFactory(new PropertyValueFactory<>("total"));

        colFaDate.setCellFactory(c -> new TableCell<>() {
            protected void updateItem(LocalDate d, boolean e) { super.updateItem(d,e); setText(e||d==null?null:d.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))); }
        });
        colFaPrix.setCellFactory(c -> new TableCell<>() {
            protected void updateItem(Double p, boolean e) { super.updateItem(p,e); setText(e||p==null?null:String.format("%.2f DH",p)); }
        });

        // Statut column — shows Payé / Non payé / Annulé badge
        colFaStatut.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();
            { setAlignment(Pos.CENTER); }
            protected void updateItem(Void v, boolean e) {
                super.updateItem(v, e);
                if (e || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    model.Facture f = (model.Facture) getTableRow().getItem();
                    if (f.isAnnule()) {
                        badge.setText("Annulé");
                        badge.setStyle("-fx-background-color:#F1F5F9;-fx-text-fill:#94A3B8;-fx-background-radius:12;-fx-padding:4 14;-fx-font-size:11;-fx-font-weight:bold;");
                    } else if (f.isPaye()) {
                        badge.setText("Payé");
                        badge.setStyle("-fx-background-color:#D1FAE5;-fx-text-fill:#065F46;-fx-background-radius:12;-fx-padding:4 14;-fx-font-size:11;-fx-font-weight:bold;");
                    } else {
                        badge.setText("Non payé");
                        badge.setStyle("-fx-background-color:#FEE2E2;-fx-text-fill:#DC2626;-fx-background-radius:12;-fx-padding:4 14;-fx-font-size:11;-fx-font-weight:bold;");
                    }
                    setGraphic(badge);
                }
            }
        });

        // Paiement column — Payer button (visible only for unpaid)
        colFaPaiement.setCellFactory(col -> new TableCell<>() {
            private final Button btnPay = new Button("Payer");
            { 
                setAlignment(Pos.CENTER);
                btnPay.setStyle("-fx-background-color:#059669;-fx-text-fill:white;-fx-font-size:11;-fx-font-weight:bold;-fx-background-radius:6;-fx-padding:4 10;-fx-cursor:hand;");
                btnPay.setOnAction(e -> {
                    model.Facture f = (model.Facture) getTableRow().getItem();
                    if (f != null) { 
                        for(Analyse a : f.getAnalyses()){ 
                            if (!"Annulé".equals(a.getStatut())) {
                                a.setPaiementStatut("Payé"); 
                                AnalyseDAO.updateAnalyse(a); 
                            }
                        }
                        refreshAll(); loadFacturationPanel(); 
                    }
                });
            }
            protected void updateItem(Void v, boolean e) {
                super.updateItem(v, e);
                if (e || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    model.Facture f = (model.Facture) getTableRow().getItem();
                    if (f.isPaye() || f.isAnnule()) {
                        setGraphic(null);
                    } else {
                        setGraphic(btnPay);
                    }
                }
            }
        });

        // Impression column — Imprimer button (always visible)
        colFaImpression.setCellFactory(col -> new TableCell<>() {
            private final Button btnPrint = new Button("Imprimer");
            { 
                setAlignment(Pos.CENTER);
                btnPrint.setStyle("-fx-background-color:#2563EB;-fx-text-fill:white;-fx-font-size:11;-fx-font-weight:bold;-fx-background-radius:6;-fx-padding:4 10;-fx-cursor:hand;");
                btnPrint.setOnAction(e -> {
                    model.Facture f = (model.Facture) getTableRow().getItem();
                    if (f != null) { printFacture(f); }
                });
            }
            protected void updateItem(Void v, boolean e) {
                super.updateItem(v, e);
                if (e || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    setGraphic(btnPrint);
                }
            }
        });

        tableFacturation.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        factureFilterPaye.setItems(FXCollections.observableArrayList("Tous", "Payé", "Non payé"));
        factureFilterPaye.setValue("Tous");
        factureSearchField.textProperty().addListener((o,a,n) -> filterFacturationPanel());
        factureFilterPaye.valueProperty().addListener((o,a,n) -> filterFacturationPanel());
    }

    private void printFacture(model.Facture f) {
        javafx.print.PrinterJob job = javafx.print.PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(tableFacturation.getScene().getWindow())) {
            VBox printLayout = new VBox(15);
            printLayout.setPadding(new Insets(40));
            printLayout.setStyle("-fx-background-color: white; -fx-font-family: 'Segoe UI', sans-serif;");
            
            Label title = new Label("FACTURE");
            title.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
            Label laboName = new Label(labProps.getProperty("lab.name", "Labo Pro"));
            laboName.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #3B82F6;");
            
            VBox headerBox = new VBox(5, title, laboName, 
                new Label("Tél: " + labProps.getProperty("lab.phone", "")),
                new Label("Adresse: " + labProps.getProperty("lab.address", ""))
            );
            
            VBox patientBox = new VBox(5,
                new Label("Patient: " + f.getPatient()),
                new Label("CIN: " + f.getCin()),
                new Label("Date: " + f.getDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))
            );
            patientBox.setStyle("-fx-padding: 15; -fx-border-color: #E2E8F0; -fx-border-width: 1; -fx-background-color: #F8FAFC; -fx-border-radius: 5;");
            patientBox.setMaxWidth(Double.MAX_VALUE);
            
            GridPane itemsTable = new GridPane();
            itemsTable.setHgap(20); itemsTable.setVgap(10);
            itemsTable.setStyle("-fx-padding: 10; -fx-border-color: #CBD5E1; -fx-border-width: 0 0 1 0;");
            Label h1 = new Label("Analyse"); h1.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            Label h2 = new Label("Résultat"); h2.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            Label h3 = new Label("Prix"); h3.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            itemsTable.add(h1, 0, 0); itemsTable.add(h2, 1, 0); itemsTable.add(h3, 2, 0);
            
            int row = 1;
            for(Analyse a : f.getAnalyses()) {
                itemsTable.add(new Label(a.getTypeAnalyse()), 0, row);
                itemsTable.add(new Label(a.getResultat()), 1, row);
                itemsTable.add(new Label(String.format("%.2f DH", a.getPrix())), 2, row);
                row++;
            }
            
            HBox totalBox = new HBox();
            totalBox.setAlignment(Pos.CENTER_RIGHT);
            Label totalLbl = new Label(String.format("TOTAL: %.2f DH", f.getTotal()));
            totalLbl.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
            totalBox.getChildren().add(totalLbl);
            
            Label statutLbl;
            if (f.isAnnule()) {
                statutLbl = new Label("FACTURE ANNULÉE");
                statutLbl.setStyle("-fx-text-fill: #94A3B8; -fx-font-weight: bold; -fx-font-size: 16px;");
            } else if (f.isPaye()) {
                statutLbl = new Label("FACTURE PAYÉE");
                statutLbl.setStyle("-fx-text-fill: #059669; -fx-font-weight: bold; -fx-font-size: 16px;");
            } else {
                statutLbl = new Label("FACTURE NON PAYÉE");
                statutLbl.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold; -fx-font-size: 16px;");
            }
            totalBox.getChildren().add(0, statutLbl);
            HBox.setMargin(statutLbl, new Insets(0, 100, 0, 0));
            
            printLayout.getChildren().addAll(headerBox, patientBox, itemsTable, totalBox);
            
            boolean success = job.printPage(printLayout);
            if (success) { job.endJob(); }
        }
    }

    private ObservableList<model.Facture> allFactures = FXCollections.observableArrayList();

    private void loadFacturationPanel() {
        if (allAnalyses == null) allAnalyses = AnalyseDAO.getAllAnalyses();
        
        java.util.Map<String, model.Facture> factureMap = new java.util.HashMap<>();
        double totalPaye = 0, totalImpaye = 0;

        for (Analyse a : allAnalyses) {
            // Group ALL analyses by Patient + Date
            String key = a.getPatient() + "_" + a.getDateAnalyse().toString();
            model.Facture f = factureMap.computeIfAbsent(key, k -> new model.Facture(a.getPatient(), a.getCin() != null ? a.getCin() : "", a.getDateAnalyse()));
            f.addAnalyse(a);

            if (!"Annulé".equals(a.getStatut())) {
                if ("Payé".equals(a.getPaiementStatut())) { totalPaye += a.getPrix(); }
                else { totalImpaye += a.getPrix(); }
            }
        }
        
        allFactures.setAll(factureMap.values());
        filterFacturationPanel();
        
        lblFactPaye.setText(String.format("%.2f DH", totalPaye));
        lblFactImpaye.setText(String.format("%.2f DH", totalImpaye));
        lblFactTotal.setText(String.format("%.2f DH", totalPaye + totalImpaye));
    }

    private void filterFacturationPanel() {
        if (allFactures == null) return;
        String kw = factureSearchField.getText() == null ? "" : factureSearchField.getText().toLowerCase().trim();
        String filter = factureFilterPaye.getValue();
        
        ObservableList<model.Facture> result = FXCollections.observableArrayList();
        for (model.Facture f : allFactures) {
            if ("Payé".equals(filter) && !f.isPaye()) continue;
            if ("Non payé".equals(filter) && (f.isPaye() || f.isAnnule())) continue;
            
            if (!kw.isEmpty()) {
                if (!f.getPatient().toLowerCase().contains(kw) && !f.getCin().toLowerCase().contains(kw)) {
                    continue;
                }
            }
            result.add(f);
        }
        tableFacturation.setItems(result);
    }

    // ================================================================
    //  PARAMÈTRES PANEL
    // ================================================================
    private java.util.Properties labProps = new java.util.Properties();
    private java.io.File propsFile = new java.io.File("labo_settings.properties");

    private void setupThemeCombo() {
        paramTheme.setItems(FXCollections.observableArrayList("Clair", "Sombre"));
        paramTheme.setValue("Clair");
    }

    private void applyTheme(String theme) {
        if (contentStack.getScene() == null) return;
        String darkThemeUrl = getClass().getResource("/view/dark-theme.css").toExternalForm();
        javafx.scene.Scene scene = contentStack.getScene();
        javafx.scene.Parent root = scene.getRoot();
        if ("Sombre".equals(theme)) {
            if (!root.getStylesheets().contains(darkThemeUrl)) {
                root.getStylesheets().add(darkThemeUrl);
            }
            if (!scene.getStylesheets().contains(darkThemeUrl)) {
                scene.getStylesheets().add(darkThemeUrl);
            }
        } else {
            root.getStylesheets().remove(darkThemeUrl);
            scene.getStylesheets().remove(darkThemeUrl);
        }
    }

    private void applyThemeToDialogPane(DialogPane dp) {
        String stylesUrl = getClass().getResource("/view/styles.css").toExternalForm();
        String darkThemeUrl = getClass().getResource("/view/dark-theme.css").toExternalForm();
        
        dp.getStylesheets().add(stylesUrl);
        String selectedTheme = labProps.getProperty("app.theme", "Clair");
        if ("Sombre".equals(selectedTheme)) {
            dp.getStylesheets().add(darkThemeUrl);
        }
        
        if (dp.getScene() != null) {
            applyStylesheetsToScene(dp.getScene(), selectedTheme);
        } else {
            dp.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    applyStylesheetsToScene(newScene, selectedTheme);
                }
            });
        }
    }

    private void applyStylesheetsToScene(javafx.scene.Scene scene, String theme) {
        String stylesUrl = getClass().getResource("/view/styles.css").toExternalForm();
        String darkThemeUrl = getClass().getResource("/view/dark-theme.css").toExternalForm();
        
        if (!scene.getStylesheets().contains(stylesUrl)) {
            scene.getStylesheets().add(stylesUrl);
        }
        if ("Sombre".equals(theme)) {
            if (!scene.getStylesheets().contains(darkThemeUrl)) {
                scene.getStylesheets().add(darkThemeUrl);
            }
        } else {
            scene.getStylesheets().remove(darkThemeUrl);
        }
    }

    private void loadParametresPanel() {
        // Load props
        if (propsFile.exists()) {
            try (java.io.FileInputStream fis = new java.io.FileInputStream(propsFile)) { labProps.load(fis); } catch (Exception ignored) {}
        }
        paramLabName.setText(labProps.getProperty("lab.name", ""));
        paramLabPhone.setText(labProps.getProperty("lab.phone", ""));
        paramLabAddress.setText(labProps.getProperty("lab.address", ""));
        paramLabEmail.setText(labProps.getProperty("lab.email", ""));
        paramDoctorName.setText(labProps.getProperty("lab.doctor", ""));
        paramSpeciality.setText(labProps.getProperty("lab.speciality", ""));
        
        String t = labProps.getProperty("app.theme", "Clair");
        paramTheme.setValue(t);
        applyTheme(t); // Apply on load

        // Stats
        paramTotalPatients.setText(String.valueOf(dao.PatientDAO.getAll().size()));
        paramTotalAnalyses.setText(String.valueOf(allAnalyses != null ? allAnalyses.size() : AnalyseDAO.getAllAnalyses().size()));
        paramTotalTypes.setText(String.valueOf(dao.TypeAnalyseDAO.getAll().size()));
    }

    @FXML private void handleSaveParams(ActionEvent ev) {
        labProps.setProperty("lab.name", paramLabName.getText().trim());
        labProps.setProperty("lab.phone", paramLabPhone.getText().trim());
        labProps.setProperty("lab.address", paramLabAddress.getText().trim());
        labProps.setProperty("lab.email", paramLabEmail.getText().trim());
        labProps.setProperty("lab.doctor", paramDoctorName.getText().trim());
        labProps.setProperty("lab.speciality", paramSpeciality.getText().trim());
        
        String selectedTheme = paramTheme.getValue();
        labProps.setProperty("app.theme", selectedTheme);
        applyTheme(selectedTheme);

        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(propsFile)) {
            labProps.store(fos, "Labo Pro Settings");
            Alert a = new Alert(Alert.AlertType.INFORMATION); a.setTitle("Succès"); a.setHeaderText(null);
            a.setContentText("Paramètres enregistrés avec succès."); a.showAndWait();
        } catch (Exception ex) { showAlert("Erreur", "Impossible d'enregistrer: " + ex.getMessage()); }
    }
}
