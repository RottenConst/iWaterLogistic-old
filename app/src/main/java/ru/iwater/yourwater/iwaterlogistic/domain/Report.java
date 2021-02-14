package ru.iwater.yourwater.iwaterlogistic.domain;

import java.util.Date;

import ru.iwater.yourwater.iwaterlogistic.utils.TypeCash;

public class Report {

    final String date;
    float cash = 0.00F;
    float non_cash = 0.00F;
    float on_site = 0.00F;
    float on_terminal = 0.00F;
    float transfer = 0.00F;
    int tank = 0;
    int orderCount = 0;

    public Report(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public float getCash() {
        return cash;
    }

    public void setCash(float cash) {
        this.cash = cash + this.cash;
    }

    public float getNon_cash() {
        return non_cash;
    }

    public float getOn_site() {
        return on_site;
    }

    public float getOn_terminal() {
        return on_terminal;
    }

    public float getTransfer() {
        return transfer;
    }

    public int getTank() {
        return tank;
    }

    public void setNon_cash(float non_cash) {
        this.non_cash = non_cash + this.non_cash;
    }

    public void setOn_site(float on_site) {
        this.on_site = on_site + this.on_site;
    }

    public void setOn_terminal(float on_terminal) {
        this.on_terminal = on_terminal + this.on_terminal;
    }

    public void setTank(int tank) {
        this.tank = tank + this.tank;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }

    public void setTransfer(float transfer) {
        this.transfer += transfer;
    }

    public float getFullCash() {
        float fullCash;
        fullCash = cash + non_cash + on_site + on_terminal + transfer;
        return fullCash;
    }
}
