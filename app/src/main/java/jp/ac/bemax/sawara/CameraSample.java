package jp.ac.bemax.sawara;

/**
 * Created by m.takeda on 2015/06/23.
*/
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class CameraSample extends Activity {

    CameraView cv;
    public static Uri imageUri;
    public CameraSample(){
        super();
    }

    public CameraSample(Uri uri){
        imageUri = uri;
    }

    /** Called when the activity is first created. */
        @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 画面をフルスクリーンに設定
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
            cv = new CameraView(this);
        // カメラ用ビューの設定
        setContentView(cv);
        }
    public void setUri(Uri uri){
        this.imageUri = uri;
    }

    public static Uri getUri(){
        return imageUri;
    }

}
