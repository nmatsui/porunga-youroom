package jp.co.tokaneoka.youroomclient;

import java.util.HashMap;
import java.util.Map;

import android.app.Application;
import android.util.Log;

public class YouRoomProxy {
	private YouRoomCommand youRoomCommand = null;
	private UserSession session = null;
	private static Map<String, String> entryCache = new HashMap<String, String>();
	
	public YouRoomProxy(Application application) {
		youRoomCommand = new YouRoomCommand(new YouRoomUtil(application).getOauthTokenFromLocal());
		session = UserSession.getInstance();
	}
	
	public String getEntry(String roomId, String entryId, String updatedTime) {
    	String roomAccessTime = session.getRoomAccessTime(roomId);
    	String key = String.format("%s\t%s", roomId, entryId);
		int compareResult = YouRoomUtil.calendarCompareTo(roomAccessTime, updatedTime);
		if ( compareResult < 0 ){
			String result = youRoomCommand.getEntry(roomId, entryId);
			synchronized(entryCache) {
				if (entryCache.containsKey(key)) {
					entryCache.remove(key);
				}
				entryCache.put(key, result);
			}
			Log.i("CACHE", String.format("Not Cached [%s]", entryId));
			return result;
		}
		else {
			synchronized(entryCache) {
				if (entryCache.containsKey(key)) {
					Log.i("CACHE", String.format("Cached [%s]", entryId));
					return entryCache.get(key);
				}
				else {
					String result = youRoomCommand.getEntry(roomId, entryId);
					entryCache.put(key, result);
					Log.i("CACHE", String.format("Not Cached2 [%s]", entryId));
					return result;
				}
			}
		}
	}
}
