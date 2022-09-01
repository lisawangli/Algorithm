package com.wangli.personal;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.wangli.annotations.ARouter;

@ARouter(path = "/personal/MainActivity")
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);
        String name = getIntent().getStringExtra("name");
        Log.e("Personal","name:"+name);
    }
}