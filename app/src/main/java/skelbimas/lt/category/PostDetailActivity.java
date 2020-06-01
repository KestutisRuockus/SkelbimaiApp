package skelbimas.lt.category;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import skelbimas.lt.R;

public class PostDetailActivity extends AppCompatActivity {

    TextView mTittleTv, mDetailTv;
    ImageView mImageTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        // action bar
        ActionBar actionBar = getSupportActionBar();
        // action tiitle
        actionBar.setTitle("Post Details");
        // set back button in action bar
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        // initialized views
        mTittleTv = findViewById(R.id.titleTv);
        mDetailTv = findViewById(R.id.descriptionTv);
        mImageTv = findViewById(R.id.imageView);

        // get data from intent
        String image = getIntent().getStringExtra("image");
        String title = getIntent().getStringExtra("title");
        String description = getIntent().getStringExtra("description");

        // set data to views
        mTittleTv.setText(title);
        mDetailTv.setText(description);
        Picasso.get().load(image).into(mImageTv);
    }

    // handle on back pressed - go to previous activity
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
