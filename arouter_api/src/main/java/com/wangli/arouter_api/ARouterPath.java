package com.wangli.arouter_api;

import com.wangli.annotations.RouterBean;

import java.util.Map;

public interface ARouterPath {

    Map<String, RouterBean> getPathMap();
}
