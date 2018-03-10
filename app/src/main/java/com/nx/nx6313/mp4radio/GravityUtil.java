package com.nx.nx6313.mp4radio;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class GravityUtil  implements SensorEventListener {
	
	public GravityUtil(Context context ){
		mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);  
	}
	
	public void setOnLandscapeListener(OnLandscapeListener onLandscapeListener) {
		this.mOnLandscapeListener = onLandscapeListener;
	}

	public void setOnPortraitListener(OnPortraitListener onPortraitListener) {
		this.mOnPortraitListener = onPortraitListener;
	}

	public void setOnSpeedChangeListener(OnSpeedChangeListener onSpeedChangeListener) {
		this.mOnSpeedChangeListener = onSpeedChangeListener;
	}

	public void onResume(){
		mSensorManager.registerListener(this,  
				mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),  
                SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	public void onPause(){
		mSensorManager.unregisterListener(this); 
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		int sensorType = event.sensor.getType();  
        //values[0]:X轴，values[1]：Y轴，values[2]：Z轴
		// 平放，0，0，9,		竖放，0,9,0,		横放，9,0，0
        float[] values = event.values;  
        if (sensorType == Sensor.TYPE_GRAVITY){
            
            if(v[0] != 0 || v[1] != 0 || v[2] != 0){
            	// TODO 切换横屏
            	if(Math.abs(v[0]) < 7.5 && Math.abs(values[0]) >= 7.5 && Math.abs(values[1]) < 4){
            		if(null != mOnLandscapeListener ){
            			mOnLandscapeListener.onLandscape( );
                    }
            	}
            	
            	 // TODO 切换竖屏
            	if(v[1] < 7.5 && values[1] >= 7.5 && Math.abs(values[0]) < 4){
            		if(null != mOnPortraitListener ){
            			mOnPortraitListener.onPortrait( );
                    }
            	}
            }
            
            // TODO 加速度变化
            if(null != mOnSpeedChangeListener ){
            	mOnSpeedChangeListener.onSpeedChange(values[0], values[1], values[2]);
            }
            v[0] = values[0];
            v[1] = values[1];
            v[2] = values[2];
        }  
	}
	
	private SensorManager mSensorManager = null;
	private OnLandscapeListener mOnLandscapeListener = null;
	private OnPortraitListener mOnPortraitListener = null;
	private OnSpeedChangeListener mOnSpeedChangeListener = null;
	private float[] v = {0, 0, 0};
	
	public interface OnLandscapeListener{
		public void onLandscape();
	}
	
	public interface OnPortraitListener{
		public void onPortrait();
	}
	
	public interface OnSpeedChangeListener{
		public void onSpeedChange(float x, float y, float z);
	}
}