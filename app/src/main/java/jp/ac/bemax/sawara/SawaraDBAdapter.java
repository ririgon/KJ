package jp.ac.bemax.sawara;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

/**
 * 言葉事典アプリのDBを扱うクラス
 * @author Masaaki Horikawa
 * 2014/09/30
 */
public class SawaraDBAdapter{
	private SQLiteOpenHelper helper;
    private SQLiteDatabase mDb;
	
	public SawaraDBAdapter(Context context) {
		helper = new DBAdapter(context, "sawara.db", null, 1);
        mDb = null;
	}

	public SQLiteOpenHelper getHelper(){
		return helper;
	}

    public SQLiteDatabase openDb(){
        if(mDb == null){
            mDb = helper.getWritableDatabase();
        }
        if(!mDb.isOpen()){
            mDb = helper.getWritableDatabase();
        }
        return mDb;
    }

    public void closeDb(){
        if(mDb != null && mDb.isOpen()){
            mDb.close();
        }
    }
	
	/**
	 * group_tableのgroup_nameの写像リストを返す
	 * @return group_nameのリスト
	 */
	public List<String> getGroupNameList(){
		List<String> list = new ArrayList<String>();
		
		SQLiteDatabase db = helper.getReadableDatabase();
		
		try{
			Cursor cursor = db.rawQuery("select group_name from group_table", null);
			while(cursor.moveToNext()){
				list.add(cursor.getString(0));
			}
		}finally{
			db.close();
		}

		return list;
	}
	
	/**
	 * データベースの初期化・更新処理をおこなうインナークラス
	 * @author Masaaki Horikawa
	 * 2014/07/09
	 */
	class DBAdapter extends SQLiteOpenHelper{
		private Context context;
		
		public DBAdapter(Context context, String name, CursorFactory factory, int version) {
			super(context, name, factory, version);
			this.context = context;
		}

		/* (非 Javadoc)
		 * 初めてデータベースを作成したときに実行される
		 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
		 */
		@Override
		public void onCreate(SQLiteDatabase db) {
            String sql;
            db.beginTransaction();
            try {
                // カテゴリテーブルを新規作成
                sql = "create table category_table " +
                        "(name text unique not null, " +                // カテゴリ名
                        " icon string unique," +                // アイコン画像のパス
                        " position integer unique," +        // 表示位置
                        " modified integer )";        // 更新日時
                db.execSQL(sql);

                // アーティクルテーブルを新規作成
                sql = "create table article_table " +
                        "(name text unique not null, " +    // 名前
                        " description text not null," +        // 説明
                        " icon string unique, " +          // アイコン
                        " position integer unique," +         // 表示位置
                        " modified integer )";                    // 更新日時
                db.execSQL(sql);

                sql = "create table category_article_table " +
                        "(category_id integer not null," +        // カテゴリID
                        " article_id integer not null," +        // アーティクルID
                        " unique (category_id, article_id))";    // ユニーク制約条件
                db.execSQL(sql);

                sql = "create table tag " +
                        "(id integer unique," +        // カテゴリID
                        " tag_id integer unique not null," +        // アーティクルID
                        " media_id integer unique not null)";    // ユニーク制約条件
                db.execSQL(sql);

                // メディアテーブルの新規作成
                sql = "create table media_table " +
                        "(id integer unique," +                 // メディアID
                        "file_name text unique not null, " +    // ファイル名
                        "type integer not null, " +             // メディアタイプ
                        "article_id integer, " +                // アーティクルID
                        "icon string unique, " +                //  アイコンの画像パス
                        "modified integer)";                    // 更新日時
                        //"tag_name integer not null," +          // タグ名
                        //"sort_id integer unique not null," +     // 順番自由変更
                        //"favorite_flag bool not null )";        // 栞
                db.execSQL(sql);

                // カテゴリーとメディアの関係ビュー
                sql = "create view category_media_view as " +
                        "select A.category_id, A.article_id, B.ROWID media_id " +
                        "from category_article_table A " +
                        "inner join media_table B " +
                        "on A.article_id=B.article_id";
                db.execSQL(sql);

                /****  サンプルデータセット ***/
                File imageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                File movieDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES);

                SQLiteStatement statement, statement2;
                String sql2;

                // CategoryTable作成
                String[] catNames = {"くるま", "その他"};
                Category[] categories = new Category[2];
                for (int i=0; i<categories.length; i++) {
                    Category category = new Category(db, context, catNames[i]);
                    categories[i] = category;
                }
                // ArticleTable
                String[] artName = {"ふつうしゃ", "けい", "イギリスのバス"};
                String[] artDesc = {"ふつうのおおきさのくるま", "ちいさいくるま", "にかいだてのバス"};
                        String[][] paths = {
                        {"legacy.jpg", "legacy2.jpg"},
                        {"r1.jpg"},
                        {"buss.mp4"}
                };
                long[][] types = {
                        {Media.PHOTO, Media.PHOTO},
                        {Media.PHOTO},
                        {Media.MOVIE}
                };
                Category[] articleCategories = {categories[0]};

                for (int i = 0; i < artName.length; i++) {
                    Article article = new Article(db, context, artName[i], artDesc[i], articleCategories);
                    for (int j = 0; j < paths[i].length; j++) {
                        File file = copyFromAssets(types[i][j], paths[i][j]);
                        Media media = new Media(db, context, paths[i][j], types[i][j], article);
                        if(j==0){
                            Bitmap iconSrc = IconFactory.loadBitmapFromFileAndType(file, types[i][j]);
                            Bitmap iconBitmap = IconFactory.makeNormalIcon(iconSrc);
                            article.updateIcon(db);
                        }
                    }
                    article.updateIcon(db);
                }

                // カテゴリのアイコンを作成する
                List<Category> cs = Category.getAllCategory(db, context);
                for (Category category : cs) {
                    category.updateIcon(db);
                }

                db.setTransactionSuccessful();
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                db.endTransaction();
            }
        }

