package com.example.floppa;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

public class CardsAdapter extends RecyclerView.Adapter<CardsAdapter.CardViewHolder> {
    private static int viewHolderCount;
    private int numberItems;
    private boolean mIsLongClicked = false;
    private int mLongClickedPosition = -1;
    private SharedPreferencesHelper mSharedPreferencesHelper;

    public void changeLongClicked(boolean isLongClicked) {
        mIsLongClicked = isLongClicked;
    }

    public CardsAdapter( int numberOfItems) {
        numberItems = numberOfItems;
        viewHolderCount = 0;
    }

    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.photo_card_item;

        LayoutInflater inflater = LayoutInflater.from(context);
        mSharedPreferencesHelper = new SharedPreferencesHelper(parent.getContext());
        View photo = inflater.inflate(layoutIdForListItem, parent, false);
        CardViewHolder viewHolder = new CardViewHolder(photo);
        viewHolderCount++;
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CardViewHolder holder, int position) {
        holder.bind(position, numberItems);

        holder.PhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mIsLongClicked) {
                    AppCompatActivity activity = (AppCompatActivity) v.getContext();
                    mSharedPreferencesHelper.setCurrentPhoto(position);
                    activity.getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragmentContainer, PhotoFragment.newInstance())
                            .addToBackStack(PhotoFragment.class.getName()).commit();
                }
            }
        });

        holder.PhotoView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!mIsLongClicked) {
                    mIsLongClicked = true;
                    mLongClickedPosition = position;
                    AppCompatActivity activity = (AppCompatActivity) v.getContext();
                    holder.PhotoView.setColorFilter(R.color.colorWhite);
                    ImageView delete = activity.findViewById(R.id.iv_delete);
                    ImageView add = activity.findViewById(R.id.iv_add);
                    mSharedPreferencesHelper.setCurrentPhoto(position);
                    delete.setVisibility(View.VISIBLE);
                    add.setVisibility(View.GONE);
                    return true;
                } else if (mLongClickedPosition == position) {
                    mIsLongClicked = false;
                    mLongClickedPosition = -1;
                    AppCompatActivity activity = (AppCompatActivity) v.getContext();
                    holder.PhotoView.clearColorFilter();
                    ImageView delete = activity.findViewById(R.id.iv_delete);
                    ImageView add = activity.findViewById(R.id.iv_add);
                    delete.setVisibility(View.GONE);
                    add.setVisibility(View.VISIBLE);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return numberItems;
    }


    class CardViewHolder extends RecyclerView.ViewHolder {
        private ImageView PhotoView;
        private final List<String> photos = mSharedPreferencesHelper.getPhotoUriList();

        public CardViewHolder(View itemView) {
            super(itemView);
            PhotoView = itemView.findViewById(R.id.iv_photo_item);
        }

        void bind(int listIndex, int numberItems) {
            File f = new File(photos.get(listIndex));
            Picasso.get()
                    .load(f)
                    .into(PhotoView);
        }
    }
}
