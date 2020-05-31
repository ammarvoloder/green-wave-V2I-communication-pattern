package at.tuwien.dse.actorsimulator.component;

import at.tuwien.dse.actorsimulator.dto.Movement;
import at.tuwien.dse.actorsimulator.service.SimulatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.ManagedBean;
import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@ManagedBean
public class SimulatorComponent {

    private SimulatorService simulatorService;
    private List<Movement> movements;
    private static final Logger LOG = LoggerFactory.getLogger(SimulatorComponent.class);

    @Autowired
    public SimulatorComponent(SimulatorService simulatorService) {
        try {
            this.simulatorService = simulatorService;
            this.movements = new ArrayList<>();
            saveVehicle("AT5-T-531", "c4", "citroen");
            Thread.sleep(1000);
            saveTrafficLight(16.34912 ,48.16845);
            saveTrafficLight(16.34548, 48.16265);
            saveTrafficLight(16.33786,48.15213);
            readRoute();
            for(Movement m: movements){
                sendMovement(m);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void readRoute() throws IOException {
        ClassPathResource resource = new ClassPathResource("route.txt");
        InputStream inputStream = resource.getInputStream();
        InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(streamReader);
        for (String line; (line = reader.readLine()) != null;) {
            // Process line
            String [] arrayLine = line.split(",");
            Movement movement = new Movement();
            movement.setLongitude(Double.valueOf(arrayLine[0]));
            movement.setLatitude(Double.valueOf(arrayLine[1]));
            movement.setDistance(Double.valueOf(arrayLine[2]));
            this.movements.add(movement);
        }
    }

    private void saveVehicle(String vid, String model, String producer){
        this.simulatorService.saveVehicle(vid, model, producer);
    }

    private void saveTrafficLight(double longitude, double latitude){
        this.simulatorService.saveTrafficLight(longitude, latitude);
    }

    private void sendMovement(Movement movement){
        movement.setDateTime(LocalDateTime.now());
        movement.setVin("AT5-T-531");
        movement.setSpeed(60.0);

    }

}
