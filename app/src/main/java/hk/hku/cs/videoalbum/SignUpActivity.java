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

import hk.hku.cs.videoalbum.helper.RemoteServerConnect;

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        Button signUpBtn = (Button) findViewById(R.id.submitSignUpBtn);
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String signUpUsername = ((EditText) findViewById(R.id.signUpLoginInput)).getText().toString();
                String signUpPassword = ((EditText) findViewById(R.id.signUpPasswordInput)).getText().toString();
                String retypePassword = ((EditText) findViewById(R.id.retypePasswordInput)).getText().toString();

                if (signUpUsername.trim().length() == 0 || signUpPassword.trim().length() == 0) {
                    alert("Sign Up", "Please enter new user name and password.");
                } else if (!signUpPassword.equals(retypePassword)) {
                    alert("Sign Up", "Password not match!");
                } else if (!signUpUsername.matches("^[a-zA-Z][a-zA-Z0-9]*")) {
                    alert("Sign Up", "User name must only contain alphanumeric character");
                } else if (!signUpPassword.matches("[a-zA-Z0-9]*")) {
                    alert("Sign Up", "User name must only contain alphanumeric character");
                } else if (signUpUsername.length() > 10) {
                    alert("Sign Up", "User name cannot longer than 10 character.");
                } else if (signUpPassword.length() > 20) {
                    alert("Sign Up", "Password cannot longer than 20 character.");
                } else {
                    signUp(signUpUsername, signUpPassword);
                }
            }
        });
    }

    private void alert(String title, String mymessage) {
        new AlertDialog.Builder(this)
                .setMessage(mymessage)
                .setTitle(title)
                .setCancelable(true)
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        }
                )
                .show();
    }

    private void signUp(final String username, final String password) {
        final ProgressDialog pdialog = new ProgressDialog(this);

        pdialog.setCancelable(false);
        pdialog.setMessage("Signing up ...");
        pdialog.show();

        final String url = "http://i.cs.hku.hk/~kfwong/videoalbum/login.php?action=signup&username=" + username + "&userkey=" + password;

        AsyncTask<String, Void, String> task = new AsyncTask<String, Void, String>() {
            boolean success;
            String jsonString;

            @Override
            protected String doInBackground(String... arg0) {
                success = true;
                RemoteServerConnect connect = new RemoteServerConnect();
                jsonString = connect.getUrlResponse(url);
                if (jsonString.equals("Fail to sign up"))
                    success = false;
                return null;
            }

            @Override
            protected void onPostExecute(String response) {
                if (success) {
                    try {
                        System.out.println(jsonString);
                        JSONObject rootJSONObj = new JSONObject(jsonString);
                        String result = rootJSONObj.getString("result");
                        if ("SUCCESS".equals(result)) {
                            SharedPreferences preferences = SignUpActivity.this.getSharedPreferences("video-album-login", 0);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("username", username);
                            editor.putString("password", password);
                            editor.apply();
                            Intent myIntent = new Intent(SignUpActivity.this, ListUserVideoActivity.class);
                            startActivityForResult(myIntent, 0);
                        } else {
                            alert("Sign Up", "Failure");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    alert("Error", "Fail to sign up");
                }
                pdialog.hide();
            }
        }.execute("");
    }
}
