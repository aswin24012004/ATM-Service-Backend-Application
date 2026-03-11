package com.atm.model;

public class ATM {
    private  int id;
    private double totalBalance;

    public ATM(int id, double totalBalance) {
        this.id = id;
        this.totalBalance = totalBalance;
    }
    public ATM() {throw new UnsupportedOperationException("Use parameterized constructor instead");}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public double getTotalBalance() {
        return totalBalance;
    }

    public void setTotalBalance(double totalBalance) {
        this.totalBalance = totalBalance;
    }

	
}
