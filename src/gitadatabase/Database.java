package gitadatabase;

import java.sql.*;
import java.util.ArrayList;

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
        String sqlGite =
            "CREATE TABLE IF NOT EXISTS gite (" +
            "  git_id           INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "  git_destinazione VARCHAR(30), " +
            "  git_durata       INTEGER, " +
            "  git_prezzo       INTEGER" +
            ");";

        String sqlClassi =
            "CREATE TABLE IF NOT EXISTS classi (" +
            "  cla_id        INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "  cla_anno      INTEGER, " +
            "  cla_sezione   VARCHAR(1), " +
            "  cla_indirizzo VARCHAR(10)" +
            ");";

        String sqlAlunni =
            "CREATE TABLE IF NOT EXISTS alunni (" +
            "  alu_matr    INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "  alu_nome    VARCHAR(15), " +
            "  alu_cognome VARCHAR(15), " +
            "  alu_cla_id  INTEGER, " +
            "  FOREIGN KEY(alu_cla_id) REFERENCES classi(cla_id)" +
            ");";

        String sqlPartecipazione =
            "CREATE TABLE IF NOT EXISTS partecipazione (" +
            "  par_id     INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "  par_alu_id INTEGER, " +
            "  par_git_id INTEGER, " +
            "  FOREIGN KEY(par_alu_id) REFERENCES alunni(alu_matr), " +
            "  FOREIGN KEY(par_git_id) REFERENCES gite(git_id)" +
            ");";

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
            stmt.execute(sqlGite);
            stmt.execute(sqlClassi);
            stmt.execute(sqlAlunni);
            stmt.execute(sqlPartecipazione);
            System.out.println("Tabelle create correttamente!");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // ================================================================== GITE

    /** Inserisce una gita e restituisce l'id assegnato dal DB (-1 se errore). */
    public int aggiungiGita(Gita g) {
        String insert = "INSERT INTO gite(git_destinazione, git_durata, git_prezzo) VALUES(?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, g.getLuogo());
            pstmt.setInt(2, g.getDurata());
            pstmt.setInt(3, g.getPrezzo());
            pstmt.executeUpdate();
            ResultSet keys = pstmt.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return -1;
    }

    /** Elimina una gita e tutte le sue partecipazioni (transazione). */
    public void eliminaGita(int gitId) {
        try (Connection conn = DriverManager.getConnection(url)) {
            conn.setAutoCommit(false);
            try {
                PreparedStatement delPart = conn.prepareStatement(
                    "DELETE FROM partecipazione WHERE par_git_id = ?");
                delPart.setInt(1, gitId);
                delPart.executeUpdate();

                PreparedStatement delGita = conn.prepareStatement(
                    "DELETE FROM gite WHERE git_id = ?");
                delGita.setInt(1, gitId);
                delGita.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Rollback eliminaGita: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    /** Restituisce tutte le gite (senza studenti). */
    public ArrayList<Gita> getGite() {
        ArrayList<Gita> lista = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM gite")) {
            while (rs.next()) {
                lista.add(new Gita(
                    rs.getInt("git_id"),
                    rs.getString("git_destinazione"),
                    rs.getInt("git_durata"),
                    rs.getInt("git_prezzo")
                ));
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return lista;
    }

    public void stampaGite() {
        for (Gita g : getGite())
            System.out.println(g.getId() + " | " + g.getLuogo() + " | " +
                               g.getDurata() + " giorni | " + g.getPrezzo() + " euro");
    }

    // ================================================================ CLASSI

    /** Inserisce una classe e restituisce l'id assegnato dal DB (-1 se errore). */
    public int aggiungiClasse(Classe c) {
        String insert = "INSERT INTO classi(cla_anno, cla_sezione, cla_indirizzo) VALUES(?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, c.getAnno());
            pstmt.setString(2, c.getSezione());
            pstmt.setString(3, c.getIndirizzo());
            pstmt.executeUpdate();
            ResultSet keys = pstmt.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return -1;
    }

    /** Restituisce tutte le classi. */
    public ArrayList<Classe> getClassi() {
        ArrayList<Classe> lista = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM classi")) {
            while (rs.next()) {
                lista.add(new Classe(
                    rs.getInt("cla_id"),
                    rs.getInt("cla_anno"),
                    rs.getString("cla_sezione"),
                    rs.getString("cla_indirizzo")
                ));
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return lista;
    }

    public void stampaClassi() {
        for (Classe c : getClassi())
            System.out.println(c.getId() + " | " + c.getAnno() + " | " +
                               c.getSezione() + " | " + c.getIndirizzo());
    }

    // ================================================================ ALUNNI

    /**
     * Inserisce un alunno e restituisce la matricola assegnata dal DB (-1 se errore).
     * Richiede che Classe abbia già un id valido (cioè sia stata salvata nel DB).
     */
    public int aggiungiAlunno(Studente s) {
        String insert = "INSERT INTO alunni(alu_nome, alu_cognome, alu_cla_id) VALUES(?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, s.getNome());
            pstmt.setString(2, s.getCognome());
            pstmt.setInt(3, s.getClasse().getId());
            pstmt.executeUpdate();
            ResultSet keys = pstmt.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return -1;
    }

    /** Elimina un alunno e tutte le sue partecipazioni (transazione). */
    public void eliminaAlunno(int aluMatr) {
        try (Connection conn = DriverManager.getConnection(url)) {
            conn.setAutoCommit(false);
            try {
                PreparedStatement delPart = conn.prepareStatement(
                    "DELETE FROM partecipazione WHERE par_alu_id = ?");
                delPart.setInt(1, aluMatr);
                delPart.executeUpdate();

                PreparedStatement delAlu = conn.prepareStatement(
                    "DELETE FROM alunni WHERE alu_matr = ?");
                delAlu.setInt(1, aluMatr);
                delAlu.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Rollback eliminaAlunno: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Restituisce gli alunni iscritti a una gita tramite JOIN su partecipazione.
     * Ha bisogno della lista classi già caricata per associare l'oggetto Classe.
     */
    public ArrayList<Studente> getAlunniPerGita(int gitId, ArrayList<Classe> classi) {
        ArrayList<Studente> lista = new ArrayList<>();
        String query =
            "SELECT a.alu_matr, a.alu_nome, a.alu_cognome, a.alu_cla_id " +
            "FROM alunni a " +
            "JOIN partecipazione p ON p.par_alu_id = a.alu_matr " +
            "WHERE p.par_git_id = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, gitId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Classe classe = cercaClassePerId(classi, rs.getInt("alu_cla_id"));
                lista.add(new Studente(
                    rs.getInt("alu_matr"),
                    rs.getString("alu_nome"),
                    rs.getString("alu_cognome"),
                    classe
                ));
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return lista;
    }

    // =========================================================== PARTECIPAZIONE

    /** Registra la partecipazione di un alunno a una gita. */
    public void aggiungiPartecipazione(int aluMatr, int gitId) {
        String insert = "INSERT INTO partecipazione(par_alu_id, par_git_id) VALUES(?, ?)";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(insert)) {
            pstmt.setInt(1, aluMatr);
            pstmt.setInt(2, gitId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    /** Rimuove la partecipazione di un alunno a una gita specifica. */
    public void eliminaPartecipazione(int aluMatr, int gitId) {
        String del = "DELETE FROM partecipazione WHERE par_alu_id = ? AND par_git_id = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(del)) {
            pstmt.setInt(1, aluMatr);
            pstmt.setInt(2, gitId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // ================================================================= RESET

    public void cancellaDati() {
        String[] tabelle = {"partecipazione", "alunni", "classi", "gite"};
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = OFF");
            for (String t : tabelle) {
                stmt.executeUpdate("DELETE FROM " + t);
                stmt.executeUpdate("DELETE FROM sqlite_sequence WHERE name = '" + t + "'");
            }
            stmt.execute("PRAGMA foreign_keys = ON");
            System.out.println("Tabelle pulite con successo.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    // =============================================================== UTILITY

    private Classe cercaClassePerId(ArrayList<Classe> classi, int id) {
        for (Classe c : classi)
            if (c.getId() == id) return c;
        return null;
    }
}