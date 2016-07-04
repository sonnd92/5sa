package fiveship.vn.fiveship.activity;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.utility.Utils;

public class GalleryItemFragment extends Fragment{

    private String src;

    public static synchronized GalleryItemFragment newInstance(String src) {

        GalleryItemFragment instance = new GalleryItemFragment();
        //Bundle args = new Bundle();
        //args.putString(PRODUCT_TYPE, typeProducts);
        //instance.setArguments(args);
        instance.src = src;
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.gallery_picture_item, container, false);

        Utils.setImageToImageView(getActivity(), src, (ImageView)view.findViewById(R.id.gallery_img));

        return view;
    }
}
