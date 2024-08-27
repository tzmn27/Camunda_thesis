package org.camunda.project;

import jakarta.inject.Named;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestClient;

@RequestMapping("/config")
@Named
public class Approval implements JavaDelegate {
    //config for the API-Token
    private final ConfigProperties config;

    public Approval(ConfigProperties config) {
        this.config = config;
    }

    @Override
    @GetMapping
    public void execute(DelegateExecution execution) {
        //Token
        String token = config.apiToken();

        //Get variables
        int thesisID = (int) execution.getVariable("thesisID");
        String approvalString = (String) execution.getVariable("approve");
        boolean approval = approvalString.equals("true");

        //Format json-body into string
        String data = String.format("{\"data\":{\"zulassung\": " + approval + "}}");

        //Build restClient Object
        RestClient restClient = RestClient.builder().baseUrl("http://localhost:1337/api/").build();

        //HTTP-Request
        ResponseEntity<Void> response = restClient.put()
                .uri("theses/" + thesisID)
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .body(data)
                .retrieve()
                .toBodilessEntity();
    }
}
