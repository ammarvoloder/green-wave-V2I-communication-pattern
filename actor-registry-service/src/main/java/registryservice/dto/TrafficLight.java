package registryservice.dto;

import java.time.LocalDateTime;

public class TrafficLight {

    private Long id;
    private Boolean color;
    private Double longitude;
    private Double latitude;
    private LocalDateTime timestamp;

    public TrafficLight(Long id, Boolean color, Double longitude, Double latitude, LocalDateTime timestamp){
        this.id = id;
        this.color = color;
        this.longitude = longitude;
        this.latitude = latitude;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean isColor() {
        return color;
    }

    public void setColor(boolean color) {
        this.color = color;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
