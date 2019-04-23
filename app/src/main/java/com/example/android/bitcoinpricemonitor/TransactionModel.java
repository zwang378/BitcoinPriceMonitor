package com.example.android.bitcoinpricemonitor;

public class TransactionModel {
    private int price;
    private double amount;
    private double total;

    public TransactionModel(int price, double amount, double total) {
        this.price = price;
        this.amount = amount;
        this.total = total;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
