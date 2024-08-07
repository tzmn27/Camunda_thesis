package org.camunda.project;

public class Pruefer {
    private String vorname;
    private String nachname;
    private String titel;
    private boolean verfuegbar;

    public Pruefer(String vorname, String nachname, String titel, Boolean verfuegbar) {
        this.vorname = vorname;
        this.nachname = nachname;
        this.titel = titel;
        this.verfuegbar = verfuegbar;
    }

    public String setJSON() {
        return String.format("{\"data\":{\"Vorname\":"+vorname+",\"Nachname\":"+nachname+",\"Titel\":"+titel+",\"Verfuegbar\":"+verfuegbar+"}}");
    }
}
