🌐 **Language:** [🇬🇧 English](README.md) | [🇫🇷 Français](README.fr.md)

---

# Labo Pro — Laboratory Management System

This repository contains the source code of Labo Pro, a desktop application built with JavaFX and MySQL designed for the complete management of medical analysis laboratories. It provides tools for patient record tracking, test scheduling, and billing, with native support for real-time theme switching.

---

## Visual Interface Guide

### General Dashboard and Main View

* **Light mode overview (1.png)**: This capture shows the general dashboard on first launch in light mode. The pure white background and light-colored indicator cards highlight the number of registered patients, ongoing analyses, and financial earnings. The central chart displays the daily distribution of analyses.

![Dashboard - Light mode](screenshots/1.png)


### Input Windows and Dialog Boxes
* **Add patient**: Modal dialog box for creating a new patient record with field validation.

![New patient dialog](screenshots/2.png)


* **Analysis registration**: Window for assigning a medical test to an existing patient with pre-filled selection.

![New analysis dialog](screenshots/3.png)

* **Numerical results entry**: Form for validating laboratory measurements against reference values.

![Billing interface](screenshots/4.png)

* **Multi-criteria search filters**: Advanced search panel for extracting a patient's history.
  
![Search filters](screenshots/5.png)




### Test Management and Database

* **Medical patient list**: Displays the patient table. When clicking "History", a table containing all previously performed analyses for the patient appears.
  
![New analysis dialog](screenshots/7.png)

* **Medical analyses list**: Displays the medical test tracking table with color-coded statuses for each sample.
  
![Results entry dialog](screenshots/8.png)


* **Billing and payments detail**: Shows the financial management of each record with contextual buttons to trigger quick payment or print a receipt.

![Search filters](screenshots/9.png)

* **Laboratory configuration form**: Allows entering contact information and the responsible biologist's name to customize document headers.

![Dashboard - Dark mode](screenshots/10.png)




### Premium Dark Mode Interface

* **Dark mode dashboard**: Shows the complete transition of the main interface to a dark slate theme (`#1E293B`) with contrasting light gray text (`#E2E8F0`).
* **Dark mode analysis table**: Demonstrates the analysis table rendered under the dark theme, ensuring that statuses remain perfectly readable.
* **Styled popups and menus**: Illustrates the successful application of the dark style to ComboBox flyout windows and DatePicker calendar windows.



![Data tables - Dark mode](screenshots/11.png)
![Menus and calendars - Dark mode](screenshots/12.png)

---

## Application Architecture (MVC + DAO)

The application is built following the MVC (Model-View-Controller) design pattern combined with Data Access Objects (DAO) to isolate SQL queries from the rest of the application logic.

* **Model**: Classes in the `src/model` folder represent database entities as simple Java Beans.
* **View**: FXML files in the `src/view` folder define the interface structure while CSS files apply the visual design.
* **Controller**: Controllers in the `src/controller` folder intercept user events and update the view based on the data.
* **DAO**: Classes in the `src/dao` folder execute SQL queries for creating, reading, updating, and deleting records in the MySQL database.

```mermaid
flowchart TD
    U([User]) --> V

    subgraph V ["View (FXML + CSS)"]
        V1[dashboard.fxml]
        V2[analyse_view.fxml]
        V3[styles.css / dark-theme.css]
    end

    V -->|Clicks & inputs| C

    subgraph C ["Controller (Java)"]
        C1[DashboardController]
        C2[AnalyseController]
    end

    C -->|Calls CRUD methods| D

    subgraph D ["DAO (Data Access)"]
        D1[PatientDAO]
        D2[AnalyseDAO]
        D3[TypeAnalyseDAO]
    end

    D -->|SQL Queries| DB[("MySQL<br/>Database")]
    DB -->|Results| D
    D -->|Java Objects| M

    subgraph M ["Model (JavaBeans)"]
        M1[Patient]
        M2[Analyse]
        M3[TypeAnalyse]
        M4[Facture]
    end

    M -->|Formatted data| C
    C -->|Updates display| V
    V -->|Refreshed interface| U

    style U fill:#6366F1,stroke:#4338CA,color:#fff
    style V fill:#3B82F6,stroke:#1E40AF,color:#fff
    style V1 fill:#60A5FA,stroke:#2563EB,color:#fff
    style V2 fill:#60A5FA,stroke:#2563EB,color:#fff
    style V3 fill:#60A5FA,stroke:#2563EB,color:#fff
    style C fill:#10B981,stroke:#047857,color:#fff
    style C1 fill:#34D399,stroke:#059669,color:#fff
    style C2 fill:#34D399,stroke:#059669,color:#fff
    style D fill:#F59E0B,stroke:#D97706,color:#fff
    style D1 fill:#FBBF24,stroke:#F59E0B,color:#fff
    style D2 fill:#FBBF24,stroke:#F59E0B,color:#fff
    style D3 fill:#FBBF24,stroke:#F59E0B,color:#fff
    style DB fill:#EF4444,stroke:#B91C1C,color:#fff
    style M fill:#8B5CF6,stroke:#6D28D9,color:#fff
    style M1 fill:#A78BFA,stroke:#7C3AED,color:#fff
    style M2 fill:#A78BFA,stroke:#7C3AED,color:#fff
    style M3 fill:#A78BFA,stroke:#7C3AED,color:#fff
    style M4 fill:#A78BFA,stroke:#7C3AED,color:#fff
 ```
 ### Theme Switching Logic

The toggle between light and dark mode is performed dynamically in memory. When a user selects dark mode, the controller loads the `dark-theme.css` stylesheet and adds it directly to the root node of the main scene. To work around JavaFX limitations with secondary windows (dialogs and context menus), the controller applies the stylesheet at the scene object level of each dialog and propagates the style to system popups.

---

## Key Features

* **Patient record tracking**: Registration, fast search by national ID card number (CIN) or name, and complete history of associated tests.
* **Analysis management**: Configuration of test types with reference values and pre-set pricing. Sample progress status tracking.
* **Integrated billing**: Payment tracking, one-click quick payment, and automatic generation of print-ready invoices.
* **Interactive statistics**: Distribution charts and financial indicators updated in real time with every data change.

---

## Installation and Configuration Guide

### Database Setup

1. Make sure a local MySQL server is running on your machine.
2. Run the SQL script provided at the root to initialize the tables and seed data:
   ```bash
   mysql -u root -p < "script base de donnee.sql"
   ```

### Quick Launch on Windows

An automated script is provided to compile and launch the application in one click. Simply double-click the file:
```bash
run.bat
```

### Importing into IntelliJ IDEA

1. Open IntelliJ IDEA and import the project root folder.
2. In the project structure configuration, set the project SDK to Java 17 or higher.
3. Add the `lib/javafx-sdk-24.0.1/lib` folder as a global project library.
4. Create a new application run configuration to execute the main class `Main`.
5. Add the following VM options to load the graphical interface modules:
   ```text
   --module-path "lib/javafx-sdk-24.0.1/lib" --add-modules javafx.controls,javafx.fxml
   ```
6. Run the application.

---

## License

This project is distributed under the MIT License. You are free to use and modify it.
