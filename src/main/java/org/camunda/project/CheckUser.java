package org.camunda.project;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Named;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestClient;

@RequestMapping("/config")
@Named
public class CheckUser implements JavaDelegate {
    //config for the API-Token
    private final ConfigProperties config;

    public CheckUser(ConfigProperties config) {
        this.config = config;
    }

    @Override
    @GetMapping
    public void execute(DelegateExecution execution) {
        //Token
        String token = config.apiToken();

        //Get userID
        String nutzer = (String) execution.getVariable("initiator");

        //Build restClient Object
        RestClient restClient = RestClient.builder().baseUrl("http://localhost:1337/api/").build();

        //HTTP-Request
        String result = restClient.get()
                .uri("students/?filters[user][$eq]=" + nutzer)
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .retrieve()
                .body(String.class);

        //Check if data is emtpy -> empty = no masterdata existing
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode node = mapper.readTree(result).get("data");
            if (node.isEmpty()) {
                execution.setVariable("credentials", false);
                //If user is Han, set different UserID so the flow does not fail
                if (nutzer.equals("Han")) {
                    execution.setVariable("studentID", 3);
                }
            } else {
                execution.setVariable("credentials", true);
                //Set userID
                execution.setVariable("studentID", node.get(0).get("id").asInt());
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
