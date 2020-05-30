package at.tuwien.dse.actorsimulator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;


@SpringBootApplication
public class ActorSimulatorApplication
{

	public static void main(String[] args) throws IOException
	{
		SpringApplication.run(ActorSimulatorApplication.class, args);

		Client client = ClientBuilder.newClient();

		saveVehicle(client, "A70-M-622", "5", "bmw");
	}

	private static void readRoute() throws IOException
	{
		ClassPathResource resource = new ClassPathResource("route.txt");
		InputStream inputStream = resource.getInputStream();
		InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
		BufferedReader reader = new BufferedReader(streamReader);
		List<String> res = new ArrayList<>();
		for (String line; (line = reader.readLine()) != null;) {
			// Process line
			System.out.println(line);
			res.add(line);
		}

		System.out.println(res.get(res.size()-1));
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

	// construct and return URI of the request for all REST request in one method
	private static String constructorURIofResource(String host, int port, String methodName, String pathParam) {
		StringBuilder stringBuilder = new StringBuilder("http://" + host +  ":" + port + "/" + methodName);
		if (!pathParam.isEmpty()) {
			stringBuilder.append("/").append(pathParam);
		}
		return stringBuilder.toString();
	}

}
