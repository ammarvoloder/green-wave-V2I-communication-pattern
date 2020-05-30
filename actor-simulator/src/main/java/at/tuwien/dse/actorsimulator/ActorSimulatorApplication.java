package at.tuwien.dse.actorsimulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import sun.rmi.runtime.Log;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;


@SpringBootApplication
public class ActorSimulatorApplication
{
    private static final Logger LOG = LoggerFactory.getLogger(ActorSimulatorApplication.class);

	public static void main(String[] args)
	{
		SpringApplication.run(ActorSimulatorApplication.class, args);

		Client client = ClientBuilder.newClient();

		saveVehicle(client, "A70-M-622", "5", "bmw");
		getVehicles(client);
	}


	private static void saveVehicle(Client client, String vehicleId, String model, String producer) {
		String uri = constructorURIofResource("api-gateway",  10113, "addVehicle",  "");
		client.target(uri)
				.queryParam("vehicleID", vehicleId)
				.queryParam("producer", producer)
				.queryParam("model", model)
				.request().header("id", producer)
				.build("POST")
				.invoke();
	}

	private static void getVehicles(Client client){
		String uri = constructorURIofResource("api-gateway",  10113, "getAllVehicles",  "");
		Response response = client.target(uri)
				.request()
				.build("GET")
				.invoke();
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
