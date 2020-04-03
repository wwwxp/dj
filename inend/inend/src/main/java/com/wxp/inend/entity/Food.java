package com.wxp.inend.entity;

public class Food {
    private String foodName;
    private Float price;
    private Float oldPrice;
    private Integer margin;
    private Integer saleNumber;
    private String type;
    private String comment;
    private Integer goodResponse;
    private String img;

    @Override
    public String toString() {
        return "Food{" +
                "foodName='" + foodName + '\'' +
                ", price=" + price +
                ", oldPrice=" + oldPrice +
                ", margin=" + margin +
                ", saleNumber=" + saleNumber +
                ", type='" + type + '\'' +
                ", comment='" + comment + '\'' +
                ", goodResponse=" + goodResponse +
                '}';
    }

    public Integer getGoodResponse() {
        return goodResponse;
    }

    public void setGoodResponse(Integer goodResponse) {
        this.goodResponse = goodResponse;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public Integer getStar() {
        return goodResponse;
    }

    public void setStar(Integer star) {
        this.goodResponse = star;
    }

    public Integer getMargin() {
        return margin;
    }

    public void setMargin(Integer margin) {
        this.margin = margin;
    }

    public Integer getSaleNumber() {
        return saleNumber;
    }

    public void setSaleNumber(Integer saleNumber) {
        this.saleNumber = saleNumber;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public Float getOldPrice() {
        return oldPrice;
    }

    public void setOldPrice(Float oldPrice) {
        this.oldPrice = oldPrice;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }


}
