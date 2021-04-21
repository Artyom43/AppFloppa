package com.example.floppa;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class SharedPreferencesHelper {
    public static final String SHARED_PREF_NAME = "SHARED_PREF_NAME";
    public static final String PHOTOS_KEY = "PHOTOS_KEY";
    public static final String CURRENT_PHOTO_KEY = "CURRENT_PHOTO_KEY";
    public static final Type CURRENT_PHOTO_TYPE = new TypeToken<String>() {}.getType();
    public static final Type PHOTOS_TYPE = new TypeToken<List<String>>() {}.getType();
    private SharedPreferences mSharedPreferences;
    private Gson mGson = new Gson();

    public SharedPreferencesHelper(Context context) {
        mSharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }

    public List<String> getPhotoUriList() {
        List<String> photos = mGson.fromJson(mSharedPreferences.getString(PHOTOS_KEY, ""), PHOTOS_TYPE);
        return photos == null ? new ArrayList<>() : photos;
    }

    public boolean addPhoto(String photo) {
        List<String> photos = getPhotoUriList();
        photos.add(photo);
        mSharedPreferences.edit().putString(PHOTOS_KEY, mGson.toJson(photos, PHOTOS_TYPE)).apply();
        return true;
    }

    public String getCurrentPhoto() {
        return mGson.fromJson(mSharedPreferences
                .getString(CURRENT_PHOTO_KEY, ""), CURRENT_PHOTO_TYPE);
    }

    public void setCurrentPhoto(int position) {
        String uri = getPhotoUriList().get(position);
        mSharedPreferences.edit()
                .putString(CURRENT_PHOTO_KEY, mGson.toJson(uri, CURRENT_PHOTO_TYPE)).apply();
    }

    public void deletePhoto(String photoUri) {
        List<String> photoList = getPhotoUriList();
        for (String photo : photoList) {
            if(photo.equals(photoUri)) {
                photoList.remove(photo);
                break;
            }
        }
        mSharedPreferences.edit()
                .putString(PHOTOS_KEY, mGson.toJson(photoList, PHOTOS_TYPE)).apply();
    }

    public void clear() {
        mSharedPreferences.edit().clear().apply();
    }

}
