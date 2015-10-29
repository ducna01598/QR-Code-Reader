package com.kaopiz.qrcodereader;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import net.simonvt.menudrawer.MenuDrawer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private IntentIntegrator integrator;
    private MenuDrawer mDrawer;
    private List<String> listHistory;
    private SharedPreferences.Editor editor;
    private SharedPreferences historyPref;
    private ListView listView;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        historyPref = getSharedPreferences("PREFERENCE_HISTORY", MODE_PRIVATE);
        editor = historyPref.edit();
        Set<String> set = historyPref.getStringSet("HISTORY", null);
        listHistory = new ArrayList<>();
        if (set != null) {
            listHistory.addAll(set);
        }
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 250, getResources().getDisplayMetrics());

        mDrawer = MenuDrawer.attach(this, MenuDrawer.Type.BEHIND);
        mDrawer.setContentView(R.layout.activity_main);
        mDrawer.setMenuView(R.layout.list_history);
        mDrawer.setMenuSize((int) px);
        mDrawer.setDropShadowSize(2);
        mDrawer.setDropShadowColor(Color.RED);

        if (listHistory != null) {
            listView = (ListView) findViewById(R.id.listview);
            adapter = new ArrayAdapter<String>(MainActivity.this, R.layout.list_item_history, listHistory);
            listView.setAdapter(adapter);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(ScannerActivity.class);
        integrator.setOrientationLocked(false);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setOrientationLocked(true);

        Button buttonScan = (Button) findViewById(R.id.btn_scan);
        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                integrator.initiateScan();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Set<String> set = historyPref.getStringSet("HISTORY", null);
        if (listHistory != null) {
            listHistory.clear();
            if (set != null) {
                listHistory.addAll(set);
            }
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Log.d("MainActivity", "Cancelled scan");
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Log.d("MainActivity", "Scanned");
                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                listHistory.add(result.getContents());
                Set<String> set = new HashSet<String>();
                set.addAll(listHistory);
                editor.putStringSet("HISTORY", set);
                editor.commit();
                adapter.notifyDataSetChanged();
            }
        } else {
            Log.d("MainActivity", "Weird");
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_history) {
            mDrawer.openMenu();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
