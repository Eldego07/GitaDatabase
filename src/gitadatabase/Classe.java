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
    // Attributi
    private int anno;
    private String sezione;
    private String indirizzo;

    public Classe(int anno, String sezione, String indirizzo) {
        this.anno = anno;
        this.sezione = sezione;
        this.indirizzo = indirizzo;
    }

    public int getAnno() {
        return anno;
    }

    public String getSezione() {
        return sezione;
    }

    public String getIndirizzo() {
        return indirizzo;
    }
     
}
