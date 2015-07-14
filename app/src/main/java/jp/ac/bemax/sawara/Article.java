package jp.ac.bemax.sawara;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;


/**
 * 言葉事典で取り扱う、１情報単位を表すクラス
 * @author Masaaki Horikawa
 * 2014/09/05
 */
public class Article{
	//static final String TABLE_NAME = "article_table";
	//static final String ID = "ROWID";
	static final String NAME = "name";
	static final String DESCRIPTION = "description";
	static final String POSITION = "position";
	static final String MODIFIED = "modified";

	private long rowid;
    private Context context;
	
	/**
	 * Article.javaコンストラクタ
	 */
	private Article(long id, Context context){
		rowid = id;
        this.context = context;
	}

    public static Article getArticle(Context context, long id){
        return new Article(id, context);
    }

	public Article(SQLiteDatabase db, Context context, String name, String description){
        db.beginTransaction();
        try {
            rowid = insert(db, name, description);
            setPosition(db, rowid);
            this.context = context;

            db.setTransactionSuccessful();
        }finally {
            db.endTransaction();
        }
    }

    /**
     *
     * @param db
     * @param context
     * @param name
     * @param description
     * @param categories
     */
    public Article(SQLiteDatabase db, Context context, String name, String description, Category[] categories){
        db.beginTransaction();
        try {
            rowid = insert(db, name, description);
            setPosition(db, rowid);
            this.context = context;

            // TODO category_article_tableを更新
            for(Category category: categories) {
                String sql = "insert into category_article_table(category_id, article_id) values (?,?)";
                SQLiteStatement statement = db.compileStatement(sql);
                statement.bindLong(1, category.getId());
                statement.bindLong(2, rowid);
                statement.executeInsert();
            }

            db.setTransactionSuccessful();
        }finally {
            db.endTransaction();
        }
    }

	public long insert(SQLiteDatabase db, String name, String description) {
        String sql = "insert into article_table(name, description, modified) values (?,?,?)";
        SQLiteStatement statement = db.compileStatement(sql);
        statement.bindString(1, name);
        statement.bindString(2, description);
        statement.bindLong(3, System.currentTimeMillis());
        long id = statement.executeInsert();

        return id;
    }

    public long getId(){
        return rowid;
    }

	/**
	 * 
	 * @return
	 * @throws Exception 
	 */
	public long getModified(SQLiteDatabase db)  {
		String[] selectionArgs = {""+rowid};
		Cursor cursor = db.rawQuery("select modified from article_table where ROWID=?", selectionArgs);
		cursor.moveToFirst();
		long modified = cursor.getLong(0);
		
		return modified;
	}

	/**
	 * 
	 * @param modified
	 * @throws Exception 
	 */
	public void setModified(SQLiteDatabase db, long modified) {
		SQLiteStatement statement = db.compileStatement("update article_table set modified=? where ROWID=?");
		statement.bindLong(1, modified);
		statement.bindLong(2, rowid);
		statement.executeUpdateDelete();
	}

	/**
	 * 
	 * @param name
	 */
	public void setName(SQLiteDatabase db, String name) {
		SQLiteStatement statement = db.compileStatement("update article_table set name=? where ROWID=?");
		statement.bindString(1, name);
		statement.bindLong(2, rowid);
		statement.executeUpdateDelete();
	}

	/**
	 * 
	 * @param description
	 * @throws Exception 
	 */
	public void setDescription(SQLiteDatabase db, String description) {
		SQLiteStatement statement = db.compileStatement("update article_table set description=? where ROWID=?");
		statement.bindString(1, description);
		statement.bindLong(2, rowid);
		statement.executeUpdateDelete();
	}
	
	/**
	 * item_nameを返す
	 * @return アイテムの名前
	 * @throws Exception 
	 */
	public String getName(SQLiteDatabase db){
		String[] selectionArgs = {""+rowid};
		Cursor cursor = db.rawQuery("select name from article_table where ROWID=?", selectionArgs);
		cursor.moveToFirst();
		String name = cursor.getString(0);
		
		return name;
	}
	
