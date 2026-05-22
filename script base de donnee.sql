-- Création de l'utilisateur admin
CREATE USER IF NOT EXISTS 'admin'@'localhost' IDENTIFIED BY 'admin';
GRANT ALL PRIVILEGES ON *.* TO 'admin'@'localhost' WITH GRANT OPTION;
FLUSH PRIVILEGES;

-- Création de la base de données
CREATE DATABASE IF NOT EXISTS laboratoire CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE laboratoire;

-- Table : patient
CREATE TABLE IF NOT EXISTS patient (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    cin VARCHAR(30) DEFAULT '',
    telephone VARCHAR(30) DEFAULT '',
    date_ajout TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_cin (cin)
);

-- Table : type_analyse
CREATE TABLE IF NOT EXISTS type_analyse (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL UNIQUE,
    prix_defaut DOUBLE NOT NULL DEFAULT 0,
    resultat_fixe VARCHAR(255) DEFAULT ''
);

-- Table : analyse
CREATE TABLE IF NOT EXISTS analyse (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient VARCHAR(100) NOT NULL,
    cin VARCHAR(30) DEFAULT '',
    type_analyse VARCHAR(100) NOT NULL,
    date_analyse DATE NOT NULL,
    resultat VARCHAR(255) DEFAULT '',
    prix DOUBLE NOT NULL DEFAULT 0,
    statut VARCHAR(20) NOT NULL DEFAULT 'En cours',
    paye VARCHAR(20) NOT NULL DEFAULT 'Non payé'
);

-- Données par défaut pour les types d'analyses
INSERT IGNORE INTO type_analyse (nom, prix_defaut, resultat_fixe) VALUES
('NFS', 120.0, ''),
('Glycémie', 40.0, 'Normal|Élevé|Bas'),
('Bilan', 200.0, ''),
('Cholestérol', 80.0, 'Normal|Élevé|Bas'),
('Triglycérides', 70.0, 'Normal|Élevé'),
('Acide Urique', 60.0, 'Normal|Élevé'),
('TSH', 150.0, 'Normal|Hypothyroïdie|Hyperthyroïdie'),
('Vitamine D', 180.0, 'Suffisant|Insuffisant|Carence'),
('Hépatite B', 90.0, 'Positif|Négatif'),
('HIV', 100.0, 'Positif|Négatif');
