package com.example.android.bitcoinpricemonitor;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class TableViewAdapter extends RecyclerView.Adapter {
    List<TransactionModel> transactionList;

    public TableViewAdapter(List<TransactionModel> transactionList) {
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.table_list_item, parent, false);

        return new RowViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        RowViewHolder rowViewHolder = (RowViewHolder) holder;

        int rowPos = rowViewHolder.getAdapterPosition();

        if (rowPos == 0) {
            // Header Cells. Main Headings appear here
            rowViewHolder.txtPrice.setBackgroundResource(R.drawable.table_cell_bg);
            rowViewHolder.txtAmount.setBackgroundResource(R.drawable.table_cell_bg);
            rowViewHolder.txtTotal.setBackgroundResource(R.drawable.table_cell_bg);

            rowViewHolder.txtPrice.setText("Price");
            rowViewHolder.txtAmount.setText("Amount");
            rowViewHolder.txtTotal.setText("Total");
        } else {
            TransactionModel model = transactionList.get(rowPos-1);

            // Content Cells. Content appear here
            rowViewHolder.txtPrice.setBackgroundResource(R.drawable.table_cell_bg);
            rowViewHolder.txtAmount.setBackgroundResource(R.drawable.table_cell_bg);
            rowViewHolder.txtTotal.setBackgroundResource(R.drawable.table_cell_bg);

            rowViewHolder.txtPrice.setText(model.getPrice()+"");
            rowViewHolder.txtAmount.setText(model.getAmount()+"");
            rowViewHolder.txtTotal.setText(model.getTotal()+"");
        }
    }

    @Override
    public int getItemCount() {
        return transactionList.size()+1; // one more to add header row
    }

    public class RowViewHolder extends RecyclerView.ViewHolder {
        protected TextView txtPrice;
        protected TextView txtAmount;
        protected TextView txtTotal;

        public RowViewHolder(View itemView) {
            super(itemView);

            txtPrice = itemView.findViewById(R.id.txtPrice);
            txtAmount = itemView.findViewById(R.id.txtAmount);
            txtTotal = itemView.findViewById(R.id.txtTotal);
        }
    }

    // Clean all elements of the recycler
    public void clear() {
        transactionList.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<TransactionModel> list) {
        transactionList.addAll(list);
        notifyDataSetChanged();
    }
}
