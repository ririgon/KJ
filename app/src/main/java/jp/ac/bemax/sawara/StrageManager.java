package jp.ac.bemax.sawara;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.provider.MediaStore;

/**
 * ストレージとのやり取りをするクラス
 *
 * @author Masaaki Horikawa
 * 2014/12/02
 */
public class StrageManager {
	private File pictureDir;
	private File movieDir;
	private File iconDir;
	private Context mContext;

    /*
	public StrageManager(Context context){
		mContext = context;
		pictureDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
		movieDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES);
		iconDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
	}
    */

	/**
	 * イメージ画像をストレージから呼び出す
	 * @param path 画像の保存先のパス
	 * @return 画像

	public static Bitmap loadImage(String path){
		Bitmap image = BitmapFactory.decodeFile(path);
		return image;
	}
	*/

	/**
	 * 動画のサムネイル画像を返す
	 * @param path
	 * @return

	public static Bitmap loadMovieThumbnail(String path){
		Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Images.Thumbnails.MINI_KIND);
		return thumbnail;
	}
    */
	
	/**
	 * 
	 * @param path
	 * @return

	public static Bitmap loadIcon(String path){
		Bitmap icon = BitmapFactory.decodeFile(path);
		return icon;
	}
    */
}
