package com.wxp.inend.entity;

public class FoodAllField {

    private Integer Id;
    private String foodName;
    private String brith;
    private String bzq;
    private Float price;
    private Float oldPrice;
    private Integer margin;
    private Integer saleNumber;
    private String type;
    private String comment;
    private Integer goodResponse;
    private String img;

    @Override
    public int hashCode() {
        return Id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {

        return Id==((FoodAllField)obj).getId();
    }

    public FoodAllField(){}

    public FoodAllField(Integer id, String foodName, String brith, String bzq, Float price, Float oldPrice, Integer margin, Integer saleNumber, String type, String comment, Integer goodResponse, String img) {
        Id = id;
        this.foodName = foodName;
        this.brith = brith;
        this.bzq = bzq;
        this.price = price;
        this.oldPrice = oldPrice;
        this.margin = margin;
        this.saleNumber = saleNumber;
        this.type = type;
        this.comment = comment;
        this.goodResponse = goodResponse;
        this.img = img;
    }

    @Override
    public String toString() {
        return "FoodAllField{" +
                "Id=" + Id +
                ", foodName='" + foodName + '\'' +
                ", brith='" + brith + '\'' +
                ", bzq='" + bzq + '\'' +
                ", price=" + price +
                ", oldPrice=" + oldPrice +
                ", margin=" + margin +
                ", saleNumber=" + saleNumber +
                ", type='" + type + '\'' +
                ", comment='" + comment + '\'' +
                ", goodResponse=" + goodResponse +
                ", img='" + img + '\'' +
                '}';
    }

    public Integer getId() {
        return Id;
    }

    public void setId(Integer id) {
        Id = id;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getBrith() {
        return brith;
    }

    public void setBrith(String brith) {
        this.brith = brith;
    }

    public String getBzq() {
        return bzq;
    }

    public void setBzq(String bzq) {
        this.bzq = bzq;
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
}
