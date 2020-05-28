package at.tuwien.dse.apigateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;


public class ApiGatewayService {

    private static final Logger LOG = LoggerFactory.getLogger(ApiGatewayService.class);

    private Client client;

    @Autowired()
    public ApiGatewayService() {
        client = ClientBuilder.newClient();
    }
    /**
     * Forward call to insert vehicle to the entity store service and then dao
     *
     * @param producer  The producer of the new vehicle
     * @param vehicleID The id of the new vehicle
     * @param model     The model of the new vehicle
     * @param headerId  Header value of a key id
     * @return Response entity with the status received after vehicle insertion in entity store service
     */
    public ResponseEntity addVehicle(String producer, String vehicleID, String model, String headerId) {
        LOG.info("Send REST request to insert new vehicle with id: " + vehicleID);
        String uri = constructorURIofResource("actor-registry-service", 40001, "addVehicle", "");
        Response response = client.target(uri).queryParam("producer", producer).queryParam("vehicleID", vehicleID).queryParam("model", model)
                .request()
                .build("POST")
                .invoke();

        return ResponseEntity.status(response.getStatus()).body("");
    }

    // construct and return URI of the request for all REST request in one method
    private static String constructorURIofResource(String host, int port, String methodName, String pathParam) {
        StringBuilder stringBuilder = new StringBuilder("http://" + host +  ":" + port + "/" + methodName);
        if (!pathParam.isEmpty()) {
            stringBuilder.append("/").append(pathParam);
        }
        return stringBuilder.toString();
    }


}
