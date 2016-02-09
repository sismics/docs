package com.sismics.docs.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.sismics.docs.R;
import com.sismics.docs.util.OkHttpUtil;
import com.sismics.docs.util.PreferenceUtil;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

/**
 * @author bgamard.
 */
public class FilePagerAdapter extends PagerAdapter {
    /**
     * Files list.
     */
    private List<JSONObject> files;

    /**
     * Context.
     */
    private Context context;

    /**
     * File pager adapter.
     *
     * @param context Context
     * @param filesArray Files
     */
    public FilePagerAdapter(Context context, JSONArray filesArray) {
        this.files = new ArrayList<>();
        for (int i = 0; i < filesArray.length(); i++) {
            files.add(filesArray.optJSONObject(i));
        }
        this.context = context;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = LayoutInflater.from(container.getContext()).inflate(R.layout.file_viewpager_item, container, false);

        ImageViewTouch fileImageView = (ImageViewTouch) view.findViewById(R.id.fileImageView);
        final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.fileProgressBar);
        JSONObject file = files.get(position);
        String fileUrl = PreferenceUtil.getServerUrl(context) + "/api/file/" + file.optString("id") + "/data?size=web";

        // Load image
        OkHttpUtil.picasso(context)
                .load(fileUrl)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE) // Don't memory cache the images
                .into(fileImageView, new Callback.EmptyCallback() {
                    @Override
                    public void onSuccess() {
                        progressBar.setVisibility(View.GONE);
                    }
                });

        fileImageView.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);

        container.addView(view, 0);

        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        if (files == null) {
            return 0;
        }

        return files.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    /**
     * Return the object at a given position.
     *
     * @param position Position
     * @return Object
     */
    public JSONObject getObjectAt(int position) {
        if (files == null || position < 0 || position >= files.size()) {
            return null;
        }

        return files.get(position);
    }

    /**
     * Remove a file.
     *
     * @param fileId File ID
     */
    public void remove(String fileId) {
        if (files == null || fileId == null) return;

        for (JSONObject file : files) {
            if (fileId.equals(file.optString("id"))) {
                files.remove(file);
                notifyDataSetChanged();
                break;
            }
        }
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}
