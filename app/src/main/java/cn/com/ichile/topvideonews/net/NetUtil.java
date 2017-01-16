package cn.com.ichile.topvideonews.net;

import android.content.res.AssetManager;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.com.ichile.topvideonews.App;
import cn.com.ichile.topvideonews.Cons;
import cn.com.ichile.topvideonews.callback.OnNetDataCallback;
import cn.com.ichile.topvideonews.domain.VideoBean;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * FBI WARNING ! MAGIC ! DO NOT TOUGH !
 * Created by WangZQ on 2016/12/30 - 15:48.
 */

public class NetUtil {
    private static OkHttpClient mOkHttpClient;
    private static String mType;
    private static OnNetDataCallback mOnNetDataCallback;

    private static android.os.Handler mHandler = new android.os.Handler(Looper.getMainLooper());

    private static OkHttpClient initHttpClinet() {
//        if (mOkHttpClient == null) {
//            synchronized (NetUtil.class) {
//                if (mOkHttpClient == null) {
//                    mOkHttpClient = new OkHttpClient();
//                }
//            }
//        }

        if (mOkHttpClient == null) {
            synchronized (NetUtil.class) {
                if (mOkHttpClient == null) {
                    OkHttpClient.Builder builder = new OkHttpClient.Builder();
                    builder.connectionPool(new ConnectionPool(10, 60000, TimeUnit.SECONDS));
                    mOkHttpClient = builder.build();
                }
            }
        }

        return mOkHttpClient;
    }

    public static void getVideoList(String type, OnNetDataCallback onNetDataCallback) {

        mOnNetDataCallback = onNetDataCallback;
        getNetVideoList(type, onNetDataCallback);
       // getNativeVideoList(type, onNetDataCallback);
    }

    public static void getNetVideoList(String type, OnNetDataCallback onNetDataCallback) {
        mType = type;
        Log.i("NetUtil", mType);
        mOnNetDataCallback = onNetDataCallback;

        try {
            if (mType == null) mType = Cons.HOT;
            OkHttpClient okHttpClient = initHttpClinet();
            Request request = new Request.Builder().url(Cons.BASE + mType).build();
            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    mOnNetDataCallback.onError(e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    String string = response.body().string();

                    final List<VideoBean> list = new ArrayList<>();
                    JSONArray jsonArray = null;
                    try {
                        jsonArray = new JSONArray(string);

                        JSONObject object = (JSONObject) jsonArray.get(0);
                        JSONArray videoArray = (JSONArray) object.get("videos");
                        int length = videoArray.length();
                        for (int i = 0; i < length; i++) {
                            JSONObject video = (JSONObject) videoArray.get(i);
                            VideoBean videoBean = new VideoBean();
                            videoBean.setImage(video.getString("image"));
                            videoBean.setThumb(video.getString("thumbnail"));
                            videoBean.setTitle(video.getString("title"));
                            videoBean.setUrl(video.getString("video_url"));
                            videoBean.setAuthor(video.getString("contentFrom"));
                            videoBean.setToken(SystemClock.currentThreadTimeMillis() + "");
                            list.add(videoBean);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    // OriginalDao dao = new OriginalDao(context, null);
                    //long result = dao.addAll(list);
                    if (mOnNetDataCallback != null) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mOnNetDataCallback.onSuccess(list);
                            }
                        });

                    }

                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            mOnNetDataCallback.onError(e.getMessage());
        }
    }


    public static void getNativeVideoList(String type, OnNetDataCallback onNetDataCallback) {

        if (type == Cons.TV) {
            StringBuilder assertFile = getAssertFile("detail12_female11_14__2.txt");
            formatJsonFile(assertFile);
        } else if (type == Cons.HOT) {
            StringBuilder assertFile = getAssertFile("detail6_female11_14__2.txt");
            formatJsonFile(assertFile);
        }

    }

    private static void formatJsonFile(StringBuilder assertFile) {
        final List<VideoBean> list = new ArrayList<>();
        JSONArray videoArray = null;
        try {
            videoArray = new JSONArray(assertFile.toString());
            Log.i("ssss", videoArray.length() + "");
            int length = videoArray.length();
            for (int i = 0; i < length; i++) {
                JSONObject video = (JSONObject) videoArray.get(i);
                VideoBean videoBean = new VideoBean();
                videoBean.setImage(video.getString("thumb_url"));
//                    videoBean.setThumb(video.getString("thumbnail"));
//                    videoBean.setTitle(video.getString("title"));
                videoBean.setUrl(video.getString("file_url"));
//                    videoBean.setAuthor(video.getString("contentFrom"));
//                    videoBean.setToken(SystemClock.currentThreadTimeMillis() + "");
                Log.i("ssss", videoBean.toString() + "");
                list.add(videoBean);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // OriginalDao dao = new OriginalDao(context, null);
        //long result = dao.addAll(list);
        if (mOnNetDataCallback != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mOnNetDataCallback.onSuccess(list);
                }
            });

        }
    }

    public static StringBuilder getAssertFile(String dir) {
        AssetManager assets = App.getAppContext().getAssets();
        StringBuilder sb = null;
        BufferedReader bufferedReader = null;
        try {
            InputStream is = assets.open(dir);
            bufferedReader = new BufferedReader(new InputStreamReader(is));
            sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return sb;
    }
}