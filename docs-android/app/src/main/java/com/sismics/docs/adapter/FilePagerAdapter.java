package com.sismics.docs.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.androidquery.AQuery;
import com.androidquery.callback.BitmapAjaxCallback;
import com.sismics.docs.R;
import com.sismics.docs.util.PreferenceUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

/**
 * @author bgamard.
 */
public class FilePagerAdapter extends PagerAdapter {
    /**
     * Files list.
     */
    private JSONArray files;

    /**
     * AQuery.
     */
    private AQuery aq;

    /**
     * Context.
     */
    private Context context;

    /**
     * Auth token used to download files.
     */
    private String authToken;

    public FilePagerAdapter(Context context, JSONArray files) {
        this.files = files;
        this.context = context;
        this.authToken = PreferenceUtil.getAuthToken(context);
        aq = new AQuery(context);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = LayoutInflater.from(container.getContext()).inflate(R.layout.file_viewpager_item, container, false);

        ImageViewTouch fileImageView = (ImageViewTouch) view.findViewById(R.id.fileImageView);
        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.fileProgressBar);
        JSONObject file = files.optJSONObject(position);
        String fileUrl = PreferenceUtil.getServerUrl(context) + "/api/file/" + file.optString("id") + "/data?size=web";
        aq.id(fileImageView)
                .image(new BitmapAjaxCallback()
                        .url(fileUrl)
                        .progress(progressBar)
                        .animation(AQuery.FADE_IN_NETWORK)
                        .cookie("auth_token", authToken));

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

        return files.length();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
