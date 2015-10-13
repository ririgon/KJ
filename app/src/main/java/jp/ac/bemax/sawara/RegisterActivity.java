package jp.ac.bemax.sawara;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.os.Bundle;

/**
 * @author horikawa
 *
 */
public class RegisterActivity extends Activity implements OnClickListener, OnItemClickListener{
	// ** 各種定数の設定 **
	// 画面モードの定数
	static final int MODE_NOTHING = 0;		// 画面モードの指定が無い → エラー
	static final int NEW_MODE = 1;				// 新規登録モード
	static final int UPDATE_MODE = 2;			// 更新モード
	static final int READ_MODE = 3;				// 閲覧モード
	// ボタン用ID
	static final int RETURN_BUTTON = 101;	// 戻るボタン
	static final int ALBAM_BUTTON = 102;	// アルバムボタン
	static final int MOVIE_BUTTON = 103;		// 動画ボタン
	static final int PHOTO_BUTTON = 104;		// 写真ボタン
	static final int REGIST_BUTTON = 105;	// 決定ボタン
	static final int UPDATE_BUTTON = 106;	// 更新ボタン
	static final int DELETE_BUTTON = 107;	// 削除ボタン
	// 画像アイテムの識別用
	static final int PICTURE = 201;					// 写真
	static final int MOVIE = 202;					// 動画
	// 暗黙インテント呼び出し用
	static final int IMAGE_CAPTUER = 301;	// 画像キャプチャ
	static final int MOVIE_CAPTUER = 302;	// 動画撮影
	static final int STRAGE_READ = 303;		// アルバム呼び出し
	// 動画撮影の最大時間（秒）
	private final int MOVIE_MAX_TIME = 20;	// 最大20秒撮影
	private final int MOVIE_QUALITY = 0;		// 動画のクオリティ（低）
	
	private final int MP = RelativeLayout.LayoutParams.MATCH_PARENT;
	
	// ** インスタンス変数の宣言 **
	// ハンドラ
	private Handler mHandler;
	// View
	private RelativeLayout registerLayout;		// レイアウト
	// 画像一覧用のアダプタ
	private ImageAdapter imageViewerAdapter;
	// 画像一覧用の変数
	//private List<Media> mMediaList;

	// タグ一覧用のアダプタ
	private ArrayAdapter<VTextView> tagViewerAdapter;
	// 写真、動画の保存先ファイル
	private File mediaFile;
	// 現在のカテゴリ（新規登録時に使用）
	private Category thisCategory;
    private Category[] thisArticleCategories;
    private Article thisArticle;
	
	private SawaraDBAdapter dbAdapter;
	
	// ディスプレイ関連のstaticな変数
	static float displayDensity;
	static int buttonSize;
	static int gridViewColmn;
	static float displayWidth;
	static float displayHeight;
	static int frameSize;
	// 初期設定用のオブジェクト
	static Configuration conf;
	
	private int mode;

	private Bundle bundle = new Bundle();
	
