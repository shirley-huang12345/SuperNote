package com.uservoice.uservoicesdk.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.uservoice.uservoicesdk.R;
import com.uservoice.uservoicesdk.Session;
import com.uservoice.uservoicesdk.UserVoice;
import com.uservoice.uservoicesdk.babayaga.Babayaga;
import com.uservoice.uservoicesdk.model.BaseModel;
import com.uservoice.uservoicesdk.ui.PortalAdapter;
import com.uservoice.uservoicesdk.ui.Utils;

public class PortalActivity extends BaseListActivity implements SearchActivity {

    private PortalAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkSearchSetting();

        if(Session.getInstance().getConfig() == null) {
            finish();
            return;
        }
        if(Session.getInstance().getConfig() == null || Session.getInstance().getConfig().getAPPTitle() == null)
            setTitle(R.string.uf_sdk_feedback_and_help);
        else
            setTitle(Session.getInstance().getConfig().getAPPTitle());
        getListView().setDivider(null);
        Babayaga.track(Babayaga.Event.VIEW_KB);
        setListAdapter(mAdapter = new PortalAdapter(this));
        getListView().setOnItemClickListener(getModelAdapter());
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if(UserVoice.sNeedReload){
            mAdapter.reload();
        }
        UserVoice.sNeedReload = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(Session.getInstance().getConfig() == null) return true;
        if(Build.VERSION.SDK_INT < 21 && Utils.isSimilarToWhite(UserVoice.sColor)) getMenuInflater().inflate(R.menu.uv_portal_light, menu);
        else getMenuInflater().inflate(R.menu.uv_portal, menu);
        MenuItem item = menu.findItem(R.id.uv_action_contact);
        if(item != null && !Session.getInstance().getConfig().shouldShowContactUs())
            item.setVisible(false);
        setupScopedSearch(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.uv_action_contact) {
            if(Session.getInstance().getClientConfig() != null){
                startActivity(new Intent(this, ContactActivity.class));
            }else{
                Toast.makeText(this, R.string.uf_sdk_no_network_connection_title, Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public PortalAdapter getModelAdapter() {
        return (PortalAdapter) getListAdapter();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        BaseModel.cancelTask();
    }
}
