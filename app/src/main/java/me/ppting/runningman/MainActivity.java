package me.ppting.runningman;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;

import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import android.net.Uri;
import android.provider.MediaStore.Images.Media;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface;

public class MainActivity extends Activity //implements BDLocationListener
{
    //导入百度地图
    public MapView mmapView = null;
    public BaiduMap mBaiduMap =null;
    //去除百度logo

    //定位相关
    //定位的客户端
    private LocationClient mlocationClient;
    //自定义图标
    private BitmapDescriptor mIconLocation;
    //定位的监听器
    private MyLocationListener myLocationListener;
    private Context context;
    private boolean isFirstIn = true;
    private double mLatitude;
    private double mLongtitude;
    //方向传感器
    private MyOrientationListener myOrientationListener;
    private float mCurrentX;

    //定义button
    private ImageButton buttonsite;//回到当前定位位置
    private Button startrecord;
    private Button stopbutton;
    private Button pausebutton;
    private Button continuebutton;
    private Button distancebutton;
    private Button step_count;
    private Button cal_count;
    private Button end_cal,end_distance,end_step,end_time;

    LatLng mFirstPoint;
    //com.baidu.mapapi.model.LatLng mNextPoint;
    //设置开始记录状态
    private boolean isStarttag=false;
    private boolean isStopTag=false;
    private boolean isPauseTag=false;
    private boolean isStartPoint=true;
    private boolean isContinueTag=false;
    int i=0;

    //计算距离
    private double sum;
    //使用数据库
    private DBManager mgr;

    //计步 卡路里
    private Thread thread;  //定义线程对象
    private static long timer = 0;// 运动时间
    private static long startTimer = 0;// 开始时间
    private static int total_step = 0;//总步数
    private static Double distance;
    private static Double calories;
    private android.app.Dialog alterdialog,endalterdialog;
    private static SeekBar seekBar;
    private static android.widget.EditText editText;
    public static android.content.SharedPreferences sharedPreferences;
    public static final String SETP_SHARED_PREFERENCES = "setp_shared_preferences";
    private android.content.SharedPreferences.Editor editor;
    private android.view.View dialogview,layout;
    private boolean isrun = false;

