/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gitadatabase;

/**
 *
 * @author taboada.taddeo
 */
public class Classe {
    private int id;        // cla_id (PK dal DB)
    private int anno;
    private String sezione;
    private String indirizzo;

    /** Usato quando si carica dal DB (id già noto). */
    public Classe(int id, int anno, String sezione, String indirizzo) {
        this.id = id;
        this.anno = anno;
        this.sezione = sezione;
        this.indirizzo = indirizzo;
    }

    /** Usato prima di salvare nel DB (id non ancora noto, messo a 0). */
    public Classe(int anno, String sezione, String indirizzo) {
        this(0, anno, sezione, indirizzo);
    }

    public int getId()          { return id; }
    public void setId(int id)   { this.id = id; }
    public int getAnno()        { return anno; }
    public String getSezione()  { return sezione; }
    public String getIndirizzo(){ return indirizzo; }

    @Override
    public String toString() {
        return anno + "ª " + sezione + " - " + indirizzo;
    }
}
