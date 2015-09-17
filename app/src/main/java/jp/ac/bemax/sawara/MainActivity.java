package jp.ac.bemax.sawara;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import java.util.Calendar;

/**
 * Created by Katuya on 2015/09/08.
 */
public class MainActivity extends Activity implements View.OnClickListener{


    // ディスプレイ関連のstaticな変数
    static float displayDensity;
    static int buttonSize;

    static int gridViewColmn;
    static float displayWidth;
    static float displayHeight;
    static int frameSize;
    // 初期設定用のオブジェクト
    static Configuration conf;

    View icon;
    View camera;
    View video;
    View ber;

    int season = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Calendar calendar = Calendar.getInstance();
        final int month = calendar.get(Calendar.MONTH);

        if(month >= 3 && month <= 6){
            season = 1 ;
        }else if(month >= 6 && month <= 9){
            season = 2 ;
        }else if(month >= 9 && month <= 12){
            season = 3 ;
        }else if(month >= 12 && month <= 3){
            season = 4 ;
        }

        switch (season){
            case 1 : setContentView(R.layout.home_spring);
                    break;
            case 2 : setContentView(R.layout.home_summer);
                break;
            case 3 : setContentView(R.layout.home_fall);
                break;
            case 4 : setContentView(R.layout.home_winter);
                break;

        }

        // ディスプレイサイズを取得する
        WindowManager windowManager = getWindowManager();
        Point displaySize = new Point();
        windowManager.getDefaultDisplay().getSize(displaySize);
        DisplayMetrics outMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics

                (outMetrics);
        displayDensity = outMetrics.density;
        displayWidth = displaySize.x;
        displayHeight = displaySize.y - 25 * displayDensity;
        buttonSize = (int)(displayHeight / 5);
        gridViewColmn = (int)((displayWidth - buttonSize) /

                (buttonSize * 2));
        //アイコンのタッチフラグ
        icon = findViewById(R.id.icon);
        icon.setOnClickListener(this);
        //カメラのタッチフラグ
        camera = findViewById(R.id.camera);
        camera.setOnClickListener(this);
        //ビデオのタッチフラグ
        video = findViewById(R.id.video);
        video.setOnClickListener(this);
        //メニューバーのタッチフラグ
        ber = findViewById(R.id.ber);
        ber.setOnClickListener(this);

    }

    public void onClick(View v) {
        //プロフィールを押した場合
        if(v == icon){
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), SplashActivity.class);
            startActivity(intent);}
        //カメラを押した場合
        if(v == camera){
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), SplashActivity.class);
            startActivity(intent);}
        //ビデオを押した場合
        if(v == video){
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), SplashActivity.class);
            startActivity(intent);}
        //バーを押した場合
        if(v == ber){
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), SplashActivity.class);
            startActivity(intent);}
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
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
