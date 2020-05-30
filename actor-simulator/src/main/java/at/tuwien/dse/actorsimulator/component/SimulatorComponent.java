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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@ManagedBean
public class SimulatorComponent {

    private SimulatorService simulatorService;
    private static final Logger LOG = LoggerFactory.getLogger(SimulatorComponent.class);


    @Autowired
    public SimulatorComponent(SimulatorService simulatorService) {
        try {
            this.simulatorService = simulatorService;
            readRoute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readRoute() throws IOException {
        ClassPathResource resource = new ClassPathResource("route.txt");
        InputStream inputStream = resource.getInputStream();
        InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(streamReader);
        List<String> res = new ArrayList<>();
        for (String line; (line = reader.readLine()) != null;) {
            // Process line
            String [] arrayLine = line.split(",");
            Movement movement = new Movement();
            movement.setLongitude(Double.valueOf(arrayLine[0]));
            movement.setLatitude(Double.valueOf(arrayLine[1]));
            movement.setDateTime(LocalDateTime.now());
            simulatorService.putMovementInQueue(movement);
            LOG.info("Sending in queue");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
