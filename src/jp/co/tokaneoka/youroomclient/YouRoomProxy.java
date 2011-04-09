package jp.co.tokaneoka.youroomclient;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class YouRoomProxy {
	private static DBHelper helper = null;
	private YouRoomCommand youRoomCommand = null;
	//private UserSession session = null;
	//private static Map<String, String> entryCache = new HashMap<String, String>();
	
	public YouRoomProxy(Activity activity) {
		helper = new DBHelper(activity);
		youRoomCommand = new YouRoomCommand(new YouRoomUtil(activity.getApplication()).getOauthTokenFromLocal());
		//session = UserSession.getInstance();
	}
	
	public String getEntry(String roomId, String entryId, String updatedTime) {
		SQLiteDatabase db = helper.getWritableDatabase();
		String result = "";
		
		Cursor c = db.rawQuery("select result from entries where entryId = ? and roomId = ? and updatedTime = ? ;", new String[]{entryId, roomId, updatedTime});
		
		if (c.getCount() == 1) {
			Log.i("CACHE", String.format("Cache Hit  [%s]", entryId));
			c.moveToFirst();
			result =  c.getString(0);
		}
		else {
			Log.i("CACHE", String.format("Cache Miss [%s]", entryId));
			String res = youRoomCommand.getEntry(roomId, entryId);
			db.beginTransaction();
			try {
				db.execSQL("delete from entries where entryId = ? and roomId = ? ;", new String[]{entryId, roomId});
				db.execSQL("insert into entries(entryId, roomId, updatedTime, result) values(?, ?, ?, ?) ;",new String[]{entryId, roomId, updatedTime, res});
				result =  res;
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
			}
		}
		c.close();
		db.close();
		
		return result;
		
		
//		
//    	String roomAccessTime = session.getRoomAccessTime(roomId);
//    	String key = String.format("%s\t%s", roomId, entryId);
//		int compareResult = YouRoomUtil.calendarCompareTo(roomAccessTime, updatedTime);
//		if ( compareResult < 0 ){
//			String result = youRoomCommand.getEntry(roomId, entryId);
//			synchronized(entryCache) {
//				if (entryCache.containsKey(key)) {
//					entryCache.remove(key);
//				}
//				entryCache.put(key, result);
//			}
//			Log.i("CACHE", String.format("Not Cached [%s]", entryId));
//			return result;
//		}
//		else {
//			synchronized(entryCache) {
//				if (entryCache.containsKey(key)) {
//					Log.i("CACHE", String.format("Cached [%s]", entryId));
//					return entryCache.get(key);
//				}
//				else {
//					String result = youRoomCommand.getEntry(roomId, entryId);
//					entryCache.put(key, result);
//					Log.i("CACHE", String.format("Not Cached2 [%s]", entryId));
//					return result;
//				}
//			}
//		}
	}
	private class DBHelper extends SQLiteOpenHelper {

		public DBHelper(Context context) {
			super(context, "porungadb", null, 1);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.beginTransaction();
			try {
				db.execSQL("create table entries (entryId text primary key,roomId text not null, updatedTime text not null, result text not null);");
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}
	}
}
