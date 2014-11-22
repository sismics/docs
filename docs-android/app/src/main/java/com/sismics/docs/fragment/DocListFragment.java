package com.sismics.docs.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.sismics.docs.DividerItemDecoration;
import com.sismics.docs.R;
import com.sismics.docs.activity.DocumentActivity;
import com.sismics.docs.adapter.DocListAdapter;
import com.sismics.docs.listener.RecyclerItemClickListener;
import com.sismics.docs.resource.DocumentResource;

import org.apache.http.Header;
import org.json.JSONObject;

/**
 * @author bgamard.
 */
public class DocListFragment extends Fragment {
    /**
     * Documents adapter.
     */
    DocListAdapter adapter;

    // Infinite scrolling things
    private boolean loading = true;
    private int previousTotal = 0;
    int firstVisibleItem, visibleItemCount, totalItemCount;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.doc_list_fragment, container, false);

        // Configure the RecyclerView
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.docList);
        adapter = new DocListAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLongClickable(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(getResources().getDrawable(R.drawable.abc_list_divider_mtrl_alpha)));

        // Configure the LayoutManager
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        // Document opening
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                JSONObject document = adapter.getItemAt(position);
                if (document != null) {
                    Intent intent = new Intent(getActivity(), DocumentActivity.class);
                    intent.putExtra("document", document.toString());
                    startActivity(intent);
                }
            }
        }));

        // Infinite scrolling
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                visibleItemCount = recyclerView.getChildCount();
                totalItemCount = layoutManager.getItemCount();
                firstVisibleItem = layoutManager.findFirstVisibleItemPosition();

                if (loading) {
                    if (totalItemCount > previousTotal) {
                        loading = false;
                        previousTotal = totalItemCount;
                    }
                }
                if (!loading && totalItemCount - visibleItemCount <= firstVisibleItem + 3) {
                    loadDocuments();
                    loading = true;
                }
            }
        });

        // Grab the documents
        loadDocuments();

        return view;
    }

    /**
     * Refresh the document list.
     */
    private void loadDocuments() {
        DocumentResource.list(getActivity(), adapter.getItemCount(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                adapter.addDocuments(response.optJSONArray("documents"), false);
                if (getView() != null) {
                    getView().findViewById(R.id.progressBar).setVisibility(View.GONE);
                }
            }
        });
    }
}