	/**
	 * descriptionを返す
	 * @return アイテムの詳細
	 * @throws Exception 
	 */
	public String getDescription(SQLiteDatabase db) {
		String[] selectionArgs = {""+rowid};
		Cursor cursor = db.rawQuery("select description from article_table where ROWID=?", selectionArgs);
		cursor.moveToFirst();
		String description = cursor.getString(0);
		
		return description;
	}

	public long getPosition(SQLiteDatabase db) {
		String[] selectionArgs = {""+rowid};
		Cursor cursor = db.rawQuery("select position from article_table where ROWID=?", selectionArgs);
		cursor.moveToFirst();
		long position = cursor.getLong(0);
		
		return position;
	}

	public void setPosition(SQLiteDatabase db, long position) {
		SQLiteStatement statement = db.compileStatement("update article_table set position=? where ROWID=?");
		statement.bindLong(1, position);
		statement.bindLong(2, rowid);
		statement.executeUpdateDelete();
	}

    public List<Category> createCategoriesForArticle(SQLiteDatabase db, List<Category> categories){
		String sql = "insert into category_article_table(category_id, article_id) values (?, ?)";
        SQLiteStatement statement = db.compileStatement(sql);
        for(Category category: categories){
            statement.bindLong(1, category.getId());
            statement.bindLong(2, rowid);
            statement.executeInsert();
        }
		return categories;
    }

    /**
     *
     * @param db
     */
    public void updateIcon(SQLiteDatabase db){
        String icon = null;
        File dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        db.beginTransaction();
        try {
			// TODO アイコンファイル名を読みだす
			String sql = "select icon from article_table where ROWID=?";
			String[] selectionArgs = {"" + rowid};
			Cursor cursor = db.rawQuery(sql, selectionArgs);
			int row = cursor.getCount();
			while (cursor.moveToNext()) {
				icon = cursor.getString(0);
			}
			cursor.close();

			// TODO 画像を保存する
			icon = "article_icon_" + rowid + ".png";
			FileOutputStream fos = null;
			try {
				Bitmap iconBitmap = makeArticleIconBitmap(db);
				fos = new FileOutputStream(new File(dir, icon));
				if (iconBitmap == null)
					throw new Exception("アイコンメディアがない");
				iconBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);

				// TODO 新規作成の場合に実行する
				SQLiteStatement statement = db.compileStatement("update article_table set icon=? where ROWID=?");
				statement.bindString(1, icon);
				statement.bindLong(2, rowid);
				statement.executeUpdateDelete();

				db.setTransactionSuccessful();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (fos != null)
						fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
        }finally {
            db.endTransaction();
        }
    }

    public String getIconFileName(SQLiteDatabase db){
        String fileName = null;

        String sql = "select icon from article_table where ROWID=?";
        String[] selectionArgs = {""+rowid};
        Cursor cursor = db.rawQuery(sql, selectionArgs);
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            fileName = cursor.getString(0);
        }
        cursor.close();

