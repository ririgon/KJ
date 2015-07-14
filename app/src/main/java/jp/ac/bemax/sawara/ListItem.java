package jp.ac.bemax.sawara;

import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;

import java.io.File;
import java.io.Serializable;
import java.net.ContentHandler;

/**
 * GridViewに表示するアイテムに実装するインターフェイス
 * @author Masaaki Horikawa
 * 2014/09/30
 */
public class ListItem implements Serializable{
    static final int IMAGE_SIZE = 480;
    private long id;
    private String name;
    private File iconFile;

    public ListItem(long id, String name, File iconFile){
        this.id = id;
        this.name = name;
        this.iconFile = iconFile;
    }

	public long getId(){ return id;};

    public String getName(){return name;};

    public Bitmap getIcon(){
        Bitmap icon = BitmapFactory.decodeFile(iconFile.getPath());
        return icon;
    }
}
