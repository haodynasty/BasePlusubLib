package com.plusub.lib.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.RemoteViews;

import com.plusub.lib.util.DownloadUtils;
import com.plusub.lib.util.FileUtils;
import com.plusub.lib.util.LogUtils;
import com.plusub.lib.util.MD5Encryptor;
import com.plusub.lib.util.StringUtils;
import com.plusub.lib.util.ToastUtils;
import com.plusub.lib.view.R;

import java.io.File;

/**
 * app升级服务, 使用前必须在Manifest中进行注册<b>com.plusub.lib.service.AppUpgradeService
 * <ul>
 * <br>使用方法如下：
 * <pre class="prettyprint">
 * Intent intent = new Intent(getApplicationContext(), AppUpgradeService.class);
 * intent.putExtra(AppUpgradeService.EXTRA_DOWLOAD_URL, url);
 * startService(intent);
 *</pre>
 * </ul>
 * 使用后直接可以升级并安装，下载过程中会弹出下载进度栏进度，
 * 下载完成后需要手动停止服务
 * @author blakequ Blakequ@gmail.com
 *
 */
public class AppUpgradeService extends Service {

	public static final int APP_VERSION_LATEST = 0;
    public static final int APP_VERSION_OLDER = 1;
    
    public static final String EXTRA_DOWLOAD_URL = "downloadUrl";

    public static final int mNotificationId = 100;
    private String mDownloadUrl = null;
    private NotificationManager mNotificationManager = null;
    private Notification mNotification = null;
    private PendingIntent mPendingIntent = null;

    private File destDir = null;
    private File destFile = null;

    private static final int DOWNLOAD_FAIL = -1;
    private static final int DOWNLOAD_SUCCESS = 0;
    
    private Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case DOWNLOAD_SUCCESS:
//                ToastUtils.show(getApplicationContext(), R.string.app_upgrade_download_sucess);
                install(destFile);
                break;
            case DOWNLOAD_FAIL:
                ToastUtils.show(getApplicationContext(), R.string.app_upgrade_download_fail);
                mNotificationManager.cancel(mNotificationId);
                break;
            default:
                break;
            }
            stopSelf();
        }

    };
    
    
    private DownloadUtils.DownloadListener downloadListener = new DownloadUtils.DownloadListener() {
        @Override
        public void downloading(int progress) {
        	//放慢刷新进度
        	if (progress % 10 == 0) {
        		mNotification.contentView.setProgressBar(R.id.app_upgrade_progressbar, 100, progress, false);
        		mNotification.contentView.setTextViewText(R.id.app_upgrade_progresstext, progress + "%");
        		mNotificationManager.notify(mNotificationId, mNotification);
			}
        }

        @Override
        public void downloaded() {
            mNotification.contentView.setViewVisibility(R.id.app_upgrade_progressblock, View.GONE);
            mNotification.defaults = Notification.DEFAULT_SOUND;
            mNotification.contentIntent = mPendingIntent;
            mNotification.contentView.setTextViewText(R.id.app_upgrade_progresstext, getResources().getString(R.string.app_upgrade_download_over));
            mNotificationManager.notify(mNotificationId, mNotification);
            if (destFile.exists() && destFile.isFile() && checkApkFile(destFile.getPath())) {
                Message msg = mHandler.obtainMessage();
                msg.what = DOWNLOAD_SUCCESS;
                mHandler.sendMessage(msg);
            }
            mNotificationManager.cancel(mNotificationId);
        }
    };
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("NewApi")
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            mDownloadUrl = intent.getStringExtra(EXTRA_DOWLOAD_URL);
            if (!StringUtils.isEmpty(mDownloadUrl)){
                if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)){
                    destDir = new File(FileUtils.getSDCardPath().getAbsolutePath());
                    if (destDir.exists()) {
                        File destFile = new File(destDir.getPath() + "/" + MD5Encryptor.GetMD5Code(mDownloadUrl));
                        if (destFile.exists()) {
                            destFile.delete();
                        }
                    }
                }else {
                    LogUtils.e("AppUpgradeService", "AppUpgradeService sdcard not exist, can not download apk file");
                    stopSelf();
                    return super.onStartCommand(intent, flags, startId);
                }

                mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                mNotification = new Notification();

                mNotification.contentView = new RemoteViews(getApplication().getPackageName(), R.layout.app_upgrade_notification);

                Intent completingIntent = new Intent();
                completingIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                completingIntent.setClass(getApplication().getApplicationContext(), AppUpgradeService.class);

                mPendingIntent = PendingIntent.getActivity(AppUpgradeService.this, R.string.app_name, completingIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                mNotification.icon = R.drawable.ic_launcher;
                mNotification.tickerText = getResources().getString(R.string.app_upgrade_download_begin);
                mNotification.contentIntent = mPendingIntent;
                mNotification.contentView.setProgressBar(R.id.app_upgrade_progressbar, 100, 0, true);
                mNotification.contentView.setTextViewText(R.id.app_upgrade_progresstext, "0%");
                mNotificationManager.cancel(mNotificationId);
                mNotificationManager.notify(mNotificationId, mNotification);
                new AppUpgradeThread().start();
            }else{
                LogUtils.e("AppUpgradeService", "AppUpgradeService download url is null");
                stopSelf();
            }
        }else{
            LogUtils.e("AppUpgradeService", "AppUpgradeService intent is null");
            stopSelf();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    class AppUpgradeThread extends Thread{

        @Override
        public void run() {
            if (destDir == null) {
                destDir = new File(FileUtils.getSDCardPath().getAbsolutePath());
            }
            if (destDir.exists() || destDir.mkdirs()) {
                destFile = new File(destDir.getPath() + "/" + MD5Encryptor.GetMD5Code(mDownloadUrl));
                if (destFile.exists() && destFile.isFile() && checkApkFile(destFile.getPath())) {
                    install(destFile);
                    stopSelf();
                } else {
                    try {
                        DownloadUtils.download(mDownloadUrl, destFile, false, downloadListener);
                    } catch (Exception e) {
                        Message msg = mHandler.obtainMessage();
                        msg.what = DOWNLOAD_FAIL;
                        mHandler.sendMessage(msg);
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public boolean checkApkFile(String apkFilePath) {
        boolean result = false;
        try{
            PackageManager pManager = getPackageManager();
            PackageInfo pInfo = pManager.getPackageArchiveInfo(apkFilePath, PackageManager.GET_ACTIVITIES);
            if (pInfo == null) {
                result =  false;
            } else {
                result =  true;
            }
        } catch(Exception e) {
            result =  false;
            e.printStackTrace();
        }
        return result;
    }

    public void install(File apkFile){
        Uri uri = Uri.fromFile(apkFile);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        startActivity(intent);
    }

}
