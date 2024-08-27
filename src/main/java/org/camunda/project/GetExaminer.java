package org.camunda.project;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Named;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.spin.Spin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

/*
Class for testing to fill list of examiners dynamically.
This class is not implemented in the process, it can be used for further development.
*
*
*
*
*
*
*
*
*
*
*
*
*
*
*
*
*/
@RequestMapping("/config")
@Named
public class GetExaminer implements JavaDelegate {
    //config for the API-Token
    private final ConfigProperties config;

    public GetExaminer(ConfigProperties config) {
        this.config = config;
    }

    @Override
    @GetMapping
    public void execute(DelegateExecution execution) {
        //Token
        String token = config.apiToken();

        //Set map for examiner with value
        Map<String, String> variables = new HashMap<>();

        //Build restClient Object
        RestClient restClient = RestClient.builder().baseUrl("http://localhost:1337/api/").build();

        //HTTP-Request
        String result = restClient.get()
                .uri("pruefers/?filters[verfuegbar][$eq]=true").header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .retrieve()
                .body(String.class);

        //Save every available examiner in the Map
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode node = mapper.readTree(result).get("data");
            int counter = 0;

            for (JsonNode subnode : node) {
                counter++;
                String prof = subnode.get("attributes").get("nachname").asText();
                String counterString = String.valueOf(counter);
                variables.put(prof, counterString);
            }
            execution.setVariable("selectExaminer", Spin.JSON(variables));

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
