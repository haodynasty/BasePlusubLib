/*
 * FileName: BaseApplication.java
 * Copyright (C) 2014 Plusub Tech. Co. Ltd. All Rights Reserved <admin@plusub.com>
 * 
 * Licensed under the Plusub License, Version 1.0 (the "License");
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * author  : Administrator
 * date     : 2014-6-1 下午10:18:15
 * last modify author :
 * version : 1.0
 */
package com.plusub.lib;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Environment;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.plusub.lib.annotate.JsonParserUtils;
import com.plusub.lib.constant.PlusubConfig;
import com.plusub.lib.exp.UEHandler;
import com.plusub.lib.service.BaseService;
import com.plusub.lib.service.NetStateReceiver;
import com.plusub.lib.task.DataRefreshTask;
import com.plusub.lib.task.UserTask;
import com.plusub.lib.util.CacheManager;
import com.plusub.lib.util.CacheUtils;
import com.plusub.lib.util.FileUtils;
import com.plusub.lib.util.JSONUtils;
import com.plusub.lib.util.LogUtils;
import com.plusub.lib.util.StringUtils;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * 系统application
 * <li>注意：需要在manifest的string.xml中定义一个字段app_id,用于建立本地文件存储路径</li>
 * @author service@plusub.com
 *
 */
public abstract class BaseApplication extends Application {

	private String sessionId;
	public static BaseApplication instance;
	/**版本码*/
	public static int mVersionCode;
	/**版本号*/
    public static String mVersionName;
    /**缓存路径*/
    public static String mCachePath;
    /**错误日志路径*/
    public static String errorLogPath ;
    /**未捕获异常处理*/
    private UEHandler ueHandler;
    /**是否锁定屏幕为竖屏*/
    private boolean isLockScreen = true;
    /**网络注册监听*/
    private NetStateReceiver receiver = null;
    /**用户异步任务列表*/
	public static List<UserTask> taskList = new LinkedList<UserTask>(); 
	/**网络请求任务列表*/
	public static List<DataRefreshTask> refreshList = new LinkedList<DataRefreshTask>();

    public static BaseApplication getInstance(){
    	return instance;
    }
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
        initLocalVersion();
        initEnv();
        getHttpCache();
    	// 启动网络状态监听
		receiver = new NetStateReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		registerReceiver(receiver, filter);
		//实例
        instance = this;
        ueHandler = new UEHandler(errorLogPath, this);
        // 设置异常处理实例
       Thread.setDefaultUncaughtExceptionHandler(ueHandler);
       //设置Debug模式下打印日志
       LogUtils.setDebugLog(PlusubConfig.isDebugSwitch);
       //关闭异常日志输出到本地
       LogUtils.setSyns(PlusubConfig.isLogSaveToFileSwitch);
       //打开解析异常打印开关
       JSONUtils.setPrintSwitch(PlusubConfig.isPrintParserErrorSwitch);
       JsonParserUtils.setPrintSwitch(PlusubConfig.isPrintJsonErrorSwitch);
	}
	
	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub
		super.onTerminate();
		exit();
		exitApp(this);
	}
	

	/**
	 * 处理系统未捕获异常
	 * <p>Title: doUncatchException
	 * <p>Description: 
	 * @param savePath 保存异常日志的路径
	 * @param threadId 抛出异常的线程id，主线程为id为1
	 * @param content 异常内容
	 */
	public abstract void doUncatchException(String savePath, long threadId, String content);
//	{
		//将异常上传服务器
//		uploadErrorToServer(savePath);
		//主线程异常，退出应用并重启
//		if (threadId == 1) { 
			// 此处示例发生异常后，重新启动应用
			//Intent intent = new Intent(this, LoadingActivity.class);
			//intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			//startActivity(intent);
					
