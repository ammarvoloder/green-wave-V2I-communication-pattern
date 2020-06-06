package at.tuwien.dse.actorsimulator.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Vehicle {

    private String vin;
    private String model;
    private String producer;
    private Double speed;
    @JsonIgnore
    private boolean speedDetermined;

    public Vehicle() {

    }

    public Vehicle(String vin, String model, String producer) {
        this.vin = vin;
        this.model = model;
        this.producer = producer;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public boolean isSpeedDetermined() {
        return speedDetermined;
    }

    public void setSpeedDetermined(boolean speedDetermined) {
        this.speedDetermined = speedDetermined;
    }
}
