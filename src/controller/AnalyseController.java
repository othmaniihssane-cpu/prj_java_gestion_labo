package controller;

import dao.AnalyseDAO;
import model.Analyse;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;


public class AnalyseController {

    @FXML
    private TextField txtPatient;
    @FXML
    private TextField txtType;
    @FXML
    private DatePicker dpDate;
    @FXML
    private TextField txtResultat;
    @FXML
    private TextField txtPrix;

    @FXML
    private TextField searchField;
    @FXML
    private Button searchButton;


    @FXML
    private TableView<Analyse> tableAnalyse;
    @FXML
    private TableColumn<Analyse, Integer> colId;
    @FXML
    private TableColumn<Analyse, String> colPatient;
    @FXML
    private TableColumn<Analyse, String> colType;
    @FXML
    private TableColumn<Analyse, LocalDate> colDate;
    @FXML
    private TableColumn<Analyse, String> colResultat;
    @FXML
    private TableColumn<Analyse, Double> colPrix;



    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPatient.setCellValueFactory(new PropertyValueFactory<>("patient"));
        colType.setCellValueFactory(new PropertyValueFactory<>("typeAnalyse"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateAnalyse"));
        colResultat.setCellValueFactory(new PropertyValueFactory<>("resultat"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));

        loadTable();


        tableAnalyse.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateForm(newSelection);
            }
        });

    }

    private void loadTable() {
        ObservableList<Analyse> list = AnalyseDAO.getAllAnalyses();
        tableAnalyse.setItems(list);
    }

    private void populateForm(Analyse analyse) {
        txtPatient.setText(analyse.getPatient());
        txtType.setText(analyse.getTypeAnalyse());
        dpDate.setValue(analyse.getDateAnalyse());
        txtResultat.setText(analyse.getResultat());
        txtPrix.setText(String.valueOf(analyse.getPrix()));
    }

    @FXML
    public void handleAdd(ActionEvent event) {
        if (isValid()) {
            Analyse a = new Analyse(
                    txtPatient.getText(),
                    "",
                    txtType.getText(),
                    dpDate.getValue(),
                    txtResultat.getText(),
                    Double.parseDouble(txtPrix.getText()),
                    "En cours",
                    "Non payé");
            AnalyseDAO.addAnalyse(a);
            loadTable();
            handleClear(null);
        }
    }

    @FXML
    public void handleUpdate(ActionEvent event) {
        Analyse selected = tableAnalyse.getSelectionModel().getSelectedItem();
        if (selected != null && isValid()) {
            Analyse a = new Analyse(
                    selected.getId(),
                    txtPatient.getText(),
                    selected.getCin(),
                    txtType.getText(),
                    dpDate.getValue(),
                    txtResultat.getText(),
                    Double.parseDouble(txtPrix.getText()),
                    selected.getStatut(),
                    selected.getPaiementStatut());
            AnalyseDAO.updateAnalyse(a);
            loadTable();
            handleClear(null);
        } else {
            showAlert("Selection Error", "Please select an item to update.");
        }
    }

    @FXML
    public void handleDelete(ActionEvent event) {
        Analyse selected = tableAnalyse.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Confirmation avant suppression
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText(null);
            alert.setContentText("Voulez-vous vraiment supprimer cette analyse ?");

            java.util.Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                AnalyseDAO.deleteAnalyse(selected.getId());
                loadTable();
                handleClear(null);
            }
        } else {
            showAlert("Erreur de sélection", "Veuillez sélectionner un élément à supprimer.");
        }
    }

    @FXML
    public void handleClear(ActionEvent event) {
        txtPatient.clear();
        txtType.clear();
        dpDate.setValue(null);
        txtResultat.clear();
        txtPrix.clear();
        tableAnalyse.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleSearch() {

        String keyword = searchField.getText().toLowerCase();

        ObservableList<Analyse> allData = AnalyseDAO.getAllAnalyses();
        ObservableList<Analyse> filteredList = FXCollections.observableArrayList();

        for (Analyse a : allData) {
            if (keyword.isEmpty()) {
                loadTable();
                return;
            }
            if (a.getPatient() != null &&
                    a.getPatient().toLowerCase().contains(keyword)) {

                filteredList.add(a);
            }
        }

        tableAnalyse.setItems(filteredList);
    }
    @FXML
    private void handleShowAll() {
        tableAnalyse.setItems(AnalyseDAO.getAllAnalyses());
    }




    private boolean isValid() {
        if (txtPatient.getText().trim().isEmpty() || txtType.getText().trim().isEmpty() ||
                dpDate.getValue() == null || txtResultat.getText().trim().isEmpty() ||
                txtPrix.getText().trim().isEmpty()) {
            showAlert("Erreur", "Tous les champs sont obligatoires");
            return false;
        }
        try {
            double price = Double.parseDouble(txtPrix.getText());
            if (price < 0) {
                showAlert("Erreur", "Le prix ne peut pas être négatif");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Le prix doit être un nombre valide");
            return false;
        }
        return true;
    }



    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
