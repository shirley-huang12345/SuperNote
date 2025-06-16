package com.uservoice.uservoicesdk.ui;

import java.util.List;

import android.content.Context;

public abstract class PaginatedAdapter<T> extends ModelAdapter<T> {

    private int page = 1;
    private boolean noMore = false;

    public PaginatedAdapter(Context context, int layoutId, List<T> objects) {
        super(context, layoutId, objects);
    }


    public void loadMore() {
        if (loading || searchActive || objects.size() == getTotalNumberOfObjects() || noMore) return;
        loading = true;
        notifyDataSetChanged();
        loadPage(page, new DefaultCallback<List<T>>(context) {
            @Override
            public void onModel(List<T> model) {
                if (model.size()==0) {
                    noMore = true;
                }
                objects.addAll(model);
                page += 1;
                loading = false;
                notifyDataSetChanged();
            }
        });
    }

    protected abstract int getTotalNumberOfObjects();

    protected List<T> getObjects() {
        return shouldShowSearchResults() ? searchResults : objects;
    }

    public void reload() {
        // not *correct* but probably good enough. the correct thing would be to cancel the load somehow and proceed
        if (loading)
            return;
        page = 1;
        noMore = false;
        objects.clear();
        notifyDataSetChanged();
        loadMore();
    }
}
