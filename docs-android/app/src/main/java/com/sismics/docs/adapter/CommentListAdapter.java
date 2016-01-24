package com.sismics.docs.adapter;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sismics.docs.R;
import com.sismics.docs.util.OkHttpUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Comment list adapter.
 *
 * @author bgamard.
 */
public class CommentListAdapter extends BaseAdapter {
    /**
     * Tags.
     */
    private List<JSONObject> commentList = new ArrayList<>();

    /**
     * Context.
     */
    private Context context;

    /**
     * Comment list adapter.
     *
     * @param commentsArray Comments
     */
    public CommentListAdapter(Context context, JSONArray commentsArray) {
        this.context = context;
        for (int i = 0; i < commentsArray.length(); i++) {
            commentList.add(commentsArray.optJSONObject(i));
        }
    }

    @Override
    public int getCount() {
        return commentList.size();
    }

    @Override
    public JSONObject getItem(int position) {
        return commentList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).optString("id").hashCode();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            LayoutInflater vi = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.comment_list_item, parent, false);
        }

        // Fill the view
        JSONObject comment = getItem(position);
        TextView creatorTextView = (TextView) view.findViewById(R.id.creatorTextView);
        TextView dateTextView = (TextView) view.findViewById(R.id.dateTextView);
        TextView contentTextView = (TextView) view.findViewById(R.id.contentTextView);
        ImageView gravatarImageView = (ImageView) view.findViewById(R.id.gravatarImageView);
        creatorTextView.setText(comment.optString("creator"));
        dateTextView.setText(DateFormat.getDateFormat(dateTextView.getContext()).format(new Date(comment.optLong("create_date"))));
        contentTextView.setText(comment.optString("content"));

        // Gravatar image
        String gravatarUrl = "http://www.gravatar.com/avatar/" + comment.optString("creator_gravatar") + "?s=128d=identicon";
        OkHttpUtil.picasso(context)
                .load(gravatarUrl)
                .into(gravatarImageView);

        return view;
    }

    /**
     * Add a new comment.
     *
     * @param comment Comment
     */
    public void add(JSONObject comment) {
        commentList.add(comment);
        notifyDataSetChanged();
    }

    /**
     * Remove a comment.
     *
     * @param commentId Comment ID
     */
    public void remove(String commentId) {
        for (JSONObject comment : commentList) {
            if (comment.optString("id").equals(commentId)) {
                commentList.remove(comment);
                notifyDataSetChanged();
                return;
            }
        }
    }
}
