package ru.iwater.yourwater.iwaterlogistic.domain;

public class NotificationOrder {
    String id;
    String period;
    String status;
    String address;
    String date;
    public boolean notify;
    public boolean isFail;

    public NotificationOrder(String id, String period, String address, String status, String date) {
        this.id = id;
        this.period = period;
        this.status = status;
        this.address = address;
        this.date = date;
        this.notify = false;
        this.isFail = false;
    }

    public  NotificationOrder() {
        this.id = "";
        this.period = "";
        this.status = "";
        this.date = "";
        this.notify = false;
        this.isFail = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean getNotify() {
        return notify;
    }

    public void setNotify(boolean notify) {
        this.notify = notify;
    }

    public boolean isFail() {
        return isFail;
    }

    public void setFail(boolean fail) {
        isFail = fail;
    }
}
