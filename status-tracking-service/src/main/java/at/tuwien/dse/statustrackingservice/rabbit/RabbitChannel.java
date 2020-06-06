package at.tuwien.dse.statustrackingservice.rabbit;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitChannel {

    public static final String SPEED_QUEUE = "speed_queue";
    private static final Logger LOG = LoggerFactory.getLogger(RabbitChannel.class);
    private static final String STATUS_QUEUE = "status_queue";
    private static final String MOVEMENT_STATUS_EXCHANGE = "movement_status";
    private Channel channel;


    public RabbitChannel() {
        createConnection();
    }

    private void createConnection() {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        connectionFactory.setPort(5672);
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");

        LOG.info("Opening connection with rabbitmq.");
        Connection connection = null;
        try {
            connection = connectionFactory.newConnection();
            channel = connection.createChannel();

            channel.exchangeDeclare(MOVEMENT_STATUS_EXCHANGE, "fanout");
            String mov_status_queue = channel.queueDeclare(STATUS_QUEUE, false, false, false, null).getQueue();
            channel.queueBind(mov_status_queue, MOVEMENT_STATUS_EXCHANGE, "");
            // we don't have to read from speed_queue because determined speed will be saved with a next movement
            //channel.queueDeclare(SPEED_QUEUE, false, false, false, null);
            LOG.info("Creating new rabbitmq channel.");
        } catch (IOException | TimeoutException e) {
            LOG.error(e.getMessage());
        }
    }

    public Channel getChannel() {
        return channel;
    }
}

