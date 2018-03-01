package com.superking.iap;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.superking.iap.billing.IabException;
import com.superking.iap.billing.IabHelper;
import com.superking.iap.billing.IabHelper.IabAsyncInProgressException;
import com.superking.iap.billing.IabHelper.OnConsumeFinishedListener;
import com.superking.iap.billing.IabHelper.OnIabPurchaseFinishedListener;
import com.superking.iap.billing.IabHelper.OnIabSetupFinishedListener;
import com.superking.iap.billing.IabResult;
import com.superking.iap.billing.Inventory;
import com.superking.iap.billing.Purchase;
import com.superking.iap.billing.SkuDetails;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.cocos2dx.cpp.AppActivity;
import org.cocos2dx.lib.BuildConfig;

public class PaymentInterface {
    private static final String KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkWChfhejgv4A68aOfUn8Gtb5jtBAjuAyimLPtF/caiLsbshO/bHtUcOoI5bfH8KvJ+R02VR3Z4Cg/A4KPXJOhDZvXCsRHC1s6gOYENq5ub/3gvO//fdXdw+V1hiT3hTXMQy9eAq765o3HqomxcLbIqqW00bbu+qTiOQQObwGlycZTaxsEEV/W+d64US6yDF+9FXn8oUK6zFf3Cp+EUsAKd9WHyiHM0tSH4GvuuX0goO1IdziaAsK1G6ZwnbmUnaBIrHi7otBxYeot7AnnWS019IHRrvP8JlTTBdUO28TCS2D7LH+zubfcsBtzRFnCcTMnbWmqnZs5B9mu6Ajt4qx8wIDAQAB";
    private static PaymentInterface mInstance = null;
    private String TAG = "PaymentInterface";
    private boolean mAvailable = false;
    private IabHelper mHelper = null;
    private List<Purchase> mPendingPurchase = null;
    private boolean mPendingPurchaseDone = false;

    private native void nativePurchaseFailure(String str);

    private native void nativePurchaseSuccess(String str);

    private native void nativeUpdatePackageDetails(String str, String str2, String str3, String str4);

    private native void nativeVerifyPurchase(String str, String str2, String str3, String str4, String str5);

    public static PaymentInterface getInstance() {
        if (mInstance == null) {
            mInstance = new PaymentInterface();
        }
        return mInstance;
    }

    public static void purchase(final String pkgId) {
        AppActivity app = AppActivity.getInstance();
        if (app != null) {
            app.runOnUiThread(new Runnable() {
                public void run() {
                    PaymentInterface.getInstance().startPurchase(pkgId);
                }
            });
        }
    }

    public static void updateProductDetails(final String shopKey, final String[] skuIds) {
        AppActivity app = AppActivity.getInstance();
        if (app != null) {
            app.runOnUiThread(new Runnable() {
                public void run() {
                    PaymentInterface.getInstance().updateSkuDetailsLocally(shopKey, skuIds);
                }
            });
        }
    }

    public void updateSkuDetailsLocally(final String shopKey, String[] skuIdsArray) {
        if (this.mAvailable && this.mHelper != null) {
            final ArrayList<String> skuIdsList = new ArrayList(Arrays.asList(skuIdsArray));
            new Thread(new Runnable() {
                public void run() {
                    try {
                        Inventory inv = PaymentInterface.this.mHelper.queryInventory(true, skuIdsList, null);
                        Iterator it = skuIdsList.iterator();
                        while (it.hasNext()) {
                            String id = (String) it.next();
                            SkuDetails sku = inv.getSkuDetails(id);
                            PaymentInterface.this.nativeUpdatePackageDetails(shopKey, id, "price_amount", Double.toString(((double) sku.getPriceAmountMicros()) / 1000000.0d));
                            PaymentInterface.this.nativeUpdatePackageDetails(shopKey, id, "price_currency_code", sku.getPriceCurrencyCode());
                        }
                    } catch (IabException e) {
                        e.printStackTrace();
                    } catch (IllegalArgumentException e2) {
                        e2.printStackTrace();
                    } catch (IllegalStateException e3) {
                        e3.printStackTrace();
                    } catch (NullPointerException e4) {
                        e4.printStackTrace();
                    }
                }
            }).start();
        }
    }

    public static void checkQueue() {
        getInstance().iterateThroughPendingPurchaseQueue();
    }

