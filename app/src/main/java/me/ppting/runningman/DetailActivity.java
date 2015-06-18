package me.ppting.runningman;

/**
 * Created by PPTing on 15/6/17.
 */
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.view.View;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
/**
 * Created by Administrator on 2015/6/16 0016.
 */
public class DetailActivity extends android.app.Activity {
    android.widget.ImageView img;
    android.widget.Button    distance,steps,cal,time,sharebutton;
    DBManager mgr;
    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_view);
        //����UI
        img=(android.widget.ImageView)findViewById(R.id.detail_view_img);
        distance=(android.widget.Button)findViewById(R.id.detail_view_distance);
        steps=(android.widget.Button)findViewById(R.id.detail_view_steps);
        cal=(android.widget.Button)findViewById(R.id.detail_view_cal);
        time=(android.widget.Button)findViewById(R.id.detail_view_time);
        sharebutton = (android.widget.Button)findViewById(me.ppting.runningman.R.id.share_button);

        mgr=new DBManager(this);
        android.database.Cursor cursor=mgr.query();

        android.content.Intent intent=getIntent();
        int position=intent.getIntExtra("item",1);
        cursor.move(position);
        //����ݿ���ݵ��뵽Activity��
        byte[]ob=cursor.getBlob(cursor.getColumnIndex("run_img"));
        android.graphics.Bitmap bitmap= android.graphics.BitmapFactory.decodeByteArray(ob, 0, ob.length);
        img.setImageBitmap(bitmap);
        distance.setText("距离：" + cursor.getString(cursor.getColumnIndex("distance")));
        steps.setText("步数："+cursor.getString(cursor.getColumnIndex("steps")));
        cal.setText("卡路里："+cursor.getString(cursor.getColumnIndex("cal")));
        int timer=Integer.parseInt(cursor.getString(cursor.getColumnIndex("time")))/1000;
        time.setText("耗时："+timer+"秒");
        sharebutton.setOnClickListener(new android.view.View.OnClickListener()
        {
            @Override
            public void onClick(android.view.View v)
            {
                View view= v.getRootView();
                view.setDrawingCacheEnabled(true);
                Bitmap bitmap_1= view.getDrawingCache();
                Log.d("detailactivity", "" + bitmap_1);

                Uri uri=Uri.parse(MediaStore.Images.Media.insertImage
                        (getContentResolver(),bitmap_1,null,null));
                Log.d("detailavtivity","share");
                Intent intent=new Intent(Intent.ACTION_SEND);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                startActivity(intent);
                view.destroyDrawingCache();
            }
        });

    }


}
