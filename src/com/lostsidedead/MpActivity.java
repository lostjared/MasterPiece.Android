package com.lostsidedead;


import android.app.Activity;
import android.os.Bundle;
import android.widget.*;

public class MpActivity extends Activity {

	public TextView txtview;
	
	public puzzleGame mp_v;
	public static String MP_KEY="masterpiece";
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mp_v = (puzzleGame) findViewById(R.id.mpv);
        
        if(savedInstanceState == null) {
           	mp_v.setMode(mp_v.START);
        	mp_v.setPaused(false);
        } else {
        	
        }
        
        txtview =  (TextView) findViewById(R.id.textView1);
        txtview.setText("Tap Logo to Start");
        mp_v.txtview = txtview;
        mp_v.spin_ctrl = (Spinner) findViewById(R.id.spin02);
    }
    
    public void onPause() {
    	super.onPause();
    	mp_v.setPaused(true);   	
    }
    
    public void onResume() {
    	super.onResume();
    	mp_v.setPaused(false);
    } 
}