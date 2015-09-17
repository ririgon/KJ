package jp.ac.bemax.sawara;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AlphaAnimation;

/**
 * Created by Katuya on 2015/09/08.
 */
public class SplashActivity  extends Activity {

    Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // �X�v���b�V���p�̃r���[���擾����
        setContentView(R.layout.splash);
        splashAnimation();
        // 2�b������MainActivity���Ăяo����SplashActivity���I������
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // MainActivity���Ăяo��
                Intent intent = new Intent(getApplicationContext(),
                        MainActivity.class);
                startActivity(intent);
                // SplashActivity���I������
                SplashActivity.this.finish();
            }
        }, 5 * 1000); // 5000�~���b��i5�b��j�Ɏ��s
    }
    private void splashAnimation(){
        AlphaAnimation alphaanime = new AlphaAnimation(1, 0);
        alphaanime.setStartOffset(2000);
        alphaanime.setDuration(1000);
        alphaanime.setFillAfter(true);
    }
}
