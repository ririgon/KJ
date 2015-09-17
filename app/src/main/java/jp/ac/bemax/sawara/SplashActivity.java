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
        // スプラッシュ用のビューを取得する
        setContentView(R.layout.splash);
        splashAnimation();
        // 2秒したらMainActivityを呼び出してSplashActivityを終了する
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // MainActivityを呼び出す
                Intent intent = new Intent(getApplicationContext(),
                        MainActivity.class);
                startActivity(intent);
                // SplashActivityを終了する
                SplashActivity.this.finish();
            }
        }, 5 * 1000); // 5000ミリ秒後（5秒後）に実行
    }
    private void splashAnimation(){
        AlphaAnimation alphaanime = new AlphaAnimation(1, 0);
        alphaanime.setStartOffset(2000);
        alphaanime.setDuration(1000);
        alphaanime.setFillAfter(true);
    }
}
