package ru.iwater.yourwater.iwaterlogistic.domain;

public class OrderMap {
    String id;
    String name;
    String time;
    String period;
    String status;
    String coords;

    public OrderMap(String id, String name, String time, String period, String status, String coords) {
        this.id = id;
        this.name = name;
        this.time = time;
        this.period = period;
        this.status = status;
        this.coords = coords;
    }

    public OrderMap() {
        this.id = "";
        this.name = "";
        this.time = "";
        this.period = "";
        this.status = "";
        this.coords = "";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCoords() {
        return coords;
    }

    public void setCoords(String coords) {
        this.coords = coords;
    }
}
