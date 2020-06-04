package at.tuwien.dse.actorsimulator.dto;

import java.time.LocalDateTime;

public class TrafficLightStatus {

    private boolean green;
    private Long trafficLightId;

    public TrafficLightStatus(){

    }
    public TrafficLightStatus(boolean green, Long trafficLightId, LocalDateTime dateTime) {
        this.green = green;
        this.trafficLightId = trafficLightId;
        this.dateTime = dateTime;
    }

    public boolean isGreen() {
        return green;
    }

    public void setGreen(boolean green) {
        this.green = green;
    }

    public Long getTrafficLightId() {
        return trafficLightId;
    }

    public void setTrafficLightId(Long trafficLightId) {
        this.trafficLightId = trafficLightId;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    private LocalDateTime dateTime;
}
