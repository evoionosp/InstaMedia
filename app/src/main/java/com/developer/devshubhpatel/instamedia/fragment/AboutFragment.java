package com.developer.devshubhpatel.instamedia.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.developer.devshubhpatel.instamedia.R;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.developer.devshubhpatel.instamedia.InitClass.ShowInternetStatus;
import static com.developer.devshubhpatel.instamedia.InitClass.mFirebaseRemoteConfig;


/**
 * Created by SHUBH PATEL on 12/31/2015.
 */
public class AboutFragment extends Fragment {


    CircleImageView shubhImg, sidImg;
    CardView cardShubh, cardSid;
    Button btnFollow, btnShareApp;
    public AboutFragment() {
        // Required empty public constructor
    }



    View rootview;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootview = inflater.inflate(R.layout.fragment_about, container, false);

        shubhImg = (CircleImageView) rootview.findViewById(R.id.shubh_dp);
        sidImg = (CircleImageView) rootview.findViewById(R.id.sid_dp);
        cardShubh = (CardView) rootview.findViewById(R.id.card_shubh);
        cardSid = (CardView) rootview.findViewById(R.id.card_sid);
        btnFollow = (Button) rootview.findViewById(R.id.btn_followme);
        btnShareApp = (Button) rootview.findViewById(R.id.btn_share_app);

        if(mFirebaseRemoteConfig.getBoolean("SID_SHOW"))
            cardSid.setVisibility(View.VISIBLE);
        else
            cardSid.setVisibility(View.GONE);



        Picasso.with(getActivity().getApplicationContext())
                .load(mFirebaseRemoteConfig.getString("MY_DP_URL"))
                .placeholder(R.drawable.ic_account_circle_black_24dp)
                .into(shubhImg);

        Picasso.with(getActivity().getApplicationContext())
                .load(mFirebaseRemoteConfig.getString("SID_DP_URL"))
                .placeholder(R.drawable.ic_account_circle_black_24dp)
                .into(sidImg);

        cardShubh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ShowInternetStatus(rootview)) {
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:pshubh96@yahoo.com"));
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Insta Media");
                    emailIntent.putExtra(Intent.EXTRA_TEXT, "Sent from InstaMedia App");
                    startActivity(Intent.createChooser(emailIntent, "Send Email to Developer"));
                }
            }
        });

        cardSid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ShowInternetStatus(rootview)) {
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:siddh.nayak@gmail.com"));
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Insta Media");
                    emailIntent.putExtra(Intent.EXTRA_TEXT, "Sent from InstaMedia App");
                    startActivity(Intent.createChooser(emailIntent, "Send Email to Developer"));
                }
            }
        });

        btnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ShowInternetStatus(rootview)) {
                    String scheme = "http://instagram.com/_u/imshubhpatel";
                    String path = "https://instagram.com/imshubhpatel";
                    String nomPackageInfo = "com.instagram.android";
                    Intent intent;
                    try {
                        getActivity().getPackageManager().getPackageInfo(nomPackageInfo, 0);
                        intent = new Intent(Intent.ACTION_VIEW, Uri.parse(scheme));
                    } catch (Exception e) {
                        intent = new Intent(Intent.ACTION_VIEW, Uri.parse(path));
                    }
                    startActivity(intent);
                }
            }
        });

        btnShareApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_SUBJECT, "IMedia For Instagram");
                    String sAux = "\nOften download photos,videos or profile pictures ? Try this app\n\n";
                    sAux = sAux + "https://play.google.com/store/apps/details?id=com.developer.devshubhpatel.instamedia \n\n";
                    i.putExtra(Intent.EXTRA_TEXT, sAux);
                    startActivity(Intent.createChooser(i, "Share with"));
                } catch(Exception e) {
                    Log.e("SHARE APP",e.toString());
                }
            }
        });

        return rootview;

    }
}
