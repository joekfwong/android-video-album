package hk.hku.cs.videoalbum;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button loginBtn = (Button) findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = ((EditText) findViewById(R.id.loginInput)).getText().toString();
                String password = ((EditText) findViewById(R.id.passwordInput)).getText().toString();

                if (username.trim().length() > 0 && password.trim().length() > 0) {
                    // login activity
                    connect(username, password, v);
                } else {
                    // prompt enter both username and password
                    alert( "Login", "Please enter username and password" );
                }
            }
        });
    }

    protected void alert(String title, String mymessage){
        new AlertDialog.Builder(this)
                .setMessage(mymessage)
                .setTitle(title)
                .setCancelable(true)
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton){}
                        }
                )
                .show();
    }

    public String getJsonPage(String url) {
        HttpURLConnection conn_cshomepage = null;
        final int HTML_BUFFER_SIZE = 2*1024*1024;
        char htmlBuffer[] = new char[HTML_BUFFER_SIZE];

        try {
            URL url_cshomepage = new URL(url);
            conn_cshomepage = (HttpURLConnection) url_cshomepage.openConnection();
            conn_cshomepage.setInstanceFollowRedirects(true);

            BufferedReader reader_moodle = new BufferedReader(new InputStreamReader(conn_cshomepage.getInputStream()));
            String HTMLSource = ReadBufferedHTML(reader_moodle, htmlBuffer, HTML_BUFFER_SIZE);
            reader_moodle.close();
            return HTMLSource;
        } catch (Exception e) {
            e.printStackTrace();
            return "Fail to login";
        } finally {
            // When HttpClient instance is no longer needed,
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
            if (conn_cshomepage != null) {
                conn_cshomepage.disconnect();
            }
        }
    }

    public String ReadBufferedHTML(BufferedReader reader, char [] htmlBuffer, int bufSz) throws java.io.IOException
    {
        htmlBuffer[0] = '\0';
        int offset = 0;
        do {
            int cnt = reader.read(htmlBuffer, offset, bufSz - offset);
            if (cnt > 0) {
                offset += cnt;
            } else {
                break;
            }
        } while (true);
        return new String(htmlBuffer);
    }

    private void connect(final String username, final String password, final View v){
        final ProgressDialog pdialog = new ProgressDialog(this);

        pdialog.setCancelable(false);
        pdialog.setMessage("Logging in ...");
        pdialog.show();

        final String url = "http://i.cs.hku.hk/~kfwong/videoalbum/login.php?username=" + username + "&userkey=" + password;

        AsyncTask<String, Void, String> task = new AsyncTask<String, Void, String>() {
            boolean success;
            String jsonString;

            @Override
            protected String doInBackground(String... arg0) {
                // TODO Auto-generated method stub
                success = true;
                jsonString = getJsonPage(url);
                if (jsonString.equals("Fail to login"))
                    success = false;
                return null;
            }

            @Override
            protected void onPostExecute(String response) {
                if (success) {
                    try {
                        JSONObject rootJSONObj = new JSONObject(jsonString);
                        String result = rootJSONObj.getString("result");
                        if ("SUCCESS".equals(result)) {
                            //alert( "Login", "Success" );
                            SharedPreferences preferences = v.getContext().getSharedPreferences("video-album-login", 0);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("username", username);
                            editor.putString("password", password);
                            editor.apply();
                            Intent myIntent = new Intent(v.getContext(), ListUserVideoActivity.class);
                            startActivityForResult(myIntent, 0);
                        } else {
                            alert( "Login", "Failure" );
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    alert( "Error", "Fail to login" );
                }
                pdialog.hide();
            }
        }.execute("");
    }

}
