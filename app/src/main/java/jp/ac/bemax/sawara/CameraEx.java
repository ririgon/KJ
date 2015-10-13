package jp.ac.bemax.sawara;
import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

//カメラの制御
public class CameraEx extends Activity {
    //アクティビティ起動時に呼ばれる
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        //フルスクリーンの指定(1)
        getWindow().clearFlags(
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(new CameraView(this));
    }
}