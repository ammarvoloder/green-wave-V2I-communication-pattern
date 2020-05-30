package at.tuwien.dse.statustrackingservice.rabbit;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitChannel {

    private static final Logger LOG = LoggerFactory.getLogger(RabbitChannel.class);
    private static final String MOVEMENT_QUEUE = "movement_queue";
    private Channel channel;


    public RabbitChannel() {
        createConnection();
    }

    private void createConnection() {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("rabbitmq");
        connectionFactory.setPort(5672);
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");

        LOG.info("Opening connection with rabbitmq.");
        Connection connection = null;
        try {
            connection = connectionFactory.newConnection();
            channel = connection.createChannel();
            channel.queueDeclare(MOVEMENT_QUEUE, false, false, false, null);
            LOG.info("Creating new rabbitmq channel.");
        } catch (IOException | TimeoutException e) {
            LOG.error(e.getMessage());
        }
    }

    public Channel getChannel() {
        return channel;
    }
}

