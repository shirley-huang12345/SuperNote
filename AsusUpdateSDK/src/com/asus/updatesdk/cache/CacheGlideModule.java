package com.asus.updatesdk.cache;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.engine.cache.DiskCache;
import com.bumptech.glide.load.engine.cache.DiskLruCacheWrapper;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.module.GlideModule;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;

import java.io.File;

public class CacheGlideModule implements GlideModule {
    private static final String EXTERNAL_DISK_CACHE_PATH = "/Android/data/com.asus.asusupdatesdk"
            + "/cache/";
    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 50;

    @Override
    public void applyOptions(final Context context, GlideBuilder glideBuilder) {
        PackageManager pm = context.getPackageManager();
        int hasPerm = pm.checkPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                context.getPackageName());
        if (hasPerm == PackageManager.PERMISSION_GRANTED) {
            //EXTERNAL_STORAGE
            glideBuilder.setDiskCache(new DiskCache.Factory() {
                @Override
                public DiskCache build() {
                    File diskCacheDirectory = new File(Environment.getExternalStorageDirectory()
                            .getPath() + EXTERNAL_DISK_CACHE_PATH, "icons");
                    if (diskCacheDirectory.mkdirs()) {
                        return DiskLruCacheWrapper.get(diskCacheDirectory, DISK_CACHE_SIZE);
                    } else {
                        diskCacheDirectory = context.getCacheDir();
                        return DiskLruCacheWrapper.get(diskCacheDirectory, DISK_CACHE_SIZE);
                    }

                }
            });
        } else {
            //INTERNAL_STORAGE
            glideBuilder.setDiskCache(new InternalCacheDiskCacheFactory(context, DISK_CACHE_SIZE));
        }
    }

    @Override
    public void registerComponents(Context context, Glide glide) {
    }
}
