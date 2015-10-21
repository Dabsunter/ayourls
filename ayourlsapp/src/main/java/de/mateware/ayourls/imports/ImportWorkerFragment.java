package de.mateware.ayourls.imports;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mateware.ayourls.R;
import de.mateware.ayourls.network.NetworkHelper;
import de.mateware.ayourls.yourslapi.Volley;
import de.mateware.ayourls.yourslapi.YourlsError;
import de.mateware.ayourls.yourslapi.YourlsRequest;
import de.mateware.ayourls.yourslapi.action.DbStats;
import de.mateware.ayourls.yourslapi.action.Stats;

/**
 * Created by mate on 21.10.2015.
 */
public class ImportWorkerFragment extends Fragment {

    private static Logger log = LoggerFactory.getLogger(ImportWorkerFragment.class);

    private static final String TAG_WORKER_FRAGMENT = "import_worker_fragment";
    private ImportWorkerCallback callback;

    public int limitLinksPerCall = 10;
    public long totalLinksOnServer = 0;

    public ImportWorkerFragment() {
    }

    public static ImportWorkerFragment findOrCreateFragment(FragmentManager fm, ImportWorkerCallback callback) {
        ImportWorkerFragment fragment = (ImportWorkerFragment) fm.findFragmentByTag(TAG_WORKER_FRAGMENT);
        if (fragment == null) {
            fragment = new ImportWorkerFragment();
            fm.beginTransaction()
              .add(fragment, TAG_WORKER_FRAGMENT)
              .commit();
        }
        fragment.callback = callback;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public void callDbStats(final Context context) {
        if (NetworkHelper.isConnected(context)) {
            YourlsRequest<DbStats> request = new YourlsRequest<>(context, new DbStats(), new Response.Listener<DbStats>() {
                @Override
                public void onResponse(DbStats response) {
                    log.debug(response.toString());
                    totalLinksOnServer = response.getTotalLinks();
                    if (totalLinksOnServer > 0) {
                        callUrlStats(context,0,limitLinksPerCall);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    log.error(error.toString());
                    callback.onNetworkError(new YourlsError(error));
                }
            });
            Volley.getInstance(context)
                  .addToRequestQueue(request);
        } else {
            callback.onNetworkError(new YourlsError(new VolleyError(getString(R.string.dialog_error_no_connection_message))));
        }
    }

    public void callUrlStats(Context context, int start, int limit) {
        if (NetworkHelper.isConnected(context)) {
            YourlsRequest<Stats> request = new YourlsRequest<>(context, new Stats(start,limit), new Response.Listener<Stats>() {
                @Override
                public void onResponse(Stats response) {
                    log.debug(response.toString());

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    log.error(error.toString());
                    callback.onNetworkError(new YourlsError(error));
                }
            });
            Volley.getInstance(context)
                  .addToRequestQueue(request);
        } else {
            callback.onNetworkError(new YourlsError(new VolleyError(getString(R.string.dialog_error_no_connection_message))));
        }
    }

    public long getTotalLinksOnServer() {
        return totalLinksOnServer;
    }

    public interface ImportWorkerCallback {
        void onNetworkError(YourlsError error);

        void showWaitDialog();

        void hideWaitDialog();
    }


}