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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@RequestMapping("/config")
@Named
public class SendThesis implements JavaDelegate {
    //config for the API-Token
    private final ConfigProperties config;

    public SendThesis(ConfigProperties config) {
        this.config = config;
    }

    @Override
    @GetMapping
    public void execute(DelegateExecution execution) {
        //Token
        String token = config.apiToken();

        //Settings for the date fields
        String pattern = "yyyy-MM-dd";
        SimpleDateFormat formatCamunda = new SimpleDateFormat("dd.MM.yyyy");
        SimpleDateFormat formatStrapi = new SimpleDateFormat(pattern);
        Date date;
        try {
            date = formatCamunda.parse((String) execution.getVariable("startDate"));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        String startDate = formatStrapi.format(date);
        String beantragungunsdatum = new SimpleDateFormat(pattern).format(new Date());

        //Get variables
        String degree = (String) execution.getVariable("degree");
        String title = (String) execution.getVariable("title");
        int firstExaminer = (int) execution.getVariable("firstExaminer");
        int secondExaminer = (int) execution.getVariable("secondExaminer");
        int studentID = (int) execution.getVariable("studentID");
        int thesisID;
        String putData;
        String examinerIDs = String.format("[" + firstExaminer + "," + secondExaminer + "]");

        //JSON body into String
        String postData = String.format("{\"data\":{\"studienabschluss\":\"" + degree + "\",\"titel\":\"" + title + "\",\"startdatum\":\"" + startDate + "\",\"beantragungsdatum\":\"" + beantragungunsdatum + "\"}}");

        //Build restClient Object
        RestClient restClient = RestClient.builder().baseUrl("http://localhost:1337/api/").build();

        //HTTP-Request
        ResponseEntity<Void> postResponse = restClient.post()
                .uri("theses/")
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .body(postData)
                .retrieve()
                .toBodilessEntity();

        //Sleep timer
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //HTTP-Request
        String result = restClient.get()
                .uri("theses/?filters[titel][$eq]=" + title).header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .retrieve()
                .body(String.class);

        //Save thesis
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode node = mapper.readTree(result).get("data");
            thesisID = node.get(0).get("id").asInt();
            execution.setVariable("thesisID", thesisID);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        //Sleep timer
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //JSON body into String
        putData = String.format("{\"data\":{\"student\": " + studentID + ",\"pruefer\": " + examinerIDs + "}}");

        //HTTP-Request
        ResponseEntity<Void> putResponse = restClient.put()
                .uri("theses/" + thesisID)
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .body(putData)
                .retrieve()
                .toBodilessEntity();

    }
}