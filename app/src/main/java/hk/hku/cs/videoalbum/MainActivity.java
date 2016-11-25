package hk.hku.cs.videoalbum;

import android.app.ProgressDialog;
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

import hk.hku.cs.videoalbum.helper.AlertBox;
import hk.hku.cs.videoalbum.helper.RemoteServerConnect;

public class MainActivity extends AppCompatActivity {

    private AlertBox alertBox = new AlertBox();

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
                    connect(username, password);
                } else {
                    alertBox.alert("Login", "Please enter username and password");
                }
            }
        });

        Button signUpBtn = (Button) findViewById(R.id.signUpBtn);
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, SignUpActivity.class);
                startActivityForResult(myIntent, 0);
            }
        });
    }

    private void connect(final String username, final String password){
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
                success = true;
                RemoteServerConnect connect = new RemoteServerConnect();
                jsonString = connect.getUrlResponse(url);
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
                            SharedPreferences preferences = MainActivity.this.getSharedPreferences("video-album-login", 0);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("username", username);
                            editor.putString("password", password);
                            editor.apply();
                            Intent myIntent = new Intent(MainActivity.this, ListUserVideoActivity.class);
                            startActivityForResult(myIntent, 0);
                        } else {
                            alertBox.alert("Login", "Failure");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    alertBox.alert("Error", "Fail to login");
                }
                pdialog.hide();
            }
        }.execute("");
    }
}
