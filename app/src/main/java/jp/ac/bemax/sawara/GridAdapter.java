package jp.ac.bemax.sawara;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources.Theme;
import android.database.sqlite.SQLiteAbortException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * ホーム画面のグリッドビューに並べる項目を扱うクラス
 * @author Masaaki Horikawa
 * 2014/09/02
 */
public class GridAdapter extends ArrayAdapter<ListItem> implements OnItemClickListener{
	private Context context;		// コンテキスト(HomeActivity)
	private int resourceId;			// グリッドに表示するアイテムのレイアウトXML
	private List<ListItem> list;	// グリッドに表示するアイテムのリスト
	private Point dispSize; 		// 画面のサイズ

	static int frameSize;
	static float density;

	public GridAdapter(Context context, int resource, List<ListItem> objects) {
		super(context, resource, objects);
		this.context = context;
		this.resourceId = resource;
		this.list = objects;
		
		// 画面のサイズを取得して、dispSizeにセットする
		WindowManager wManager = ((Activity)context).getWindowManager();
		dispSize = new Point();
		wManager.getDefaultDisplay().getSize(dispSize);
		DisplayMetrics outMetrics = new DisplayMetrics();
		wManager.getDefaultDisplay().getMetrics(outMetrics);
		density = outMetrics.density;
		int buttonSize = (int)(dispSize.y / 5);
		frameSize = buttonSize * 2;
	}

	/* (非 Javadoc)
	 * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		
		if(convertView == null){
			// アイテム用のレイアウトXMLを読み込む
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(resourceId, null);
            
            holder = new ViewHolder();
            holder.imageView = (ImageView)convertView.findViewById(R.id.list_item_image);
            holder.vTextView = (VTextView)convertView.findViewById(R.id.list_vTextView);
            
    		AbsListView.LayoutParams absParams = new AbsListView.LayoutParams(frameSize, frameSize);
    		convertView.setLayoutParams(absParams);
    		
    		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
    		params.setMargins(5, 10, 5, 10);
    		holder.imageView.setLayoutParams(params);
    		
    		params = new LinearLayout.LayoutParams((int)(frameSize / 5), LinearLayout.LayoutParams.MATCH_PARENT);
    		params.setMargins(5, 5, 0, 5);
    		holder.vTextView.setLayoutParams(params);
    		holder.vTextView.setPadding(10, 10, 10, 10);
    		
            convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		// 表示するアイテムを取り出す
		ListItem listItem = getItem(position);

        // imageViewにitemの画像をセットする
        Bitmap bmp = listItem.getIcon();
        holder.imageView.setImageBitmap(bmp);
        holder.imageView.setBackground(ButtonFactory.getThemaFrame(context));

        // 縦書きのtextViewにアイテムの値をセットする
        holder.vTextView.setText(listItem.getName());
        holder.vTextView.setBackground(getVTextBack());

        return convertView;
	}

	private Drawable getVTextBack(){
		TypedValue mainColorValue = new TypedValue();
		Theme theme = context.getTheme();
		theme.resolveAttribute(R.attr.mainColor, mainColorValue, true);
		
		GradientDrawable drawable = new GradientDrawable();
		drawable.setStroke(5, context.getResources().getColor(mainColorValue.resourceId));
		drawable.setColor(Color.WHITE);
		drawable.setCornerRadius(10);
		
		return drawable;
	}

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }


    class ViewHolder{
		ImageView imageView;
		VTextView vTextView;
	}
}

