package com.app.hopity.clustring;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.util.LruCache;

import com.androidmapsextensions.ClusterOptions;
import com.androidmapsextensions.ClusterOptionsProvider;
import com.androidmapsextensions.Marker;
import com.app.hopity.R;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.util.List;

/**
 * Created by Mushi on 8/11/2016.
 */
public class DemoClusterOptionsProvider implements ClusterOptionsProvider {

    private static final int[] res = {R.drawable.m1, R.drawable.m2, R.drawable.m3, R.drawable.m4, R.drawable.m5};

    private static final int[] forCounts = {10, 100, 1000, 10000, Integer.MAX_VALUE};

    private Bitmap[] baseBitmaps;
    private LruCache<Integer, BitmapDescriptor> cache = new LruCache<Integer, BitmapDescriptor>(128);

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Rect bounds = new Rect();

    private ClusterOptions clusterOptions = new ClusterOptions().anchor(0.5f, 0.5f);

    public DemoClusterOptionsProvider(Resources resources) {
        baseBitmaps = new Bitmap[res.length];
        for (int i = 0; i < res.length; i++) {
            baseBitmaps[i] = BitmapFactory.decodeResource(resources, res[i]);
        }
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(resources.getDimension(R.dimen.text_size));
    }

    @Override
    public ClusterOptions getClusterOptions(List<Marker> markers) {

        int markersCount = markers.size();
        BitmapDescriptor cachedIcon = cache.get(markersCount);
        if (cachedIcon != null) {
            return clusterOptions.icon(cachedIcon);
        }

        Bitmap base;
        int i = 0;
        do {
            base = baseBitmaps[i];
        } while (markersCount >= forCounts[i++]);

        Bitmap bitmap = base.copy(Bitmap.Config.ARGB_8888, true);

        String text = String.valueOf(markersCount);
        paint.getTextBounds(text, 0, text.length(), bounds);
        float x = bitmap.getWidth() / 2.0f;
        float y = (bitmap.getHeight() - bounds.height()) / 2.0f - bounds.top;

        Canvas canvas = new Canvas(bitmap);
        canvas.drawText(text, x, y, paint);

        BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(bitmap);
        cache.put(markersCount, icon);

        return clusterOptions.icon(icon);
    }
}
