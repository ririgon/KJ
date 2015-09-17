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

/**
 * Created by Katuya on 2015/09/08.
 */
public class MainActivity extends Activity implements View.OnClickListener{


    // �f�B�X�v���C�֘A��static�ȕϐ�
    static float displayDensity;
    static int buttonSize;

    static int gridViewColmn;
    static float displayWidth;
    static float displayHeight;
    static int frameSize;
    // �����ݒ�p�̃I�u�W�F�N�g
    static Configuration conf;

    View icon;
    View camera;
    View video;
    View ber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.home_winter);
        // �f�B�X�v���C�T�C�Y���擾����
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
        //�A�C�R���̃^�b�`�t���O
        icon = findViewById(R.id.icon);
        icon.setOnClickListener(this);
        //�J�����̃^�b�`�t���O
        camera = findViewById(R.id.camera);
        camera.setOnClickListener(this);
        //�r�f�I�̃^�b�`�t���O
        video = findViewById(R.id.video);
        video.setOnClickListener(this);
        //���j���[�o�[�̃^�b�`�t���O
        ber = findViewById(R.id.ber);
        ber.setOnClickListener(this);

    }

    public void onClick(View v) {
        if(v == icon){
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), SplashActivity.class);
            startActivity(intent);}
        if(v == camera){
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), SplashActivity.class);
            startActivity(intent);}
        if(v == video){
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), SplashActivity.class);
            startActivity(intent);}
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
