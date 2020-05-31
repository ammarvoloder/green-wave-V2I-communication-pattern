package at.tuwien.dse.actorsimulator.component;

import at.tuwien.dse.actorsimulator.dto.Movement;
import at.tuwien.dse.actorsimulator.dto.Vehicle;
import at.tuwien.dse.actorsimulator.rabbit.RabbitChannel;
import at.tuwien.dse.actorsimulator.service.SimulatorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.DeliverCallback;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@ManagedBean
public class SimulatorComponent {

    private SimulatorService simulatorService;
    private List<Movement> movements;
    private List<Vehicle> vehicles;
    private static final Logger LOG = LoggerFactory.getLogger(SimulatorComponent.class);
    private ExecutorService pool = Executors.newFixedThreadPool(3);
    private Vehicle v1;
    private Vehicle v2;
    private Vehicle v3;
    private RabbitChannel rabbitChannel;
    private ObjectMapper objectMapper;

    @Autowired
    public SimulatorComponent(SimulatorService simulatorService) {
        try {
            this.rabbitChannel = new RabbitChannel();
            this.simulatorService = simulatorService;
            this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
            this.vehicles = new ArrayList<>();
            this.movements = new ArrayList<>();
            consumeQueue();
            createVehicles();
            createTrafficLights();
            readRoute();
            pool.execute(new SimulationThread(v1, movements, rabbitChannel));
            Thread.sleep(2000);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void createVehicles() {
        v1 = saveVehicle("A11-T-111", "C4", "Citroen", 60.0);
        v2 = saveVehicle("B22-E-222", "3", "Bmw", 80.0);
        v3 = saveVehicle("C33-K-333", "E550", "Mercedes", 70.0);
        vehicles.add(v1);
        vehicles.add(v2);
        vehicles.add(v3);
    }

    private void createTrafficLights(){
        saveTrafficLight(16.34912 ,48.16845);
        saveTrafficLight(16.34548, 48.16265);
        saveTrafficLight(16.33786,48.15213);
    }

    private void consumeQueue(){
        DeliverCallback speedCallback = (consumerTag, message) -> {
            String msg = new String(message.getBody(), StandardCharsets.UTF_8);
            Movement movement = objectMapper.readValue(msg, Movement.class);
            LOG.info("Speed read");
            vehicles.stream().filter(i->i.getVin().equals(movement.getVin())).findFirst() //
                    .ifPresent(v -> v.setSpeed(movement.getSpeed()));
        };
        try {
            LOG.info(rabbitChannel.toString());
            rabbitChannel.getChannel().basicConsume("speed_queue", true, speedCallback, consumerTag -> {});
        } catch (IOException e) {
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

    private Vehicle saveVehicle(String vin, String model, String producer, Double speed){
        Vehicle v = new Vehicle(vin, model, producer);
        v.setSpeed(speed);
        this.simulatorService.saveVehicle(vin, model, producer);
        return v;
    }

    private void saveTrafficLight(double longitude, double latitude){
        this.simulatorService.saveTrafficLight(longitude, latitude);
    }

    private void sendMovement(Movement movement){
        movement.setDateTime(LocalDateTime.now());
        movement.setSpeed(60.0);
        movement.setCrash(false);
        try {
            this.simulatorService.putMovementInQueue(movement);
            double speed = movement.getSpeed() / 3.6;
            long timeToWait = (long)(movement.getDistance() / speed * 1000);
            Thread.sleep(timeToWait);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
