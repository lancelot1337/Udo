package io.branch.referral;

import cz.msebera.android.httpclient.HttpStatus;
import org.cocos2dx.lib.BuildConfig;

public class BranchError {
    public static final int ERR_API_LVL_14_NEEDED = -108;
    public static final int ERR_BRANCH_DUPLICATE_REFERRAL_CODE = -106;
    public static final int ERR_BRANCH_DUPLICATE_URL = -105;
    public static final int ERR_BRANCH_INIT_FAILED = -104;
    public static final int ERR_BRANCH_INVALID_REQUEST = -116;
    public static final int ERR_BRANCH_KEY_INVALID = -114;
    public static final int ERR_BRANCH_NOT_INSTANTIATED = -109;
    public static final int ERR_BRANCH_NO_CONNECTIVITY_STATUS = -113;
    public static final int ERR_BRANCH_NO_SHARE_OPTION = -110;
    public static final int ERR_BRANCH_REDEEM_REWARD = -107;
    public static final int ERR_BRANCH_REQ_TIMED_OUT = -111;
    public static final int ERR_BRANCH_RESOURCE_CONFLICT = -115;
    public static final int ERR_BRANCH_UNABLE_TO_REACH_SERVERS = -112;
    public static final int ERR_INVALID_REFERRAL_CODE = -103;
    public static final int ERR_NO_INTERNET_PERMISSION = -102;
    public static final int ERR_NO_SESSION = -101;
    int errorCode_ = ERR_BRANCH_NO_CONNECTIVITY_STATUS;
    String errorMessage_ = BuildConfig.FLAVOR;

    public String getMessage() {
        return this.errorMessage_;
    }

    public int getErrorCode() {
        return this.errorCode_;
    }

    public String toString() {
        return getMessage();
    }

    public BranchError(String failMsg, int statusCode) {
        this.errorMessage_ = failMsg + initErrorCodeAndGetLocalisedMessage(statusCode);
    }

    private String initErrorCodeAndGetLocalisedMessage(int statusCode) {
        if (statusCode == RemoteInterface.NO_CONNECTIVITY_STATUS) {
            this.errorCode_ = ERR_BRANCH_NO_CONNECTIVITY_STATUS;
            return " Branch API Error: poor network connectivity. Please try again later.";
        } else if (statusCode == RemoteInterface.NO_BRANCH_KEY_STATUS) {
            this.errorCode_ = ERR_BRANCH_KEY_INVALID;
            return " Branch API Error: Please enter your branch_key in your project's manifest file first.";
        } else if (statusCode == ERR_BRANCH_INIT_FAILED) {
            this.errorCode_ = ERR_BRANCH_INIT_FAILED;
            return " Did you forget to call init? Make sure you init the session before making Branch calls.";
        } else if (statusCode == ERR_NO_SESSION) {
            this.errorCode_ = ERR_NO_SESSION;
            return " Unable to initialize Branch. Check network connectivity or that your branch key is valid.";
        } else if (statusCode == ERR_NO_INTERNET_PERMISSION) {
            this.errorCode_ = ERR_NO_INTERNET_PERMISSION;
            return " Please add 'android.permission.INTERNET' in your applications manifest file.";
        } else if (statusCode == ERR_BRANCH_DUPLICATE_URL) {
            this.errorCode_ = ERR_BRANCH_DUPLICATE_URL;
            return " Unable to create a URL with that alias. If you want to reuse the alias, make sure to submit the same properties for all arguments and that the user is the same owner.";
        } else if (statusCode == ERR_BRANCH_DUPLICATE_REFERRAL_CODE) {
            this.errorCode_ = ERR_BRANCH_DUPLICATE_REFERRAL_CODE;
            return " That Branch referral code is already in use.";
        } else if (statusCode == ERR_BRANCH_REDEEM_REWARD) {
            this.errorCode_ = ERR_BRANCH_REDEEM_REWARD;
            return " Unable to redeem rewards. Please make sure you have credits available to redeem.";
        } else if (statusCode == ERR_API_LVL_14_NEEDED) {
            this.errorCode_ = ERR_API_LVL_14_NEEDED;
            return "BranchApp class can be used only with API level 14 or above. Please make sure your minimum API level supported is 14. If you wish to use API level below 14 consider calling getInstance(Context) instead.";
        } else if (statusCode == ERR_BRANCH_NOT_INSTANTIATED) {
            this.errorCode_ = ERR_BRANCH_NOT_INSTANTIATED;
            return "Branch instance is not created. Make  sure your Application class is an instance of BranchLikedApp.";
        } else if (statusCode == ERR_BRANCH_NO_SHARE_OPTION) {
            this.errorCode_ = ERR_BRANCH_NO_SHARE_OPTION;
            return " Unable create share options. Couldn't find applications on device to share the link.";
        } else if (statusCode == ERR_BRANCH_REQ_TIMED_OUT) {
            this.errorCode_ = ERR_BRANCH_REQ_TIMED_OUT;
            return " Request to Branch server timed out. Please check your internet connectivity";
        } else if (statusCode >= HttpStatus.SC_INTERNAL_SERVER_ERROR || statusCode == ERR_BRANCH_UNABLE_TO_REACH_SERVERS) {
            this.errorCode_ = ERR_BRANCH_UNABLE_TO_REACH_SERVERS;
            return " Unable to reach the Branch servers, please try again shortly.";
        } else if (statusCode == HttpStatus.SC_CONFLICT || statusCode == ERR_BRANCH_RESOURCE_CONFLICT) {
            this.errorCode_ = ERR_BRANCH_RESOURCE_CONFLICT;
            return " A resource with this identifier already exists.";
        } else if (statusCode >= HttpStatus.SC_BAD_REQUEST || statusCode == ERR_BRANCH_INVALID_REQUEST) {
            this.errorCode_ = ERR_BRANCH_INVALID_REQUEST;
            return " The request was invalid.";
        } else {
            this.errorCode_ = ERR_BRANCH_NO_CONNECTIVITY_STATUS;
            return " Check network connectivity and that you properly initialized.";
        }
    }
}
