package org.camunda.project;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Named;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestClient;

@RequestMapping("/config")
@Named
public class Test implements JavaDelegate {

    private final ConfigProperties config;

    public Test(ConfigProperties config) {
        this.config = config;
    }

    @Override
    @GetMapping
    public void execute(DelegateExecution execution){
        String token = config.apiToken();
        String vorname = (String) execution.getVariable("vorname");
        String nachname = (String) execution.getVariable("nachname");
        String titel = (String) execution.getVariable("titel");
        String test = (String) execution.getVariable("verfuegbar");
        boolean verfuegbar;

        verfuegbar = test.equals("true");

        String data = String.format("{\"data\":{\"Vorname\":\""+vorname+"\",\"Nachname\":\""+nachname+"\",\"Titel\":\""+titel+"\",\"Verfuegbar\":\""+verfuegbar+"\"}}");

        RestClient restClient = RestClient.builder().baseUrl("http://localhost:1337/api/").build();

        Pruefer neu = new Pruefer(vorname, nachname, titel, verfuegbar);

        ResponseEntity<Void> response = restClient.post()
                .uri("pruefers/")
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .body(data)
                .retrieve()
                .toBodilessEntity();
    }
}
