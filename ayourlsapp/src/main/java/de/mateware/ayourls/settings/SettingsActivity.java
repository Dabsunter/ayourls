package de.mateware.ayourls.settings;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import de.mateware.ayourls.R;

/**
 * Created by mate on 23.09.2015.
 */
public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blank);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                                       .replace(R.id.content_frame, new SettingsFragment())
                                       .commit();
        }
    }
}
