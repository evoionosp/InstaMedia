package com.developer.devshubhpatel.instamedia;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.developer.devshubhpatel.instamedia.utils.Constant;
import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.github.paolorotolo.appintro.ISlideBackgroundColorHolder;
import com.pixplicity.easyprefs.library.Prefs;
import android.Manifest;

import static com.developer.devshubhpatel.instamedia.InitClass.mAuth;

/**
 * Created by patel on 23-04-2017.
 */

public class IntroActivity extends AppIntro implements ISlideBackgroundColorHolder {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setFadeAnimation();

        askForPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 5);


        addSlide(AppIntroFragment.newInstance(getResources().getString(R.string.app_name), getString(R.string.intro_0_msg),R.drawable.app_logo_large, getResources().getColor(R.color.colorPrimary)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_step_1), getString(R.string.intro_1_msg),R.drawable.ss_1, getResources().getColor(R.color.colorAccent)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_step_2), getString(R.string.intro_2_msg),R.drawable.ss_2, getResources().getColor(R.color.colorPrimary)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_step_3), getString(R.string.intro_3_msg),R.drawable.ss_3, getResources().getColor(R.color.colorAccent)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_step_5), getString(R.string.intro_5_msg),R.drawable.ss_5, getResources().getColor(R.color.colorPrimary)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_step_4), getString(R.string.intro_4_msg),R.drawable.ss_4, getResources().getColor(R.color.colorAccent)));

        // Hide Skip/Done button.
       /* showSkipButton(false);
        setProgressButtonEnabled(false);
*/
        // Turn vibration on and set intensity.
        // NOTE: you will probably need to ask VIBRATE permission in Manifest.
        setVibrate(true);
        setVibrateIntensity(30);
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        navigateAhead();
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        navigateAhead();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        // Do something when the slide changes.
    }

    @Override
    public int getDefaultBackgroundColor() {
        // Return the default background color of the slide.
        return Color.parseColor("#000000");
    }

    @Override
    public void setBackgroundColor(@ColorInt int backgroundColor) {
        // Set the background color of the view within your slide to which the transition should be applied.

    }

    public void navigateAhead(){
        Prefs.putBoolean(Constant.FIRST_RUN, false);
            if(mAuth != null){
                if(mAuth.getCurrentUser() != null){
                    finish();
                }else{
                    startActivity(new Intent(IntroActivity.this, SignInActivity.class));
                    finish();
                }
            }else{
                startActivity(new Intent(IntroActivity.this, SignInActivity.class));
                finish();
            }
    }

}
