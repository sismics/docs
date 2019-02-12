package com.sismics.docs.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.sismics.docs.R;
import com.sismics.docs.activity.DocumentViewActivity;
import com.sismics.docs.adapter.DocListAdapter;
import com.sismics.docs.event.DocumentAddEvent;
import com.sismics.docs.event.DocumentDeleteEvent;
import com.sismics.docs.event.DocumentEditEvent;
import com.sismics.docs.event.SearchEvent;
import com.sismics.docs.listener.HttpCallback;
import com.sismics.docs.listener.RecyclerItemClickListener;
import com.sismics.docs.resource.DocumentResource;
import com.sismics.docs.ui.view.DividerItemDecoration;
import com.sismics.docs.ui.view.EmptyRecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

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

    // View cache
    private EmptyRecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;

    // Infinite scrolling things
    private boolean loading = true;
    private int previousTotal = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.doc_list_fragment, container, false);

        // Configure the RecyclerView
        recyclerView = view.findViewById(R.id.docList);
        adapter = new DocListAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLongClickable(true);

        // Configure the LayoutManager
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        // Configure the swipe refresh layout
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadDocuments(view, true);
            }
        });

        // Document opening
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                JSONObject document = adapter.getItemAt(position);
                if (document != null) {
                    openDocument(document);
                }
            }
        }));

        // Infinite scrolling
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                    loadDocuments(getView(), false);
                    loading = true;
                }
            }
        });

        // Grab the documents
        loadDocuments(view, true);

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
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(SearchEvent event) {
        query = event.getQuery();
        loadDocuments(getView(), true);
    }

    /**
     * A document edit event has been fired.
     *
     * @param event Document edit event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(DocumentEditEvent event) {
        adapter.updateDocument(event.getDocument());
    }

    /**
     * A document delete event has been fired.
     *
     * @param event Document delete event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(DocumentDeleteEvent event) {
        adapter.deleteDocument(event.getDocumentId());
    }

    /**
     * A document add event has been fired.
     *
     * @param event Document add event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(DocumentAddEvent event) {
        // Refresh the list, maybe the new document fit in it
        loadDocuments(getView(), true);

        // Open the newly created document
        openDocument(event.getDocument());
    }

    /**
     * Open a document.
     *
     * @param document Document to open
     */
    private void openDocument(JSONObject document) {
        Intent intent = new Intent(getActivity(), DocumentViewActivity.class);
        intent.putExtra("document", document.toString());
        startActivity(intent);
    }

    /**
     * Refresh the document list.
     *
     * @param view View
     * @param reset If true, reload the documents
     */
    private void loadDocuments(final View view, final boolean reset) {
        if (view == null) return;
        final View progressBar = view.findViewById(R.id.progressBar);
        final TextView documentsEmptyView = view.findViewById(R.id.documentsEmptyView);

        if (reset) {
            loading = true;
            previousTotal = 0;
            adapter.clearDocuments();
        } else {
            swipeRefreshLayout.setRefreshing(true);
        }

        recyclerView.setEmptyView(progressBar);

        DocumentResource.list(getActivity(), reset ? 0 : adapter.getItemCount(), query, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                adapter.addDocuments(response.optJSONArray("documents"));
                documentsEmptyView.setText(R.string.no_documents);
                recyclerView.setEmptyView(documentsEmptyView);
            }

            @Override
            public void onFailure(JSONObject response, Exception e) {
                documentsEmptyView.setText(R.string.error_loading_documents);
                recyclerView.setEmptyView(documentsEmptyView);

                if (!reset) {
                    // We are loading a new page, so the empty view won't be visible, pop a toast
                    Toast.makeText(getActivity(), R.string.error_loading_documents, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFinish() {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }
}
