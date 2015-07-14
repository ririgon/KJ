package jp.ac.bemax.sawara;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 画像からアイコンを作成するクラス
 * @author Masaaki Horikawa
 * 2014/12/03
 */
public class IconFactory {
	static final int ICON_WIDTH = 120;
	static final int ICON_HEIGHT = 90;
	static final int IMAGE_SIZE = 120;

    /**
     * Bitmap画像をファイルに保存する
     */
    public static void storeBitmapToFile(File file, Bitmap image) throws Exception{
        boolean compress = false;
        FileOutputStream fos = null;
        try{
            fos = new FileOutputStream(file);
            compress = image.compress(Bitmap.CompressFormat.PNG, 100, fos);
        }catch (IOException e){
            throw new Exception("ファイルが開けなかったよ");
        }finally {
            try{
                if(fos!=null)
                    fos.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if(!compress)
            throw new Exception("アイコン画像の保存に失敗したよ");
    }

    public static Bitmap getNullImage(){
        return Bitmap.createBitmap(ICON_WIDTH, ICON_HEIGHT, Config.ARGB_4444);
    }

    /**
     * ファイルから画像を読み込んで返す
     * @param mediaFile
     * @param mediaType
     * @return 作成したアイコン画像
     */
    public static Bitmap loadBitmapFromFileAndType(File mediaFile, long mediaType){
        Bitmap image = null;

        if(mediaType == Media.PHOTO) {

            // サイズを確定するための仮読み込み
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inJustDecodeBounds = true;

            BitmapFactory.decodeFile(mediaFile.getPath(), opt);

            // 読み込み時の精度を決定
            int size = opt.outWidth;
            if (opt.outHeight > size) {
                size = opt.outHeight;
            }
            opt.inSampleSize = size / IMAGE_SIZE;

            // 本格的に画像を読み込む
            opt.inJustDecodeBounds = false;
            image = BitmapFactory.decodeFile(mediaFile.getPath(), opt);
        }
        if(mediaType == Media.MOVIE){
            // 動画のサムネイル画像を取得する
            image = ThumbnailUtils.createVideoThumbnail(mediaFile.getPath(), MediaStore.Images.Thumbnails.MINI_KIND);
        }
        return image;
    }

    /**
     * アイコンを作成する
     * @return アイコンのビットマップ
     */
	public static Bitmap makeNormalIcon(Bitmap iconSrc) throws Exception{
        Bitmap iconBitmap = ThumbnailUtils.extractThumbnail(iconSrc, ICON_WIDTH, ICON_HEIGHT);
		return iconBitmap;
	}

    public static Bitmap makeSixMatrixIcon(Context context, Bitmap[] bitmaps){
        Bitmap icon = Bitmap.createBitmap(ICON_WIDTH, ICON_HEIGHT*3/2, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(icon);
        Rect srcRect = new Rect(0, 0, ICON_WIDTH, ICON_HEIGHT);
        Rect dstRect = null;
        int width = ICON_WIDTH/2;
        int height = ICON_HEIGHT/2;

        for(int i=0; i<6 && i<bitmaps.length; i++){
            Bitmap src = bitmaps[i];
            int left = (i%2) * width;
            int top = (i/2) * height;
            dstRect = new Rect(left, top, left + width, top + height);
            if(src!=null)
                canvas.drawBitmap(src, srcRect, dstRect, null);
        }
        return icon;
    }
}
