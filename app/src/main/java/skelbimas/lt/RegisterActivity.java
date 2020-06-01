package skelbimas.lt;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private EditText regName, regPhone, regPassw, regConfirmPassw;
    private Button btnReg;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        regName = findViewById(R.id.register_username_input);
        regPhone = findViewById(R.id.register_phone_number_input);
        regPassw = findViewById(R.id.register_password_input);
        regConfirmPassw = findViewById(R.id.register_confirm_password_input);

        btnReg = findViewById(R.id.register_btn);

        loadingBar = new ProgressDialog(this);

        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                createAccount();
            }
        });

    }

    private void createAccount() {
        String name = regName.getText().toString();
        String phone = regPhone.getText().toString();
        String password = regPassw.getText().toString();
        String confirmPassword = regConfirmPassw.getText().toString();

        if (TextUtils.isEmpty(name)){
            Toast.makeText(this, "Name required.", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(phone)){
            Toast.makeText(this, "Phone number required", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(password)){
            Toast.makeText(this, "Password required", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(confirmPassword)){
            Toast.makeText(this, "Confirm password required.", Toast.LENGTH_SHORT).show();
        }else{
            loadingBar.setTitle("Create account");
            loadingBar.setMessage("Please wait while credentials will be checked.");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            validatePhoneNumber(name, phone, password, confirmPassword);
        }
    }

    private void validatePhoneNumber(final String name, final String phone, final String password, final String confirmPassword) {
            final DatabaseReference databaseReference;
            databaseReference = FirebaseDatabase.getInstance().getReference();

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (!(dataSnapshot.child("Users").child(phone).exists())){
                        HashMap<String, Object> userDataMap = new HashMap<>();
                        userDataMap.put("phone", phone);
                        userDataMap.put("name", name);
                        userDataMap.put("password", password);
                        userDataMap.put("confirmPassword", confirmPassword);

                        databaseReference.child("Users").child(phone).updateChildren(userDataMap)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()){
                                            Toast.makeText(RegisterActivity.this, "Account has been created.", Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();

                                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                            startActivity(intent);
                                        }else{
                                            loadingBar.dismiss();
                                            Toast.makeText(RegisterActivity.this, "Something went wrong.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }else{
                        Toast.makeText(RegisterActivity.this, "This number " + phone + " exist.", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                        Toast.makeText(RegisterActivity.this, "Please try again with another number.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
    }
}
