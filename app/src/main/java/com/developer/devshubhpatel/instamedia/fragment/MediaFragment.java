package com.developer.devshubhpatel.instamedia.fragment;

import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.developer.devshubhpatel.instamedia.InitClass;
import com.developer.devshubhpatel.instamedia.InstagramMediaAdapter;
import com.developer.devshubhpatel.instamedia.IntroActivity;
import com.developer.devshubhpatel.instamedia.R;
import com.developer.devshubhpatel.instamedia.models.FMedia;
import com.developer.devshubhpatel.instamedia.utils.Constant;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.developer.devshubhpatel.instamedia.InitClass.mDatabaseRef;
import static com.developer.devshubhpatel.instamedia.InitClass.mFirebaseRemoteConfig;
import static com.developer.devshubhpatel.instamedia.InitClass.performClipBoardCheck;
import static com.developer.devshubhpatel.instamedia.MainActivity.cbm;


public class MediaFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private RecyclerView recyclerView;
    private InstagramMediaAdapter adapter;
    private List<FMedia> mediaList;
    SwipeRefreshLayout swiperefresh;
    ValueEventListener valueEventListener;

    public MediaFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    View rootview;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootview = inflater.inflate(R.layout.fragment_recyclerview, container, false);
        recyclerView = (RecyclerView) rootview.findViewById(R.id.recyclerView);
        mediaList = new ArrayList<>();
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        swiperefresh = (SwipeRefreshLayout) rootview.findViewById((R.id.swiperefresh));
        swiperefresh.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent, R.color.colorPrimaryDark);
        swiperefresh.setOnRefreshListener(this);
        adapter = new InstagramMediaAdapter(getActivity(),getActivity().getApplicationContext(), mediaList);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mediaList.clear();
                try {
                    for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                        mediaList.add(dsp.getValue(FMedia.class));
                    }
                } catch (Exception e){
                    Log.e("DATA CHANGE",e.getMessage());
                    Snackbar.make(rootview,"RENDER ERROR : "+e.getMessage(),Snackbar.LENGTH_SHORT).show();
                } finally {
                    Collections.reverse(mediaList);
                    int tmp = mediaList.size();
                    if(mFirebaseRemoteConfig.getBoolean("show_top_native_ad")){
                        if(mediaList.size() > 0)
                            mediaList.add(0,new FMedia(2));
                    }
                    if(mFirebaseRemoteConfig.getBoolean("show_list_native_ads")) {
                        int freq = (int) mFirebaseRemoteConfig.getDouble("ads_list_freq");
                        for (int i = 1; i <= tmp; i++) {
                            if (i % freq == 0)
                                mediaList.add(i, new FMedia(2));
                        }
                        if (tmp % freq != 0)
                            mediaList.add(new FMedia(2));
                    }
                    adapter.notifyDataSetChanged();
                    if (dataSnapshot.getChildrenCount() == 0) {
                        Snackbar
                                .make(recyclerView, "Copy \"Shared URL\" from Instagram post to download.", Snackbar.LENGTH_INDEFINITE)
                                .setAction("HELP ME", new View.OnClickListener() {
                                    @Override
                                    public void onClick(final View view) {
                                        startActivity(new Intent(getActivity(), IntroActivity.class));
                                    }
                                }).show();
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("DATA CHANGE ON CANCEL",databaseError.getMessage());
            }
        };


        if(mDatabaseRef != null){
            mDatabaseRef.orderByChild("timeStamp").limitToLast(20).addValueEventListener(valueEventListener);
            mDatabaseRef.keepSynced(true);
        }

        return rootview;
    }


    @Override
    public void onResume() {
        mediaFetch();
        super.onResume();
    }


    public void mediaFetch() {
        performClipBoardCheck(cbm);



    }

    @Override
    public void onRefresh() {
        if (InitClass.ShowInternetStatus(rootview))
            mediaFetch();
        swiperefresh.setRefreshing(false);
    }
}
