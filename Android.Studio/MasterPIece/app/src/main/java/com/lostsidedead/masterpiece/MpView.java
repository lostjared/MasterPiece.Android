package com.lostsidedead.masterpiece;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.graphics.*;

public class MpView extends View {
	
	public Paint paint = new Paint();
	
	public MpView(Context context, AttributeSet attrs) {
		super(context, attrs);	
	}

	@Override
	public void onDraw(Canvas c) {
		super.onDraw(c);
	}
	
}
