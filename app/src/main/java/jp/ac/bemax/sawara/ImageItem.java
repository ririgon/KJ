package jp.ac.bemax.sawara;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.File;

/**
 * Created by 雅章 on 2015/01/23.
 */
public class ImageItem {
    private long mediaId;
    private File imageFile;

    public ImageItem(long mediaId, File imageFile){
        this.mediaId = mediaId;
        this.imageFile = imageFile;
    }

    public long getId(){
        return this.mediaId;
    }

    public File getFile(){
        return this.imageFile;
    }
}
