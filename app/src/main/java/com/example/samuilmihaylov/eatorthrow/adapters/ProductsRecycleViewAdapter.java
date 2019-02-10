package com.example.samuilmihaylov.eatorthrow.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.samuilmihaylov.eatorthrow.R;
import com.example.samuilmihaylov.eatorthrow.activities.AddProductActivity;
import com.example.samuilmihaylov.eatorthrow.models.Product;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Objects;

import androidx.annotation.RequiresApi;

public class ProductsRecycleViewAdapter extends RecyclerView.Adapter<ProductsRecycleViewAdapter.ViewHolder> {

    private static final int REQUEST_FOR_ACTIVITY_CODE = 9;

    private final FirebaseDatabase database;
    private final FirebaseAuth auth;
    private ArrayList<Product> products;
    private FirebaseStorage storage;
    private Context mContext;

    public ProductsRecycleViewAdapter(ArrayList<Product> products) {
        this.products = products;
        this.storage = FirebaseStorage.getInstance();
        this.database = FirebaseDatabase.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ProductsRecycleViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                                    int viewType) {
        mContext = parent.getContext();

        // create a new view
        CardView view = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.product_item, parent, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        final Product product = this.products.get(position);

        holder.mProductName.setText(product.getProductName());
        holder.mProductCategory.setText(product.getProductCategory());
        holder.mProductPurchaseDate.setText(product.getPurchaseDate());
        holder.mProductExpiryDate.setText(product.getExpiryDate());
        holder.mProductAdditionalNote.setText(product.getAdditionalNote());

        final DatabaseReference databaseRef = this.database.getReference("products").child(Objects.requireNonNull(this.auth.getUid()));
        final StorageReference storageRef = storage.getReference();

        if (product.getProductImageUrl() != null) {
            storageRef.child(product.getProductImageUrl()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    String imageURL = uri.toString();
                    Glide.with(holder.mProductImage.getContext()).load(imageURL).into(holder.mProductImage);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.e("Product Image load", "Could not load the product image from firebase storage");
                }
            });
        }

        holder.mEditProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, AddProductActivity.class);
                intent.putExtra("product", product);
                mContext.startActivity(intent);
            }
        });

        holder.mDeleteProductBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {
                databaseRef.child(product.getId()).setValue(null, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if (product.getProductImageUrl() != null) {
                            storageRef.child(product.getProductImageUrl()).delete()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d("Product Image delete", "Successfully deleted");
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    Log.e("Product Image delete", "Could not delete the image");
                                }
                            });
                        }
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView mProductName;
        private final TextView mProductCategory;
        private final TextView mProductPurchaseDate;
        private final TextView mProductExpiryDate;
        private final TextView mProductAdditionalNote;
        private final ImageView mProductImage;
        private final Button mDeleteProductBtn;
        private final Button mEditProductBtn;

        ViewHolder(CardView cardView) {
            super(cardView);
            mProductName = cardView.findViewById(R.id.product_name_text_value);
            mProductCategory = cardView.findViewById(R.id.product_category_text_value);
            mProductPurchaseDate = cardView.findViewById(R.id.product_purchase_date_text_value);
            mProductExpiryDate = cardView.findViewById(R.id.product_expiry_date_text_value);
            mProductAdditionalNote = cardView.findViewById(R.id.product_additional_note_text_value);
            mProductImage = cardView.findViewById(R.id.product_photo_image_id);
            mDeleteProductBtn = cardView.findViewById(R.id.delete_btn_id);
            mEditProductBtn = cardView.findViewById(R.id.edit_btn_id);
        }
    }
}