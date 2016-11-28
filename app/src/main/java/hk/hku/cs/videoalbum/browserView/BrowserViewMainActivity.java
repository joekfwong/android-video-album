package hk.hku.cs.videoalbum.browserView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import hk.hku.cs.videoalbum.R;

public class BrowserViewMainActivity extends Activity {
    private EditText urlText;
    private Button goButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser_view_main);
        urlText = (EditText) findViewById(R.id.url_field);
        goButton = (Button) findViewById(R.id.go_button);
        // Setup event handlers
        goButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                Intent launchactivity= new Intent(BrowserViewMainActivity.this,BrowserViewBrowserActivity.class);
                launchactivity.putExtra("url", urlText.getText());
                startActivity(launchactivity);
            }
        });
        urlText.setOnEditorActionListener(new OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if (actionId == EditorInfo.IME_NULL) {
                    Intent launchactivity= new Intent(BrowserViewMainActivity.this,BrowserViewBrowserActivity.class);
                    launchactivity.putExtra("url", urlText.getText());
                    startActivity(launchactivity);
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_browser_view_main, menu);
        return true;
    }

}
