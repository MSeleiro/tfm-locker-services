package ctt.locker.services.communication.controller;

import ctt.locker.services.communication.model.Order;
import ctt.locker.services.communication.model.State;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest;
import java.net.URI;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.time.Duration;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class OrderProcessingServiceController {

    private static String DAPR_HOST = System.getenv().getOrDefault("DAPR_HOST", "http://localhost");
    private static String DAPR_HTTP_PORT = System.getenv().getOrDefault("DAPR_HTTP_PORT", "3500");
    private static final String DAPR_STATE_STORE = "statestore";
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public void saveOrders(State[] states) { 
        try {
            URI baseUrl = new URI(DAPR_HOST+":"+DAPR_HTTP_PORT);
            URI stateStoreUrl = new URI(baseUrl + "/v1.0/state/" + DAPR_STATE_STORE);

            ObjectMapper objectMapper = new ObjectMapper();

            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(states)))
                    .uri(stateStoreUrl)
                    .build();
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
    }


    @PostMapping(path = "/orders", consumes = MediaType.ALL_VALUE)
    public String processOrders(@RequestBody Order order) {
        State state = new State(String.valueOf(order.getOrderId()), order);
        State[] payload = new State[] {state};

        saveOrders(payload);

        return "CID" + order.getOrderId();
    }
}
