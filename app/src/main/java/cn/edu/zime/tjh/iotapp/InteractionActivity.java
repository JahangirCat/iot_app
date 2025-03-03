package cn.edu.zime.tjh.iotapp;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class InteractionActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interaction);
        
        
    }
    
    public void connection(){
        
    }
    
    public void InteractionScan(){
        
        
    }
    public void InteractionBack(View V){
        finish();
    }
}
