package jp.ac.bemax.sawara;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RelativeLayout;

/**
 * ホーム画面のアクティビティ
 * @author Masaaki Horikawa
 * 2014/07/02
 */
public class HomeActivity extends Activity implements OnClickListener, OnMenuItemClickListener, OnItemClickListener{
	// 画面更新用のID
	static final int DISPLAY_CHANGE = 0;
	static final int THEMA_CHANGE = 1;
	// インテント呼び出し用ID 
	static final int REGISTER = 100;
	// 画面用のID
	private final int CATEGORY_VIEW = 1;
	private final int ARTICLE_VIEW = 2;

    // 各VIEW用のID
	private final int HOME_LAYOUT = 1;
    private final int NEW_BUTTON = 2;
    private final int SETTING_BUTTON = 3;
    private final int RETURN_BUTTON = 4;
    private final int GRID_VIEW = 5;
    private final int CATEGORY_TEXT_VIEW = 6;

	private final int MP = RelativeLayout.LayoutParams.MATCH_PARENT;
	
	private Handler mHandler;
	private RelativeLayout homeLayout;
	private GridAdapter gridAdapter;
	//private List<ListItem> listItems;
	private int viewMode;
	private Category thisCategory;
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
		ButtonFactory.setButtonFrameSize(buttonSize);
		
		// 設定ファイルを読み込む
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

        // DataBaseを開く
		dbAdapter = new SawaraDBAdapter(this);
        SQLiteDatabase db = dbAdapter.openDb();
        dbAdapter.dump(db);
        db.close();

		// viewMode設定
		viewMode = CATEGORY_VIEW;

		// アダプタにカテゴリのリストを設定する
		gridAdapter = new GridAdapter(this, R.layout.list_item, new ArrayList<ListItem>());

        // ViewHolderを初期化
        ViewHolder holder = new ViewHolder(this);

		// homeLayoutを作成 R.id.home_layout
		homeLayout = new RelativeLayout(this);
		homeLayout.setId(HOME_LAYOUT);
        homeLayout.setTag(holder);
		
		setContentView(homeLayout);
        homeLayout.setTag(holder);
		
		// 画面更新用のハンドラを設定する
		final HomeActivity thisObj = this;
		mHandler = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				List<ListItem> listItems;
				ViewHolder holder = (ViewHolder) homeLayout.getTag();

