package skelbimas.lt.category;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import skelbimas.lt.AdminAddNewProductActivity;
import skelbimas.lt.R;
import skelbimas.lt.ViewHolder;
import skelbimas.lt.model.Items;

import static com.google.firebase.storage.FirebaseStorage.getInstance;

public class PhonesCategory extends AppCompatActivity {

    LinearLayoutManager mLinearLayoutManager; // for sorting
    SharedPreferences mSharedPreferences; // for saving sorts settings
    RecyclerView mRecyclerView;
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mRef;

    FirebaseRecyclerAdapter<Items, ViewHolder> firebaseRecyclerAdapter;
    FirebaseRecyclerOptions<Items> options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phones_category);

        // Action bar
        ActionBar actionBar = getSupportActionBar();
        // set title
        actionBar.setTitle("Posts List");

        mSharedPreferences = getSharedPreferences("SortSettings", MODE_PRIVATE);
        String mSorting = mSharedPreferences.getString("Sort", "newest"); // where if no settings is selected newest will be default

        // since default value is newest so for first time it will be displayed newest posts

        if (mSorting.equals("newest")){
            mLinearLayoutManager = new LinearLayoutManager(this);
            // this will load the items from bottom. means newest first
            mLinearLayoutManager.setReverseLayout(true);
            mLinearLayoutManager.setStackFromEnd(true);
        }
        else if (mSorting.equals("oldest")){
            mLinearLayoutManager = new LinearLayoutManager(this);
            // this will load the items from bottom. means oldest first
            mLinearLayoutManager.setReverseLayout(false);
            mLinearLayoutManager.setStackFromEnd(false);
        }


        // RecyclerView
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);



        // send querry to firebaseDatabse
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mRef = mFirebaseDatabase.getReference("Data");

        showData();
    }

    // show data
    private void showData(){
        options = new FirebaseRecyclerOptions.Builder<Items>().setQuery(mRef, Items.class).build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Items, ViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Items items) {
                holder.setDetails(getApplicationContext(), items.getTitle(), items.getDescription(), items.getImage());
            }

            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                // Inflaitng layout row xml
                View itemsView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row, parent, false);

                ViewHolder viewHolder = new ViewHolder(itemsView);
                // item click listener
                viewHolder.setOnClickListener(new ViewHolder.ClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        //get data from Firebase at the position clicked
                        String mTitle = getItem(position).getTitle();
                        String mDesc = getItem(position).getDescription();
                        String mImage = getItem(position).getImage();

                        //pass this data to new activity
                        Intent intent = new Intent(view.getContext(), PostDetailActivity.class);
                        intent.putExtra("title", mTitle); // put Title
                        intent.putExtra("description", mDesc); // put Description
                        intent.putExtra("image", mImage); // put Image url
                        startActivity(intent);
                    }

                    @Override
                    public void onItemLongClick(View view, int position) {
                        // get current title
                        final String ctTitle = getItem(position).getTitle();
                        // get current description
                        final String cDescr = getItem(position).getDescription();
                        // get current Image url
                        final String cImage = getItem(position).getImage();

                        // show dialog onLong click
                        AlertDialog.Builder builder = new AlertDialog.Builder(PhonesCategory.this);
                        // options to display in dialog
                        String[] options = {"UPDATE", " DELETE"};
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // handle dialog item clicks
                                if (which ==0){
                                    // update clicked
                                    // start activity with putting current data
                                    Intent intent = new Intent(PhonesCategory.this, AdminAddNewProductActivity.class);
                                    intent.putExtra("cTitle", ctTitle);
                                    intent.putExtra("cDesc", cDescr);
                                    intent.putExtra("cImage", cImage);
                                    startActivity(intent);
                                }
                                if (which ==1){
                                    // delete clicked
                                    // method call
                                    showDeleteDataDialog(ctTitle, cImage);
                                }
                            }
                        });
                        builder.create().show();
                    }
                });

                return viewHolder;
            }
        };

        // set layout as LinearLayout
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        firebaseRecyclerAdapter.startListening();
        // set adapter to firebase recycler view
        mRecyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    private void showDeleteDataDialog(final String currentTitle, final String currentImage) {
        // alert dialog
        final AlertDialog.Builder builder = new AlertDialog.Builder(PhonesCategory.this);
        builder.setTitle("Delete");
        builder.setMessage("Are you sure to delete this post?");
        // set positive/yes button
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // user pressed "Yes", delete data
                Query mQuery = mRef.orderByChild("title").equalTo(currentTitle);
                mQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()){
                            ds.getRef().removeValue(); // remove value from firebase where title matches
                        }
                        // show massage that post(s) deleted
                        Toast.makeText(PhonesCategory.this, "Post deleted successfully...", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // if anything goes wrong get and show error massage
                        Toast.makeText(PhonesCategory.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                // delete image using reference of url from firebaseStorage
                StorageReference mPictureRefe = getInstance().getReferenceFromUrl(currentImage);
                mPictureRefe.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // delete successfully
                        Toast.makeText(PhonesCategory.this, "Image deleted successfully...", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // unable to delete
                        // if anything goes wrong while deleting image, get and show error message
                        Toast.makeText(PhonesCategory.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        // set negative/no button
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                // user pressed "No", just dismiss dialog
                dialog.dismiss();
            }
        });
        // show dialog
        builder.create().show();
    }

    // Search data
    private void firebaseSearch(String searchText){

        // convert String entered in search view to lowercase
        String query = searchText.toLowerCase();

        Query firebaseSearchQuery = mRef.orderByChild("search").startAt(query).endAt(query + "\uf8ff");

        options = new FirebaseRecyclerOptions.Builder<Items>().setQuery(firebaseSearchQuery, Items.class).build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Items, ViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Items items) {
                holder.setDetails(getApplicationContext(), items.getTitle(), items.getDescription(), items.getImage());
            }

            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                // Inflaitng layout row xml
                View itemsView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row, parent, false);

                ViewHolder viewHolder = new ViewHolder(itemsView);
                // item click listener
                viewHolder.setOnClickListener(new ViewHolder.ClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        //get data from Firebase at the position clicked
                        String mTitle = getItem(position).getTitle();
                        String mDesc = getItem(position).getDescription();
                        String mImage = getItem(position).getImage();

                        //pass this data to new activity
                        Intent intent = new Intent(view.getContext(), PostDetailActivity.class);
                        intent.putExtra("title", mTitle); // put Title
                        intent.putExtra("description", mDesc); // put Description
                        intent.putExtra("image", mImage); // put Image url
                        startActivity(intent);
                    }

                    @Override
                    public void onItemLongClick(View view, int position) {
                        // get current title
                        final String ctTitle = getItem(position).getTitle();
                        // get current description
                        final String cDescr = getItem(position).getDescription();
                        // get current Image url
                        final String cImage = getItem(position).getImage();

                        // show dialog onLong click
                        AlertDialog.Builder builder = new AlertDialog.Builder(PhonesCategory.this);
                        // options to display in dialog
                        String[] options = {"UPDATE", " DELETE"};
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // handle dialog item clicks
                                if (which ==0){
                                    // update clicked
                                    // start activity with putting current data
                                    Intent intent = new Intent(PhonesCategory.this, AdminAddNewProductActivity.class);
                                    intent.putExtra("cTitle", ctTitle);
                                    intent.putExtra("cDesc", cDescr);
                                    intent.putExtra("cImage", cImage);
                                    startActivity(intent);
                                }
                                if (which ==1){
                                    // delete clicked
                                    // method call
                                    showDeleteDataDialog(ctTitle, cImage);
                                }
                            }
                        });
                        builder.create().show();
                    }
                });

                return viewHolder;
            }
        };

        // set layout as LinearLayout
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        firebaseRecyclerAdapter.startListening();
        // set adapter to firebase recycler view
        mRecyclerView.setAdapter(firebaseRecyclerAdapter);

    }

    // load data in recyclerView on start
    @Override
    protected void onStart() {
        super.onStart();
        if (firebaseRecyclerAdapter != null){
            firebaseRecyclerAdapter.startListening();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // inflate the menu; this adds items to the action bar if it presents
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                firebaseSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {

                // filter as you type
                firebaseSearch(query);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        // handle other actioBar item clicks here
        if (id == R.id.action_sort){
            // display alert dialog to choose sorting
            showSortDialog();
            return true;
        }
        if (id == R.id.action_add){
            // start add post activity
            startActivity(new Intent(PhonesCategory.this, AdminAddNewProductActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showSortDialog() {
        // option to display in dialog
        String[] sortOptions = {"Newest", "Oldest"};
        // create alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sort by") // set title
                .setIcon(R.drawable.ic_action_sort) // set icon
                .setItems(sortOptions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        // the 'which' argument contains the index position of the selected item
                        // 0 means "Newest", 1 means "Oldest"
                        if (which == 0 ){
                            // sort by newest
                            // edit our shared preferences
                            SharedPreferences.Editor editor = mSharedPreferences.edit();
                            editor.putString("Sort", "newest"); // where 'Sort' is key & 'newest' is value
                            editor.apply(); // apply/save the value in our shared preferences
                            recreate(); // restart activity to take affect
                        }
                        else if ( which == 1){
                            // sort by oldest
                            // edit our shared preferences
                            SharedPreferences.Editor editor = mSharedPreferences.edit();
                            editor.putString("Sort", "oldest"); // where 'Sort' is key & 'oldest' is value
                            editor.apply(); // apply/save the value in our shared preferences
                            recreate(); // restart activity to take affect
                        }
                    }
                });
        builder.show();
    }
}
