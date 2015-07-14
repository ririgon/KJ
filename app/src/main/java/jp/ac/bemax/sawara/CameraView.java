package jp.ac.bemax.sawara;

/**
 * Created by m.takeda on 2015/06/23.
 */

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraView extends SurfaceView implements SurfaceHolder.Callback,PictureCallback {
		private SurfaceHolder holder=null;
		private Camera camera=null;
		////private static final String SDCARD_FOLDER="/sdcard/CameraSample/";
		//private CameraSample cs;
		Uri imageUri = CameraSample.getUri();
		public CameraView(Context context){
		super(context);

		// TODO Auto-generated constructor stub
		holder=getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		// 保存用フォルダ作成
//		File dirs=new File(SDCARD_FOLDER);
//		if(!dirs.exists()){
//		dirs.mkdir();
//		}
		}

		@Override
public void onPictureTaken(byte[]data,Camera camera){
		// TODO Auto-generated method stub
		SimpleDateFormat date=new SimpleDateFormat("yyyyMMdd_kkmmss");
		String datName="P"+date.format(new Date())+".jpg";
		try{
		// データ保存
		savePhotoData(imageUri.getPath(),data);
		}catch(Exception e){
		// TODO Auto-generated catch block
		if(camera!=null){
		camera.release();
		camera=null;
		}
		}
		// プレビュー再開
		camera.startPreview();
		}

        private void savePhotoData(String imageUri, byte[] data) throws Exception {
                // TODO Auto-generated method stub
		FileOutputStream outStream=null;
			URI uri = null;

		try{
		outStream=new FileOutputStream(uri.getPath());//SDCARD_FOLDER+imageUri);
		outStream.write(data);
		outStream.close();
		}catch(Exception e){
		if(outStream!=null){
		outStream.close();
		}
		throw e;
		}
		}

		@Override
public boolean onTouchEvent(MotionEvent event){
		// TODO Auto-generated method stub
		if(event.getAction()== MotionEvent.ACTION_DOWN){
		camera.takePicture(null,null,this);
		}
		return true;
		}

		@Override
public void surfaceCreated(SurfaceHolder holder){
		// TODO Auto-generated method stub
		if(camera!=null){
		return;
		}
		camera= Camera.open();
		try{
		camera.setPreviewDisplay(holder);
		}catch(IOException e){
		// TODO Auto-generated catch block
		if(camera!=null){
		camera.release();
		camera=null;
		}
		}
		}

		@Override
public void surfaceChanged(SurfaceHolder holder,int format,int width,int height){
		// TODO Auto-generated method stub
		Log.v("Camera", "format=" + format + ", width=" + width + ", height=" + height);
		Camera.Parameters params=camera.getParameters();
		params.setPreviewSize(width,height);
		camera.setParameters(params);
		camera.startPreview();
		}

		@Override
public void surfaceDestroyed(SurfaceHolder holder){
		// TODO Auto-generated method stub
		camera.stopPreview();
		camera.release();
		camera=null;
		}
		}