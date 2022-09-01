package com.wangli.arouter_api;

import android.app.Activity;
import android.util.LruCache;

public class ParamterManager {
    private static ParamterManager instance;

    public static ParamterManager getInstance() {
        if (instance == null){
            synchronized (ParamterManager.class) {
                if(instance == null){
                    instance = new ParamterManager();
                }
            }
        }
        return instance;
    }

    private LruCache<String,ParameterGet> cache;
    static final String FILE_SUFFIX_NAME = "$$Parameter";

    private ParamterManager() {
        cache = new LruCache<>(100);
    }
    public void loadParamter(Activity activity) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        String className = activity.getClass().getName();
        ParameterGet parameterGet = cache.get(className);
        if (parameterGet==null) {
            Class<?> clazz = Class.forName(className+FILE_SUFFIX_NAME);
            parameterGet = (ParameterGet) clazz.newInstance();
            cache.put(className,parameterGet);
        }
        parameterGet.getParameter(activity);
    }


}
