package com.wxp.inend.entity;

public class BuyFood {
    private String foodName;
    private float price;
    private int number;
    private float totalPrice;
    private String type;

    public BuyFood(String foodName, float price, int number, float totalPrice, String type) {
        this.foodName = foodName;
        this.price = price;
        this.number = number;
        this.totalPrice = totalPrice;
        this.type = type;
    }

    @Override
    public String toString() {
        return "BuyFood{" +
                "foodName='" + foodName + '\'' +
                ", price=" + price +
                ", number=" + number +
                ", totalPrice=" + totalPrice +
                ", type='" + type + '\'' +
                '}';
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        setTotalPrice(number*getPrice());
        this.number = number;
    }

    public float getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(float totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
