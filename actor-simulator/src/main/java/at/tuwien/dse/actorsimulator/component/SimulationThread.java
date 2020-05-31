package at.tuwien.dse.actorsimulator.component;

import at.tuwien.dse.actorsimulator.dto.Movement;
import at.tuwien.dse.actorsimulator.dto.Vehicle;
import at.tuwien.dse.actorsimulator.rabbit.RabbitChannel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class SimulationThread implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(SimulationThread.class);

    private Vehicle vehicle;
    private List<Movement> movements;
    private RabbitChannel rabbitChannel;
    private ObjectMapper objectMapper;

    public SimulationThread(Vehicle vehicle, List<Movement> movements, RabbitChannel rabbitChannel){
        this.vehicle = vehicle;
        this.movements = movements;
        this.rabbitChannel = rabbitChannel;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }
    @Override
    public void run() {
        for(int i=0; i<movements.size(); i++){
            try {
                Movement m = movements.get(i);
                m.setSpeed(vehicle.getSpeed());
                m.setVin(vehicle.getVin());
                m.setDateTime(LocalDateTime.now());
                m.setCrash(false);
                String msg = objectMapper.writeValueAsString(m);
                LOG.info("Getting vehicle's speed: " + vehicle.getSpeed());
                // rabbitChannel.getChannel().basicPublish("", "movement_queue", null, msg.getBytes());
                double speed = m.getSpeed() / 3.6;
                long timeToWait = (long) (m.getDistance() / speed * 1000);
                Thread.sleep(timeToWait);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
