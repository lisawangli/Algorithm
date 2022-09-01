package com.wangli.arouter_api;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;

import com.wangli.annotations.RouterBean;


public class RouterManager {
    private String group;
    private String path;
    private static RouterManager instance;

    public static RouterManager getInstance() {
        if (instance ==null) {
            synchronized (RouterManager.class) {
                if (instance == null) {
                    instance = new RouterManager();
                }
            }
        }
        return instance;
    }

    private LruCache<String,ARouterGroup> groupCache;
    private LruCache<String,ARouterPath> pathCache;
    private final static String FILE_GROUP_NAME = "ARouter$$Group$$";
    private RouterManager() {
        groupCache = new LruCache<>(100);
        pathCache = new LruCache<>(100);
    }

    public BundleManager build(String path) {
        if (TextUtils.isEmpty(path)||!path.startsWith("/")) {
            throw new IllegalArgumentException("path 不正确，正确写法/app/Mactivity");
        }
        if (path.lastIndexOf("/")==0) {
            throw new IllegalArgumentException("path 不正确，正确写法/app/Mactivity");
        }
        String finalGroup = path.substring(1,path.indexOf("/",1));
        if (TextUtils.isEmpty(finalGroup)){
            throw new IllegalArgumentException("path 不正确，正确写法/app/Mactivity");
        }
        this.path = path;
        this.group = finalGroup;
        return new BundleManager();
    }

    public Object navigation(Context context,BundleManager bundleManager) {
        String groupClassName = context.getPackageName()+"."+FILE_GROUP_NAME+group;
        Log.e("RouterManager","groupClassName:"+groupClassName);

        ARouterGroup loadGroup = groupCache.get(group);
        try {
        if (loadGroup==null) {
            Class<?> aClass = null;

                aClass = Class.forName(groupClassName);

            loadGroup = (ARouterGroup) aClass.newInstance();
            groupCache.put(group,loadGroup);
        }
        if (loadGroup.getGroupMap().isEmpty()) {
            throw new RuntimeException("没有找到group");
        }
        ARouterPath loadPath = pathCache.get(path);
        if (loadPath == null) {
            Class<? extends ARouterPath> clazz = loadGroup.getGroupMap().get(group);
            loadPath = clazz.newInstance();
            pathCache.put(path,loadPath);
        }
        if (loadPath!=null) {
            if (loadPath.getPathMap().isEmpty()) {
                throw new RuntimeException("没有找到path");
            }

            RouterBean routerBean = loadPath.getPathMap().get(path);
            if (routerBean!=null) {
                switch (routerBean.getTypeEnum()) {
                    case ACTIVITY:
                        Intent intent = new Intent(context,routerBean.getMyClass());
                        intent.putExtras(bundleManager.getBundle());
                        context.startActivity(intent);
                        break;
                    case CALL:
                        Class<?> clazz = routerBean.getMyClass();
                        Call call = (Call) clazz.newInstance();
                        bundleManager.setCall(call);
                        return bundleManager.getCall();
                }
            }

        }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
