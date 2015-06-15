package me.ppting.runningman;

/**
 * Created by PPTing on 15/6/4.
 * 方向传感器
 */

public class MyOrientationListener implements android.hardware.SensorEventListener
{

    private android.hardware.SensorManager mSensorManager;
    private android.content.Context mContext;
    private android.hardware.Sensor mSensor;
    private float lastX;
    public MyOrientationListener(android.content.Context context)
    {
        this.mContext=context;
    }
    public void start()
    {
        mSensorManager=(android.hardware.SensorManager)mContext.getSystemService(android.content.Context.SENSOR_SERVICE);
        if(mSensorManager!=null)
        {
            //获得方向传感器
            mSensor = mSensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_ORIENTATION);
        }
        if(mSensor!=null)
        {
            mSensorManager.registerListener(this,mSensor, android.hardware.SensorManager.SENSOR_DELAY_NORMAL);
        }
    }
    public void stop()
    {
        mSensorManager.unregisterListener(this);
    }
    @Override
    public void onAccuracyChanged(android.hardware.Sensor arg0, int arg1)
    {
        // TODO Auto-generated method stub

    }
    //方向发生改变
    @Override
    public void onSensorChanged(android.hardware.SensorEvent event)
    {
        if(event.sensor.getType()== android.hardware.Sensor.TYPE_ORIENTATION)
        {
            float x = event.values[android.hardware.SensorManager.DATA_X];
            if(Math.abs(x-lastX)>1.0)
            {
                if(mOrientationListener!=null)
                    mOrientationListener.OnOrientationChange(x);
            }
            lastX=x;
        }
    }
    private OnOrientationListener mOrientationListener;
//    public void setmSensorManager(SensorManager mSensorManager)
//    {
//        this.mSensorManager = mSensorManager;
//
//    }

    public void setmOrientationListener(OnOrientationListener mOrientationListener) {
        this.mOrientationListener = mOrientationListener;

    }


    public interface OnOrientationListener
    {
        void OnOrientationChange(float x);
    }

}
