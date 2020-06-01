package skelbimas.lt;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.paperdb.Paper;
import skelbimas.lt.model.Users;
import skelbimas.lt.prevalant.Prevalant;

public class MainActivity extends AppCompatActivity {

    private Button mainLogin, mainRegister;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainLogin = findViewById(R.id.main_login_btn);
        mainRegister = findViewById(R.id.main_join_now_btn);
        loadingBar = new ProgressDialog(this);

        Paper.init(this);

        mainLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        mainRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        String userPhoneKey = Paper.book().read(Prevalant.UserPhoneKey);
        String userPasswordKey = Paper.book().read(Prevalant.UserPasswordKey);

        if (userPhoneKey != "" && userPasswordKey != ""){
            if (!TextUtils.isEmpty(userPhoneKey) && !TextUtils.isEmpty(userPasswordKey)){

                allowAccessAccount(userPhoneKey, userPasswordKey);

                loadingBar.setTitle("You're logged in.");
                loadingBar.setMessage("Loading...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();
            }
        }

    }

    private void allowAccessAccount(final String userPhoneKey, final String userPasswordKey) {

        final DatabaseReference databaseReference;

        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.child("Users").child(userPhoneKey).exists()){
                    Users userData = dataSnapshot.child("Users").child(userPhoneKey).getValue(Users.class);
                        if (userData.getPhone().equals(userPhoneKey)){
                            if (userData.getPassword().equals(userPasswordKey)){
                                Toast.makeText(MainActivity.this, "Loading...", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                startActivity(intent);
                            }else{
                                loadingBar.dismiss();
                                Toast.makeText(MainActivity.this, "Wrong password.", Toast.LENGTH_SHORT).show();
                            }
                        }
                }else {
                    Toast.makeText(MainActivity.this, "Account with this number " + userPhoneKey + " exist.", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
