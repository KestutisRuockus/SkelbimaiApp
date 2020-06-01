package skelbimas.lt;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rey.material.widget.CheckBox;

import io.paperdb.Paper;
import skelbimas.lt.model.Users;
import skelbimas.lt.prevalant.Prevalant;

public class LoginActivity extends AppCompatActivity {

    private EditText logNumber, logPassword;
    private Button btnLogin;
    private CheckBox rememberMe;
    private TextView adminPanel, notadminPanel;

    private ProgressDialog loadingBar;
    private String parentDbName = "Users";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        logNumber = findViewById(R.id.login_phone_number_input);
        logPassword = findViewById(R.id.login_password_input);
        btnLogin = findViewById(R.id.login_btn);
        rememberMe = findViewById(R.id.remember_me_chkb);
        Paper.init(this);
        adminPanel = findViewById(R.id.admin_panel_link);
        notadminPanel = findViewById(R.id.not_admin_panel_link);
        loadingBar = new ProgressDialog(this);


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginIntoApp();
            }
        });

        adminPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                btnLogin.setText("Login as Seller");
                adminPanel.setVisibility(View.INVISIBLE);
                notadminPanel.setVisibility(View.VISIBLE);
                parentDbName = "Admin";
            }
        });

        notadminPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                btnLogin.setText("Login");
                adminPanel.setVisibility(View.VISIBLE);
                notadminPanel.setVisibility(View.INVISIBLE);
                parentDbName = "Users";


            }
        });
    }



    private void loginIntoApp() {
        String phone = logNumber.getText().toString();
        String password = logPassword.getText().toString();

        if (TextUtils.isEmpty(phone)){
            Toast.makeText(this, "Phone number required.", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(password)){
            Toast.makeText(this, "Password required.", Toast.LENGTH_SHORT).show();
        }else{
            loadingBar.setTitle("Loading account");
            loadingBar.setMessage("Please wait while credentials will be checked.");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            allowAccessAccount(phone, password);
        }

    }

    private void allowAccessAccount(final String phone, final String password) {

        if (rememberMe.isChecked()){
            Paper.book().write(Prevalant.UserPhoneKey, phone);
            Paper.book().write(Prevalant.UserPasswordKey, password);
        }

        final DatabaseReference databaseReference;
        databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.child("Users").child(phone).exists()){
                    Users userData = dataSnapshot.child("Users").child(phone).getValue(Users.class);

                    if (userData.getPhone().equals(phone)){
                        if (userData.getPassword().equals(password)) {
                            if (parentDbName.equals("Admin")) {
                                Toast.makeText(LoginActivity.this, "WELCOME, Admin. You logged successfully.", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                startActivity(intent);

                            }
                            else if (parentDbName.equals("Users")) {
                                Toast.makeText(LoginActivity.this, "WELCOME, User. You logged successfully.", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(LoginActivity.this, Home2Activity.class);
                                startActivity(intent);
                            }
                        }else{
                            loadingBar.dismiss();
                            Toast.makeText(LoginActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    }
                }else {
                    Toast.makeText(LoginActivity.this, "Account with this number " + phone + " does not exist.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


}