    public static void finishPurchase(String pkgId) {
        getInstance().completePurchase(pkgId);
    }

    public void onCreate(Context ctx) {
        this.mHelper = new IabHelper(ctx, KEY);
        this.mAvailable = false;
        this.mHelper.startSetup(new OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (result.isSuccess()) {
                    PaymentInterface.this.mAvailable = true;
                    if (PaymentInterface.this.mHelper != null) {
                        PaymentInterface.this.mHelper.enableDebugLogging(true, PaymentInterface.this.TAG);
                        try {
                            PaymentInterface.this.mPendingPurchase = PaymentInterface.this.mHelper.queryInventory().getAllPurchases();
                            PaymentInterface.this.iterateThroughPendingPurchaseQueue();
                            return;
                        } catch (IabException e) {
                            e.printStackTrace();
                            return;
                        } catch (IllegalArgumentException e2) {
                            e2.printStackTrace();
                            return;
                        } catch (IllegalStateException e3) {
                            e3.printStackTrace();
                            return;
                        } catch (NullPointerException e4) {
                            e4.printStackTrace();
                            return;
                        }
                    }
                    return;
                }
                Log.d(PaymentInterface.this.TAG, "Problem setting up In-app Billing: " + result);
                PaymentInterface.this.mAvailable = false;
            }
        });
    }

    public void onDestroy() {
        if (this.mHelper != null) {
            try {
                this.mHelper.dispose();
            } catch (IabAsyncInProgressException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
            } catch (IllegalStateException e3) {
                e3.printStackTrace();
            } catch (NullPointerException e4) {
                e4.printStackTrace();
            }
        }
        this.mHelper = null;
        this.mAvailable = false;
    }

    private void startPurchase(final String pkgId) {
        if (this.mAvailable && this.mHelper != null) {
            try {
                this.mHelper.launchPurchaseFlow(AppActivity.getInstance(), pkgId, 10002, new OnIabPurchaseFinishedListener() {
                    public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
                        if (result.isFailure()) {
                            Log.d(PaymentInterface.this.TAG, "Error purchasing: " + result);
                            PaymentInterface.this.completePurchase(pkgId);
                            PaymentInterface.this.nativePurchaseFailure(pkgId);
                            return;
                        }
                        PaymentInterface.this.nativeVerifyPurchase(purchase.getSku(), purchase.getOrderId(), purchase.getSignature(), purchase.getToken(), purchase.getOriginalJson());
                    }
                }, BuildConfig.FLAVOR);
            } catch (IabAsyncInProgressException e) {
                e.printStackTrace();
                nativePurchaseFailure(pkgId);
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
            } catch (IllegalStateException e3) {
                e3.printStackTrace();
            } catch (NullPointerException e4) {
                e4.printStackTrace();
            }
        }
    }

    private void completePurchase(final String pkgId) {
        if (this.mAvailable && this.mHelper != null) {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        final Inventory inv = PaymentInterface.this.mHelper.queryInventory();
                        if (inv.hasPurchase(pkgId)) {
                            AppActivity.getInstance().runOnUiThread(new Runnable() {
                                public void run() {
                                    try {
                                        PaymentInterface.this.mHelper.consumeAsync(inv.getPurchase(pkgId), new OnConsumeFinishedListener() {
                                            public void onConsumeFinished(Purchase purchase, IabResult result) {
                                            }
                                        });
                                    } catch (IabAsyncInProgressException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    } catch (IabException e) {
                        e.printStackTrace();
                    } catch (IllegalArgumentException e2) {
                        e2.printStackTrace();
                    } catch (IllegalStateException e3) {
                        e3.printStackTrace();
                    } catch (NullPointerException e4) {
                        e4.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private void iterateThroughPendingPurchaseQueue() {
        if (!this.mPendingPurchaseDone) {
            this.mPendingPurchaseDone = true;
            if (this.mPendingPurchase != null) {
                for (Purchase purchase : this.mPendingPurchase) {
                    nativeVerifyPurchase(purchase.getSku(), purchase.getOrderId(), purchase.getSignature(), purchase.getToken(), purchase.getOriginalJson());
                }
            }
        }
    }

    public boolean handleActivityResult(int request, int response, Intent data) {
        if (this.mHelper == null) {
            return false;
        }
        return this.mHelper.handleActivityResult(request, response, data);
    }
}
