package com.wangli.arouter_api;

import android.content.Context;
import android.os.Bundle;

import java.io.Serializable;

public class BundleManager {
    //参数传递
    private Bundle bundle = new Bundle();

    private Call call;

    public Call getCall() {
        return call;
    }

    public void setCall(Call call) {
        this.call = call;
    }

    public Bundle getBundle(){
        return bundle;
    }

    //外界传入的参数
    public BundleManager withString(String key,String value) {
        bundle.putString(key,value);
        return this;
    }

    public BundleManager withBoolean(String key,boolean value) {
        bundle.putBoolean(key,value);
        return this;
    }
    public BundleManager withInt(String key,int value) {
        bundle.putInt(key,value);
        return this;
    }

    public BundleManager withSerializable(String key, Serializable value) {
        bundle.putSerializable(key,value);
        return this;
    }

    public BundleManager withBundle(Bundle bundle) {
        this.bundle = bundle;
        return  this;
    }

    public Object navigation(Context context) {
        return RouterManager.getInstance().navigation(context,this);
    }
}
