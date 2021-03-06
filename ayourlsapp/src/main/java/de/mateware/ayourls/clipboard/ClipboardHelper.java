package de.mateware.ayourls.clipboard;

import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mateware.ayourls.utils.PowerManagerCompat;

/**
 * Created by mate on 02.10.2015.
 */
public class ClipboardHelper {

    private static Logger log = LoggerFactory.getLogger(ClipboardHelper.class);

    private static ClipboardHelper instance;
    private static String lastClipTextSet;

    public static ClipboardHelper getInstance(Context context) {
        if (instance == null) instance = new ClipboardHelper(context.getApplicationContext());
        return instance;
    }

    private ClipboardManager clipboardManager;
    private android.text.ClipboardManager clipboardManagerCompat;

    private ClipboardManager.OnPrimaryClipChangedListener listener;

    private ClipboardHelper(final Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            listener = new ClipboardManager.OnPrimaryClipChangedListener() {
                @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                @Override
                public void onPrimaryClipChanged() {
                    log.debug("clipboard content changed, listener:" + this.toString());

                    //Only handle it if device is in interactive mode
                    if (!PowerManagerCompat.isInteractive(context)) {
                        log.debug("Device is not interactive, return without work");
                        return ;
                    }

                    if (!TextUtils.isEmpty(lastClipTextSet) && lastClipTextSet.equals(getClipContent()))
                        return;
                    Intent intent = new Intent(ClipboardChangeReceiver.ACTION);
                    context.sendBroadcast(intent);
                }
            };

        } else {
            clipboardManagerCompat = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        }
    }

//    public static void checkClipboardActivation(Context context) {
//        context = context.getApplicationContext();
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//        boolean serverCheck = prefs.getBoolean(context.getString(R.string.pref_key_server_check), false);
//        boolean clipboardMonitor = prefs.getBoolean(context.getString(R.string.pref_key_app_clipboard_monitor),false);
//
//        ClipboardHelper clipboardHelper = getInstance(context);
//        if (clipboardMonitor && serverCheck) {
//            clipboardHelper.registerClipBoardListener();
//        } else {
//            clipboardHelper.unregisterClipboardListener();
//        }
//    }

    public void registerClipBoardListener() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (clipboardManager != null) {
                log.debug("Register clipboard listener");
                clipboardManager.addPrimaryClipChangedListener(listener);
            }
        } else {
            //TODO find solution for pre Honeycomb
        }
    }

    public void unregisterClipboardListener() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (clipboardManager != null) {
                log.debug("Unregister clipboard listener");
                clipboardManager.removePrimaryClipChangedListener(listener);
            }
        } else {
            //TODO find solution for pre Honeycomb
        }
    }


    public CharSequence getClipContent() {
        log.debug("Getting clip content");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (clipboardManager != null) {
                if (clipboardManager.hasPrimaryClip()) {
                    ClipData clip = clipboardManager.getPrimaryClip();
                    ClipData.Item item = clip.getItemAt(0);
                    if (item.getText() != null)
                        return item.getText().toString();
                    return item.getText();
                }
            }
        } else {
            if (clipboardManagerCompat != null) {
                if (clipboardManagerCompat.hasText()) {
                    return clipboardManagerCompat.getText();
                }
            }
        }
        return null;
    }

    public boolean setClipContent(String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (clipboardManager != null) {
                ClipData clip = ClipData.newPlainText("aYourls", text);
                lastClipTextSet = text;
                clipboardManager.setPrimaryClip(clip);
                return true;
            }
        } else {
            if (clipboardManagerCompat != null) {
                clipboardManagerCompat.setText(text);
                return true;
            }
        }
        return false;
    }


}
