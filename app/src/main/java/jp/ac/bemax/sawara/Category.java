package jp.ac.bemax.sawara;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * Articleが属するカテゴリを表すクラス
 * @author Masaaki Horikawa
 * 2014/09/30
 */
public class Category{
	public static final String TABLE_NAME = "category_table";
    public static final String ID = "ROWID";
	public static final String NAME = "name";
	public static final String MODIFIED = "modified";
	public static final String POSITION = "position";
    public static final String ICON = "icon";

    public static final int ICON_WIDTH = 320;
    public static final int ICON_HEIGHT = 240;
	
	private long rowid;
    private Context context;

    /**
     * IDを指定して、カテゴリオブジェクトを作成する
     * @param id
     */
	private Category(Context context, long id){
		rowid = id;
        this.context = context;
	}

    /**
     * カテゴリ名から、新しいカテゴリを作成する。
     * オブジェクト作成と当時に、DBにinsertする。
     * @param db
     * @param name
     */
	public Category(SQLiteDatabase db, Context context, String name){
        this.context = context;
        db.beginTransaction();
        try {
            rowid = insert(db, name);
            setPosition(db, rowid);
            db.setTransactionSuccessful();
        }finally {
            db.endTransaction();
        }
    }

    public static Category getCategory(SQLiteDatabase db, Context context, long categoryId){
        Category category = null;
        if(db != null) {
            String sql = "select ROWID from Category_table where ROWID=?";
            String[] selectionArgs = {"" + categoryId};
            Cursor cursor = db.rawQuery(sql, selectionArgs);
            if (cursor.getCount() > 0) {
                category = new Category(context, categoryId);
            }
        }else{
            category = new Category(context, categoryId);
        }
        return category;
    }

    /**
     * カテゴリ名とアーティクル配列から、新しいカテゴリを作成する
     * オブジェクト作成と当時に、DBにinsertする。
     * @param db
     * @param context
     * @param name
     * @param articles
     */
    public Category(SQLiteDatabase db, Context context, String name, Article[] articles) throws Exception {
        db.beginTransaction();
        try {
            // カテゴリテーブルにデータを作成
            rowid = insert(db, name);
            setPosition(db, rowid);

            // カテゴリアーティクルテーブルにデータを作成
            String sql = "insert into category_article_table(article_id, category_id) values (?,?)";
            SQLiteStatement statement = db.compileStatement(sql);
            for (Article article : articles) {
                statement.bindLong(1, article.getId());
                statement.bindLong(2, rowid);
                statement.executeInsert();
            }

            // カテゴリのアイコンを作成する
            Bitmap iconBitmap = makeCategoryIconBitmap(db, context);
            updateIcon(db);

            db.setTransactionSuccessful();
        }catch (Exception e){
            e.printStackTrace();
            throw new Exception("新しいカテゴリを作れなかったよ");
        }finally {
            db.endTransaction();
        }
    }

    /**
     * DBにインサートを行う。インサートに成功したら、
     * @param db
     * @param name
     * @return
     */
	public long insert(SQLiteDatabase db, String name){
		String sql = "insert into category_table(name, modified) values (?,?)";
		SQLiteStatement statement = db.compileStatement(sql);
		statement.bindString(1, name);
		statement.bindLong(2, System.currentTimeMillis());
		long id = statement.executeInsert();
		
		return id;
	}
	
	public int delete(SQLiteDatabase db){
		String sql = "delete from category_table where ROWID=?";
		SQLiteStatement statement = db.compileStatement(sql);
		statement.bindLong(1, rowid);
		int row = statement.executeUpdateDelete();
		
		if(row > 0){
			rowid = -1;
		}
		
		return row;
	}

    public void updateIcon(SQLiteDatabase db){
        String iconFileName = null;
        File dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        Bitmap iconBitmap = null;

        db.beginTransaction();
        // TODO アイコン画像を作成する

        String[] selectionArgs = {"" + rowid};
        try {
            String sql = "select min(media_id) from category_media_view where category_id=? group by article_id";
            Cursor cursor = db.rawQuery(sql, selectionArgs);
            if (cursor.getCount() > 0) {
                Bitmap[] mediaBitmaps = new Bitmap[cursor.getCount()];
                while (cursor.moveToNext()) {
                    mediaBitmaps[cursor.getPosition()] = Media.getMedia(null, context, cursor.getLong(0)).getIconBitmap(db);
                }
                iconBitmap = IconFactory.makeSixMatrixIcon(context, mediaBitmaps);
            }
            if(iconBitmap == null)
                iconBitmap = IconFactory.getNullImage();

            // TODO 画像を保存する
            iconFileName = "category_icon_"+rowid+".png";
            FileOutputStream fos = null;
            try{
                fos = new FileOutputStream(new File(dir, iconFileName));
                iconBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);

                // TODO 新規作成の場合に実行する
                SQLiteStatement statement = db.compileStatement("update category_table set icon=? where ROWID=?");
                statement.bindString(1, iconFileName);
                statement.bindLong(2, rowid);
                statement.executeUpdateDelete();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }finally {
                try{
                    if(fos != null)
                        fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            db.setTransactionSuccessful();
        }finally {
            db.endTransaction();
        }
    }

    public String getIconFileName(SQLiteDatabase db){
        String fileName = null;

        String sql = "select icon from category_table where ROWID=?";
        String[] selectionArgs = {""+rowid};
        Cursor cursor = db.rawQuery(sql, selectionArgs);
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            fileName = cursor.getString(0);
        }
        cursor.close();

        return fileName;
    }

