package com.example.floppa;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class GalleryFragment extends Fragment {

    private static final int REQUEST_CODE_GET_PHOTO = 111;
    public static final int WRITE_PERMISSION_RQ = 123;
    private RecyclerView mPhotoList;
    private CardsAdapter mCardsAdapter;
    private ImageView mAddPhoto;
    private ImageView mDelete;
    private BottomSheetDialog mBottomSheetDialog;
    private int mPhotoNumber;
    private SharedPreferencesHelper mSharedPreferencesHelper;


    private View.OnClickListener mOnDeleteClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mSharedPreferencesHelper.deletePhoto(mSharedPreferencesHelper.getCurrentPhoto());
            mCardsAdapter.changeLongClicked(false);
            mDelete.setVisibility(View.GONE);
            mAddPhoto.setVisibility(View.VISIBLE);
            --mPhotoNumber;

            File file = new File(mSharedPreferencesHelper.getCurrentPhoto());
            if(file.exists()) {
                file.delete();
            }

            GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 3);
            mPhotoList.setLayoutManager(layoutManager);
            mPhotoList.setHasFixedSize(true);  // если знаем заранее размер списка

            mCardsAdapter = new CardsAdapter(mPhotoNumber);
            mPhotoList.setAdapter(mCardsAdapter);
        }
    };


    public static GalleryFragment newInstance() {
        Bundle args = new Bundle();

        GalleryFragment fragment = new GalleryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)  {
        View v = inflater.inflate(R.layout.fr_gallery, container, false);
        mSharedPreferencesHelper = new SharedPreferencesHelper(getActivity());
        mPhotoNumber = mSharedPreferencesHelper.getPhotoUriList().size();

        if (mPhotoNumber == 0) {
            CardView helloText = v.findViewById(R.id.cv_hello);
            helloText.setVisibility(View.VISIBLE);
        }

        mBottomSheetDialog =
                new BottomSheetDialog(getActivity(), R.style.BottomSheetDialogTheme);
        mPhotoList = v.findViewById(R.id.rv_cards);
        mAddPhoto = v.findViewById(R.id.iv_add);
        mDelete = v.findViewById(R.id.iv_delete);

        mAddPhoto.setVisibility(View.VISIBLE);
        mAddPhoto.setOnClickListener(mOnAddClickListener);
        mDelete.setOnClickListener(mOnDeleteClickListener);

        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 3);
        mPhotoList.setLayoutManager(layoutManager);
        mPhotoList.setHasFixedSize(true);  // если знаем заранее размер списка

        mCardsAdapter = new CardsAdapter(mPhotoNumber);
        mPhotoList.setAdapter(mCardsAdapter);
        return v;
    }

    private View.OnClickListener mOnAddClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            View bottomSheetView = LayoutInflater.from(getActivity().getApplicationContext())
                    .inflate(
                            R.layout.layout_bottom_sheet,
                            v.findViewById(R.id.bottom_sheet_container)
                    );
            bottomSheetView.findViewById(R.id.tv_add_local).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mBottomSheetDialog.cancel();
                    if (isWritePermissionGranted()) {
                        openGallery();
                    } else {
                        requestWritePermission();
                    }
                }
            });

            bottomSheetView.findViewById(R.id.tv_add_web).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mBottomSheetDialog.cancel();
                    getFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragmentContainer, WebDownloadFragment.newInstance())
                            .addToBackStack(WebDownloadFragment.class.getName())
                            .commit();
                }
            });

            bottomSheetView.findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mBottomSheetDialog.cancel();
                }
            });

            mBottomSheetDialog.setContentView(bottomSheetView);
            mBottomSheetDialog.show();
        }
    };

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, REQUEST_CODE_GET_PHOTO);
    }

    private void requestWritePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            new AlertDialog
                    .Builder(getActivity())
                    .setMessage("Без разрешения на взаимодействие с памятью у приложения не получится хранить фотографии Флопп")
                    .setPositiveButton("Понятно", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION_RQ);
                        }
                    })
                    .show();
        } else {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION_RQ);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != WRITE_PERMISSION_RQ) return;
        if (grantResults.length != 1) return;
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            mBottomSheetDialog.cancel();
            new AlertDialog
                    .Builder(getActivity())
                    .setMessage("Вы можете дать разрешение в настройках устройства")
                    .setPositiveButton("Понятно", null)
                    .show();
        }
    }

    private boolean isWritePermissionGranted() {
        return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_GET_PHOTO
                && resultCode == Activity.RESULT_OK
                && data != null) {
            Uri photoUri = data.getData();
            try {
                Bitmap profilePic = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), photoUri);
                File appDir = new File(getActivity().getApplicationContext().getExternalFilesDir("FloppaApp").getAbsolutePath());
                if (!appDir.exists()) {
                    appDir.mkdirs();
                }
                String fileName = new RandomStringGenerator().generateRandomString(20) + ".jpg";
                File file = new File(appDir, fileName);
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    profilePic.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String filePath = file.getPath();
                mSharedPreferencesHelper.addPhoto(filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 3);
            mPhotoList.setLayoutManager(layoutManager);
            mPhotoList.setHasFixedSize(true);  // если знаем заранее размер списка
            mPhotoNumber = mSharedPreferencesHelper.getPhotoUriList().size();
            mCardsAdapter = new CardsAdapter(mPhotoNumber);
            mPhotoList.setAdapter(mCardsAdapter) ;
            CardView helloText = getView().findViewById(R.id.cv_hello);
            helloText.setVisibility(View.GONE);
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}

