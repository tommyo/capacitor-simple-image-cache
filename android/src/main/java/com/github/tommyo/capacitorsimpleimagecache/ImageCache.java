package com.github.tommyo.capacitorsimpleimagecache;

import android.net.Uri;

import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.common.CacheKey;
import com.facebook.cache.disk.FileCache;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.datasource.DataSources;
import com.facebook.drawee.backends.pipeline.Fresco;

import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import com.getcapacitor.FileUtils;
import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;

import java.io.File;

@NativePlugin
public class ImageCache extends Plugin {

    @Override
    public void load() {
        super.load();
        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(getContext())
                .setDownsampleEnabled(true)
                .build();
        Fresco.initialize(getContext(), config);
    }

    private String localUrl(final CacheKey cacheKey) {
        File file = null;
        ImagePipelineFactory imagePipelineFactory = ImagePipelineFactory.getInstance();
        FileCache fileCache = imagePipelineFactory.getMainFileCache();
        if (fileCache.hasKey(cacheKey)) {
            file = ((FileBinaryResource) fileCache.getResource(cacheKey)).getFile();
        }
        fileCache = imagePipelineFactory.getSmallImageFileCache();
        if (fileCache.hasKey(cacheKey)) {
            file = ((FileBinaryResource) fileCache.getResource(cacheKey)).getFile();
        }
        return FileUtils.getPortablePath(getContext(), bridge.getLocalUrl(), Uri.fromFile(file));
    }

    @PluginMethod()
    public void get(final PluginCall call) {
        String src = call.getString("src", "");
        JSObject obj = new JSObject();
        if (!src.contains("http:") && !src.contains("https:")) {
            obj.put("value", src);
            return;
        }

        Uri url = Uri.parse(src);

        ImagePipeline imagePipeline = Fresco.getImagePipeline();

        ImageRequest request = ImageRequestBuilder.fromRequest(ImageRequest.fromUri(src))
                .setLowestPermittedRequestLevel(ImageRequest.RequestLevel.FULL_FETCH)
                .build();

        CacheKey cacheKey = imagePipeline.getCacheKeyFactory().getEncodedCacheKey(request, getContext());

        DataSource<CloseableReference<CloseableImage>> dataSource = imagePipeline.fetchDecodedImage(request, getContext());

        try {
            CloseableReference<CloseableImage> result = DataSources.waitForFinalResult(dataSource);
            obj.put("value", localUrl(cacheKey));
            call.resolve(obj);
            result.close();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            call.error(throwable.getMessage());
        } finally {
            dataSource.close();
        }
    }

    @PluginMethod()
    public void hasItem(PluginCall call) {
        String src = call.getString("src", "");
        Uri url = Uri.parse(src);
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        boolean has = imagePipeline.isInDiskCacheSync(url);
        JSObject obj = new JSObject();
        obj.put("value", has);
        call.resolve(obj);
    }

    @PluginMethod()
    public void clearItem(PluginCall call) {
        String src = call.getString("src", "");
        Uri url = Uri.parse(src);
        JSObject obj = new JSObject();
        try {
            ImagePipeline imagePipeline = Fresco.getImagePipeline();
            imagePipeline.evictFromCache(url);
            obj.put("value", true);
            call.resolve();
        } catch (Exception e) {
            obj.put("value", false);
            call.resolve(obj);
        }
    }

    @PluginMethod()
    public void clear(PluginCall call) {
        JSObject obj = new JSObject();
        try {
            ImagePipeline imagePipeline = Fresco.getImagePipeline();
            imagePipeline.clearCaches();
            obj.put("value", true);
            call.resolve();
        } catch (Exception e) {
            obj.put("value", false);
            call.resolve(obj);
        }
    }
}
