package com.example.floppa;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

public class WebDownloadFragment extends Fragment {
    private static final int WRITE_PERMISSION_RQ = 123;
    private ImageView mBack;
    private TextInputEditText mInput;
    private ImageView mPhoto;
    private String mPhotoUrl;
    private MaterialButton mLocalDownload;
    private MaterialButton mStorageDownload;
    private SharedPreferencesHelper mSharedPreferences;

    private View.OnClickListener mOnBackClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, GalleryFragment.newInstance())
                    .commit();
        }
    };

    private View.OnClickListener mOnLocalDownloadClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mPhotoUrl = mInput.getText().toString();
            if (mPhotoUrl.equals("")) {
                Toast.makeText(getActivity(), getString(R.string.empty_input), Toast.LENGTH_SHORT)
                        .show();
            } else {
                Picasso.get()
                        .load(mPhotoUrl)
                        .into(mPhoto, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                mPhoto.setVisibility(View.VISIBLE);
                                mLocalDownload.setVisibility(View.GONE);
                                mStorageDownload.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onError(Exception e) {
                                Toast.makeText(getActivity(), "Что-то не так с сылкой", Toast.LENGTH_SHORT)
                                        .show();
                            }
                        });
                hideKeyboardFrom(getContext(), getView());
            }
        }
    };

    public static void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private View.OnClickListener mOnStorageDownloadClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isWritePermissionGranted()) {
                new DownloadImageTask(WebDownloadFragment.this)
                        .execute(mPhotoUrl);
            } else {
                requestWritePermission();
            }
        }
    };

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
            new DownloadImageTask(WebDownloadFragment.this)
                    .execute(mPhotoUrl);
        } else {
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

    public static WebDownloadFragment newInstance() {
        Bundle args = new Bundle();

        WebDownloadFragment fragment = new WebDownloadFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fr_webdownloader, container, false);
        mSharedPreferences = new SharedPreferencesHelper(getContext());

        mBack = v.findViewById(R.id.iv_back);
        mBack.setVisibility(View.VISIBLE);
        mBack.setOnClickListener(mOnBackClickListener);

        mInput = v.findViewById(R.id.et_input);
        mPhoto = v.findViewById(R.id.iv_single_photo);

        mLocalDownload = v.findViewById(R.id.btn_download_local);
        mLocalDownload.setOnClickListener(mOnLocalDownloadClickListener);
        mStorageDownload = v.findViewById(R.id.btn_download_storage);
        mStorageDownload.setOnClickListener(mOnStorageDownloadClickListener);
        return v;
    }



    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<WebDownloadFragment> mFragmentWeakReference;

        private DownloadImageTask(WebDownloadFragment fragment) {
            mFragmentWeakReference = new WeakReference<>(fragment);
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                return getBitmap(strings[0]);
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap image) {
            WebDownloadFragment fragment = mFragmentWeakReference.get();

            if (fragment != null) {
                File appDir = new File(getActivity()
                        .getApplicationContext()
                        .getExternalFilesDir("FloppaApp")
                        .getAbsolutePath());
                if (!appDir.exists()) {
                    appDir.mkdirs();
                }
                String fileName = new RandomStringGenerator().generateRandomString(20) + ".jpg";
                File file = new File(appDir, fileName);
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    image.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mSharedPreferences.addPhoto(file.getAbsolutePath());
                Toast.makeText(fragment.getContext(), "Успешно!", Toast.LENGTH_SHORT)
                        .show();
            }
        }

        private Bitmap getBitmap(String url) throws IOException {
            return Picasso.get().load(url).get();
        }
    }
}