	/* (非 Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// ディスプレイサイズを取得する
		WindowManager windowManager = getWindowManager();
		Point displaySize = new Point();
		windowManager.getDefaultDisplay().getSize(displaySize);
		DisplayMetrics outMetrics = new DisplayMetrics();
		windowManager.getDefaultDisplay().getMetrics(outMetrics);
		displayDensity = outMetrics.density;
		displayWidth = displaySize.x;
		displayHeight = displaySize.y - 25 * displayDensity;
		buttonSize = (int)(displayHeight / 5);
		gridViewColmn = (int)((displayWidth - buttonSize) / (buttonSize * 2));
		
		// DBとのコネクション
		dbAdapter = new SawaraDBAdapter(this);
		
		setContentView(R.layout.register);
		
		// ** アクティビティにテーマを設定する **
		File confFile = new File(getFilesDir(), "sawara.conf");
		conf = Configuration.loadConfig(confFile);
		if(conf == null){
			conf = new Configuration();
			conf.setTheme("DefaultTheme");
			Configuration.storeConfig(confFile, conf);
		}
		String themeVal = conf.getTheme();
		int resid = getResources().getIdentifier(themeVal, "style", getPackageName());
		setTheme(resid);
		
		// ** インテントの処理 **
		// インテントを取得する
		Intent intent = getIntent();
		mode = intent.getIntExtra("mode", 0); // モード設定が無い場合は、0
		
		// ** ビューア用の初期設定 **
		// イメージビューア用のアダプタ設定
		imageViewerAdapter = new ImageAdapter(this);
		
		//** Viewの設定 **
		// レイアウト
		registerLayout = (RelativeLayout)findViewById(R.id.register_layout);
		// テーマから背景画像を設定
		TypedValue outValue = new TypedValue();
		getTheme().resolveAttribute(R.attr.mainBack, outValue, true);
		Bitmap backBitmap = BitmapFactory.decodeResource(getResources(), outValue.resourceId);
		BitmapDrawable backDrawable = new BitmapDrawable(getResources(), backBitmap);
		backDrawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
		registerLayout.setBackground(backDrawable);
		
		// ** 各Viewの設定 **
		ViewHolder holder = new ViewHolder();
		RelativeLayout.LayoutParams params;
		// 名前テキスト
		holder.nameTextView = (VTextView)findViewById(R.id.register_name);
		holder.nameTextView.setBackGround();
		holder.nameTextView.setTextSize(100);
		// 詳細テキスト
		holder.discriptionTextView = (VTextView)findViewById(R.id.register_description);
		holder.discriptionTextView.setBackGround();
		holder.discriptionTextView.setTextSize(80);
		// アルバムボタン作成
		holder.albamButton = new Button(this);
		holder.albamButton.setBackground(ButtonFactory.getButtonDrawable(this, R.drawable.album_image));
		holder.albamButton.setId(ALBAM_BUTTON);
		holder.albamButton.setOnClickListener(this);
		params = new RelativeLayout.LayoutParams(buttonSize, buttonSize);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		params.addRule(RelativeLayout.LEFT_OF, R.id.register_description);
		params.setMargins(5, 5, 5, 5);
		holder.albamButton.setLayoutParams(params);
		// 動画ボタン作成
		holder.movieButton = new Button(this);
		holder.movieButton.setBackground(ButtonFactory.getButtonDrawable(this, R.drawable.movie_image));
		holder.movieButton.setId(MOVIE_BUTTON);
		holder.movieButton.setOnClickListener(this);
		params = new RelativeLayout.LayoutParams(buttonSize, buttonSize);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		params.addRule(RelativeLayout.LEFT_OF, ALBAM_BUTTON);
		params.setMargins(5, 5, 5, 5);
		holder.movieButton.setLayoutParams(params);
		// 写真ボタン作成
		holder.photoButton = new Button(this);
		holder.photoButton.setBackground(ButtonFactory.getButtonDrawable(this, R.drawable.camera_image));
		holder.photoButton.setId(PHOTO_BUTTON);
		holder.photoButton.setOnClickListener(this);
		params = new RelativeLayout.LayoutParams(buttonSize, buttonSize);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		params.addRule(RelativeLayout.LEFT_OF, MOVIE_BUTTON);
		params.setMargins(5, 5, 5, 5);
		holder.photoButton.setLayoutParams(params);
		// 決定ボタン
		holder.registButton = new Button(this);	
		holder.registButton.setBackground(ButtonFactory.getButtonDrawable(this, R.drawable.regist_button_image));
		holder.registButton.setId(REGIST_BUTTON);
		holder.registButton.setOnClickListener(this);
		params = new RelativeLayout.LayoutParams(buttonSize, buttonSize);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		params.setMargins(5, 5, 5, 5);
		holder.registButton.setLayoutParams(params);
		// 戻るボタン
		holder.returnButton = new Button(this);
		holder.returnButton.setBackground(ButtonFactory.getButtonDrawable(this, R.drawable.return_button_image));
		holder.returnButton.setId(RETURN_BUTTON);
		holder.returnButton.setOnClickListener(this);
		params = new RelativeLayout.LayoutParams(buttonSize, buttonSize);
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		params.setMargins(5, 5, 5, 5);
		holder.returnButton.setLayoutParams(params);
		// 更新ボタン
		holder.updateButton = new Button(this);
		holder.updateButton.setBackground(ButtonFactory.getButtonDrawable(this, R.drawable.update_button_image));
		holder.updateButton.setId(UPDATE_BUTTON);
		holder.updateButton.setOnClickListener(this);
		params = new RelativeLayout.LayoutParams(buttonSize, buttonSize);
		params.addRule(RelativeLayout.LEFT_OF, R.id.register_description);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		params.setMargins(5, 5, 5, 5);
		holder.updateButton.setLayoutParams(params);
		// 削除ボタン
		holder.deleteButton = new Button(this);
		holder.deleteButton.setBackground(ButtonFactory.getButtonDrawable(this, R.drawable.delete_button_image));
		holder.deleteButton.setId(DELETE_BUTTON);
		holder.deleteButton.setOnClickListener(this);
		params = new RelativeLayout.LayoutParams(buttonSize, buttonSize);
		params.addRule(RelativeLayout.LEFT_OF, UPDATE_BUTTON);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		params.setMargins(5, 5, 5, 5);
		holder.deleteButton.setLayoutParams(params);
		// タグビューア
		holder.tagViewerView = (GridView)findViewById(R.id.register_tag_viewer);
		params = new RelativeLayout.LayoutParams(300, MP);
		//params.addRule(RelativeLayout.ABOVE, REGIST_BUTTON);
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		params.setMargins(5, 5, 5, 5);
		holder.tagViewerView.setLayoutParams(params);
		// 画像ビューア
		holder.imageViewerView = (GridView)findViewById(R.id.register_image_viewer);
		holder.imageViewerView.setAdapter(imageViewerAdapter);
		params = new RelativeLayout.LayoutParams(MP, (int)(buttonSize*4));
		//params.addRule(RelativeLayout.ABOVE, ALBAM_BUTTON);
		params.addRule(RelativeLayout.LEFT_OF, R.id.register_description);
		params.addRule(RelativeLayout.RIGHT_OF, R.id.register_tag_viewer);
		params.setMargins(5, 5, 5, 5);
		holder.imageViewerView.setLayoutParams(params);

		registerLayout.setTag(holder);
		
		final RegisterActivity thisObj = this;
		mHandler = new Handler(){
		
			public void handleMessage(android.os.Message msg) {
				super.handleMessage(msg);
				
				//Article article;
				
				ViewHolder holder = (ViewHolder) registerLayout.getTag();
				registerLayout.removeAllViews();
				// ** Viewの配置 **
				registerLayout.addView(holder.nameTextView);
				registerLayout.addView(holder.discriptionTextView);
				// タグビューア
				registerLayout.addView(holder.tagViewerView);
				// 画像ビューア
				registerLayout.addView(holder.imageViewerView);
				// LayoutParamsの宣言
				RelativeLayout.LayoutParams params;

                // DBアクセススタート
                SQLiteDatabase db = dbAdapter.openDb();

                // モードごとに配置する
                switch (msg.what) {
                    case NEW_MODE:    // == 新規登録モード ==
                        Toast.makeText(thisObj, "NEW_MODE", Toast.LENGTH_SHORT).show();

                        // ** インテントからカテゴリ情報を取得 **
                        long categoryId = getIntent().getLongExtra("category_id", -1);
                        thisCategory = Category.getCategory(db, thisObj, categoryId);
                        thisArticleCategories = new Category[]{thisCategory};

                        // ** ボタン配置 **
                        // アルバムボタン
                        registerLayout.addView(holder.albamButton);
                        // 動画ボタン
                        registerLayout.addView(holder.movieButton);
                        // 写真ボタン
                        registerLayout.addView(holder.photoButton);
                        // 決定ボタン
                        registerLayout.addView(holder.registButton);

                        // ** リスナー登録 **
                        // 名前テキスト
                        holder.nameTextView.setOnClickListener(thisObj);
                        holder.nameTextView.setFocusableInTouchMode(true);
                        // 詳細テキスト
                        holder.discriptionTextView.setOnClickListener(thisObj);
                        holder.discriptionTextView.setFocusableInTouchMode(true);


                        break;
                    case UPDATE_MODE:    // == 更新モード ==
                        Toast.makeText(thisObj, "UPDATE_MODE", Toast.LENGTH_SHORT).show();

                        // ** Articleを取得 **
                        thisArticleCategories =thisArticle.getCategoriesThis(db);

                        holder.nameTextView.setText(thisArticle.getName(db));
                        holder.discriptionTextView.setText(thisArticle.getDescription(db));

                        // ImageViewerの初期化
                        imageViewerAdapter.clear();
                        imageViewerAdapter.addAll(thisArticle.getImageItems(db, thisObj));

                        // ** ボタン配置 **
                        // アルバムボタン
                        registerLayout.addView(holder.albamButton);
                        // 動画ボタン
                        registerLayout.addView(holder.movieButton);
                        // 写真ボタン
                        registerLayout.addView(holder.photoButton);
                        // 決定ボタン
                        registerLayout.addView(holder.registButton);

                        // ** リスナー登録 **
                        // 名前テキスト
                        holder.nameTextView.setOnClickListener(thisObj);
                        holder.nameTextView.setFocusableInTouchMode(true);
                        // 詳細テキスト
                        holder.discriptionTextView.setOnClickListener(thisObj);
                        holder.discriptionTextView.setFocusableInTouchMode(true);

                        // リスナを解除する
                        holder.imageViewerView.setOnItemClickListener(null);
                        break;
                    case READ_MODE:    // == 閲覧モード ==
                        Toast.makeText(thisObj, "READ_MODE", Toast.LENGTH_SHORT).show();

                        // ** Articleを取得 **
                        long articleId = getIntent().getLongExtra("article_id", -1);
                        thisArticle = Article.getArticle(thisObj, articleId);
                        thisArticleCategories =thisArticle.getCategoriesThis(db);

                        holder.nameTextView.setText(thisArticle.getName(db));
                        holder.discriptionTextView.setText(thisArticle.getDescription(db));

                        // ImageViewerの初期化
                        imageViewerAdapter.clear();
                        imageViewerAdapter.addAll(thisArticle.getImageItems(db, thisObj));

                        // ** Viewの設置 **
                        // 戻るボタン
                        registerLayout.addView(holder.returnButton);
                        // 更新ボタン
                        registerLayout.addView(holder.updateButton);
                        // 削除ボタン
                        registerLayout.addView(holder.deleteButton);

                        // リスナ登録
                        holder.imageViewerView.setOnItemClickListener(thisObj);

                        // リスナ解除
                        holder.nameTextView.setOnClickListener(null);
                        holder.discriptionTextView.setOnClickListener(null);

                        break;
                }
                // DBを閉じる
                db.close();
            }
		};

		mHandler.sendEmptyMessage(mode);
	}

	/* (非 Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		// ** ローカル変数の初期化 **
		Intent intent = new Intent();		// インテント
		File dir = null;							// 保存先ファイル

		ViewHolder holder = (ViewHolder) registerLayout.getTag();
		switch(v.getId()){
		case ALBAM_BUTTON:		// == アルバム読み込みモード ==
			// 結果を呼び出しもとActivityに返す

			// Activityを終了
			finish();
			break;

		case PHOTO_BUTTON:		// == 写真撮影モード ==
			// ** 保存先を作成 **
			String fileName = "" + System.currentTimeMillis() + ".jpg";
            mediaFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName);
			Uri imageUri = Uri.fromFile(mediaFile);

			CameraEx Camera = new CameraEx();
			Camera.onCreate(bundle);

			//p461
			//このcase分をベースにしてビューをサンプルに作る。

//			// ** 写真撮影用の暗黙インテントを呼び出す準備 **
//			intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
//			intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
//			intent.addCategory(Intent.CATEGORY_DEFAULT);
//
//			// インテントを呼び出す
//			startActivityForResult(intent, IMAGE_CAPTUER);
			break;

		case MOVIE_BUTTON:		// == 動画撮影モード ==
			// ** 保存先を作成 **
			fileName = "" + System.currentTimeMillis() + ".mp4";
            mediaFile = new File(getExternalFilesDir(Environment.DIRECTORY_MOVIES), fileName);
			Uri movieUri = Uri.fromFile(mediaFile);

			// ** 動画撮影用の暗黙院展とを呼び出す準備 **
			intent.setAction(MediaStore.ACTION_VIDEO_CAPTURE);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, movieUri);
			// 動画のクオリティを設定
			intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, MOVIE_QUALITY);
			// 動画の最大撮影時間を設定
			intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, MOVIE_MAX_TIME);

			// インテントを呼び出す
			startActivityForResult(intent, MOVIE_CAPTUER);
			break;

		//*** 入力データを登録する ***
		case REGIST_BUTTON:
			// 基本値をセットする
			String name = holder.nameTextView.getText().toString();
			String description = holder.discriptionTextView.getText().toString();

			SQLiteDatabase db = dbAdapter.openDb();

			boolean success = false;

			if(mode == NEW_MODE) {
				// 新しいArticleを作成する
				db.beginTransaction();
				try {
					if(name.length() == 0 || description.length() == 0)
						throw new Exception("名前または説明がありません");

					thisArticle = new Article(db, this, name, description);
					//新しいMediaを登録する
					List<ImageItem> items = imageViewerAdapter.getImageItems();

					if(items.size() == 0)
						throw new Exception("画像がありません");

                    Media[] medias = new Media[items.size()];
                    for(int i=0; i<medias.length; i++){
                        medias[i] = Media.getMedia(db, this, items.get(i).getId());
                        medias[i].setArticleId(db, thisArticle.getId());
                    }

					//TODO カテゴリーアーティクルテーブルを更新する
                    for(Category category: thisArticleCategories){
                        thisArticle.setCategory(db, category);
                    }

					db.setTransactionSuccessful();
					success = true;
				}catch(Exception e){
					e.printStackTrace();
					// TODO エラー画面を表示する

				}finally {
					db.endTransaction();
				}
			}else if(mode == UPDATE_MODE){
				// Articleを更新する
				db.beginTransaction();
				try {
					if(name.length() == 0 || description.length() == 0)
						throw new Exception("名前または説明がありません");

                    thisArticle.setName(db, name);
                    thisArticle.setDescription(db, description);
                    thisArticle.setModified(db, System.currentTimeMillis());

                    List<ImageItem> items = imageViewerAdapter.getImageItems();
					if(items.size() == 0)
						throw new Exception("画像がありません");

                    // メディアを更新する
                    Media[] medias = new Media[items.size()];
                    for (int i = 0; i < medias.length; i++) {
                        medias[i] = Media.getMedia(db, this, items.get(i).getId());
                        if (medias[i].getArticleId(db) <= 0) {
                            medias[i].setArticleId(db, thisArticle.getId());
                        }
                    }

                    //TODO カテゴリーアーティクルテーブルを更新する
                    for(Category category: thisArticleCategories){
                        thisArticle.setCategory(db, category);
                    }

                    db.setTransactionSuccessful();
                    success = true;
                }catch (Exception e){
                    e.printStackTrace();
				}finally {
					db.endTransaction();
				}
			}
			// 成功
			if(success){
				intent.putExtra("article_id", thisArticle.getId());
				setResult(RESULT_OK, intent);
			}else{
				setResult(RESULT_CANCELED, intent);
			}

			db.close();
			finish();

			break;
		case RETURN_BUTTON:
			finish();
			break;
		case UPDATE_BUTTON:
			mHandler.sendEmptyMessage(UPDATE_MODE);
			mode = UPDATE_MODE;
			break;
		}
	}

	/*
	 *
	 * (非 Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		// インテントからの返信が成功した場合
		if(resultCode == RESULT_OK){
			SQLiteDatabase db = dbAdapter.openDb();
            db.beginTransaction();
            try {
                switch (requestCode) {

                    //*** 写真を撮影した場合 ***
                    case IMAGE_CAPTUER:
                        Bitmap itemIcon;
                        try {
                            Media newMedia = new Media(db, this, mediaFile.getName(), Media.PHOTO);
                            File iconFile = newMedia.getIconFile(db);
                            ImageItem item = new ImageItem(newMedia.getId(), iconFile);
                            // ビューアに反映する
                            imageViewerAdapter.add(item);
                            imageViewerAdapter.notifyDataSetChanged();
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                        break;

                    //*** 動画を撮影した場合 ***
                    case MOVIE_CAPTUER:
                        try {
                            Media newMedia = new Media(db, this, mediaFile.getName(), Media.MOVIE);
                            File iconFile = newMedia.getIconFile(db);
                            ImageItem item = new ImageItem(newMedia.getId(), iconFile);

                            imageViewerAdapter.add(item);
                            imageViewerAdapter.notifyDataSetChanged();
                        }catch (Exception e){
                            e.printStackTrace();
                            itemIcon = IconFactory.getNullImage();
                        }
                        break;
                }

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                db.close();
            }
        }
	}


	/*
	 *  viewerアイテムがクリックされたときに呼び出される
	 *  (非 Javadoc)
	 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		SQLiteDatabase db = dbAdapter.openDb();

        db.beginTransaction();
        try {
            switch (parent.getId()) {
                case R.id.register_image_viewer:
                    ImageItem item = imageViewerAdapter.getItem(position);
                    if (item.getId() != -1) {
                        Media media = Media.getMedia(db, this, item.getId());
                        long type = media.getType(db);
                        File mediaFile = media.getMediaFile(db);

                        Uri uri = Uri.fromFile(mediaFile);
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        if (type == Media.PHOTO) {
                            intent.setDataAndType(uri, "image/*");
                        } else {
                            intent.setDataAndType(uri, "video/*");
                        }
                        startActivity(intent);
                    }
                    break;
                case R.id.register_tag_viewer:
                    break;
            }

            db.setTransactionSuccessful();
        }catch (Exception e){

        }finally {
            db.endTransaction();
            db.close();
        }
    }

	/*
	void putNewModeButtons(){
		RelativeLayout.LayoutParams params;

		params = new RelativeLayout.LayoutParams(200,200);

	}
	*/

	private int[] getImageTypes(int photo, int movie){
		int[] types = new int[photo + movie];

		for(int i=0; i<types.length; i++){
			if(i < photo){
				types[i] = PICTURE;
			}else{
				types[i] = MOVIE;
			}
		}

		return types;
	}

	private String[] getImagePaths(String[] photo, String[] movie){
		String[] paths = new String[photo.length + movie.length];

		for(int i=0; i<paths.length; i++){
			if(i<photo.length){
				paths[i] = photo[i];
			}else{
				paths[i] = movie[i-photo.length];
			}
		}

		return paths;
	}

	class ViewHolder{
		VTextView nameTextView;
		VTextView discriptionTextView;
		Button updateButton;
		Button deleteButton;
		Button registButton;
		Button albamButton;
		Button returnButton;
		Button photoButton;
		Button movieButton;
		GridView imageViewerView;
		GridView tagViewerView;
	}
}
