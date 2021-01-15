package ru.iwater.yourwater.iwaterlogistic.domain;

public class Order {

    String id;
    String name;
    String order;
    String cash;
    String cash_b;
    String time;
    String contact;
    String notice;
    String date;
    String period;
    String address;
    String status;
    String coords;

    public Order(String id, String name, String order, String cash, String cash_b, String time, String contact, String notice, String date, String period, String address, String status, String coords) {
        this.id = id;
        this.name = name;
        this.order = order;
        this.cash = cash;
        this.cash_b = cash_b;
        this.time = time;
        this.contact = contact;
        this.notice = notice;
        this.date = date;
        this.period = period;
        this.address = address;
        this.status = status;
        this.coords = coords;
    }

    public Order() {
        this.id = "";
        this.name = "";
        this.order = "";
        this.cash = "";
        this.cash_b = "";
        this.time = "";
        this.contact = "";
        this.notice = "";
        this.date = "";
        this.period = "";
        this.address = "";
        this.status = "";
        this.coords = "";
    }

    public String getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public String getOrder(){
        return order;
    }

    public String getCash(){
        return cash;
    }

    public String getCash_b() {
        return cash_b;
    }

    public String getTime() {
        return time;
    }

    public String getContact(){
        return contact;
    }

    public String getNotice(){
        return notice;
    }

    public String getDate(){
        return date;
    }

    public String getPeriod(){
        return period;
    }

    public String getAddress(){
        return address;
    }

    public String getStatus(){
        return status;
    }

    public String getCoords(){
        return coords;
    }

    public String getAllOrder(){
        return id + " " + name + " "+ order + " "+ cash + " "+ contact + " "+ notice + " "+ date + " "+ period + " "+ address + " "+ status + " "+ coords;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public void setCash(String cash) {
        this.cash = cash;
    }

    public void setCash_b(String cash_b) {
        this.cash_b = cash_b;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }

    public void setCoords(String coords) {
        this.coords = coords;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
