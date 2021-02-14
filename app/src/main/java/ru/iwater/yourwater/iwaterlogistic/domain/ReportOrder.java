package ru.iwater.yourwater.iwaterlogistic.domain;

import ru.iwater.yourwater.iwaterlogistic.utils.TypeCash;

public class ReportOrder {

    private int orderID;
    private float cash = 0.00F;
    private TypeCash typeCash;
    private int thank;

    public ReportOrder(int orderID, float cash, TypeCash typeCash) {
        this.orderID = orderID;
        this.cash += cash;
        this.typeCash = typeCash;
        this.thank = 0;
    }

    public int getOrderID() {
        return orderID;
    }

    public float getCash() {
        return cash;
    }

    public TypeCash getTypeCash() {
        return typeCash;
    }

    public String getTypeCashTitle() {
        return typeCash.getTitle();
    }

    public int getThank() {
        return thank;
    }

    public void setThank(int thank) {
        this.thank = thank;
    }
}
