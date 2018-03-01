package io.branch.referral.util;

import com.google.firebase.analytics.FirebaseAnalytics.Param;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public class CommerceEvent {
    private String affiliation;
    private String coupon;
    private CurrencyType currencyType;
    private List<Product> products;
    private Double revenue;
    private Double shipping;
    private Double tax;
    private String transactionID;

    public Double getRevenue() {
        return this.revenue;
    }

    public void setRevenue(Double revenue) {
        this.revenue = revenue;
    }

    public CurrencyType getCurrencyType() {
        return this.currencyType;
    }

    public void setCurrencyType(CurrencyType currencyType) {
        this.currencyType = currencyType;
    }

    public String getTransactionID() {
        return this.transactionID;
    }

    public void setTransactionID(String transactionID) {
        this.transactionID = transactionID;
    }

    public Double getShipping() {
        return this.shipping;
    }

    public void setShipping(Double shipping) {
        this.shipping = shipping;
    }

    public Double getTax() {
        return this.tax;
    }

    public void setTax(Double tax) {
        this.tax = tax;
    }

    public String getCoupon() {
        return this.coupon;
    }

    public void setCoupon(String coupon) {
        this.coupon = coupon;
    }

    public String getAffiliation() {
        return this.affiliation;
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public void addProduct(Product product) {
        if (this.products == null) {
            this.products = new ArrayList();
        }
        this.products.add(product);
    }

    public CommerceEvent(Double revenue, CurrencyType currencyType, String transactionID, Double shipping, Double tax, String coupon, String affiliation, List<Product> products) {
        this.revenue = revenue;
        this.currencyType = currencyType;
        this.transactionID = transactionID;
        this.shipping = shipping;
        this.tax = tax;
        this.coupon = coupon;
        this.affiliation = affiliation;
        this.products = products;
    }

    public CommerceEvent(Double revenue, CurrencyType currencyType, String transactionID, Double shipping, Double tax, String coupon, String affiliation, Product product) {
        this.revenue = revenue;
        this.currencyType = currencyType;
        this.transactionID = transactionID;
        this.shipping = shipping;
        this.tax = tax;
        this.coupon = coupon;
        this.affiliation = affiliation;
        this.products = new ArrayList();
        this.products.add(product);
    }

    public JSONObject getCommerceJSONObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("revenue", this.revenue);
            jsonObject.put(Param.CURRENCY, this.currencyType);
            jsonObject.put("transactionID", this.transactionID);
            jsonObject.put(Param.SHIPPING, this.shipping);
            jsonObject.put(Param.TAX, this.tax);
            jsonObject.put(Param.COUPON, this.coupon);
            jsonObject.put("affiliation", this.affiliation);
            if (getProducts() != null) {
                jsonObject.put("products", getProducts());
            }
        } catch (JSONException e) {
        }
        return jsonObject;
    }

    public List<JSONObject> getProducts() {
        if (this.products == null) {
            return null;
        }
        List<JSONObject> products = new ArrayList();
        for (Product p : this.products) {
            products.add(p.getProductJSONObject());
        }
        return products;
    }
}
