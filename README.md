# Systeme de gestion de laboratoire Labo Pro

Ce depot contient le code source de Labo Pro, une application de bureau developpee en JavaFX et MySQL destinee a la gestion complete des laboratoires d'analyses medicales. Elle propose des outils de suivi des dossiers patients, de planification des examens et de facturation, avec un support natif pour le changement de theme en temps reel.

---

## Guide visuel de l'interface

Le dossier de captures d'ecran montre les differents aspects de l'interface graphique a travers douze captures distinctes.

### Tableau de bord general et vue principale

* **Vue globale en mode clair (1.png)** : Cette capture montre le tableau de bord general lors du premier demarrage en mode clair. L'arriere-plan blanc pur et les cartes d'indicateurs de couleur claire mettent en evidence le nombre de patients inscrits, les analyses en cours et les gains financiers. Le graphique central montre la repartition quotidienne des analyses.

![Tableau de bord - Mode clair](screenshots/1.png)


### Fenetres de saisie et boites de dialogue
* **Ajout de patient** : Boite de dialogue modale destinee a la creation d'une nouvelle fiche patient avec validation des champs.

![Dialogue nouveau patient](screenshots/2.png)


* **Enregistrement d'analyse** : Fenetre d'attribution d'un examen medical a un patient existant avec selection pre-remplie.

![Dialogue nouvelle analyse](screenshots/3.png)

* **Saisie des resultats numeriques** : Formulaire de validation des mesures du laboratoire par rapport aux valeurs de reference.

![Interface de facturation](screenshots/4.png)

* **Filtres de recherche multicriteria** : Panneau de recherche approfondie pour extraire l'historique d'un patient.
  
![Filtres de recherche](screenshots/5.png)




### Gestion des examens et base de donnees

* **Liste des patients médicaux**  : affichage du tableau des patients. Lorsqu’on clique sur “Historique”, un tableau contenant toutes les analyses précédemment effectuées pour le patient apparaît.
  
![Dialogue nouvelle analyse](screenshots/7.png)

* **Liste des analyses medicales** : Affiche le tableau de suivi des examens medicaux avec les statuts colores pour chaque echantillon.
  
![Dialogue de saisie de resultats](screenshots/8.png)


* **Detail de la facturation et paiements** : Montre la gestion financiere de chaque dossier avec les boutons contextuels pour declencher un encaissement rapide ou imprimer une quittance.

![Filtres de recherche](screenshots/9.png)

* **Formulaire de configuration du laboratoire** : Permet de renseigner les informations de contact et le nom du medecin biologiste responsable pour personnaliser l'en-tete des documents.

![Tableau de bord - Mode sombre](screenshots/10.png)




### Interface en mode sombre premium

* **Dashboard en mode sombre** : Affiche la transition complete de l'interface centrale vers un theme ardoise fonce (`#1E293B`) avec des textes contrastes en gris clair (`#E2E8F0`).
* **Tableau des analyses en mode sombre** : Montre le rendu du tableau d'analyses sous le theme sombre, garantissant que les statuts restent parfaitement lisibles.
* **Popups et menus styles** : Illustre l'application reussie du style sombre sur les fenetres volantes de type ComboBox et les fenetres de calendrier DatePicker.



![Tableaux de donnees - Mode sombre](screenshots/11.png)
![Menus et calendriers - Mode sombre](screenshots/12.png)

---

## Architecture d'Application (MVC + DAO)

L'application est construite selon le patron de conception MVC (Modele-Vue-Controleur) associe a des objets d'acces aux donnees (DAO) pour isoler les requetes SQL du reste de la logique applicative.

* **Modele** : Les classes du dossier `src/model` representent les entites de la base de donnees sous forme de Java Beans simples.
* **Vue** : Les fichiers FXML du dossier `src/view` definissent la structure de l'interface tandis que les fichiers CSS appliquent la charte graphique.
* **Controleur** : Les controleurs du dossier `src/controller` interceptent les evenements utilisateur et mettent a jour la vue en fonction des donnees.
* **DAO** : Les classes du dossier `src/dao` executent les requetes SQL de creation, lecture, mise a jour et suppression dans la base MySQL.

