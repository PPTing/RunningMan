package me.ppting.runningman;

/**
 * Created by PPTing on 15/6/17.
 */
import android.util.Log;

/**
 * Created by Administrator on 2015/6/15 0015.
 */
public class DBManager {
    private DBHelper helper;
    private android.database.sqlite.SQLiteDatabase db;
    public DBManager(android.content.Context context){
        helper=new DBHelper(context);
        db=helper.getWritableDatabase();
    }
    public void add(run_data run){
        int i=1;
        android.util.Log.d("dbtest", "Manager add() start");
        android.database.Cursor cursor=db.rawQuery("select * from RUNNING",null);
        android.util.Log.d("dbtest", "Manager get the cursor");
        if(cursor.moveToFirst())
        {
            cursor.moveToLast();
            Log.d("dbtest3", "Manager cursor move to last");
            i=cursor.getInt(cursor.getColumnIndex("id"));
            Log.d("dbtest", "Manager count:" + i);
            i++;
        }
        android.graphics.Bitmap bitmap=run.run_img;
        java.io.ByteArrayOutputStream os = new java.io.ByteArrayOutputStream();
        bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,os);
        byte[] bytes=os.toByteArray();
        //调用sql语句进行添加
        /*db.execSQL("insert into RUNNING VALUES(?,?,?,?,?,?,?)",
                new Object[]{i,run.distance,run.steps,run.cal,run.time,bytes,run.date});*/
        db.execSQL("insert into RUNNING VALUES(?,?,?,?,?,?,?)",
                new Object[]{i,run.distance,run.steps,run.cal,run.time,bytes,run.date});
        //db.execSQL("insert into RUNNING VALUES(?,NULL,NULL,NULL,NULL,NULL,NULL)",new Object[]{i});
        //db.execSQL("insert into RUNNING(id) values("+i+")");
        //"(id INTEGER primary key,distance DOUBLE,steps INTEGER,cal DOUBLE,time INTEGER,run_img BLOB��date DATE)";
        android.util.Log.d("dbtest", "Manager add finish");

    }
    public android.database.Cursor query(){
        android.database.Cursor cursor=db.rawQuery("select* from RUNNING",null);
        return cursor;
    }
    public void delete(int id){
        Log.d("dbtest2", "DBManager delete:"+id);
        db.delete("RUNNING", "id=" + id + "", null);
        Log.d("dbtest2", "DBManager delete success." );
    }
}