        public File copyFromAssets(long type, String filename){
            String filePath = null;
            byte[] buffer = new byte[1024*4];
            File dir = null;
            if(type == Media.PHOTO){
                dir =context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            }else if(type == Media.MOVIE){
                dir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES);
            }
            File outFile = new File(dir, filename);
            InputStream is = null;
            FileOutputStream fos = null;
            try{
                is = context.getAssets().open(filename);
                fos = new FileOutputStream(outFile);
                int num = -1;
                while(-1 != (num = is.read(buffer))){
                    fos.write(buffer, 0, buffer.length);
                }
                filePath = outFile.getPath();
            }catch(IOException e){
                e.printStackTrace();
            }finally{
                try{
                    fos.close();
                    is.close();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
			return outFile;
		}
		
		/* (非 Javadoc)
		 * データベースがアップデートされたときに実行される
		 * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
		 */
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			
		}
	}
	
	/**
	 * データベースをダンプする
	 */
	public void dump(SQLiteDatabase db){
		Cursor cursor = db.rawQuery("select ROWID, * from article_table",null);
		
		while(cursor.moveToNext()){
			String str = "|";
			for(int i = 0; i < cursor.getColumnCount(); i++){
				str += cursor.getString(i) + "|";
			}
			Log.d("article", str);
		}
		
		cursor = db.rawQuery("select ROWID, * from category_table", null);
		while(cursor.moveToNext()){
			String str = "|";
			for(int i = 0; i < cursor.getColumnCount(); i++){
				str += cursor.getString(i) + "|";
			}
			Log.d("category", str);
		}
		
		cursor = db.rawQuery("select ROWID, * from category_article_table", null);
		while(cursor.moveToNext()){
			String str = "|";
			for(int i = 0; i < cursor.getColumnCount(); i++){
				str += cursor.getString(i) + "|";
			}
			Log.d("category_article", str);
		}
		
		cursor = db.rawQuery("select ROWID, * from media_table", null);
		while(cursor.moveToNext()){
			String str = "|";
			for(int i = 0; i < cursor.getColumnCount(); i++){
				str += cursor.getString(i) + "|";
			}
			Log.d("media_table", str);
		}

        cursor = db.rawQuery("select category_id, article_id, media_id from category_media_view", null);
        while(cursor.moveToNext()){
            String str = "|";
            for(int i=0; i<cursor.getColumnCount(); i++){
                str += cursor.getString(i) + "|";
            }
            Log.d("category_media_view", str);
        }
	}
}
