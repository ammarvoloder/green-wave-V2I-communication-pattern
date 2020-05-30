package at.tuwien.dse.actorsimulator.service;

import at.tuwien.dse.actorsimulator.dto.Movement;
import at.tuwien.dse.actorsimulator.rabbit.RabbitChannel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SimulatorService {


    private RabbitChannel rabbitChannel;
    private ObjectMapper objectMapper;

    @Autowired
    public SimulatorService() {
        rabbitChannel = new RabbitChannel();
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    }

    public void putMovementInQueue(Movement movement) throws IOException {
        String msg = objectMapper.writeValueAsString(movement);
        rabbitChannel.getChannel().basicPublish("","movement_queue",null,msg.getBytes());
    }
}
