package me.ppting.runningman;

/**
 * Created by PPTing on 15/6/17.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
/**
 * Created by Administrator on 2015/6/16 0016.
 */
public class HistoryActivity extends Activity {
    ListView listView;
    //CardView listView;
    DBManager mgr;
    static java.util.ArrayList<java.util.HashMap<String,String>>list_data;
//    private RecyclerView mRecyclerView;
//    private RecyclerView.Adapter mAdapter;
//    private RecyclerView.LayoutManager mLayoutManager;
    @Override
    protected void onCreate(android.os.Bundle savedInstanceState)
    {
        Log.d("dbtest", "HistotyActivity onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_view);
        mgr=new DBManager(this);
        Cursor cursor=mgr.query();
        Log.d("dbtest", " " + cursor.getCount());
        cursor.moveToFirst();
        listView=(ListView)findViewById(R.id.listView);
        //listView=(CardView)findViewById(R.id.listView);
        list_data=new ArrayList<java.util.HashMap<String,String>>();

        do{
            HashMap<String,String> map=new HashMap<String,String>();
            map.put("DATE",cursor.getString(cursor.getColumnIndex("date")));
            map.put("ID",cursor.getString(cursor.getColumnIndex("id")));
            list_data.add(map);}
        while (cursor.moveToNext());
        //传数据
        android.widget.SimpleAdapter adapter=new android.widget.SimpleAdapter(this,list_data, R.layout.list_row,
                new String[]{"DATE","ID"},new int[]{R.id.list_row_date, R.id.list_row_id});
        listView.setAdapter(adapter);
        //查看
        listView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                Log.d("history","click");
                android.content.Intent intent=new android.content.Intent();
                intent.setClass(HistoryActivity.this, DetailActivity.class);
                intent.putExtra("item", ++position);
                startActivity(intent);
                android.util.Log.d("dbtest","HistoryActivity onItemClick");
            }
        });
        //删除
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                Log.d("dbtest2", "LongClick");
                AlertDialog.Builder builder = new AlertDialog.Builder(HistoryActivity.this);
                builder.setTitle("提示");
                builder.setMessage("确认删除该行数据吗？");
                Log.d("dbtest2", "position:" + position);
                builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        /*Cursor cursor1=mgr.query();
                        cursor1.move(position);
                        int del_num = cursor1.getInt(cursor1.getColumnIndex("s_num"));
                        Log.d("dbtest2", "del_num:" + del_num);
                        mgr.delete(++del_num);*/
                        int del_id = Integer.parseInt(list_data.get(position).get("ID"));
                        Log.d("dbtest2", "del_id:" + del_id);
                        mgr.delete(del_id);
                        list_data.remove(position);
                        Log.d("dbtest2", "list_data remove a data");
                        //刷新listview
                        Log.d("dbtest2", "list_data 开始刷新");
                        SimpleAdapter adapter = new SimpleAdapter(HistoryActivity.this, list_data, R.layout.list_row,
                                new String[]{"DATE", "ID"}, new int[]{R.id.list_row_date, R.id.list_row_id});
                        listView.setAdapter(adapter);
                    }
                });
                builder.show();
                return true;
            }
        });
    }
}
