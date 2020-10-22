package com.fxn.utility;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import com.fxn.modals.Img;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by akshay on 02/13/20.
 */

public class ImageVideoFetcher extends AsyncTask<Cursor, Void, ImageVideoFetcher.ModelList> {

    public int startingCount = 0;
    public String header = "";
    private final ArrayList<Img> selectionList = new ArrayList<>();
    private final ArrayList<Img> LIST = new ArrayList<>();
    private ArrayList<String> preSelectedUrls = new ArrayList<>();
    @SuppressLint("StaticFieldLeak")
    private final Context context;

    public ImageVideoFetcher(Context context) {
        this.context = context;
    }

    public int getStartingCount() {
        return startingCount;
    }

    public void setStartingCount(int startingCount) {
        this.startingCount = startingCount;
    }

    public ArrayList<String> getPreSelectedUrls() {
        return preSelectedUrls;
    }

    public ImageVideoFetcher setPreSelectedUrls(ArrayList<String> preSelectedUrls) {
        this.preSelectedUrls = preSelectedUrls;
        return this;
    }

    @Override
    protected ModelList doInBackground(Cursor... cursors) {
        Cursor cursor = cursors[0];
        try {
            if (cursor != null) {
                int data = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
                int mediaType = cursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE);
                int contentUrl = cursor.getColumnIndex(MediaStore.Files.FileColumns._ID);

                //int videoDate = cursor.getColumnIndex(MediaStore.Video.Media.DATE_TAKEN);
                int imageDate = cursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED);

                int limit = 100;
                if (cursor.getCount() < limit) {
                    limit = cursor.getCount() - 1;
                }
                cursor.move(limit);
                synchronized (context) {
                    int pos = getStartingCount();
                    for (int i = limit; i < cursor.getCount(); i++) {
                        cursor.moveToNext();
                        Uri path = Uri.withAppendedPath(Constants.IMAGE_VIDEO_URI, "" + cursor.getInt(contentUrl));
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(cursor.getLong(imageDate) * 1000);
                        String dateDifference = Utility.getDateDifference(context, calendar);
                        int media_type = cursor.getInt(mediaType);
                        if (!header.equalsIgnoreCase("" + dateDifference)) {
                            header = "" + dateDifference;
                            pos += 1;

                            LIST.add(new Img("" + dateDifference, "", "", "", media_type));
                        }
                        Img img = new Img("" + header, "" + path, cursor.getString(data), "" + pos, media_type);
                        img.setPosition(pos);
                        if (preSelectedUrls.contains(img.getUrl())) {
                            img.setSelected(true);
                            selectionList.add(img);
                        }
                        pos += 1;
                        LIST.add(img);
                    }
                    cursor.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ModelList(LIST, selectionList);
    }

    public static class ModelList {
        ArrayList<Img> LIST;
        ArrayList<Img> selection;

        public ModelList(ArrayList<Img> LIST, ArrayList<Img> selection) {
            this.LIST = LIST;
            this.selection = selection;
        }

        public ArrayList<Img> getLIST() {
            return LIST;
        }

        public ArrayList<Img> getSelection() {
            return selection;
        }
    }
}
