/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gitadatabase;

import java.sql.*;

/**
 *
 * @author taboada.taddeo
 */
public class Database {
    private String url;

    public Database() {
        this.url = "jdbc:sqlite:gite.db?foreign_keys=true";
        creaDatabase();
    }
    
    private void creaDatabase() {        
        String sqlGite = "CREATE TABLE IF NOT EXISTS gite (" +
                         "git_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "git_destinazione varchar(30), " +
                         "git_durata INTEGER, " +
                         "git_prezzo INTEGER" +
                         ");";
        
        String sqlClassi = "CREATE TABLE IF NOT EXISTS classi (" +
                         "cla_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "cla_anno INTEGER, " +
                         "cla_sezione varchar(1), " +
                         "cla_indirizzo varchar(10)" +
                         ");";
        
        String sqlAlunni = "CREATE TABLE IF NOT EXISTS alunni (" +
                         "alu_matr INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "alu_nome varchar(15), " +
                         "alu_cognome varchar(15), " +
                         "alu_cla_id INTEGER, " +
                         "FOREIGN KEY(alu_cla_id) REFERENCES classi(cla_id)" +
                         ");";
        
        String sqlPartecipazione = "CREATE TABLE IF NOT EXISTS partecipazione (" +
                         "par_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "par_alu_id INTEGER, " +
                         "par_git_id INTEGER, " +
                         "FOREIGN KEY(par_alu_id) REFERENCES alunni(alu_matr), " +
                         "FOREIGN KEY(par_git_id) REFERENCES gite(git_id)" +
                         ");";

        try (Connection conn = DriverManager.getConnection(url);
            Statement stmt = conn.createStatement()) {

            // Abilita le foreign keys in SQLite
            //stmt.execute("PRAGMA foreign_keys = ON");

            // Creazione tabelle
            stmt.execute(sqlGite);
            stmt.execute(sqlClassi);
            stmt.execute(sqlAlunni);
            stmt.execute(sqlPartecipazione);

            System.out.println("Tabelle create correttamente!");

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
    
    public void aggiungiGita(Gita g) {
        try (Connection conn = DriverManager.getConnection(url)) {
            String insert = "INSERT INTO gite(git_destinazione, git_durata, git_prezzo) VALUES(?, ?, ?)";

            PreparedStatement pstmt = conn.prepareStatement(insert);
            pstmt.setString(1, g.getLuogo());
            pstmt.setInt(2, g.getDurata());
            pstmt.setInt(3, g.getPrezzo());
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
    
    public void stampaGite() {
        String query = "SELECT * FROM gite";

        try (Connection conn = DriverManager.getConnection(url);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int id = rs.getInt("git_id");
                String destinazione = rs.getString("git_destinazione");
                int durata = rs.getInt("git_durata");
                int prezzo = rs.getInt("git_prezzo");

                System.out.println(
                    id + " | " +
                    destinazione + " | " +
                    durata + " giorni | " +
                    prezzo + " euro"
                );
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
    
    public void aggiungiClasse(Classe c) {
        try (Connection conn = DriverManager.getConnection(url)) {
            String insert = "INSERT INTO classi(cla_anno, cla_sezione, cla_indirizzo) VALUES(?, ?, ?)";

            PreparedStatement pstmt = conn.prepareStatement(insert);
            pstmt.setInt(1, c.getAnno());
            pstmt.setString(2, c.getSezione());
            pstmt.setString(3, c.getIndirizzo());
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
    
    public void stampaClassi() {
        String query = "SELECT * FROM classi";

        try (Connection conn = DriverManager.getConnection(url);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int id = rs.getInt("cla_id");
                int anno = rs.getInt("cla_anno");
                String sezione = rs.getString("cla_sezione");
                String indirizzo = rs.getString("cla_indirizzo");

                System.out.println(
                    id + " | " +
                    anno + " | " +
                    sezione + " | " +
                    indirizzo
                );
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
    
    public void cancellaDati() {
        String sqlGite = "DELETE FROM gite";
        String sqlClassi = "DELETE FROM classi";
        String sqlAlunni = "DELETE FROM alunni";
        String sqlPartecipazione = "DELETE FROM partecipazione";
        
        String resetIdGite = "DELETE FROM sqlite_sequence WHERE name = 'gite'";
        String resetIdClassi = "DELETE FROM sqlite_sequence WHERE name = 'classi'";
        String resetIdAlunni = "DELETE FROM sqlite_sequence WHERE name = 'alunni'";
        String resetIdPartecipazione = "DELETE FROM sqlite_sequence WHERE name = 'partecipazione'";

        try (Connection conn = DriverManager.getConnection(url);
            Statement stmt = conn.createStatement()) {
            
            stmt.executeUpdate(sqlGite);
            stmt.executeUpdate(sqlClassi);
            stmt.executeUpdate(sqlAlunni);
            stmt.executeUpdate(sqlPartecipazione);
            
            stmt.executeUpdate(resetIdGite);
            stmt.executeUpdate(resetIdClassi);
            stmt.executeUpdate(resetIdAlunni);
            stmt.executeUpdate(resetIdPartecipazione);
            
            System.out.println("Tabella pulita con successo.");
            
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
