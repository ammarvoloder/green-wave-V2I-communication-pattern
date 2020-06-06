package at.tuwien.dse.actorsimulator.component;

import at.tuwien.dse.actorsimulator.dto.TrafficLightStatus;
import at.tuwien.dse.actorsimulator.rabbit.RabbitChannel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.AMQP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class StatusScheduler implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(StatusScheduler.class);
    private static final String MOVEMENT_STATUS_EXCHANGE = "movement_status";

    private List<TrafficLightStatus> trafficLightStatuses;
    private RabbitChannel rabbitChannel;
    private ObjectMapper objectMapper;


    public StatusScheduler(List<TrafficLightStatus> trafficLightStatuses, RabbitChannel rabbitChannel) {
        this.rabbitChannel = rabbitChannel;
        this.trafficLightStatuses = trafficLightStatuses;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    }

    @Override
    public void run() {
        LOG.info("Sendig status at fixed rate...");
        trafficLightStatuses.forEach(trafficLight -> {
            trafficLight.setGreen(!trafficLight.isGreen());
            trafficLight.setDateTime(LocalDateTime.now());
            String msg;
            try {
                msg = objectMapper.writeValueAsString(trafficLight);
                AMQP.BasicProperties messaageId = new AMQP.BasicProperties().builder().messageId("traffic").build();
                rabbitChannel.getChannel().basicPublish(MOVEMENT_STATUS_EXCHANGE, "", messaageId, msg.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
