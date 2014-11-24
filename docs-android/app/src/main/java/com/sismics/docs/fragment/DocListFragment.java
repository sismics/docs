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
import com.sismics.docs.R;
import com.sismics.docs.activity.DocumentActivity;
import com.sismics.docs.adapter.DocListAdapter;
import com.sismics.docs.event.SearchEvent;
import com.sismics.docs.listener.RecyclerItemClickListener;
import com.sismics.docs.resource.DocumentResource;
import com.sismics.docs.ui.view.DividerItemDecoration;

import org.apache.http.Header;
import org.json.JSONObject;

import de.greenrobot.event.EventBus;

/**
 * @author bgamard.
 */
public class DocListFragment extends Fragment {
    /**
     * Documents adapter.
     */
    private DocListAdapter adapter;

    /**
     * Search query.
     */
    private String query;

    // Infinite scrolling things
    private boolean loading = true;
    private int previousTotal = 0;

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

                int visibleItemCount = recyclerView.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();

                if (loading) {
                    if (totalItemCount > previousTotal) {
                        loading = false;
                        previousTotal = totalItemCount;
                    }
                }
                if (!loading && totalItemCount - visibleItemCount <= firstVisibleItem + 3) {
                    loadDocuments(false);
                    loading = true;
                }
            }
        });

        // Grab the documents
        loadDocuments(true);

        EventBus.getDefault().register(this);
        return view;
    }

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }

    /**
     * A search event has been fired.
     *
     * @param event Search event
     */
    public void onEvent(SearchEvent event) {
        query = event.getQuery();
        loadDocuments(true);
    }

    /**
     * Refresh the document list.
     *
     * @param reset If true, reload the documents
     */
    private void loadDocuments(final boolean reset) {
        if (reset) {
            loading = true;
            previousTotal = 0;
            adapter.clearDocuments();
            if (getView() != null) {
                getView().findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            }
        }

        DocumentResource.list(getActivity(), reset ? 0 : adapter.getItemCount(), query, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                adapter.addDocuments(response.optJSONArray("documents"));
                if (getView() != null) {
                    getView().findViewById(R.id.progressBar).setVisibility(View.GONE);
                }
            }
        });
    }
}
