package com.plusub.lib.util;

import android.app.Activity;
import android.content.Context;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.plusub.lib.BaseApplication;
import com.plusub.lib.net.Caller;

import java.io.File;

/**
 * CacheManager
 * 
 * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2014-2-11
 */
public class CacheManager {

    private static CacheUtils httpCache = null;
    private static ImageLoader imageCache = null;

    private CacheManager() {

    }

    /**
     * get the singleton instance of HttpCache
     * 
     * @param context {@link Activity#getApplicationContext()}
     * @return
     */
    public static CacheUtils getHttpCache(Context context) {
    	return Caller.getRequestCache(context);
    }

    /**
     * get the singleton instance of ImageCache
     * 
     * @return
     * @see #getImageCache(Context)
     */
    public static ImageLoader getImageCache(Context context) {
    	if (imageCache == null) {
            synchronized (CacheManager.class) {
                if (imageCache == null) {
                	imageCache = ImageLoader.getInstance();
                	initImageLoader(imageCache, context);
                }
            }
        }
        return imageCache;
    }
    
    /**
	 * This configuration tuning is custom. You can tune every option, you may tune some of them, 
	 * or you can create default configuration by ImageLoaderConfiguration.createDefault(this); method.
	 * //http://www.cnblogs.com/qinghuaideren/archive/2013/05/06/3061986.html
	 * <p>Title: initImageLoader
	 * <p>Description: 
	 * @param context
	 */
	private static void initImageLoader(ImageLoader cache, Context context) {
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
				.threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()//缓存多个不同尺寸图片
				.diskCacheFileNameGenerator(new Md5FileNameGenerator())
				.diskCache(new UnlimitedDiscCache(new File(BaseApplication.mCachePath)))
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				.writeDebugLogs() // Remove for release app
				.build();
		// Initialize ImageLoader with configuration.
		cache.init(config);
	}
}