    android.os.Handler handler = new android.os.Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);        //更新UI


            if (timer != 0 && total_step != 0.0) {

                //计算热量
                calories = total_step * 10.1;
            } else {
                calories = 0.00;
            }
            countStep();      //调用步数方法

            step_count.setText("步数:" + total_step);
            cal_count.setText("卡路里:" + formatDouble(calories));
            distancebutton.setText("距离:"+formatDouble(sum));

            android.util.Log.d("tag_totalstep", total_step + "");

        }
    };
    @Override
    protected  void onCreate(Bundle savedInstanceState)
    {


        Log.d("MainActivity", "onCreate");
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        this.context=this;
        mgr=new DBManager(this);

        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        dialogview = layoutInflater.inflate(R.layout.setting_dialog, null);
        layout = layoutInflater.inflate(R.layout.end_dialog,null);

        mmapView=(MapView)findViewById(R.id.bmapView);//获取百度地图控件实例
        mBaiduMap = mmapView.getMap();
        //mBaiduMap.setMapType(mBaiduMap.MAP_TYPE_SATELLITE);//卫星图

        //去除百度logo 在获取地图控件后去除
        View child = mmapView.getChildAt(1);
        if(child!=null&&(child instanceof ImageView)||child instanceof ZoomControls)
        {
            child.setVisibility(View.INVISIBLE);
        }
        //去除缩放按钮
        mmapView.showZoomControls(false);

        //初始化按钮等
        initid();
        init_dialog();
        init_thread();
        //初始化定位
        initView();//调用后面的函数进行缩放
        initLocation();

        if (sharedPreferences == null) {
            sharedPreferences = getSharedPreferences(SETP_SHARED_PREFERENCES,
                    MODE_PRIVATE);
        }

        //设置button 回到当前定位位置
        buttonsite = (ImageButton)findViewById(R.id.backtomylocation_button);
        buttonsite.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                centerToMyLocation();
            }
        });
    }
    //设置菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();


        if (id == R.id.action_settings) {         //actionbar点击事件

            editText.setText(sharedPreferences.getString("weight", ""));
            editText.setSelection(editText.getText().length());
            alterdialog.show();
            return true;
        }
        //查看历史记录
        if(id==R.id.action_history){
            Intent intent=new Intent();
            intent.setClass(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
    //@Override
    //设置缩放，标尺大概为500米
    protected void initView()
    {
        mmapView = (MapView)findViewById(R.id.bmapView);
        mBaiduMap = mmapView.getMap();
        MapStatusUpdate mus = MapStatusUpdateFactory.zoomTo(15.0f);
        mBaiduMap.setMapStatus(mus);
    }
    //初始化id
    private void initid()
    {
        //初始化按钮 开始，结束，暂停，继续
        startrecord = (Button)findViewById(R.id.start_button);
        stopbutton = (Button)findViewById(R.id.stop_button);
        pausebutton = (Button)findViewById(R.id.pause_button);
        continuebutton = (Button)findViewById(R.id.continue_button);
        distancebutton = (Button)findViewById(R.id.distances);//距离
        step_count = (Button) findViewById(R.id.numberofstep);//步数
        cal_count = (Button) findViewById(R.id.calorie);//卡路里
        editText = (android.widget.EditText) dialogview.findViewById(R.id.weight);
        editText.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        end_cal = (Button)layout.findViewById(R.id.end_cal);
        end_distance = (Button)layout.findViewById(R.id.end_distance);
        end_step = (Button)layout.findViewById(R.id.end_step);
        end_time = (Button)layout.findViewById(R.id.end_time);
    }
    private void init_thread() {
        if (thread == null) {   //子线程用来通知主线程更新UI
            Log.d("tag_thread", "1");

            thread = new Thread() {// 子线程用于监听当前步数的变化

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    super.run();
                    int temp = 0;
                    while (true) {
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        if (StepCounterService.FLAG) {
                            Message msg = new Message();
                            if (temp != StepDetector.CURRENT_SETP) {
                                temp = StepDetector.CURRENT_SETP;
                            }
                            if (startTimer != System.currentTimeMillis()) {
                                timer = System.currentTimeMillis()
                                        - startTimer;
                            }
                            handler.sendMessage(msg);//通知主线程
                        }
                    }
                }
            };
            thread.start();
        }
    }

    private void init_dialog() {
        alterdialog = new android.app.AlertDialog.Builder(this).setTitle("设置").setView(dialogview). //actionbar弹出视图
                setPositiveButton("保存", new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                editor = sharedPreferences.edit();
                editor.putString("weight", editText.getText().toString());
                editor.commit();
            }
        }).setNegativeButton("取消", new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
            }
        }).create();

        endalterdialog = new android.app.AlertDialog.Builder(this).setTitle("详细数据").setView(layout).
                setPositiveButton("返回", new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub

                    }
                }).
                setNegativeButton("分享", new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        // TODO Auto-generated method stub
                        Intent intent=new Intent(Intent.ACTION_SEND);
                        mBaiduMap=mmapView.getMap();
                        mBaiduMap.snapshot(new BaiduMap.SnapshotReadyCallback()
                        {
                            @Override
                            public void onSnapshotReady(android.graphics.Bitmap bitmap)
                            {
                                Uri uri = Uri.parse(android.provider.MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, null, null));
                                android.content.Intent intent = new android.content.Intent(Intent.ACTION_SEND);
                                intent.setType("image/*");
                                intent.putExtra(Intent.EXTRA_STREAM, uri);
                                startActivity(intent);
                            }
                        });
                    }
                }).create();


    }
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mmapView.onDestroy();
    }
    @Override
    protected void onResume()
    {
        super.onResume();
        mmapView.onResume();

    }
    @Override
    protected void onStart() {
        super.onStart();
        //开始定位
        mBaiduMap.setMyLocationEnabled(true);
        if(!mlocationClient.isStarted())
            mlocationClient.start();
        //开启方向传感器
        myOrientationListener.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mmapView.onPause();
    }
    @Override
    protected void onStop()
    {
        super.onStop();
        //停止定位
        //mBaiduMap.setMyLocationEnabled(false);
        //mlocationClient.stop();
        //关闭方向传感器
        myOrientationListener.stop();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    //初始化定位
    private void initLocation()
    {
        Log.d("MainActivity", "开始定位");
        mlocationClient=new LocationClient(this);
        myLocationListener=new MyLocationListener();
        mlocationClient.registerLocationListener(myLocationListener);//进行注册
        //设置定位的相关设置
        LocationClientOption option=new LocationClientOption();
        option.setCoorType("bd09ll");//定位SDK可以返回bd09、bd09ll、gcj02三种类型坐标，若需要将定位点的位置
        // 通过百度Android地图SDK进行地图展示，请返回bd09ll，将无偏差的叠加在百度地图上。
        option.setIsNeedAddress(true);
        option.setOpenGps(true);//打开GPS
        option.setScanSpan(2000);
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //一秒钟刷新一次
        mlocationClient.setLocOption(option);
        //初始化定位图标
        mIconLocation = BitmapDescriptorFactory.fromResource(R.mipmap.navi_map_gps_locked);//不是drawable
        //方向传感器相关
        myOrientationListener = new MyOrientationListener(context);
        myOrientationListener.setmOrientationListener(new MyOrientationListener.OnOrientationListener() {
            @Override
            public void OnOrientationChange(float x) {
                mCurrentX = x;
            }
        });
        myLocationListener = new MyLocationListener();
    }
    //全局点坐标

    ////////////////////////////监听类/////////////////////////////////////////////////////
    private class MyLocationListener implements BDLocationListener
    {
        @Override
        public void onReceiveLocation(BDLocation location)
        {
            Log.d("MyLocationListener", "监听函数");
            LatLng curLatlng = new LatLng(location.getLatitude(),location.getLongitude());//当前的点坐标
            Log.d("监听函数onreceivelocation", "curLatlng");
            MyLocationData data=new MyLocationData.Builder()//
                    .direction(mCurrentX)//方向
                    .accuracy(location.getRadius())//
                    .latitude(location.getLatitude())//经纬度
                    .longitude(location.getLongitude())//经纬度
                    .build();
            mBaiduMap.setMyLocationData(data);
            //自定义图标
            MyLocationConfiguration config = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL,true,mIconLocation);
            mBaiduMap.setMyLocationConfigeration(config);

            //更新经纬度
            mLatitude=location.getLatitude();
            mLongtitude=location.getLongitude();
            if(isFirstIn)
            {
                mFirstPoint=curLatlng;//将取得的值赋予mPoint
                //设置经纬度
                //LatLng latLng =new LatLng(location.getLatitude(),location.getLongitude());
                //设置地图中心点
                MapStatusUpdate msu= MapStatusUpdateFactory.newLatLng(mFirstPoint);
                mBaiduMap.setMapStatus(msu);
                //mBaiduMap.animateMapStatus(mus);
                isFirstIn=false;
                Toast.makeText(context, location.getAddrStr(), android.widget.Toast.LENGTH_LONG).show();
                Log.d("第一次进入", "进行定位");
            }
            else
            {
                if(isStarttag)//starttag用来点击开始按钮开始记录
                {
                    if (isStartPoint)
                    {//打印第一个点
                        // 2015.6.14 修改mPoint=curLatlng 为 mFirstPoint
                        mFirstPoint = curLatlng;//获取当前点
                        //添加起点标志物开始
                        //回到中心点开始
                        //MapStatusUpdate startPoint = MapStatusUpdateFactory.newLatLng(mPoint);
                        //mBaiduMap.animateMapStatus(startPoint);
                        //回到中心点结束
                        Log.d("打印", "起始点mPoint");
                        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.mipmap.starticon);
                        OverlayOptions option = new MarkerOptions().position(mFirstPoint).icon(bitmap);
                        mBaiduMap.addOverlay(option);
                        //添加起点标志物结束
                        isStartPoint = false;
                    }
                    //if(isFirstFirstIn){mFirstPoint=curLatlng;isFirstFirstIn=false;}
                    if (isContinueTag)//点击继续
                    {
                        Log.d("继续", "打点mFirstPoint" + mFirstPoint);
                        //重新开始
                        isStarttag=true;
                        //isStartPoint = true;
                        isPauseTag = false;
                        isContinueTag = false;
                        mFirstPoint=new LatLng(location.getLatitude(),location.getLongitude());
                        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.mipmap.starticon);
                        OverlayOptions option = new MarkerOptions().position(mFirstPoint).icon(bitmap);
                        mBaiduMap.addOverlay(option);
                    }
                    if(isPauseTag)//点击暂停
                    {
                        if(i<1)
                        {
                            i++;
                            //点击暂停
                            LatLng mLastesPoint = new LatLng(location.getLatitude(), location.getLongitude());
                            //画标志
                            BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.mipmap.starticon);
                            //构建MarkerOption，用于在地图上添加Marker
                            OverlayOptions option = new MarkerOptions().position(mLastesPoint).icon(bitmap);
                            mBaiduMap.addOverlay(option);
                            Log.d("打印", "暂停点mLastesPoint");
                        }
                        //LatLng mFirstPoint = new com.baidu.mapapi.model.LatLng(location.getLatitude(),location.getLongitude());
                    }else
                    {
                        LatLng mNextPoint = new LatLng(location.getLatitude(), location.getLongitude());//得到当前点
                        if(DistanceUtil.getDistance(mFirstPoint,mNextPoint)>50)
                        {
                            //判断当前点是否太远，舍去
                        }else
                        {
                            Log.d("mnextpoint", "" + mNextPoint);
                            Log.d("mfirstpoint", "" + mFirstPoint);
                            //画折线
                            List<LatLng> points = new ArrayList<LatLng>();
                            points.add(mNextPoint);
                            points.add(mFirstPoint);
                            //折线属性
                            OverlayOptions ooPolyline = new PolylineOptions().width(20)
                                    .color(0xAAFF0000).points(points);
                            mBaiduMap.addOverlay(ooPolyline);
                            //计算距离
                            double distances = DistanceUtil.getDistance(mFirstPoint, mNextPoint);
                            sum = sum + distances;
                            Log.d("距离", "" + distances);
                            Log.d("距离和", "" + sum);
                            mFirstPoint = mNextPoint;
                        }
                    }

                    if (isStopTag)//点击结束
                    {
                        LatLng mLastesPoint = new LatLng(location.getLatitude(), location.getLongitude());
                        Log.d("打印", "结束点");
                        //画标志
                        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.mipmap.starticon);
                        // 构建MarkerOption，用于在地图上添加Marker
                        OverlayOptions option = new MarkerOptions().position(mLastesPoint).icon(bitmap);
                        mBaiduMap.addOverlay(option);
                        Log.d("结束", "" + mLastesPoint);
                        mBaiduMap.setMyLocationEnabled(false);//停止定位
                        mlocationClient.stop();
                        myOrientationListener.stop();

                    }
                }
            }
        }
    }
    //回到我的定位位置
    private void centerToMyLocation()
    {
        LatLng latLng = new LatLng(mLatitude, mLongtitude);
        MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);
        mBaiduMap.animateMapStatus(msu);
    }
    //双击返回
    @Override
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        // TODO Auto-generated method stub
        if(keyCode == android.view.KeyEvent.KEYCODE_BACK)
        {
            exitBy2Click();      //调用双击退出函数
        }
        return false;
    }
    //双击退出函数
    private static Boolean isExit = false;
    private void exitBy2Click() {
        java.util.Timer tExit = null;
        if (isExit == false) {
            isExit = true; // 准备退出
            android.widget.Toast.makeText(this, "再按一次退出程序", android.widget.Toast.LENGTH_SHORT).show();
            tExit = new java.util.Timer();
            tExit.schedule(new java.util.TimerTask() {
                @Override
                public void run()
                {
                    isExit = false; // 取消退出
                }
            }, 2000); // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务

        } else {
            //onStop();
            finish();
            System.exit(0);
        }
    }

    /**
     * 计算并格式化doubles数值，保留两位有效数字
     *
     * @param doubles
     * @return 返回当前路程
     */
    private String formatDouble(Double doubles) {
        java.text.DecimalFormat format = new java.text.DecimalFormat("####.##");
        String distanceStr = format.format(doubles);
        return distanceStr.equals(getString(R.string.zero)) ? getString(R.string.double_zero)
                : distanceStr;
    }
    /**
     * 得到一个格式化的时间
     *
     * @param time 时间 毫秒
     * @return 时：分：秒：毫秒
     */
    private String getFormatTime(long time) {
        time = time / 1000;
        long second = time % 60;
        long minute = (time % 3600) / 60;
        long hour = time / 3600;

        // 毫秒秒显示两位
        // String strMillisecond = "" + (millisecond / 10);
        // 秒显示两位
        String strSecond = ("00" + second)
                .substring(("00" + second).length() - 2);
        // 分显示两位
        String strMinute = ("00" + minute)
                .substring(("00" + minute).length() - 2);
        // 时显示两位
        String strHour = ("00" + hour).substring(("00" + hour).length() - 2);

        return strHour + ":" + strMinute + ":" + strSecond;
        // + strMillisecond;
    }
    //记录步数
    private void countStep() {
        if (StepDetector.CURRENT_SETP % 2 == 0) {
            total_step = StepDetector.CURRENT_SETP;
        } else {
            total_step = StepDetector.CURRENT_SETP + 1;
        }

        total_step = StepDetector.CURRENT_SETP;
    }
    public void onClick(android.view.View view)
    {
        android.content.Intent service = new android.content.Intent(MainActivity.this, StepCounterService.class);
        switch (view.getId())
        {
            case R.id.start_button:
                pausebutton.setVisibility(android.view.View.VISIBLE);
                startrecord.setVisibility(android.view.View.GONE);
                continuebutton.setVisibility(View.GONE);
                stopbutton.setVisibility(View.VISIBLE);
                isStarttag=true;
                isrun = true;
                if (sharedPreferences.getString("weight", "").equals("")) {
                    Toast.makeText(MainActivity.this, "请输入你的体重", Toast.LENGTH_SHORT).show();
                    break;
                }
                startService(service);
                startTimer = System.currentTimeMillis();
                Log.d("按钮", "点击开始");
                Toast.makeText(context, "开始记录", Toast.LENGTH_LONG).show();
                break;
            case R.id.pause_button:
                continuebutton.setVisibility(View.VISIBLE);
                pausebutton.setVisibility(View.GONE);
                startrecord.setVisibility(View.GONE);
                stopbutton.setVisibility(View.VISIBLE);
                isPauseTag=true;
                stopService(service);
                android.util.Log.d("按钮", "点击暂停");
                break;
            case R.id.continue_button:
                pausebutton.setVisibility(View.VISIBLE);
                continuebutton.setVisibility(View.GONE);
                startrecord.setVisibility(View.GONE);
                stopbutton.setVisibility(View.VISIBLE);
                isPauseTag=false;
                isContinueTag=true;
                startService(service);
                Log.d("按钮", "点击继续");
                break;
            case R.id.stop_button:
                if(isrun)
                {
                    mBaiduMap = mmapView.getMap();
                    mBaiduMap.snapshot(new BaiduMap.SnapshotReadyCallback() {
                        @Override
                        public void onSnapshotReady(android.graphics.Bitmap bitmap) {
                            //获取一个run_data值
                            SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
                            java.util.Date time=new java.util.Date(System.currentTimeMillis());
                            String date=format.format(time);
                            run_data run = new run_data(sum, total_step, calories, timer, bitmap, date);
                            mgr.add(run);
                            Log.d("dbtest", "MainActivity add a data");
                        }
                    });
                    startrecord.setVisibility(View.VISIBLE);
                    pausebutton.setVisibility(View.GONE);
                    continuebutton.setVisibility(View.GONE);
                    stopbutton.setVisibility(View.GONE);
                    isStopTag = true;
                    isrun = false;
                    StepDetector.CURRENT_SETP = 0;
                    stopService(service);
                    showenddialog();
                    Log.d("按钮", "点击结束");
                }
                break;
        }
    }
    private void showenddialog() {
        android.util.Log.d("tageee", calories + " " + total_step + " " + sum + " " + timer);
        end_cal.setText(formatDouble(calories) + "");
        end_step.setText(total_step+"");
        end_distance.setText(formatDouble(sum) + "");
        end_time.setText(getFormatTime(timer) + "");
        endalterdialog.show();
    }
}
