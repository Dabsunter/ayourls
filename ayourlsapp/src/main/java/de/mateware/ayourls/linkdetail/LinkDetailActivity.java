package de.mateware.ayourls.linkdetail;


import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mateware.ayourls.DataBinder;
import de.mateware.ayourls.R;
import de.mateware.ayourls.databinding.ActivityLinkdetailBinding;
import de.mateware.ayourls.model.Link;
import de.mateware.ayourls.utils.MenuTinter;
import de.mateware.ayourls.viewmodel.LinkViewModel;
import de.mateware.ayourls.yourslapi.YourlsError;

/**
 * Created by Mate on 12.10.2015.
 */
public class LinkDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, DataBinder.QrImageLoaderCallback, LinkDetailWorkerFragment.LinkDetailWorkerCallback {

    private static Logger log = LoggerFactory.getLogger(LinkDetailActivity.class);

    public static final String EXTRA_LINK_ID = "extraLinkId";

    private static final String STATE_REFRESHING = "stateRefreshing";

    private LinkDetailWorkerFragment workerFragment;
    private ActivityLinkdetailBinding binding;
    private LinkViewModel linkViewModel;

    private boolean refreshing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState!=null)
            refreshing = savedInstanceState.getBoolean(STATE_REFRESHING,false);

        workerFragment = LinkDetailWorkerFragment.findOrCreateFragment(getSupportFragmentManager(), this);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_linkdetail);
        linkViewModel = new LinkViewModel(this);
        binding.setViewModel(linkViewModel);

        ActivityCompat.postponeEnterTransition(this);

        Bundle loaderBundle = null;
        if (savedInstanceState == null) {
            loaderBundle = new Bundle();
            loaderBundle.putLong(EXTRA_LINK_ID, getIntent().getLongExtra(EXTRA_LINK_ID, -1));
        }
        getSupportLoaderManager().initLoader(0, loaderBundle, this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(STATE_REFRESHING,refreshing);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_link_detail, menu);
        MenuTinter.tintMenu(this, menu, R.color.menu_item);
        if (refreshing)
            menu.findItem(R.id.action_refresh).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
            case R.id.action_refresh:
                refreshing = true;
                invalidateOptionsMenu();
                workerFragment.refreshLinkData(linkViewModel.getKeyword());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        log.debug("creating loader with id {} and args {}", id, args);
        return new CursorLoader(this, Link.getContentUri(args.getLong(EXTRA_LINK_ID)), null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        log.debug("loaded {} {}", loader, data);
        if (data.isBeforeFirst()) data.moveToNext();
        Link link = new Link();
        link.load(data);
        log.debug("loaded {}", link);
        linkViewModel.setLink(link);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        log.debug("reset {}", loader);
    }

    @Override
    public void onQrImageLoaded() {
        log.debug("start animation");
        ActivityCompat.startPostponedEnterTransition(this);
    }

    @Override
    public void onError(YourlsError error) {

    }

    @Override
    public void onRefreshFinished() {
        refreshing = false;
        invalidateOptionsMenu();
    }
}