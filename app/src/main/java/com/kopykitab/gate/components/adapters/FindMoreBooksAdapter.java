package com.kopykitab.gate.components.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kopykitab.gate.Book;
import com.kopykitab.gate.R;
import com.kopykitab.gate.settings.Utils;

import java.util.List;


public class FindMoreBooksAdapter extends RecyclerView.Adapter<FindMoreBooksAdapter.CustomViewHolder> {

    private Context mContext;
    private List<Book> bookList;
    private MuPdfItemClickListener mItemClickListener;

    public FindMoreBooksAdapter(Context mContext, List<Book> bookList, MuPdfItemClickListener itemClickListener) {
        this.mContext = mContext;
        this.bookList = bookList;
        this.mItemClickListener = itemClickListener;
    }

    @Override
    public void onBindViewHolder(FindMoreBooksAdapter.CustomViewHolder holder, int position, List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public FindMoreBooksAdapter.CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.find_more_list, parent, false);

        return new CustomViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(FindMoreBooksAdapter.CustomViewHolder holder, int position) {

        final Book book = bookList.get(position);

        holder.findMoreItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mItemClickListener.itemClicked(book);
            }
        });


        String bookName = book.getName();
        if (bookName.length() > 30) {
            bookName = bookName.substring(0, 27) + "...";
        }
        holder.name.setText(bookName);
        holder.image.setImageDrawable(null);
        Utils.getImageLoader(mContext).displayImage(book.getImageUrl().replaceAll(" ", "%20"), holder.image);
        String bookPrice1 = book.getPrice_1().replace("INR", "\u20B9");
        String bookPrice2 = book.getPrice_2().replace("INR", "\u20B9");

        if (bookPrice2 != null && !bookPrice2.isEmpty() && !bookPrice2.equals("")) {
            holder.price_1.setText(bookPrice1);
            holder.price_2.setText(bookPrice2);
        } else {
            holder.price_1.setVisibility(View.GONE);
            holder.price_2.setText(bookPrice1);
        }
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {

        public LinearLayout findMoreItem;
        public ImageView image;
        public TextView name, price_1, price_2;

        public CustomViewHolder(View view) {
            super(view);
            findMoreItem = (LinearLayout) view.findViewById(R.id.find_more_item);
            image = (ImageView) view.findViewById(R.id.product_image);
            name = (TextView) view.findViewById(R.id.product_name);
            price_1 = (TextView) view.findViewById(R.id.product_price_1);
            ((com.kopykitab.gate.components.TextView) price_1).setAddStrike(true);
            price_2 = (TextView) view.findViewById(R.id.product_price_2);
        }
    }
}
