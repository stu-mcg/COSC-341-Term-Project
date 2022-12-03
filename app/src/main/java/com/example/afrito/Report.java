package com.example.afrito;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.FileUtils;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.TextView;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Report implements Parcelable {
    public static final int POI = 0;
    public static final int ENV = 1;
    public static final int HZD = 2;
    public static final int INF = 3;

    private String title;
    private String desc;
    private int type;
    private double [] latLng;
    private String[] imgs;
    private Boolean userCreated;

    public Report(String title, String desc, int type, double[] latLng, Bitmap[] imgs, Context context, Boolean userCreated) {
        this.title = title;
        this.desc = desc;
        this.type = type;
        this.latLng = latLng;
        this.userCreated = userCreated;
        this.imgs = new String[imgs.length];

        for(int i = 0; i < imgs.length; i++) {
            this.imgs[i] = saveToInternalStorage(imgs[i], context);
        }
    }

    private String saveToInternalStorage(Bitmap bitmapImage, Context context){
        ContextWrapper cw = new ContextWrapper(context.getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File path=new File(directory,getUniqueFileName());

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 50, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return path.getAbsolutePath();
    }

    private Bitmap loadImageFromStorage(String path)
    {
        try {
            File f=new File(path);
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            return b;
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public String getUniqueFileName(){
        Date dNow = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("yyyyMMddHHmmssSS");
        String datetime = ft.format(dNow);
        try
        {
            Thread.sleep(1);
        }catch(Exception e)
        {

        }
        return datetime + ".bmp";
    }

    protected Report(Parcel in) {
        title = in.readString();
        desc = in.readString();
        type = in.readInt();
        latLng = in.createDoubleArray();
        imgs = in.createStringArray();
        userCreated = in.readInt() == 1 ? true : false;
    }

    public static final Creator<Report> CREATOR = new Creator<Report>() {
        @Override
        public Report createFromParcel(Parcel in) {
            return new Report(in);
        }

        @Override
        public Report[] newArray(int size) {
            return new Report[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public String getDesc() {
        return desc;
    }

    public int getType() {
        return type;
    }

    public double[] getLatLng() {
        return latLng;
    }

    public Boolean getUserCreated() {
        return userCreated;
    }

    public Bitmap[] getImgs() {
        Bitmap[] imgsOut = new Bitmap[imgs.length];
        for(int i = 0; i < imgs.length; i++){
            imgsOut[i] = loadImageFromStorage(imgs[i]);
        }
        return imgsOut;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(desc);
        parcel.writeInt(type);
        parcel.writeDoubleArray(latLng);
        parcel.writeStringArray(imgs);
        parcel.writeInt(userCreated ? 1 : 0);
    }

    public static void clearImages(Context context){
        File dir = context.getDir("imageDir", Context.MODE_PRIVATE);
        for (File file: dir.listFiles()) {
            if (!file.isDirectory())
                file.delete();
        }
    }
}
