package com.kaopiz.qrcodereader;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CompoundBarcodeView;

import java.util.List;
import java.util.Set;

public class ScannerActivity extends AppCompatActivity {
    private CompoundBarcodeView barcodeScannerView;
    private boolean onScanning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scaner);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Init barcode scanner view
        barcodeScannerView = (CompoundBarcodeView)findViewById(R.id.zxing_barcode_scanner);
        barcodeScannerView.decodeContinuous(callback);

        Button btnPause = (Button) findViewById(R.id.btn_pause);
        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onScanning){
                    ((Button)v).setText("Resume");
                    onScanning = false;
                    barcodeScannerView.pause();
                }else{
                    ((Button)v).setText("Pause");
                    onScanning = true;
                    barcodeScannerView.resume();
                }
            }
        });
    }

    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() != null) {
                barcodeScannerView.setStatusText(result.getText());
            }
            //Added preview of scanned barcode
            ImageView imageView = (ImageView) findViewById(R.id.barcodePreview);
            imageView.setImageBitmap(result.getBitmapWithResultPoints(Color.YELLOW));

            //Save history to preferences
            SharedPreferences historyPref = getSharedPreferences("PREFERENCE_HISTORY", MODE_PRIVATE);
            SharedPreferences.Editor editor = historyPref.edit();
            Set<String> set = historyPref.getStringSet("HISTORY", null);
            set.add(result.getText());
            editor.putStringSet("HISTORY", set);
            editor.commit();
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        barcodeScannerView.resume();
        onScanning = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeScannerView.pause();
        onScanning = false;
    }

    public void triggerScan(View view) {
        barcodeScannerView.decodeSingle(callback);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeScannerView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

}
