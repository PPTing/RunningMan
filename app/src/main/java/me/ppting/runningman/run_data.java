package me.ppting.runningman;

/**
 * Created by PPTing on 15/6/17.
 */
/**
 * Created by Administrator on 2015/6/15 0015.
 */
public class run_data {
    int    id,steps;
    double distance,cal;
    long   time;
    String   date;
    android.graphics.Bitmap run_img;
    public run_data(double distance,int steps,double cal,long time, android.graphics.Bitmap run_img,String date){
        //"(id INTEGER primary key,distance DOUBLE,steps INTEGER,cal DOUBLE,time INTEGER,run_img BLOB��date DATE)";
        this.distance=distance;
        this.steps=steps;
        this.cal=cal;
        this.time=time;
        this.run_img=run_img;
        this.date=date;
    }
}
