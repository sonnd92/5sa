package fiveship.vn.fiveship.utility;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import fiveship.vn.fiveship.R;

/**
 * Created by BVN-PC on 24/02/2016.
 */
public class ImageProcess{
    public static String mFilePath;
    public static void capturePictureProduct(final Context context, String title) {
        final CharSequence[] items = {context.getString(R.string.take_a_photo), context.getString(R.string.get_from_gallery), context.getString(R.string.exit)};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals(context.getString(R.string.take_a_photo))) {
                    dispatchTakePictureIntent(context);
                } else if (items[item].equals(context.getString(R.string.get_from_gallery))) {
                    if(ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        Intent intent = new Intent(
                                Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("image/*");
                        ((Activity) context).startActivityForResult(
                                Intent.createChooser(intent, context.getString(R.string.choose_file)),
                                Utils.SELECT_FILE);
                    }else{
                        ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                Utils.EACCES_PERMISSION_REQUEST_CODE);
                    }
                } else if (items[item].equals(context.getString(R.string.exit))) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private static void dispatchTakePictureIntent(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = createImageFile(context);
                mFilePath = "";
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    mFilePath = photoFile.getAbsolutePath();
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(photoFile));
                    ((Activity) context).startActivityForResult(takePictureIntent, Utils.REQUEST_IMAGE_CAPTURE);
                }
            }
        } else {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    Utils.CAMERA_PERMISSION_REQUEST_CODE);
        }
    }

    private static File createImageFile(Context context) {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = null;
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                image = File.createTempFile(
                        imageFileName,
                        ".jpg",
                        storageDir
                );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    public static Bitmap decodeSampledBitmapFromResource(String file, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(file, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeResizeImage(int maxSize) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap myImg;
        Bitmap finalImage;

        int scale = 1;

        if (options.outHeight > maxSize || options.outWidth > maxSize) {
            scale = (int) Math.pow(2, (int) Math.round(Math.log(maxSize / (double) Math.max(options.outHeight, options.outWidth)) / Math.log(0.5)));
        }

        options.inSampleSize = scale;

        // Calculate inSampleSize
        //options.inSampleSize = Math.min(options.outWidth / reqWidth, options.outHeight / reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        try {
            myImg = BitmapFactory.decodeFile(mFilePath, options);
            finalImage = Bitmap.createBitmap(myImg, 0, 0, myImg.getWidth(), myImg.getHeight(), null, true);
        } catch(Exception e) {
            options.inSampleSize = 2;
            myImg = BitmapFactory.decodeFile(mFilePath, options);
            finalImage = Bitmap.createBitmap(myImg, 0, 0, myImg.getWidth(), myImg.getHeight(), null, true);
            e.printStackTrace();
        }

        return finalImage;
    }

    public static String bitmapToBase64(Bitmap bitmap, int quality) {

        try {

            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bao);
            byte[] ba = bao.toByteArray();
            //bitmap.recycle();

            return Base64.encodeToString(ba, Base64.DEFAULT);

        } catch (Exception ex) {

            if (quality < 25) {
                return "";
            }

            return bitmapToBase64(bitmap, quality / 2);
        }
    }

}