        return fileName;
    }

	public Bitmap getIcon(SQLiteDatabase db) {
        String fileName = getIconFileName(db);
        Bitmap icon = null;

        if(fileName != null){
            icon = BitmapFactory.decodeFile(fileName);
        }

		return icon;
	}

    public File getIconFile(SQLiteDatabase db) {
        String fileName = getIconFileName(db);
        File iconFile = null;

        if(fileName != null){
            iconFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName);
        }

        return iconFile;
    }

    /**
     * このアーティクルに属する画像の配列を返す
     * @param db
     * @param context
     * @return 画像の配列
     */
    public Media[] getMedias(SQLiteDatabase db, Context context){
        Media[] medias = null;

        db.beginTransaction();
        try {
            String sql = "select ROWID from media_table where article_id=?";
            String[] selectionArgs = {"" + rowid};
            Cursor cursor = db.rawQuery(sql, selectionArgs);
            if(cursor.getCount() > 0) {
                medias = new Media[cursor.getCount()];
                while (cursor.moveToNext()) {
                    long mediaId = cursor.getLong(0);
                    Media media = Media.getMedia(db, context, mediaId);
                    medias[cursor.getPosition()] = media;
                }
            }
            db.setTransactionSuccessful();
        }finally {
            db.endTransaction();
        }
        return medias;
    }


    public static List<Article> getAllArticles(SQLiteDatabase db, Context context){
        List<Article> articles = new ArrayList<Article>();

        db.beginTransaction();
        try {
            String sql = "select ROWID from article_table";
            Cursor cursor = db.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                long id = cursor.getLong(0);
                Article article = new Article(id, context);
                articles.add(article);
            }
            db.setTransactionSuccessful();
        }finally {
            db.endTransaction();
        }

        return articles;
    }

    public void setCategory(SQLiteDatabase db, Category category){
        db.beginTransaction();
        try {
            String sql = "select category_id from category_article_table where article_id=? and category_id=?";
            String[] selectionArgs = {"" + rowid, "" + category.getId()};
            Cursor cursor = db.rawQuery(sql, selectionArgs);

            if (cursor.getCount() <= 0) {
                sql = "insert into category_article_table(category_id, article_id) values (?, ?)";
                SQLiteStatement statement = db.compileStatement(sql);
                statement.bindLong(1, category.getId());
                statement.bindLong(2, rowid);
                long id = statement.executeInsert();
                if (id == -1) {
                    throw new Exception("インサートに失敗したよ");
                }
            }
            cursor.close();

            db.setTransactionSuccessful();
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            db.endTransaction();
        }
    }

    public Category[] getCategoriesThis(SQLiteDatabase db){
        String sql = "select category_id from category_article_table where article_id=?";
        String[] selectionArgs = {""+rowid};
        Cursor cursor = db.rawQuery(sql, selectionArgs);
        Category[] categories = new Category[cursor.getCount()];
        while(cursor.moveToNext()){
            categories[cursor.getPosition()] = Category.getCategory(db, context, cursor.getLong(0));
        }
        return categories;
    }

    public Bitmap makeArticleIconBitmap(SQLiteDatabase db){
        Bitmap icon = null;

        db.beginTransaction();
        try {
            String sql = "select ROWID from media_table where article_id=?";
            String[] selectionArgs = {"" + rowid};
            Cursor cursor = db.rawQuery(sql, selectionArgs);
            if (cursor.getCount() > 0) {
                Bitmap[] mediaBitmaps = new Bitmap[cursor.getCount()];
                while (cursor.moveToNext()) {
                    mediaBitmaps[cursor.getPosition()] = Media.getMedia(null, context, cursor.getLong(0)).getIconBitmap(db);
                }
                icon = IconFactory.makeSixMatrixIcon(context, mediaBitmaps);
            }
            cursor.close();

            db.setTransactionSuccessful();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            db.endTransaction();
        }
        return icon;

    }

    public List<ImageItem> getImageItems(SQLiteDatabase db, Context context){
        List<ImageItem> iconList = new ArrayList<ImageItem>();

        db.beginTransaction();
        try {
            String sql = "select ROWID, icon from media_table where article_id=?";
            String[] selectionArgs = {"" + rowid};
            Cursor cursor = db.rawQuery(sql, selectionArgs);
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    long mediaId = cursor.getLong(0);
                    String icon = cursor.getString(1);
                    File iconFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), icon);
                    ImageItem item = new ImageItem(mediaId, iconFile);
                    iconList.add(item);
                }
            }
            db.setTransactionSuccessful();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            db.endTransaction();
        }
        return iconList;
    }
}
