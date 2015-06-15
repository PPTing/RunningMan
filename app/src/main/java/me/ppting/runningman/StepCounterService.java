package me.ppting.runningman;

/**
 * Created by PPTing on 15/6/12.
 */


//service 负责后台的需要长期运行的任务
// 计步器服务
// 运行在后台的服务程序，完成了界面部分的开发后
// 就可以开发后台的服务类StepService
// 注册或注销传感器监听器，在手机屏幕状态栏显示通知，与StepActivity进行通信，走过的步数记到哪里了？？？
public class StepCounterService extends android.app.Service {

    public static Boolean FLAG = false;// 服务运行标志

    private android.hardware.SensorManager mSensorManager;// 传感器服务
    private StepDetector detector;// 传感器监听对象

    private android.os.PowerManager mPowerManager;// 电源管理服务
    private android.os.PowerManager.WakeLock mWakeLock;// 屏幕灯

    @Override
    public android.os.IBinder onBind(android.content.Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        android.util.Log.d("tag_service", "1");
        // TODO Auto-generated method stub
        super.onCreate();

        FLAG = true;// 标记为服务正在运行

        // 创建监听器类，实例化监听对象
        detector = new StepDetector(this);

        // 获取传感器的服务，初始化传感器
        mSensorManager = (android.hardware.SensorManager) this.getSystemService(SENSOR_SERVICE);
        // 注册传感器，注册监听器
        mSensorManager.registerListener(detector,
                mSensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER),
                android.hardware.SensorManager.SENSOR_DELAY_FASTEST);

        // 电源管理服务
        mPowerManager = (android.os.PowerManager) this
                .getSystemService(android.content.Context.POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(android.os.PowerManager.SCREEN_DIM_WAKE_LOCK
                | android.os.PowerManager.ACQUIRE_CAUSES_WAKEUP, "S");
        mWakeLock.acquire();
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        FLAG = false;// 服务停止
        if (detector != null) {
            mSensorManager.unregisterListener(detector);
        }

        if (mWakeLock != null) {
            mWakeLock.release();
        }
    }

}

