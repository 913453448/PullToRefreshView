package com.cisetech.pulltorefreshview;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cisetech.pulltorefreshview.view.PullRefreshView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PullRefreshView.IOnHeaderRefreshListener, PullRefreshView.IOnfootRefreshListener {
    private ListView mListView;
    private PullRefreshView mRefreshView;
    private List<String>datas=new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView= (ListView) findViewById(R.id.id_listview);
        mRefreshView= (PullRefreshView) findViewById(R.id.id_pull);
        mRefreshView.setHeaderRefreshListener(this);
        mRefreshView.setOnfootRefreshListener(this);
        headerView=new TextView(this);
        headerView.setTextColor(Color.BLACK);
        headerView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.5f);
        headerView.setText("HEADER VIEW");
        headerView.setGravity(Gravity.CENTER);
        footView=new TextView(this);
        footView.setTextColor(Color.BLACK);
        footView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.5f);
        footView.setText("FOOT VIEW");
        footView.setGravity(Gravity.CENTER);
        initData();
    }
    private ArrayAdapter<String> mAdapter;
    private TextView headerView;
    private TextView footView;
    private void initData() {
        for (int i = 0; i < 10; i++) {
            datas.add(""+i);
        }
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, datas){
            @Override
            public String getItem(int position) {
                return "List Item--->"+datas.get(position);
            }
        };
        mListView.setAdapter(mAdapter);
        if(mListView.getHeaderViewsCount()!=0){
            mListView.removeHeaderView(headerView);
        }
        if(mListView.getFooterViewsCount()!=0){
            mListView.removeFooterView(footView);
        }
        mListView.addHeaderView(headerView);
        mListView.addFooterView(footView);
        mListView.invalidate();
    }

    @Override
    public void onHeaderRefresh(final PullRefreshView view) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "刷新完成", Toast.LENGTH_SHORT).show();
                view.refreshComplete();
                datas.clear();
                initData();
            }
        },3000);
    }

    @Override
    public void onFootRefresh(final PullRefreshView view) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "刷新完成", Toast.LENGTH_SHORT).show();
                view.refreshComplete();
                int count=Integer.parseInt(datas.get(datas.size()-1))+1;
                for (int i = count; i <count+10 ; i++) {
                    datas.add(""+i);
                }
                mAdapter.notifyDataSetChanged();
            }
        },3000);
    }
}
