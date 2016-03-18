package hope.plugins.qqlocation;


import java.util.HashMap;
import java.util.Map;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;

public class QQLocation extends CordovaPlugin {

	private static final String STOP_ACTION = "stop";
	private static final String GET_ACTION = "getCurrentPosition";
	//public LocationClient locationClient = null;
	public JSONObject jsonObj = new JSONObject();
	public boolean result = false;
	public CallbackContext callbackContext;

	public TencentLocationListener myListener;
	private TencentLocationManager mLocationManager;

	private static final int[] LEVELS = new int[] {
		TencentLocationRequest.REQUEST_LEVEL_GEO,
		TencentLocationRequest.REQUEST_LEVEL_NAME,
		TencentLocationRequest.REQUEST_LEVEL_ADMIN_AREA,
		TencentLocationRequest.REQUEST_LEVEL_POI};
	private static final int DEFAULT = 2;

	private int mLevel = LEVELS[DEFAULT];
	
	
	private static final Map<Integer, String> ERROR_MESSAGE_MAP = new HashMap<Integer, String>();
	private static final String DEFAULT_ERROR_MESSAGE = "服务端定位失败";

	static {
		ERROR_MESSAGE_MAP.put(0, "GPS定位结果");
		ERROR_MESSAGE_MAP.put(1, "网络问题引起的定位失败");
		ERROR_MESSAGE_MAP.put(2, "GPS, Wi-Fi 或基站错误引起的定位失败");
		ERROR_MESSAGE_MAP.put(4, "无法将WGS84坐标转换成GCJ-02坐标时的定位失败");
		ERROR_MESSAGE_MAP.put(404, "未知原因引起的定位失败");
	};

	public String getErrorMessage(int error) {
		String result = ERROR_MESSAGE_MAP.get(error);
		if (result == null) {
			result = DEFAULT_ERROR_MESSAGE;
		}
		return result;
	}

	@Override
	public boolean execute(String action, JSONArray args,
			final CallbackContext callbackContext) {
		setCallbackContext(callbackContext);
		if (GET_ACTION.equals(action)) {
			
			cordova.getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mLocationManager = TencentLocationManager.getInstance(cordova.getActivity());
					TencentLocationRequest request = TencentLocationRequest.create()
							.setInterval(5000) // 设置定位周期
							.setRequestLevel(mLevel); // 设置定位level
					myListener = new MyLocationListener();
					// 开始定位
					mLocationManager.requestLocationUpdates(request, myListener);
				}

			});
			return true;
		} else if (STOP_ACTION.equals(action)) {
			if(myListener != null) {
				mLocationManager.removeUpdates(myListener);
			}
			callbackContext.success(200);
			return true;
		} else {
			callbackContext
					.error(PluginResult.Status.INVALID_ACTION.toString());
		}

		while (result == false) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public class MyLocationListener implements TencentLocationListener {
		@Override
		public void onLocationChanged(TencentLocation location, int error,
				String reason) {
			try {
				jsonObj.put("locationType", error);
				jsonObj.put("code", error);
				jsonObj.put("message", getErrorMessage(error));
				if (error == TencentLocation.ERROR_OK) {
					JSONObject coords = new JSONObject();
					coords.put("latitude", location.getLatitude());
					coords.put("longitude", location.getLongitude());
					//coords.put("radius", location.getRadius());

					jsonObj.put("coords", coords);
					coords.put("altitude", location.getAltitude());
					coords.put("accuracy", location.getAccuracy());
					
					switch (mLevel) {
					case TencentLocationRequest.REQUEST_LEVEL_GEO:
						
						break;

					case TencentLocationRequest.REQUEST_LEVEL_NAME:
						jsonObj.put("name", location.getName());
						jsonObj.put("address", location.getAddress());
						break;

					case TencentLocationRequest.REQUEST_LEVEL_ADMIN_AREA:
					case TencentLocationRequest.REQUEST_LEVEL_POI:
					case 7:
						jsonObj.put("nation",location.getNation());
						jsonObj.put("province",location.getProvince());
						jsonObj.put("city",location.getCity());
						jsonObj.put("district",location.getDistrict());
						jsonObj.put("town",location.getTown());
						jsonObj.put("village",location.getVillage());
						jsonObj.put("street",location.getStreet());
						jsonObj.put("streetNo",location.getStreetNo());
						break;

					default:
						break;
					}

					Log.d("QQLocationPlugin", "run: " + jsonObj.toString());
					callbackContext.success(jsonObj);
					result = true;
				} else {
					callbackContext.error(reason);
					result = true;
				}
			} catch (JSONException e) {
				callbackContext.error(e.getMessage());
				result = true;
			}
		}

		@Override
		public void onStatusUpdate(String name, int status, String desc) {
			// ignore
		}
		
	}

	@Override
	public void onDestroy() {
		if(myListener != null) {
			mLocationManager.removeUpdates(myListener);
		}
		super.onDestroy();
	}

	private void logMsg(String s) {
		System.out.println(s);
	}

	public CallbackContext getCallbackContext() {
		return callbackContext;
	}

	public void setCallbackContext(CallbackContext callbackContext) {
		this.callbackContext = callbackContext;
	}
}