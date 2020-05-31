package at.tuwien.dse.actorsimulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;


@SpringBootApplication
public class ActorSimulatorApplication
{
    private static final Logger LOG = LoggerFactory.getLogger(ActorSimulatorApplication.class);

	public static void main(String[] args) throws IOException, InterruptedException {
        SpringApplication.run(ActorSimulatorApplication.class, args);

    }


}
