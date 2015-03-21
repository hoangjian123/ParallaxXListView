package com.example.parallaxxlistview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SimpleAdapter;

public class MainActivity extends ActionBarActivity {

	private ParallaxXListview listview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		listview = (ParallaxXListview) findViewById(R.id.listview);
		
		listview.setPullLoadEnable(true);
		listview.setPullRefreshEnable(true);

		String[] from = { "Text", "Button" };
		int[] to = { R.id.text, R.id.button };
		List<Map<String, ?>> list = new ArrayList<Map<String, ?>>();
		for (int i = 0; i < 10; i++) {
			Map<String, String> m = new HashMap<String, String>();
			m.put("Text", "Text" + i);
			m.put("Button", "Button" + i);
			list.add(m);
		}
		SimpleAdapter adapter = new SimpleAdapter(this, list,
				R.layout.parallaxlistview_test_item, from, to);
		listview.setAdapter(adapter);
		listview.setXListViewListener(new IXListViewListener() {
			
			@Override
			public void onRefresh() {
				// TODO Auto-generated method stub
				new Handler().postDelayed(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						listview.stopRefresh();
					}
				}, 2000);
			}
			
			@Override
			public void onLoadMore() {
				// TODO Auto-generated method stub
				listview.stopLoadMore();
			}
		});
		
		
//		listview.setNoMoreData();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
