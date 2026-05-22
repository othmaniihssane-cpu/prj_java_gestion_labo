import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Ensure database schema is up to date (adds new columns if missing)
        dao.AnalyseDAO.ensureSchema();
        dao.PatientDAO.ensureTable();
        dao.TypeAnalyseDAO.ensureTable();
        // Pré-générer des données de test si la base de données est vide
        dao.PatientDAO.generateIfNeeded();
        dao.TypeAnalyseDAO.generateIfNeeded();
        dao.AnalyseDAO.generateTestDataIfNeeded();

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/dashboard.fxml")
        );
        Scene scene = new Scene(loader.load());
        stage.setTitle("Labo Pro \u2014 Gestionnaire d'Analyses");
        stage.setMinWidth(950);
        stage.setMinHeight(650);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
