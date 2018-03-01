package io.branch.referral.util;

import com.facebook.share.internal.ShareConstants;

public enum ProductCategory {
    ANIMALS_AND_PET_SUPPLIES("Animals & Pet Supplies"),
    APPAREL_AND_ACCESSORIES("Apparel & Accessories"),
    ARTS_AND_ENTERTAINMENT("Arts & Entertainment"),
    BABY_AND_TODDLER("Baby & Toddler"),
    BUSINESS_AND_INDUSTRIAL("Business & Industrial"),
    CAMERA_AND_OPTICS("Camera & Optics"),
    ELECTRONICS("Electronics"),
    FOOD_BEVERAGE_AND_TOBACCO("Food, Beverage & Tobacco"),
    FURNITURE("Furniture"),
    HARDWARE("Hardware"),
    HEALTH_AND_BEAUTY("Health & Beauty"),
    HOME_AND_GARDEN("Home & Garden"),
    LUGGAGE_AND_BAGS("Luggage & Bags"),
    MATURE("mature"),
    MEDIA(ShareConstants.WEB_DIALOG_PARAM_MEDIA),
    OFFICE_SUPPLIES("Office Supplies"),
    RELIGIOUS_AND_CEREMONIAL("Religious & Ceremonial"),
    SOFTWARE("Software"),
    SPORTING_GOODS("Sporting Goods"),
    TOYS_AND_GAMES("Toys & Games"),
    VEHICLES_AND_PARTS("Vehicles & Parts");
    
    private String type;

    private ProductCategory(String type) {
        this.type = type;
    }

    public String toString() {
        return this.type;
    }
}
