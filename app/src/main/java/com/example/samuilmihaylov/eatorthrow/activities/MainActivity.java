package com.example.samuilmihaylov.eatorthrow.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.example.samuilmihaylov.eatorthrow.R;
import com.example.samuilmihaylov.eatorthrow.adapters.ProductsRecycleViewAdapter;
import com.example.samuilmihaylov.eatorthrow.models.Product;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

import androidx.annotation.RequiresApi;

public class MainActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = findViewById(R.id.action_toolbar);
        setSupportActionBar(myToolbar);

        final RecyclerView mRecyclerView = findViewById(R.id.my_recycler_view);

        mRecyclerView.setHasFixedSize(true);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        FirebaseApp.initializeApp(MainActivity.this);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference("products").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                ArrayList<Product> products = new ArrayList<>();

                for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                    Product product = productSnapshot.getValue(Product.class);
                    products.add(product);
                }

                ProductsRecycleViewAdapter mProductsRecycleViewAdapter = new ProductsRecycleViewAdapter(products);
                mRecyclerView.setAdapter(mProductsRecycleViewAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);
        menu.findItem(R.id.action_save_product).setVisible(false);

        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent intent;

        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;

            case R.id.action_add_new_product:
                intent = new Intent(this, AddProductActivity.class);
                startActivity(intent);
                return true;

            case R.id.log_out:
                GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();

                GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
                FirebaseAuth.getInstance().signOut();
                mGoogleSignInClient.signOut();

                if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                    intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                }

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }
}
