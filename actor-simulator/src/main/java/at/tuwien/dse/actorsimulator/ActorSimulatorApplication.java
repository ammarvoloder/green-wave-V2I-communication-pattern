package at.tuwien.dse.actorsimulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;


@SpringBootApplication
public class ActorSimulatorApplication
{
    private static final Logger LOG = LoggerFactory.getLogger(ActorSimulatorApplication.class);

	public static void main(String[] args) throws IOException, InterruptedException {
		SpringApplication.run(ActorSimulatorApplication.class, args);

		Client client = ClientBuilder.newClient();
		List<Location> coordinates = new ArrayList<>();

        ClassPathResource resource = new ClassPathResource("route.txt");
        InputStream inputStream = resource.getInputStream();
        InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(streamReader);

        for (String line; (line = reader.readLine()) != null;) {
            String[] pointString = line.split(",");
            double lat = Double.parseDouble(pointString[0]);
            double lng = Double.parseDouble(pointString[1]);
            double distance = Double.parseDouble(pointString[2]);
            Location location = new Location(lat, lng, distance);
            coordinates.add(location);
        }

        //STARTING SIMULATION
        for(Location location: coordinates){
            sendMovement(client, "AT-525-55", location);
        }
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

    private static void sendMovement(Client client, String id, Location location) throws InterruptedException {
	    //convert speed from km/h to m/s
	    double speed = 80.0 / 3.6;
	    long timeToWait = (long)(location.getDistance() / speed * 1000);
        System.out.println("moram preci " + location.getDistance());
        Thread.sleep(timeToWait);
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
