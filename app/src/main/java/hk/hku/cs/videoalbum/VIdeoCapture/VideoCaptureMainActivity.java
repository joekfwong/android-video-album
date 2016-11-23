package hk.hku.cs.videoalbum.VIdeoCapture;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import hk.hku.cs.videoalbum.R;

/**
 * Created by a on 11/20/2016.
 */

public class VideoCaptureMainActivity extends Activity {

    private Button goButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_capture_main);

        goButton = (Button) findViewById(R.id.go_button);
        // Setup event handlers
        goButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                Intent launchactivity= new Intent(VideoCaptureMainActivity.this,VideoCaptureBrowserActivity.class);

                startActivity(launchactivity);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_video_capture_main, menu);
        return true;
    }

}
