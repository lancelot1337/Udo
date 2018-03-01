package io.branch.referral.util;

import com.google.firebase.analytics.FirebaseAnalytics.Param;
import com.unity3d.ads.metadata.MediationMetaData;
import org.json.JSONException;
import org.json.JSONObject;

public class Product {
    private String brand;
    private ProductCategory category;
    private String name;
    private Double price;
    private Integer quantity;
    private String sku;
    private String variant;

    public String getSku() {
        return this.sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return this.price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return this.quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getBrand() {
        return this.brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getVariant() {
        return this.variant;
    }

    public void setVariant(String variant) {
        this.variant = variant;
    }

    public ProductCategory getCategory() {
        return this.category;
    }

    public void setCategory(ProductCategory category) {
        this.category = category;
    }

    public Product(String sku, String name, Double price, int quantity, String brand, String variant, ProductCategory category) {
        this.sku = sku;
        this.name = name;
        this.price = price;
        this.quantity = Integer.valueOf(quantity);
        this.brand = brand;
        this.variant = variant;
        this.category = category;
    }

    public JSONObject getProductJSONObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("sku", this.sku);
            jsonObject.put(MediationMetaData.KEY_NAME, this.name);
            jsonObject.put(Param.PRICE, this.price);
            jsonObject.put(Param.QUANTITY, this.quantity);
            jsonObject.put("brand", this.brand);
            jsonObject.put("variant", this.variant);
            jsonObject.put("category", this.category);
        } catch (JSONException e) {
        }
        return jsonObject;
    }
}