//			MainService.exitApp(this);
//			android.os.Process.killProcess(android.os.Process.myPid());
//		}else{
//			
//		}
//	}
	

	@Override
	public void onLowMemory() {
		// TODO Auto-generated method stub
		super.onLowMemory();
		CacheManager.getImageCache(getApplicationContext()).clearMemoryCache();
	}
	
	/**
	 * 图片缓存
	 * <p>Title: getImageCache
	 * <p>Description: 
	 * @return
	 */
	public ImageLoader getImageCache(){
		return CacheManager.getImageCache(getApplicationContext());
	}
	
	/**
	 * url请求缓存
	 * <p>Title: getHttpCache
	 * <p>Description: 
	 * @return
	 */
	public CacheUtils getHttpCache(){
		return CacheManager.getHttpCache(getApplicationContext());
	}
	
	public String getSessionId() {
		return sessionId;
	}
	
	public boolean isLockScreen() {
		return isLockScreen;
	}

	/**
	 * 是否锁定屏幕为竖屏(默认为true)
	 * <p>Title: setLockScreen
	 * <p>Description: 
	 * @param isLockScreen
	 */
	public void setLockScreen(boolean isLockScreen) {
		this.isLockScreen = isLockScreen;
	}

	/**
	 * 设置全局session id
	 * <p>Title: setSessionId
	 * <p>Description: 
	 * @param sessionId
	 */
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	
	/**
	 * 
	 * Title: exit
	 * Description:should invoke this method before exit app
	 * <br>{@link #exitApp(Context)} 会自动调用该方法
	 */
	private void exit()
	{
		CacheManager.getImageCache(getApplicationContext()).clearMemoryCache();
		CacheManager.getHttpCache(getApplicationContext()).clear();
		
		// 关闭广播接口
		if (receiver != null) {
			unregisterReceiver(receiver);
			receiver = null;
		}
	}
	
	/**
	 * 在应用退出时，清除一些缓存，关闭数据库，清除后台等基本操作
	 * <br><b>注意：在应用退出时应该主动调用{@link #exitApp(Context)}方法，它会自动回调该方法}</b>
	 * <p>Title: clearApp
	 * <p>Description:
	 */
	public abstract void clearApp();
	

	/**
	 * 初始化版本
	 * <p>Title: initLocalVersion
	 * <p>Description:
	 */
	private void initLocalVersion(){
        PackageInfo pinfo;
        try {
            pinfo = this.getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_CONFIGURATIONS);
            mVersionCode = pinfo.versionCode;
            mVersionName = pinfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	/**
	 * 初始化基本的变量（路径，appid）
	 * <p>Title: initEnv
	 * <p>Description:
	 */
    private void initEnv() {
        String mAppId = this.getPackageName();
        if (StringUtils.isEmpty(mAppId)) {
			mAppId = "log";
		}
        String sdcard = Environment.getExternalStorageDirectory().getAbsolutePath();
        mCachePath = sdcard+"/"+mAppId+"/cache/image";
        errorLogPath = sdcard+"/"+mAppId+"/log/error.log";
        FileUtils.createExternalStoragePublicPicture();
		if(FileUtils.isSDCardAvailable()){
			FileUtils.createDir(new File(mCachePath));
			FileUtils.createNewFile(new File(errorLogPath));
		}
    }
    

	/**
	 * 退出应用程序，结束任务，清除堆栈
	 * <p>
	 * Title: exitApp
	 * <p>
	 * Description:
	 * 
	 * @param context
	 */
	@SuppressLint("NewApi")
	public static void exitApp(Context context) {
		LogUtils.d("BaseApplication exit");
		
		stopTask();
		// 退出所有Activity或Service
		clearContextStack();
		// 清除应用信息
		BaseApplication.getInstance().exit();
		BaseApplication.getInstance().clearApp();
		
		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(ACTIVITY_SERVICE);
		activityManager.killBackgroundProcesses(context.getPackageName());
		System.exit(0);
	}

	/**
	 * when the application exit, we should stop all task which is running in
	 * the task list
	 */
	@SuppressLint("NewApi")
	@SuppressWarnings("unchecked")
	private static void stopTask() {
		//停止所有任务
		if (taskList.size() == 0 || taskList == null) {
			return;
		}
		for (UserTask userTask : taskList) {
			if (userTask.getStatus() != AsyncTask.Status.FINISHED) {
				userTask.cancel(true);
			}
		}
		taskList.clear();
	}
	
	/**
	 * 清除Activity堆栈
	 * <p>Title: clearActivityStack
	 * <p>Description:
	 */
	public static void clearContextStack(){
		for (int i = 0; i < refreshList.size(); i++) {
			if (refreshList.get(i) instanceof Activity) {
				((Activity) refreshList.get(i)).finish();
			}else if(refreshList.get(i) instanceof BaseService){
				BaseService service = ((BaseService)refreshList.get(i));
				service.exitApp();
				if (service.isAutoFinish()) {
					service.stopSelf();
				}
			}
		}
		refreshList.clear();
	}
}
