package com.example.samuilmihaylov.eatorthrow.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.samuilmihaylov.eatorthrow.R;
import com.example.samuilmihaylov.eatorthrow.models.Product;

import java.util.ArrayList;

public class ProductsRecycleViewAdapter extends RecyclerView.Adapter<ProductsRecycleViewAdapter.ViewHolder> {

    private ArrayList<Product> products;

    public ProductsRecycleViewAdapter(ArrayList<Product> products) {
        this.products = products;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ProductsRecycleViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                                    int viewType) {
        // create a new view
        CardView view = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.product_item, parent, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Product product = this.products.get(position);

        holder.mProductName.setText(product.getProductName());
        holder.mProductCategory.setText(product.getProductCategory());
        holder.mProductPurchaseDate.setText(product.getPurchaseDate());
        holder.mProductExpiryDate.setText(product.getExpiryDate());
        holder.mProductAdditionalNote.setText(product.getAdditionalNote());
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

        ViewHolder(CardView cardView) {
            super(cardView);
            mProductName = cardView.findViewById(R.id.product_name_text_value);
            mProductCategory = cardView.findViewById(R.id.product_category_text_value);
            mProductPurchaseDate = cardView.findViewById(R.id.product_purchase_date_text_value);
            mProductExpiryDate = cardView.findViewById(R.id.product_expiry_date_text_value);
            mProductAdditionalNote = cardView.findViewById(R.id.product_additional_note_text_value);
        }
    }
}