package com.developer.devshubhpatel.instamedia;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import com.developer.devshubhpatel.instamedia.models.FMedia;
import com.developer.devshubhpatel.instamedia.utils.DownloadService;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.android.gms.ads.formats.NativeAdView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.pixplicity.easyprefs.library.Prefs;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.ContentValues.TAG;
import static com.developer.devshubhpatel.instamedia.InitClass.mDatabaseRef;
import static com.developer.devshubhpatel.instamedia.InitClass.performClipBoardCheck;
import static com.developer.devshubhpatel.instamedia.MainActivity.cbm;
import static com.developer.devshubhpatel.instamedia.MainActivity.checkPermission;
import static com.developer.devshubhpatel.instamedia.MainActivity.showSnackbar;
import static com.developer.devshubhpatel.instamedia.MainActivity.showToast;
import static com.developer.devshubhpatel.instamedia.utils.Constant.SAVE_DIR;
import static com.developer.devshubhpatel.instamedia.utils.Constant.TEMP_DIR;


/**
 * Created by patel on 06-07-2016.
 */
public class InstagramMediaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private List<FMedia> medialist;
    private Activity mActivity;
    private static final int PERMISSION_REQUEST_CODE = 1;
    public boolean isShare = false;
    public File tmp;

    public class MyViewHolder extends ViewHolder {
        public TextView profileTitle;
        public ImageView mainImage, btnDownload, btnShare, btnDelete;
        public CircleImageView profileIcon;
        public ImageButton btnPlayVideo;
        public ProgressBar progressBar;

        public MyViewHolder(View view) {
            super(view);
            profileTitle = (TextView) view.findViewById(R.id.user_title);
            profileIcon = (CircleImageView) view.findViewById(R.id.user_icon);
            mainImage = (ImageView) view.findViewById(R.id.insta_image);
            btnDelete = (ImageView) view.findViewById(R.id.btn_delete);
            btnDownload = (ImageView) view.findViewById(R.id.btn_download);
            btnShare = (ImageView) view.findViewById(R.id.btn_share);
            btnPlayVideo = (ImageButton) view.findViewById(R.id.btn_video_play);
            progressBar = (ProgressBar) view.findViewById(R.id.media_progress);

        }
    }

    public static class ViewHolderAdMob extends ViewHolder {

        public NativeExpressAdView mAdView;

        public ViewHolderAdMob(View view) {
            super(view);
            mAdView = (NativeExpressAdView) view.findViewById(R.id.adView);
            mAdView.loadAd(new AdRequest.Builder().build());
        }
    }


    public InstagramMediaAdapter(Activity activity, Context mContext, List<FMedia> medialist) {
        this.mContext = mContext;
        this.medialist = medialist;
        this.mActivity = activity;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case 1: {
                View v = inflater.inflate(R.layout.media_cardview, parent, false);
                viewHolder = new MyViewHolder(v);
                break;
            }
            case 2: {
                View v = inflater.inflate(R.layout.ad_cardview, parent, false);
                viewHolder = new ViewHolderAdMob(v);
                break;
            }
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder mholder, int position) {
        final FMedia media = medialist.get(position);

        switch (mholder.getItemViewType()) {
            case 1: {
                final MyViewHolder holder = (MyViewHolder) mholder;
                // registerReceiver();

                try {

                    holder.profileTitle.setText(media.getAuthorId());
                    Picasso.with(mContext)
                            .load(media.getDisplayURL())
                            .placeholder(R.drawable.image_placeholder)
                            .into(holder.mainImage, new Callback() {
                                @Override
                                public void onSuccess() {
                                    holder.progressBar.setVisibility(View.GONE);
                                    if (!media.isVideo()) {
                                        checkPermission(mActivity);
                                        holder.btnShare.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                showToast(mContext,mContext.getString(R.string.please_wait), Toast.LENGTH_SHORT);
                                                Drawable mDrawable = holder.mainImage.getDrawable();
                                                Bitmap mBitmap = ((BitmapDrawable) mDrawable).getBitmap();
                                                shareImage(mBitmap);
                                            }
                                        });

                                        holder.btnDownload.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                File file = new File(new File(Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_PICTURES + "/InstaMedia"), media.getShortCode() + ".jpg");
                                                if (!file.exists()) {
                                                    checkPermission(mActivity);
                                                    Drawable mDrawable = holder.mainImage.getDrawable();
                                                    Bitmap mBitmap = ((BitmapDrawable) mDrawable).getBitmap();
                                                    file = storeImage(true, media.getShortCode(), mBitmap);
                                                    if (file != null) {
                                                        if (file.exists()) {
                                                            showSnackbar(view,mContext.getString(R.string.download_successful), Snackbar.LENGTH_SHORT);
                                                            galleryRefresh(mActivity, file);
                                                        } else
                                                            showSnackbar(view,mContext.getString(R.string.download_failed), Snackbar.LENGTH_SHORT);
                                                    } else
                                                        showSnackbar(view,mContext.getString(R.string.download_failed), Snackbar.LENGTH_SHORT);
                                                } else {
                                                    showSnackbar(view,mContext.getString(R.string.image_already_download), Snackbar.LENGTH_SHORT);
                                                }

                                            }
                                        });


                                    } else {

                                        holder.btnPlayVideo.setVisibility(View.VISIBLE);

                                        holder.btnPlayVideo.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {

                                                File file = new File(SAVE_DIR, media.getShortCode() + ".mp4");

                                                Intent intent = new Intent(mActivity, PlayerActivity.class);

                                                if (file.exists()) {
                                                    intent.putExtra("videoFile", file.getAbsoluteFile().toString());
                                                }
                                                intent.putExtra("videoURL", media.getDownloadURL());
                                                mActivity.startActivity(intent);
                                            }
                                        });

                                        holder.btnShare.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                shareVideo(media);
                                            }
                                        });

                                        holder.btnDownload.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                File file = new File(SAVE_DIR, media.getShortCode() + ".mp4");
                                                if (!file.exists()) {
                                                    showToast(mContext,mContext.getString(R.string.downloading), Toast.LENGTH_SHORT);
                                                    startVideoDownload(media.getShortCode() + ".mp4", media.getDownloadURL());
                                                } else {
                                                    new MaterialDialog.Builder(mActivity)
                                                            .title(R.string.confirm)
                                                            .content(R.string.video_already_download)
                                                            .positiveText(R.string.download)
                                                            .positiveColor(mContext.getResources().getColor(R.color.colorRed))
                                                            .negativeText(R.string.cancel)
                                                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                                @Override
                                                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                                    startVideoDownload(media.getShortCode() + ".mp4", media.getDownloadURL());
                                                                }
                                                            })
                                                            .show();
                                                }
                                                // checkPermission(mActivity);

                                            }
                                        });

                                    }

                                }

                                @Override
                                public void onError() {
                                    holder.progressBar.setVisibility(View.GONE);
                                    Log.e("Media Adapter", "Media from URL load Error");
                                }
                            });

                    Picasso.with(mContext)
                            .load(media.getProfileURL())
                            .placeholder(R.drawable.ic_account_circle_black_24dp)
                            .into(holder.profileIcon, new Callback() {
                                @Override
                                public void onSuccess() {
                                    holder.profileIcon.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            checkPermission(mActivity);
                                            final MaterialDialog md = new MaterialDialog.Builder(mActivity)
                                                    .customView(R.layout.profile_dialog, false)
                                                    .build();
                                            md.show();
                                           // View mdView = md.getContentView();
                                            ImageView imageView = (ImageView) md.findViewById(R.id.profile_image);
                                            final ImageButton btnProfileDownload = (ImageButton) md.findViewById(R.id.btn_profile_download);
                                            ImageButton btnProfileInfo= (ImageButton) md.findViewById(R.id.btn_profile_info);
                                            final ProgressBar profile_prog = (ProgressBar) md.findViewById(R.id.profile_progress);
                                            btnProfileDownload.setEnabled(false);

                                            btnProfileInfo.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    openInstaUser(media.getAuthorId());
                                                    md.dismiss();
                                                }
                                            });


                                            Picasso.with(mContext)
                                                    .load(media.getProfileURL())
                                                    .placeholder(R.drawable.ic_account_circle_black_24dp)
                                                    .into(imageView, new Callback() {
                                                        @Override
                                                        public void onSuccess() {
                                                            profile_prog.setVisibility(View.GONE);

                                                            btnProfileDownload.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View view) {

                                                                    md.dismiss();
                                                                }
                                                            });
                                                            btnProfileDownload.setEnabled(true);
                                                        }

                                                        @Override
                                                        public void onError() {
                                                            profile_prog.setVisibility(View.GONE);
                                                            Log.e(TAG,"ERROR IMAGE LOAD PROFILE DIALOG");

                                                        }
                                                    });

                                        }
                                    });
                                }

                                @Override
                                public void onError() {
                                    Log.e("Media Adapter", "Picasso Profile from URL load Error");
                                }
                            });


                    holder.profileTitle.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            openInstaUser(media.getAuthorId());
                        }
                    });

                    holder.btnDelete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (Prefs.getBoolean("dont_ask_delete", false))
                                deleteMedia(media.getShortCode());
                            else
                                new MaterialDialog.Builder(mActivity)
                                        .title(R.string.confirm)
                                        .content(R.string.delete_media_confirm_msg)
                                        .positiveText(R.string.delete)
                                        .positiveColor(mContext.getResources().getColor(R.color.colorRed))
                                        .negativeText(R.string.cancel)
                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                Prefs.putBoolean("dont_ask_delete", dialog.isPromptCheckBoxChecked());
                                                deleteMedia(media.getShortCode());
                                            }
                                        })
                                        .checkBoxPromptRes(R.string.dont_ask_again, false, null)
                                        .show();
                        }
                    });


                } catch (Exception e) {
                    Log.e("MEDIA ADAPTER", e.getMessage());
                }

                break;
            }
            case 2: {
                break;
            }
            case 3: {
                break;
            }
        }


    }

    @Override
    public int getItemViewType(int position) {
        return medialist.get(position).getViewType();
    }

    @Override
    public int getItemCount() {
        return medialist.size();
    }


    private File storeImage(boolean b, String fineName, Bitmap image) {

        File pictureFile = getOutputMediaFile(b, fineName);
        if (pictureFile == null) {
            Log.d(TAG,
                    "Error creating media file, check storage permissions: ");// e.getMessage());
            return null;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
        return pictureFile;
    }

    private File getOutputMediaFile(boolean isReal, String fileName) {

        if (Environment.getExternalStorageState() != null) {
            File mediaStorageDir;
            if (isReal)
                mediaStorageDir = SAVE_DIR;
            else
                mediaStorageDir = TEMP_DIR;
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.e("FILE SAVE", "Directory Path doesn't exists !");
                    return null;
                }
            }
            String mImageName = fileName + ".jpg";
            return new File(mediaStorageDir.getPath() + File.separator + mImageName);
        } else {
            Log.e("FILE SAVE", "External Storage Unmounted !");
            return null;
        }
    }

    private void openInstaUser(String userName) {
        String scheme = "http://instagram.com/_u/" + userName;
        String path = "https://instagram.com/" + userName;
        String nomPackageInfo = "com.instagram.android";
        Intent intent;
        try {
            mActivity.getPackageManager().getPackageInfo(nomPackageInfo, 0);
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(scheme));
        } catch (Exception e) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(path));
        }
        mActivity.startActivity(intent);
    }

    public void deleteMedia(final String shortcode) {
        if (shortcode != null) {
            mDatabaseRef.child(shortcode).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if (task.isSuccessful()) {
                        showSnackbar(,"Media Deleted", Snackbar.LENGTH_SHORT);
                        if(performClipBoardCheck(MainActivity.cbm) == shortcode)
                        {
                            cbm.setPrimaryClip(null);
                            Log.d("DELETE MEDIA","Cleared Clipboard on Delete Media");
                        }

                    }
                    else
                        showSnackbar(mContext,"Delete Failed ! Try Again.", Snackbar.LENGTH_LONG);

                }
            });
            return;
        }
        Log.e("DELETE MEDIA", "Shortcode Null");
        showSnackbar(,"Delete Failed ! Try Again.", Snackbar.LENGTH_LONG);
    }

    private void galleryRefresh(Activity mActivity, File file) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            final Uri contentUri = Uri.fromFile(file);
            scanIntent.setData(contentUri);
            mActivity.sendBroadcast(scanIntent);
        } else {
            final Intent intent = new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory()));
            mActivity.sendBroadcast(intent);
        }
    }



    private void startVideoDownload(String fileName, String fileURL) {
        Intent intent = new Intent(mActivity, DownloadService.class);
        intent.putExtra("fileName", fileName);
        intent.putExtra("fileURL", fileURL);
        intent.putExtra("notify", true);
        mActivity.startService(intent);
        //TODO: video downloader code
    }

    private void shareVideo(final FMedia m) {
        File file = new File(SAVE_DIR, m.getShortCode() + ".mp4");
        if (file.exists()) {
            Uri uri = Uri.parse("file://" + file.getAbsolutePath());
            if (uri != null) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.setType("video/mp4");
                mActivity.startActivity(Intent.createChooser(shareIntent, "Share Video"));
            } else {
                showSnackbar(mContext.getString(R.string.share_failed), Snackbar.LENGTH_SHORT);
            }
        } else {
            //share link dialog
            new MaterialDialog.Builder(mActivity)
                    .title(R.string.confirm)
                    .content(R.string.download_share_media_confirm_msg)
                    .positiveText(R.string.download)
                    .negativeText(R.string.share_link)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            startVideoDownload(m.getShortCode() + ".mp4",m.getDownloadURL());
                            shareVideo(m);
                        }
                    })
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            Intent i = new Intent(Intent.ACTION_SEND);
                            i.setType("text/plain");
                            i.putExtra(Intent.EXTRA_SUBJECT, "Shared from IMedia");
                            i.putExtra(Intent.EXTRA_TEXT, m.getDownloadURL());
                            mActivity.startActivity(Intent.createChooser(i, "Share URL"));
                        }
                    })
                    .show();
        }
    }

    private void shareImage(Bitmap mBitmap) {
        File file = storeImage(false, "tmp_file", mBitmap);
        if (file != null) {
            galleryRefresh(mActivity, file);
            Uri uri = Uri.parse("file://" + file.getAbsolutePath());
            if (uri != null) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.setType("image/*");
                mActivity.startActivity(Intent.createChooser(shareIntent, "Share Image"));
            } else {
                showSnackbar(mContext.getString(R.string.share_failed), Snackbar.LENGTH_SHORT);
            }
        } else {
            Log.e("SHARE FILE", "Null File");
            showSnackbar(mContext.getString(R.string.share_failed), Snackbar.LENGTH_SHORT);
        }
    }
}



  /*  private void registerReceiver(){

        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(mActivity);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MESSAGE_PROGRESS);
        bManager.registerReceiver(broadcastReceiver, intentFilter);

    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @SuppressLint("DefaultLocale")
        @Override
        public void onReceive(Context context, Intent
                intent) {
            if(intent.getAction().equals(MESSAGE_PROGRESS)){

                Download download = intent.getParcelableExtra("download");
                MaterialDialog dialog = new MaterialDialog.Builder(mActivity)
                    .progress(false, 100, false)
                    .title(R.string.downloading)
                        .cancelable(false)
                        .canceledOnTouchOutside(false)
                    .content(String.format("%d/%d MB Completed",download.getCurrentFileSize(),download.getTotalFileSize()))
                    .show();
                dialog.setProgress(download.getProgress());
                if(download.getProgress() == 100){

                    dialog.setContent("File Download Complete");
                    dialog.dismiss();
                    if(isShare){
                        isShare = false;
                        if(tmp.exists()){
                            Uri uri = Uri.parse("file://"+tmp.getAbsolutePath());
                            if (uri != null) {
                                Intent shareIntent = new Intent();
                                shareIntent.setAction(Intent.ACTION_SEND);
                                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                                shareIntent.setType("video/mp4");
                                mActivity.startActivity(Intent.createChooser(shareIntent, "Share Video"));
                            } else {
                                showSnackbar(mContext.getString(R.string.share_failed),Snackbar.LENGTH_SHORT);
                            }
                            tmp = null;
                        }
                    }

                } else {
                    dialog.setContent(String.format("Downloading... %d/%d MB",download.getCurrentFileSize(),download.getTotalFileSize()));
                }
            }
        }
    }; */

