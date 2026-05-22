# Architecture logicielle - Labo Pro

Ce document detaille l'organisation interne du projet et ses couches logiques.

## Structure du projet

```
src/
├── Main.java                        ← Point d'entree (JavaFX Application)
├── model/
│   └── Analyse.java                 ← Modele de donnees (POJO)
├── dao/
│   └── AnalyseDAO.java              ← Couche d'acces aux donnees (JDBC/MySQL)
├── controller/
│   ├── AnalyseController.java       ← Ancien controleur (CRUD simple) — conserve
│   └── DashboardController.java     ← Nouveau controleur Dashboard
├── util/
│   └── DBConnection.java            ← Connexion a la base de donnees
├── view/
│   ├── analyse_view.fxml            ← Ancienne vue — conservee
│   ├── dashboard.fxml               ← Nouvelle vue Dashboard
│   └── styles.css                   ← Feuille de style globale
```

## Couches logiques

```
┌──────────────────────────────────────────┐
│              View (FXML + CSS)           │  ← Presentation uniquement
├──────────────────────────────────────────┤
│           Controller (JavaFX)            │  ← Logique d'interface + evenements
├──────────────────────────────────────────┤
│              DAO (JDBC)                  │  ← Acces base de donnees
├──────────────────────────────────────────┤
│             Model (POJO)                 │  ← Entites metier
├──────────────────────────────────────────┤
│              Util                        │  ← Utilitaires (connexion DB)
└──────────────────────────────────────────┘
```

## Principes appliques

1. Separation des responsabilites : Chaque couche a un role unique.
2. CSS externalise : `styles.css` est reutilisable sur toutes les vues.
3. Controleurs independants : Chaque vue a son propre controleur.
4. DAO statique : Methodes utilitaires d'acces aux donnees sans etat.

## Comment ajouter une nouvelle fonctionnalite

1. Nouveau modele : Creer dans `model/`.
2. Nouvel acces DB : Creer un DAO dans `dao/`.
3. Nouvelle vue : Creer un `.fxml` dans `view/` + controleur dans `controller/`.
4. Styles : Ajouter les classes CSS dans `view/styles.css`.

## Ameliorations futures recommandees

* Extraire la logique KPIs dans une couche service.
* Ajouter un systeme de navigation entre les vues (pattern Router/Navigator).
* Implementer un pattern Observer pour la synchronisation des donnees.
* Ajouter une table patients separee avec relation FK vers analyse.
