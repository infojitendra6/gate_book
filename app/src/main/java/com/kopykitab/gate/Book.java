package com.kopykitab.gate;

import android.os.Parcel;
import android.os.Parcelable;

public class Book  implements Parcelable{

    private String productId, image, name, price_1, price_2, href;

    public Book(String productId, String image, String name, String price_1, String price_2, String href) {
        this.productId = productId;
        this.image = image;
        this.name = name;
        this.price_1 = price_1;
        this.price_2 = price_2;
        this.href = href;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice_1() {
        return price_1;
    }

    public String getImageUrl() {
        return image;
    }

    public void setPrice_1(String price_1) {
        this.price_1 = price_1;
    }

    public String getPrice_2() {
        return price_2;
    }

    public void setPrice_2(String price_2) {
        this.price_2 = price_2;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    private Book(Parcel in) {
        this.productId = in.readString();
        this.image = in.readString();
        this.name = in.readString();
        this.price_1 = in.readString();
        this.price_2 = in.readString();
        this.href = in.readString();
    }

    @Override
    public String toString() {
        return productId + ": " + image + ": " + name + ": " + price_1 + ": " +price_2 + ": " + href;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(productId);
        dest.writeString(image);
        dest.writeString(name);
        dest.writeString(price_1);
        dest.writeString(price_2);
        dest.writeString(href);
        }
    public static final Parcelable.Creator<Book> CREATOR = new Parcelable.Creator<Book>() {
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[ size];
        }

    };
}
