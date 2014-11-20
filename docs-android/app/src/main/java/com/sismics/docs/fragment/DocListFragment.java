package com.sismics.docs.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.sismics.docs.DividerItemDecoration;
import com.sismics.docs.R;
import com.sismics.docs.adapter.DocListAdapter;
import com.sismics.docs.listener.RecyclerItemClickListener;
import com.sismics.docs.resource.DocumentResource;

import org.apache.http.Header;
import org.json.JSONObject;

/**
 * @author bgamard.
 */
public class DocListFragment extends Fragment {

    DocListAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Initialize the view
        View view = inflater.inflate(R.layout.doc_list_fragment, container, false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.docList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLongClickable(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new DocListAdapter();
        recyclerView.setAdapter(adapter);

        recyclerView.addItemDecoration(new DividerItemDecoration(getResources().getDrawable(R.drawable.abc_list_divider_mtrl_alpha)));

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(getActivity(), position + " clicked", Toast.LENGTH_SHORT).show();
            }
        }));

        // Grab the documents
        refreshDocuments();

        return view;
    }

    /**
     * Refresh the document list.
     */
    private void refreshDocuments() {
        DocumentResource.list(getActivity(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                adapter.setDocuments(response.optJSONArray("documents"));
            }
        });
    }
}