    public Bitmap getIcon(SQLiteDatabase db){
        Bitmap icon = null;
        String fileName = getIconFileName(db);

        if(fileName != null){
            icon = BitmapFactory.decodeFile(fileName);
        }

        return icon;
    }

    public File getIconFile(SQLiteDatabase db){
        File file = null;
        String fileName = getIconFileName(db);

        if(fileName != null){
            file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName);
        }

        return file;
    }

	/**
	 * カテゴリの表示順をゲットする
	 * @return 表示順
	 */
	public long getPosition(SQLiteDatabase db) {
        String[] selectionArgs = {""+rowid};
		Cursor cursor = db.rawQuery("select position from category_table where ROWID=?", selectionArgs);
        cursor.moveToFirst();
        long position = cursor.getLong(0);
        cursor.close();

        return position;
	}

	/**
	 * カテゴリの表示順をセットする
	 * @param db database
     * @param position position
	 */
	public void setPosition(SQLiteDatabase db, long position) {
		SQLiteStatement statement = db.compileStatement("update category_table set position=? where ROWID=?");
        statement.bindLong(1, position);
        statement.bindLong(2, rowid);
        statement.executeUpdateDelete();
	}

	/**
	 * カテゴリの更新日時をゲットする
	 * @return 更新日時
	 */
	public long getModified(SQLiteDatabase db) {
        String[] selectionArgs = {""+rowid};
        Cursor cursor = db.rawQuery("select modified from category_table where ROWID=?", selectionArgs);
        cursor.moveToFirst();
        long modified = cursor.getLong(0);
        cursor.close();

        return modified;
	}

	/**
	 * カテゴリの更新日時をセットする
	 * @param modified 更新日時
	 */
	public void setModified(SQLiteDatabase db, long modified) {
        SQLiteStatement statement = db.compileStatement("update category_table set modified=? where ROWID=?");
        statement.bindLong(1, modified);
        statement.bindLong(2, rowid);
        statement.executeUpdateDelete();
	}

	/**
	 * カテゴリの名前をセットする
	 * @param name カテゴリの名前
	 */
	public void setName(SQLiteDatabase db, String name) {
        SQLiteStatement statement = db.compileStatement("update category_table set name=? where ROWID=?");
        statement.bindString(1, name);
        statement.bindLong(2, rowid);
        statement.executeUpdateDelete();
	}

	/**
	 * カテゴリのIDをゲットする
	 */
	public long getId() {
		return rowid;
	}

	/**
	 * カテゴリのIDセットする
	 * @param id カテゴリのID
	 */
	public void setId(long id){
		rowid = id;
	}
	
	/**
	 * カテゴリの名前をゲットする
	 */
	public String getName(SQLiteDatabase db) {
        String[] selectionArgs = {""+rowid};
        Cursor cursor = db.rawQuery("select name from category_table where ROWID=?", selectionArgs);
        cursor.moveToFirst();
        String name = cursor.getString(0);
        cursor.close();

        return name;
	}

    /**
     * カテゴリのアイコンを作成し、ビットマップで返す
     * @param db
     * @param context
     * @return
     */
	public Bitmap makeCategoryIconBitmap(SQLiteDatabase db, Context context) {
        Bitmap icon = null;

        db.beginTransaction();
        try {
            String sql = "select min(media_id) from category_media_view where category_id=? group by article_id";
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

    /*
    public static Category findCategoryByName(SQLiteDatabase db, String name){
        Category category = null;
        String[] selectionArgs = {name};
        Cursor cursor = db.rawQuery("select ROWID from category_table where name=?", selectionArgs);
        cursor.moveToFirst();
        if(cursor.getCount()>0){
            long id = cursor.getLong(0);
            category = new Category(id);
        }
        return category;
    }
*/
    public List<Article> getArticles(SQLiteDatabase db){
        List<Article> list = new ArrayList<Article>();

        String sql = "select article_id from category_article_table where category_id=?";
        String[] selectionArgs = {""+rowid};
        Cursor cursor = db.rawQuery(sql, selectionArgs);

        while(cursor.moveToNext()){
            long articleId = cursor.getLong(0);
            Article article = Article.getArticle(context, articleId);
            list.add(article);
        }
        return list;
    }

    public static List<Category> getAllCategory(SQLiteDatabase db, Context context){
        List<Category> list = new ArrayList<Category>();

        String sql = "select ROWID from category_table";
        Cursor cursor = db.rawQuery(sql, null);

        while(cursor.moveToNext()){
            long id = cursor.getLong(0);
            Category category = new Category(context, id);
            list.add(category);
        }

        return list;
    }
}
