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
    private TrafficLightStatus trafficLightStatus;
    private RabbitChannel rabbitChannel;
    private ObjectMapper objectMapper;


    public StatusScheduler(List<TrafficLightStatus> trafficLightStatuses, RabbitChannel rabbitChannel) {
        this.rabbitChannel = rabbitChannel;
        this.trafficLightStatuses = trafficLightStatuses;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    }

    public StatusScheduler(TrafficLightStatus trafficLightStatus, RabbitChannel rabbitChannel) {
        this.rabbitChannel = rabbitChannel;
        this.trafficLightStatus = trafficLightStatus;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Override
    public void run() {
        if (trafficLightStatus == null) {
            LOG.info("Sendig status at fixed rate...");
            trafficLightStatuses.forEach(trafficLight -> {
                if (trafficLight.isManualAdjusted()) {
                    trafficLight.setManualAdjusted(false);
                    return;
                }
                trafficLight.setGreen(!trafficLight.isGreen());
                trafficLight.setDateTime(LocalDateTime.now());
                try {
                    String msg = objectMapper.writeValueAsString(trafficLight);
                    AMQP.BasicProperties messageId = new AMQP.BasicProperties().builder().messageId("traffic").build();
                    rabbitChannel.getChannel().basicPublish(MOVEMENT_STATUS_EXCHANGE, "", messageId, msg.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else {
            LOG.info("Sending status after NCE event...");
            trafficLightStatus.setGreen(true);
            trafficLightStatus.setDateTime(LocalDateTime.now());
            trafficLightStatus.setManualAdjusted(true);
            try {
                String msg = objectMapper.writeValueAsString(trafficLightStatus);
                AMQP.BasicProperties messageId = new AMQP.BasicProperties().builder().messageId("traffic").build();
                rabbitChannel.getChannel().basicPublish(MOVEMENT_STATUS_EXCHANGE, "", messageId, msg.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