```mermaid
graph TD
    %% Define Styles
    classDef ui fill:#3B82F6,stroke:#1E3A8A,stroke-width:2px,color:#fff;
    classDef controller fill:#10B981,stroke:#065F46,stroke-width:2px,color:#fff;
    classDef dao fill:#F59E0B,stroke:#92400E,stroke-width:2px,color:#fff;
    classDef db fill:#EF4444,stroke:#991B1B,stroke-width:2px,color:#fff;
    classDef model fill:#8B5CF6,stroke:#5B21B6,stroke-width:2px,color:#fff;

    %% Architecture Nodes
    subgraph UI ["Couche Présentation (Vue - FXML & CSS)"]
        V1["dashboard.fxml (Tableau de bord)"]:::ui
        V2["analyse_view.fxml (Gestion Analyses)"]:::ui
        V3["styles.css & dark-theme.css"]:::ui
    end

    subgraph CTRL ["Couche Contrôle (Contrôleurs JavaFX)"]
        C1["DashboardController.java"]:::controller
        C2["AnalyseController.java"]:::controller
    end

    subgraph DAO ["Couche Accès Données (DAO)"]
        D1["PatientDAO.java"]:::dao
        D2["AnalyseDAO.java"]:::dao
        D3["TypeAnalyseDAO.java"]:::dao
    end

    subgraph DB ["Moteur de Stockage (MySQL)"]
        DB1[("Base de Données MySQL")]:::db
    end

    subgraph MDL ["Couche Modèle (Entités/JavaBeans)"]
        M1["Patient.java"]:::model
        M2["Analyse.java"]:::model
        M3["TypeAnalyse.java"]:::model
        M4["Facture.java"]:::model
    end

    %% Architecture Links
    V1 -->|Événements utilisateur| C1
    V2 -->|Événements utilisateur| C2
    C1 -->|Applique les styles| V3
    C2 -->|Applique les styles| V3

    C1 -->|Consulte / Modifie| D1
    C1 -->|Consulte / Modifie| D2
    C2 -->|Consulte / Modifie| D3

    D1 -->|Requêtes SQL CRUD| DB1
    D2 -->|Requêtes SQL CRUD| DB1
    D3 -->|Requêtes SQL CRUD| DB1

    D1 .->|Mappe les données| M1
    D2 .->|Mappe les données| M2
    D3 .->|Mappe les données| M3
    C1 .->|Utilise les entités| MDL
    C2 .->|Utilise les entités| MDL
 ```
 ### Logique du changement de theme

La bascule entre le mode clair et le mode sombre s'effectue dynamiquement en memoire. Lorsqu'un utilisateur selectionne le mode sombre, le controleur charge la feuille de style `dark-theme.css` et l'ajoute directement au noeud racine de la scene principale. Pour resoudre les limitations de JavaFX avec les fenetres secondaires (dialogues et menus contextuels), le controleur applique la feuille de style au niveau de l'objet scene de chaque dialogue et propage le style aux popups du systeme.

---

## Fonctionnalites principales

* **Suivi des dossiers patients** : Enregistrement, recherche rapide par numero de carte nationale (CIN) ou par nom, et historique complet des examens associes.
* **Gestion des analyses** : Parametrage des types d'examens avec des valeurs de reference et des tarifs pre-configures. Suivi de l'etat d'avancement des echantillons.
* **Facturation integre** : Suivi des paiements, encaissement rapide en un clic et generation automatique de factures pretes a l'impression.
* **Statistiques interactives** : Graphiques de repartition et indicateurs financiers mis a jour en temps reel a chaque modification de donnees.

---

## Guide d'installation et de configuration

### Configuration de la base de donnees

1. Assurez-vous qu'un serveur MySQL local est actif sur votre machine.
2. Executez le script SQL fourni a la racine pour initialiser les tables et les donnees initiales :
   ```bash
   mysql -u root -p < "script base de donnee.sql"
   ```

### Lancement rapide sous Windows

Un script automatique est fourni pour compiler et lancer l'application en un clic. Double-cliquez simplement sur le fichier :
```bash
run.bat
```

### Importation dans IntelliJ IDEA

1. Ouvrez IntelliJ IDEA et importez le dossier racine du projet.
2. Dans la configuration de la structure du projet, definissez le SDK de projet sur Java 17 ou une version superieure.
3. Ajoutez le dossier `lib/javafx-sdk-24.0.1/lib` comme bibliotheque globale de projet.
4. Creez une nouvelle configuration d'application pour executer la classe principale `Main`.
5. Ajoutez les parametres VM suivants pour charger les modules de l'interface graphique :
   ```text
   --module-path "lib/javafx-sdk-24.0.1/lib" --add-modules javafx.controls,javafx.fxml
   ```
6. Executez l'application.

---

## Licence

Ce projet est distribue sous la licence MIT. Vous pouvez librement l'utiliser et le modifier.

