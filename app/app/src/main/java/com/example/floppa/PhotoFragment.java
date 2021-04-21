package com.example.floppa;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.squareup.picasso.Picasso;

import java.io.File;

public class PhotoFragment extends Fragment {
    private ImageView mPhoto;
    private ImageView mBack;
    private MaterialButton mDelete;
    private SharedPreferencesHelper mSharedPreferencesHelper;

    private View.OnClickListener mOnBackClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, GalleryFragment.newInstance())
                    .commit();
        }
    };

    public static PhotoFragment newInstance() {
        Bundle args = new Bundle();

        PhotoFragment fragment = new PhotoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fr_photo, container, false);
        mPhoto = v.findViewById(R.id.iv_single_photo);
        mBack = v.findViewById(R.id.iv_back);
        mBack.setVisibility(View.VISIBLE);
        mBack.setOnClickListener(mOnBackClickListener);

        mDelete = v.findViewById(R.id.btn_delete);
        mDelete.setOnClickListener(mOnDeleteClickListener);

        mSharedPreferencesHelper = new SharedPreferencesHelper(getContext());

        File f = new File(mSharedPreferencesHelper.getCurrentPhoto());
        Picasso.get()
                .load(f)
                .into(mPhoto);

        return v;
    }

    private View.OnClickListener mOnDeleteClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mSharedPreferencesHelper.deletePhoto(mSharedPreferencesHelper.getCurrentPhoto());

            File file = new File(mSharedPreferencesHelper.getCurrentPhoto());
            if (file.exists()) {
                file.delete();
            }
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, GalleryFragment.newInstance())
                    .commit();
        }
    };
}
