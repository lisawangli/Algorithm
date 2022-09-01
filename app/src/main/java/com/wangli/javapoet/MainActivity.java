package com.wangli.javapoet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.wangli.annotations.ARouter;
import com.wangli.annotations.Parameter;
import com.wangli.annotations.RouterBean;
import com.wangli.arouter_api.ARouterPath;
import com.wangli.arouter_api.RouterManager;
import com.wangli.common.IUser;

import java.util.Map;

@ARouter(path="/app/MainActivity")
public class MainActivity extends AppCompatActivity {

    @Parameter(name = "/user/getUserInfo")
    IUser iUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                ARouter$$Group$$personal group$$app = new ARouter$$Group$$personal();
//                Map<String,Class<? extends ARouterPath>> groupMap = group$$app.getGroupMap();
//                Class<? extends ARouterPath> myClass = groupMap.get("personal");

//                    ARouter$$Path$$personal path = (ARouter$$Path$$personal)myClass.newInstance();
//                    Map<String, RouterBean> pathMap = path.getPathMap();
//                    RouterBean bean = pathMap.get("/personal/MainActivity");
//                    startActivity(new Intent(MainActivity.this,bean.getMyClass()));
                    RouterManager.getInstance().build("/personal/MainActivity").withString("name","111").navigation(MainActivity.this);

//                Class clazz = AActivity$$ARouter.findTargetClass("/app/aactivity");
//                startActivity(new Intent(MainActivity.this,clazz));
            }
        });
        findViewById(R.id.second).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Class clazz = BActivity$$ARouter.findTargetClass("/app/bactivity");
//                startActivity(new Intent(MainActivity.this,clazz));
            }
        });
    }
}