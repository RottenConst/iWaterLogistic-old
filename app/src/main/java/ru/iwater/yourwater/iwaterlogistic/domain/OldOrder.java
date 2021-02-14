package ru.iwater.yourwater.iwaterlogistic.domain;

public class OldOrder {
    String id;
    String date;
    String period;
    String address;
    String status;

    public OldOrder(String id, String date, String period, String address, String status) {
        this.id = id;
        this.date = date;
        this.period = period;
        this.address = address;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String getPeriod() {
        return period;
    }

    public String getAddress() {
        return address;
    }

    public String getStatus() {
        return status;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
