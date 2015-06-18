package me.ppting.runningman;

/**
 * Created by PPTing on 15/6/17.
 */
/**
 * Created by Administrator on 2015/6/15 0015.
 */
public class DBHelper extends android.database.sqlite.SQLiteOpenHelper {
    static final String DBName="RunManDB";
    static final int    VERSION=1;
    public DBHelper(android.content.Context context) {
        super(context, DBName, null, VERSION);
    }

    //初次进入创建数据库
    @Override
    public void onCreate(android.database.sqlite.SQLiteDatabase db) {
        android.util.Log.d("dbtest","DBHelper onCreate");
        String new_sql="create table if not exists RUNNING" +
                "(id INTEGER primary key,distance DOUBLE,steps INTEGER,cal DOUBLE,time INTEGER,run_img BLOB,date TEXT)";
        db.execSQL(new_sql);
    }

    @Override
    public void onUpgrade(android.database.sqlite.SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
