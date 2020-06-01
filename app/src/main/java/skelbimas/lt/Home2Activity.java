package skelbimas.lt;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import skelbimas.lt.category.ChatActivity;
import skelbimas.lt.category.PhonesCategory;

public class Home2Activity extends AppCompatActivity {
    private LinearLayout btnPhones;
    private LinearLayout contactUs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home2);

        btnPhones = findViewById(R.id.btnPhones);
        contactUs = findViewById(R.id.btn_contactUs);

        btnPhones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Home2Activity.this, PhonesCategory.class);
                startActivity(intent);
            }
        });

        contactUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home2Activity.this, ChatActivity.class);
                startActivity(intent);
            }
        });
    }
}
