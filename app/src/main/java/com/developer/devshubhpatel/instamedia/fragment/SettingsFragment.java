package com.developer.devshubhpatel.instamedia.fragment;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;
import androidx.appcompat.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;


import com.developer.devshubhpatel.instamedia.IntroActivity;
import com.developer.devshubhpatel.instamedia.R;
import com.developer.devshubhpatel.instamedia.SignInActivity;
import com.developer.devshubhpatel.instamedia.utils.ClipboardService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.pixplicity.easyprefs.library.Prefs;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.developer.devshubhpatel.instamedia.InitClass.ShowInternetStatus;
import static com.developer.devshubhpatel.instamedia.InitClass.mAuth;
import static com.developer.devshubhpatel.instamedia.InitClass.mDatabaseRef;

import static com.developer.devshubhpatel.instamedia.MainActivity.showSnackbar;
import static com.developer.devshubhpatel.instamedia.utils.Constant.SHOW_NOTIFICATION;


public class SettingsFragment extends Fragment {



    SwitchCompat switchService, switchNotification;
    CircleImageView userImg;
    TextView userName, userEmail, userInst;
    Button btnClearAll,btnOpenInsta,btnTutorial;
    CardView cardUser;
    LinearLayout llService, llNotification;


    public SettingsFragment() {
        // Required empty public constructor
    }
    View rootview;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootview = inflater.inflate(R.layout.fragment_settings, container, false);

        switchService = (SwitchCompat) rootview.findViewById(R.id.switch_service);
        switchNotification = (SwitchCompat) rootview.findViewById(R.id.switch_notification);
        userImg = (CircleImageView) rootview.findViewById(R.id.userImage);
        userName = (TextView) rootview.findViewById(R.id.userName);
        userEmail = (TextView) rootview.findViewById(R.id.userEmail);
        userInst = (TextView) rootview.findViewById(R.id.userInst);
        btnClearAll = (Button) rootview.findViewById(R.id.btn_clearAll);
        btnOpenInsta = (Button) rootview.findViewById(R.id.btn_openInsta);
        btnTutorial = (Button) rootview.findViewById(R.id.btn_tutorial);
        cardUser = (CardView) rootview.findViewById(R.id.card_user);
        llService = (LinearLayout) rootview.findViewById(R.id.ll_service);
        llNotification = (LinearLayout) rootview.findViewById(R.id.ll_notification);

        switchNotification.setChecked(Prefs.getBoolean(SHOW_NOTIFICATION, true));
        switchNotification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Intent i = new Intent(getActivity(), ClipboardService.class);
                if(isChecked) {
                    Prefs.putBoolean(SHOW_NOTIFICATION,true);
                } else {
                    Prefs.putBoolean(SHOW_NOTIFICATION,false);
                }
                switchService.performClick();
                switchService.performClick();
            }
        });



        switchService.setChecked(isMyServiceRunning(ClipboardService.class));
       if(!isMyServiceRunning(ClipboardService.class))llNotification.setVisibility(View.GONE);
        switchService.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Intent i = new Intent(getActivity(), ClipboardService.class);
                if(isChecked) {
                   getActivity().startService(i);
                    llNotification.setVisibility(View.VISIBLE);
                } else {
                    getActivity().stopService(i);
                    llNotification.setVisibility(View.GONE);
                }
            }
        });



        if(mAuth.getCurrentUser() != null){
            if(!mAuth.getCurrentUser().isAnonymous()){
                FirebaseUser user = mAuth.getCurrentUser();
                    String displayName = user.getDisplayName();
                    Uri profileUri = user.getPhotoUrl();
                    for (UserInfo userInfo : user.getProviderData()) {
                        if (displayName == null && userInfo.getDisplayName() != null) {
                            displayName = userInfo.getDisplayName();
                        }
                        if (profileUri == null && userInfo.getPhotoUrl() != null) {
                            profileUri = userInfo.getPhotoUrl();
                        }
                    }
                userName.setText(displayName);
                userEmail.setText(mAuth.getCurrentUser().getEmail());
                Picasso.with(getActivity().getApplicationContext())
                        .load(profileUri)
                        .placeholder(R.drawable.ic_account_circle_black_24dp)
                        .into(userImg);
                userInst.setText(R.string.click_to_signout);
            }
        }

        btnClearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ShowInternetStatus(rootview)) {
                    new MaterialDialog.Builder(getActivity())
                            .title(R.string.clear_all)
                            .content(R.string.clear_all_cnf_msg)
                            .positiveText(R.string.clear_all)
                            .positiveColor(getActivity().getResources().getColor(R.color.colorRed))
                            .negativeText(R.string.cancel)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    final MaterialDialog md = new MaterialDialog.Builder(getActivity())
                                            .title(R.string.please_wait)
                                            .content(R.string.deleting_data)
                                            .canceledOnTouchOutside(false)
                                            .cancelable(false)
                                            .progress(true, 0)
                                            .build();
                                    md.show();
                                    mDatabaseRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful())
                                                Snackbar.make(rootview, R.string.data_delete_success, Snackbar.LENGTH_LONG).show();
                                            else
                                                Snackbar.make(rootview, R.string.error_something_wrong, Snackbar.LENGTH_LONG).show();
                                            md.dismiss();
                                        }
                                    });
                                }
                            })
                            .show();
                }
            }
        });

        cardUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ShowInternetStatus(rootview)) {
                    if (mAuth.getCurrentUser() != null && !mAuth.getCurrentUser().isAnonymous()) {
                        new MaterialDialog.Builder(getActivity())
                                .title(R.string.sign_out)
                                .content(R.string.sign_out_cnf_msg)
                                .positiveText(R.string.sign_out)
                                .positiveColor(getActivity().getResources().getColor(R.color.colorRed))
                                .negativeText(R.string.cancel)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        mAuth.signOut();
                                    }
                                })
                                .show();
                    } else {
                        startActivity(new Intent(getActivity(), SignInActivity.class));
                        getActivity().finish();
                    }
                }
            }
        });

        llService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchService.performClick();
            }
        });

        llNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchNotification.performClick();
            }
        });

        btnOpenInsta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ShowInternetStatus(rootview)) {
                    Intent launchIntent = getActivity().getPackageManager().getLaunchIntentForPackage("com.instagram.android");
                    if (launchIntent != null) {
                        startActivity(launchIntent);//null pointer check in case package name was not found
                    } else {
                        showSnackbar("Instagram not Installed on Device", Snackbar.LENGTH_SHORT);
                    }
                }
            }
        });
        btnTutorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), IntroActivity.class));
            }
        });
        return rootview;
    }



    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}