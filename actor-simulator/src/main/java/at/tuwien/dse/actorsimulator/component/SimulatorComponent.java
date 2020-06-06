package at.tuwien.dse.actorsimulator.component;

import at.tuwien.dse.actorsimulator.dto.Movement;
import at.tuwien.dse.actorsimulator.dto.TrafficLightStatus;
import at.tuwien.dse.actorsimulator.dto.Vehicle;
import at.tuwien.dse.actorsimulator.rabbit.RabbitChannel;
import at.tuwien.dse.actorsimulator.service.SimulatorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.DeliverCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@ManagedBean
public class SimulatorComponent {

    private static final Logger LOG = LoggerFactory.getLogger(SimulatorComponent.class);
    private ExecutorService pool = Executors.newFixedThreadPool(3);
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    private SimulatorService simulatorService;
    private List<Movement> movements;
    private List<Vehicle> vehicles;
    private List<TrafficLightStatus> trafficLightStatuses;

    private RabbitChannel rabbitChannel;
    private ObjectMapper objectMapper;

    @Autowired
    public SimulatorComponent(SimulatorService simulatorService) {
        this.rabbitChannel = new RabbitChannel();
        this.simulatorService = simulatorService;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        this.vehicles = new ArrayList<>();
        this.movements = new ArrayList<>();
        this.trafficLightStatuses = new ArrayList<>();

    }

    @PostConstruct
    public void setUp() {
        consumeQueue();
        createVehicles();
        createTrafficLights();
        try {
            scheduler.scheduleAtFixedRate(new StatusScheduler(trafficLightStatuses, rabbitChannel), 0, 10, TimeUnit.SECONDS);
            readRoute();
            pool.execute(new SimulationThread(vehicles.get(0), movements, rabbitChannel));
            Thread.sleep(15000);
            pool.execute(new SimulationThread(vehicles.get(1), movements, rabbitChannel));
            Thread.sleep(15000);
            pool.execute(new SimulationThread(vehicles.get(2), movements, rabbitChannel));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }


    private void createVehicles() {
        saveVehicle("A11-T-111", "C4", "Citroen", 70.0);
        saveVehicle("B22-E-222", "3", "Bmw", 80.0);
        saveVehicle("C33-K-333", "E550", "Mercedes", 90.0);
    }

    private void createTrafficLights() {
        saveTrafficLight(16.34912, 48.16845, 1L, true);
        saveTrafficLight(16.34548, 48.16265, 2L, false);
        saveTrafficLight(16.33786, 48.15213, 3L, true);
    }

    private void consumeQueue() {
        DeliverCallback speedCallback = (consumerTag, message) -> {
            String msg = new String(message.getBody(), StandardCharsets.UTF_8);
            Movement movement = objectMapper.readValue(msg, Movement.class);
            LOG.info("Speed read " + movement.getSpeed());
            vehicles.stream().filter(i -> i.getVin().equals(movement.getVin())).findFirst() //
                    .ifPresent(v -> v.setSpeed(movement.getSpeed()));
        };
        try {
            LOG.info(rabbitChannel.toString());
            rabbitChannel.getChannel().basicConsume("speed_queue", true, speedCallback, consumerTag -> {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readRoute() throws IOException {
        ClassPathResource resource = new ClassPathResource("route.txt");
        InputStream inputStream = resource.getInputStream();
        InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(streamReader);
        for (String line; (line = reader.readLine()) != null; ) {
            // Process line
            String[] arrayLine = line.split(",");
            Movement movement = new Movement();
            movement.setLongitude(Double.parseDouble(arrayLine[0]));
            movement.setLatitude(Double.parseDouble(arrayLine[1]));
            movement.setDistance(Double.parseDouble(arrayLine[2]));
            this.movements.add(movement);
        }
    }

    private void saveVehicle(String vin, String model, String producer, Double speed) {
        Vehicle v = new Vehicle(vin, model, producer);
        v.setSpeed(speed);
        this.simulatorService.saveVehicle(vin, model, producer);
        vehicles.add(v);
    }

    private void saveTrafficLight(Double longitude, Double latitude, Long id, boolean green) {
        this.simulatorService.saveTrafficLight(longitude, latitude, id);
        trafficLightStatuses.add(new TrafficLightStatus(green, id, LocalDateTime.now()));
    }


}