				SQLiteDatabase db = dbAdapter.openDb();
				db.beginTransaction();
				try {
					switch (msg.what) {
						case DISPLAY_CHANGE:

							// 各VIEWを初期化＆配置する
							switch (viewMode) {
								case CATEGORY_VIEW:
									// カテゴリーのリストを取得
									List<Category> categories = Category.getAllCategory(db, thisObj);
									listItems = new ArrayList<ListItem>();
									for (Category category : categories) {
										File iconFile = category.getIconFile(db);
										ListItem item = new ListItem(category.getId(), category.getName(db), iconFile);
										listItems.add(item);
									}
									gridAdapter.clear();
									gridAdapter.addAll(listItems);

									holder.makeCategoryModeDisplay(homeLayout);
									break;
								case ARTICLE_VIEW:
									List<Article> articles = thisCategory.getArticles(db);
									listItems = new ArrayList<ListItem>();
									try {
										for (Article article : articles) {
											File iconFile = article.getIconFile(db);
											if(iconFile != null) {
												ListItem item = new ListItem(article.getId(), article.getName(db), iconFile);
												listItems.add(item);
											}
										}
										gridAdapter.clear();
										gridAdapter.addAll(listItems);

									}catch (Exception e){
										e.printStackTrace();
									}
									holder.makeArticleModeDisplay(homeLayout);
									holder.categoryTextView.setText(thisCategory.getName(db));

									break;
							}

							// ウィジェットを登録
							holder.gridView.setAdapter(gridAdapter);
							// 各アイテムをクリックした場合のリスナを登録
							holder.gridView.setOnItemClickListener(thisObj);

							break;
						case THEMA_CHANGE:
							TypedValue outValue = new TypedValue();
							getTheme().resolveAttribute(R.attr.mainBack, outValue, true);
							Bitmap backBitmap = BitmapFactory.decodeResource(getResources(), outValue.resourceId);
							BitmapDrawable backDrawable = new BitmapDrawable(getResources(), backBitmap);
							backDrawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
							homeLayout.setBackground(backDrawable);

							holder.settingButton.setBackground(ButtonFactory.getButtonDrawable(thisObj, R.drawable.setting_button_image));
							holder.newButton.setBackground(ButtonFactory.getButtonDrawable(thisObj, R.drawable.new_button_image));
							holder.returnButton.setBackground(ButtonFactory.getButtonDrawable(thisObj, R.drawable.return_button_image));

							int count = holder.gridView.getChildCount();
							for (int i = 0; i < count; i++) {
								View targetView = holder.gridView.getChildAt(i);
								holder.gridView.getAdapter().getView(i, targetView, holder.gridView);
							}
							break;
					}
					db.setTransactionSuccessful();
				}finally {
					db.endTransaction();
					db.close();
				}
            }
		};

		mHandler.sendEmptyMessage(DISPLAY_CHANGE);
		mHandler.sendEmptyMessage(THEMA_CHANGE);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem gorstItem = menu.add(Menu.NONE, 0,Menu.NONE, "おばけ");
		MenuItem heartItem = menu.add(Menu.NONE, 1,Menu.NONE, "夏");
		MenuItem starItem = menu.add(Menu.NONE, 2, Menu.NONE, "秋");
		MenuItem summerItem = menu.add(Menu.NONE, 3, Menu.NONE, "星");
		
		gorstItem.setOnMenuItemClickListener(this);
		
		heartItem.setOnMenuItemClickListener(this);
		
		starItem.setOnMenuItemClickListener(this);
		
		summerItem.setOnMenuItemClickListener(this);
		
		return true;
	}

	@Override
	public void onClick(View v) {
		Intent intent = null;
		switch(v.getId()){
		case NEW_BUTTON:
			intent = new Intent(this, RegisterActivity.class);
            intent.putExtra("mode", RegisterActivity.NEW_MODE);

            switch(viewMode){
			case CATEGORY_VIEW:
				break;
			case ARTICLE_VIEW:
				intent.putExtra("category_id", thisCategory.getId());
				break;
			}
			startActivityForResult(intent, REGISTER);
			
			break;
		case RETURN_BUTTON:
			switch(viewMode){
			case ARTICLE_VIEW:
				viewMode = CATEGORY_VIEW;
			}
			mHandler.sendEmptyMessage(DISPLAY_CHANGE);
			break;
		case SETTING_BUTTON:
			
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		SQLiteDatabase db = dbAdapter.openDb();
		dbAdapter.dump(db);

		switch(requestCode){
		case REGISTER:
			if(resultCode == RESULT_OK){
				db.beginTransaction();

				try {
					// TODO 更新されたアーティクルのアイコンを更新する
					long article_id = data.getLongExtra("article_id", -1);
					Article article = Article.getArticle(this, article_id);
					article.updateIcon(db);

					// TODO 更新されたアーティクルのカテゴリのアイコンを更新する
					Category[] categories = article.getCategoriesThis(db);
					String sql = "select article_id from category_article_table where category_id=?";
					for (Category category : categories) {
						String[] selectionArgs = {"" + category.getId()};
						Cursor cursor = db.rawQuery(sql, selectionArgs);
						int i = 0;
						for (boolean next = cursor.moveToFirst(); next && i < 6; next = cursor.moveToNext(), i++) {
							if (cursor.getLong(0) == article_id)
								category.updateIcon(db);
						}
					}
					db.setTransactionSuccessful();
				}finally {
					db.endTransaction();
				}
			}
			mHandler.sendEmptyMessage(DISPLAY_CHANGE);
			break;
	    }
        db.close();
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch(item.getItemId()){
		case 0:
			this.setTheme(R.style.GorstTheme);
			conf.setTheme("GorstTheme");
			break;
		case 1:
			this.setTheme(R.style.SummerTheme);
			conf.setTheme("SummerTheme");
			break;
		case 2:
			this.setTheme(R.style.AutumnTheme);
			conf.setTheme("AutumnTheme");
			break;
		case 3:
			this.setTheme(R.style.StarTheme);
			conf.setTheme("StarTheme");
			break;
		}
		File confFile = new File(getFilesDir(), "sawara.conf");
		Configuration.storeConfig(confFile, conf);
		mHandler.sendEmptyMessage(THEMA_CHANGE);
		return true;
	}

	/* 
	 * GridView上のアイテムをクリックしたときに呼び出されるメソッド
	 * (非 Javadoc)
	 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ViewHolder holder = (ViewHolder) homeLayout.getTag();
        SQLiteDatabase db = dbAdapter.openDb();
        ListItem item;
		switch(viewMode){
		case CATEGORY_VIEW:
			viewMode = ARTICLE_VIEW;
            item = gridAdapter.getItem(position);
            thisCategory = Category.getCategory(db, this, item.getId());

			mHandler.sendEmptyMessage(DISPLAY_CHANGE);
			break;
		case ARTICLE_VIEW:
			Intent intent = new Intent(this, RegisterActivity.class);
		    item = gridAdapter.getItem(position);
            Article article = Article.getArticle(this, item.getId());
			intent.putExtra("article_id", article.getId());
			intent.putExtra("mode", RegisterActivity.READ_MODE);

			startActivityForResult(intent, REGISTER);
			break;
		}

        db.close();
	}

    class ViewHolder{
        RelativeLayout homeLayout;
        GridView gridView;
        VTextView categoryTextView;
        Button settingButton;
        Button newButton;
        Button returnButton;

        ViewHolder(HomeActivity context){
            // gridViewを作成 R.id.grid_view
            gridView = new GridView(context);
            gridView.setId(GRID_VIEW);
            gridView.setNumColumns(gridViewColmn);
            //gridView.setOnClickListener(context);
            // categoryTextViewを作成 R.id.category_text_view
            categoryTextView = new VTextView(context);
            categoryTextView.setId(CATEGORY_TEXT_VIEW);
            // settingButtonを作成 R.id.setting_button
            settingButton = new Button(context);
            settingButton.setId(SETTING_BUTTON);
            settingButton.setBackground(ButtonFactory.getButtonDrawable(context, R.drawable.setting_button_image));
            settingButton.setOnClickListener(context);
            // newButtonを作成 R.id.new_button
            newButton = new Button(context);
            newButton.setId(NEW_BUTTON);
            newButton.setBackground(ButtonFactory.getButtonDrawable(context, R.drawable.new_button_image));
            newButton.setOnClickListener(context);
            // returnButtonを作成 R.id.return_button
            returnButton = new Button(context);
            returnButton.setId(RETURN_BUTTON);
            returnButton.setBackground(ButtonFactory.getButtonDrawable(context, R.drawable.return_button_image));
            returnButton.setOnClickListener(context);

            System.gc();
        }

        void makeCategoryModeDisplay(RelativeLayout layout){
            layout.removeAllViews();
            // LayoutParamsを用意
            RelativeLayout.LayoutParams params;

            // 設定ボタンのLayoutParamsを設定する
            params = new RelativeLayout.LayoutParams(buttonSize, buttonSize);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            settingButton.setLayoutParams(params);
            layout.addView(settingButton);

            // 新規作成ボタンのLayoutParamsを設定する
            params = new RelativeLayout.LayoutParams(buttonSize, buttonSize);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            newButton.setLayoutParams(params);
            layout.addView(newButton);

            // GridViewのLayoutParamsを設定する
            params = new RelativeLayout.LayoutParams(MP, MP);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            params.addRule(RelativeLayout.ABOVE, NEW_BUTTON);
            params.addRule(RelativeLayout.LEFT_OF, SETTING_BUTTON);
            gridView.setLayoutParams(params);
            layout.addView(gridView);

            System.gc();
        }

        void makeArticleModeDisplay(RelativeLayout layout){
            // layoutのViewをリセットする
            layout.removeAllViews();
            // LayoutParamsを用意
            RelativeLayout.LayoutParams params;

            // 設定ボタンのLayoutParamsを設定する
            params = new RelativeLayout.LayoutParams(buttonSize, buttonSize);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            settingButton.setLayoutParams(params);
            layout.addView(settingButton);

            // 戻るボタンのLayoutParamsを設定する
            params = new RelativeLayout.LayoutParams(buttonSize, buttonSize);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            returnButton.setLayoutParams(params);
            layout.addView(returnButton);

            // 新規作成ボタンのLayoutParamsを設定する
            params = new RelativeLayout.LayoutParams(buttonSize, buttonSize);
            params.addRule(RelativeLayout.RIGHT_OF, RETURN_BUTTON);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            newButton.setLayoutParams(params);
            layout.addView(newButton);

            // GridViewのLayoutParamsを設定する
            params = new RelativeLayout.LayoutParams(MP, MP);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            params.addRule(RelativeLayout.ABOVE, NEW_BUTTON);
            params.addRule(RelativeLayout.LEFT_OF, SETTING_BUTTON);
            gridView.setLayoutParams(params);
            layout.addView(gridView);

            // categoryTextViewのLayoutParamsを設定する
            params = new RelativeLayout.LayoutParams(MP, MP);
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.addRule(RelativeLayout.ALIGN_LEFT, SETTING_BUTTON);
            params.addRule(RelativeLayout.ABOVE, SETTING_BUTTON);
            categoryTextView.setLayoutParams(params);
            categoryTextView.setTextSize(100);
            layout.addView(categoryTextView);

            System.gc();
        }
    }
}
