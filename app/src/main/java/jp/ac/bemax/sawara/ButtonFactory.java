package jp.ac.bemax.sawara;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.TypedValue;

/**
 * ボタンの画像を生成するクラス
 * @author horikawa
 * 2014/12/16
 */
public class ButtonFactory {
	// テーマから取得する値
	static int  baseColor;
	static int mainColor;
	static int accentColor;
	static int buttonSize = 200;
	
	/**
	 * ボタンのDrawableを返す
	 * @param context
	 * @return ボタンのDrawable
	 */
	public static Drawable getButtonDrawable(Context context, int resource){
		// contextから色コードを取得。static変数に設定する
		setThemaColors(context);
		
		// ボタンイラストを読み込む
		Drawable image = context.getResources().getDrawable(resource);
		
		// 背景Drawableと画像を合体
		Drawable[] layers = {createBackFrame(context), image};
		LayerDrawable layerDrawable = new LayerDrawable(layers);
		layerDrawable.setLayerInset(1, 10, 0, 0, 10);
		
		return layerDrawable;
	}
	
	/**
	 * ボタンの枠画像を作成する
	 * @return 枠画像のDrawable
	 */
	public static Drawable createBackFrame(Context context){

		Paint main = new Paint();
		main.setColor(mainColor);
		Paint base = new Paint();
		base.setColor(baseColor);
		Paint accent = new Paint();
		accent.setColor(accentColor);
		RectF rectOut = new RectF(0,buttonSize/3f, buttonSize*4/5f, buttonSize);
		RectF rectIn = new RectF(5, buttonSize/3f+5, (buttonSize*4/5f)-5, buttonSize-5);
		
		Bitmap bitmap = Bitmap.createBitmap(buttonSize, buttonSize, Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawRoundRect(rectOut, 15, 15, main);
		canvas.drawRoundRect(rectIn, 15, 15, base);
		BitmapDrawable mb = new BitmapDrawable(context.getResources(), bitmap);
		
		bitmap = Bitmap.createBitmap(buttonSize, buttonSize, Config.ARGB_8888);
		canvas = new Canvas(bitmap);
		canvas.drawRoundRect(rectOut, 15, 15, main);
		canvas.drawRoundRect(rectIn, 15, 15, accent);
		BitmapDrawable ma = new BitmapDrawable(context.getResources(), bitmap);		
		
		// 状態Drawable作成
		StateListDrawable stateListDrawable = new StateListDrawable();
		stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, ma);
		stateListDrawable.addState(new int[0], mb);

		return stateListDrawable;
		
	}
	
	/**
	 * @param context
	 * @return
	 */
	public static Drawable getThemaBackground(Context context){
		// コンテキストからテーマを取得
		Resources.Theme thema = context.getTheme();
		
		TypedValue backgroundDrawableValue = new TypedValue();
		thema.resolveAttribute(R.attr.mainBack, backgroundDrawableValue, true);
		Drawable backgroundDrawable = context.getResources().getDrawable(backgroundDrawableValue.resourceId);
		
		return backgroundDrawable;
	}
	
	public static Drawable getThemaFrame(Context context){
		// コンテキストからテーマを取得
		Resources.Theme thema = context.getTheme();
		
		TypedValue frameDrawableValue = new TypedValue();
		thema.resolveAttribute(R.attr.frameBack, frameDrawableValue, true);
		Drawable frameDrawable = context.getResources().getDrawable(frameDrawableValue.resourceId);
		return frameDrawable;
	}
	
	/**
	 * contextのテーマに沿って、３つの色変数に値を設定する
	 * @param context
	 */
	public static void setThemaColors(Context context){
		// コンテキストからテーマを取得
		Resources.Theme thema = context.getTheme();
		
		// テーマデータの受け皿
		TypedValue baseColorValue = new TypedValue();
		TypedValue mainColorValue = new TypedValue();
		TypedValue accentColorValue = new TypedValue();
		
		// テーマのデータを変数にセットする
		thema.resolveAttribute(R.attr.baseColor, baseColorValue, true);
		thema.resolveAttribute(R.attr.mainColor, mainColorValue, true);
		thema.resolveAttribute(R.attr.accentColor, accentColorValue, true);

		// テーマの値から、色コードを取得
		baseColor = context.getResources().getColor(baseColorValue.resourceId);
		mainColor = context.getResources().getColor(mainColorValue.resourceId);
		accentColor = context.getResources().getColor(accentColorValue.resourceId);

	}

	public static void setButtonFrameSize(int size){
		buttonSize = size;
	}
}
