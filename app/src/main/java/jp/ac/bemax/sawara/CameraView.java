package jp.ac.bemax.sawara;
import android.content.Context;
import android.hardware.Camera;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.FileOutputStream;

//カメラの制御
public class CameraView extends SurfaceView
        implements SurfaceHolder.Callback,Camera.PictureCallback {
    private SurfaceHolder holder;//ホルダー
    private Camera        camera;//カメラ

    //コンストラクタ
    public CameraView(Context context) {
        super(context);

        //サーフェイスホルダーの生成
        holder=getHolder();
        holder.addCallback(this);

        //プッシュバッッファの指定(2)
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    //サーフェイス生成イベントの処理
    public void surfaceCreated(SurfaceHolder holder) {
        //カメラの初期化(3)
        try {
            camera=Camera.open();
            camera.setPreviewDisplay(holder);
        } catch (Exception e) {
        }
    }

    //サーフェイス変更イベントの処理
    public void surfaceChanged(SurfaceHolder holder,int format,int w,int h) {
        //カメラプレビューの開始(4)
        camera.startPreview();
    }

    //サーフェイス解放イベントの処理
    public void surfaceDestroyed(SurfaceHolder holder) {
        //カメラのプレビュー停止(5)
        camera.setPreviewCallback(null);
        camera.stopPreview();
        camera.release();
        camera=null;
    }

    //タッチ時に呼ばれる
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction()==MotionEvent.ACTION_DOWN) {
            //カメラのスクリーンショットの取得(6)
            camera.takePicture(null,null,this);
        }
        return true;
    }

    //写真撮影完了時に呼ばれる
    public void onPictureTaken(byte[] data,Camera camera) {
        //SDカードへのデータ保存(7)
        try {
            String path=Environment.getExternalStorageDirectory()+
                    "/test.jpg";
            data2file(data,path);
        } catch (Exception e) {
        }
        //プレビュー再開
        camera.startPreview();
    }

    //バイトデータ→ファイル
    private void data2file(byte[] w,String fileName)
            throws Exception {
        FileOutputStream out=null;
        try {
            out=new FileOutputStream(fileName);
            out.write(w);
            out.close();
        } catch (Exception e) {
            if (out!=null) out.close();
            throw e;
        }
    }
}