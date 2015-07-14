package jp.ac.bemax.sawara;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * 初期設定を管理するクラス
 * @author horikawa
 *
 */
public class Configuration implements Serializable{
	
	private String theme;		// テーマ
	
	public Configuration(){
		theme = "";
	}
	
	
	/**
	 * @return theme
	 */
	public String getTheme() {
		return theme;
	}

	
	/**
	 * @param theme セットする theme
	 */
	public void setTheme(String theme) {
		this.theme = theme;
	}
	
	
	/**
	 * ファイルから、Configurationオブジェクトを取得する
	 * @param confFile 保存先のファイル
	 * @return Configurationオブジェクト
	 */
	static Configuration loadConfig(File confFile){
		Configuration conf;
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(confFile));
			conf = (Configuration)ois.readObject();
		} catch (Exception e) {
			conf = null;
		}

		return conf;
	}
	
	/**
	 * ファイルに、Configurationオブジェクトを保存する
	 * @param confFile 保存先のファイル
	 * @param conf Configurationオブジェクト
	 * @return 保存に成功したらtrue
	 */
	static boolean storeConfig(File confFile, Configuration conf){
		boolean result;
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(confFile));
			oos.writeObject(conf);
			result = true;
		} catch (Exception e) {
			result = false;
			e.printStackTrace();
		}
		
		return result;
	}

}
