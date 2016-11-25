package hk.hku.cs.videoalbum;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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

                if (signUpUsername.trim().length() > 0 && signUpPassword.trim().length() > 0) {

                } else {

                }
            }
        });
    }
}
