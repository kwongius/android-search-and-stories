package com.duckduckgo.mobile.android.tasks;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.HttpStatus;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;
import ch.boye.httpclientandroidlib.impl.client.DefaultHttpClient;
import ch.boye.httpclientandroidlib.params.CoreProtocolPNames;
import ch.boye.httpclientandroidlib.util.EntityUtils;

import com.duckduckgo.mobile.android.DDGConstants;
import com.duckduckgo.mobile.android.objects.FeedObject;

import android.os.AsyncTask;
import android.util.Log;

public class MainFeedTask extends AsyncTask<Void, Void, List<FeedObject>> {

	private static String TAG = "MainFeedTask";
	
	private FeedListener listener = null;
		
	public MainFeedTask(FeedListener listener) {
		this.listener = listener;
	}
	
	@Override
	protected List<FeedObject> doInBackground(Void... arg0) {		
		JSONArray json = null;
		List<FeedObject> returnFeed = new ArrayList<FeedObject>();
		try {
			HttpClient client = new DefaultHttpClient();
			client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, DDGConstants.USER_AGENT);
			HttpGet get = new HttpGet(DDGConstants.MAIN_FEED_URL);

			if (isCancelled()) return null;
			
			HttpResponse result = client.execute(get);

			if (isCancelled()) return null;
			
			if (result.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				Log.e(TAG, "Unable to execute Query with result: " + result);
				return null;
			}
			String body = EntityUtils.toString(result.getEntity());
			Log.e(TAG, body);
			json = new JSONArray(body);
		} catch (JSONException jex) {
			Log.e(TAG, jex.getMessage(), jex);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}

		if (json != null) {
			if (isCancelled()) return returnFeed;
			for (int i = 0; i < json.length(); i++) {
				try {
					JSONObject nextObj = json.getJSONObject(i);
					if (nextObj != null) {
						FeedObject feed = new FeedObject(nextObj);
						if (feed != null) {
							returnFeed.add(feed);
						}
					}
				} catch (JSONException e) {
					Log.e(TAG, "Failed to create object with info at index " + i);
				}
			}
		}
		
		return returnFeed;
	}
	
	@Override
	protected void onPostExecute(List<FeedObject> feed) {	
		if (this.listener != null) {
			if (feed != null) {
				this.listener.onFeedRetrieved(feed);
			} else {
				this.listener.onFeedRetrievalFailed();
			}
		}
	}
	
	public static interface FeedListener {
		public void onFeedRetrieved(List<FeedObject> feed);
		public void onFeedRetrievalFailed();
	}
}
