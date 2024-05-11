package com.example.textil2;

public class ShoppingItem {

    private String id;
    private String name;
    private String info;
    private String price;
    private int imageResource;
    private int carCount;

    public ShoppingItem() {
    }

    public ShoppingItem(String name, String info, String price, int imageResource, int carCount) {
        this.name = name;
        this.info = info;
        this.price = price;
        this.imageResource = imageResource;
        this.carCount = carCount;
    }


    public String getInfo() {
        return info;
    }

    public String getPrice() {
        return price;
    }

    public int getImageResource() {
        return imageResource;
    }

    public String getName() {
        return name;
    }

    public int getCarCount() {
        return carCount;
    }

    public String _getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
