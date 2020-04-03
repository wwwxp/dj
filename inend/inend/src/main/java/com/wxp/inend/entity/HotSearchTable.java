package com.wxp.inend.entity;

public class HotSearchTable {

    private String foodName;
    private int number;

    public HotSearchTable(String fn,int num){
                foodName=fn;
                number=num;
    }

    public HotSearchTable(){}

    @Override
    public String toString() {
        return "HotSearchTable{" +
                "foodName='" + foodName + '\'' +
                ", number=" + number +
                '}';
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
