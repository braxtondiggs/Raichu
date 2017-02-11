package com.cymbit.raichu;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.preference.PreferenceManager;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.cymbit.raichu.model.Favorites;
import com.cymbit.raichu.model.Listing;
import com.joanzapata.iconify.widget.IconButton;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.varunest.sparkbutton.SparkButton;
import com.varunest.sparkbutton.SparkEventListener;

import org.parceler.Parcels;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import br.com.goncalves.pugnotification.interfaces.ImageLoader;
import br.com.goncalves.pugnotification.interfaces.OnImageLoadingCompleted;
import br.com.goncalves.pugnotification.notification.PugNotification;
import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Notification.*;

public class ImageViewActivity extends AppCompatActivity implements ImageLoader {
    private Bitmap image;
    @BindView(R.id.imageView)
    ImageView mImage;
    @BindView(R.id.image_title)
    TextView mTitle;
    @BindView(R.id.image_author)
    TextView mAuthor;
    @BindView(R.id.activity_image)
    LinearLayout mBackground;
    @BindView(R.id.saveImage)
    LinearLayout mSave;
    @BindView(R.id.favoriteImage)
    LinearLayout mFavorite;
    @BindView(R.id.favoriteButton)
    SparkButton mFavoriteButton;
    @BindView(R.id.setImage)
    LinearLayout mSet;
    @BindView(R.id.imageSize)
    TextView mSize;
    @BindView(R.id.sizeLayout)
    LinearLayout mSizeLayout;
    @BindView(R.id.imageDimensions)
    TextView mDimensions;
    @BindView(R.id.dimensionsLayout)
    LinearLayout mDimensionsLayout;
    @BindView(R.id.imageBack)
    IconButton mBack;
    @BindView(R.id.subInfo)
    TextView mSubInfo;
    @BindView(R.id.domainInfo)
    TextView mDomainInfo;
    @BindView(R.id.scoreInfo)
    TextView mScoreInfo;
    @BindView(R.id.commentInfo)
    TextView mCommentInfo;
    @BindView(R.id.dateCreated)
    TextView mDateLayout;
    List<Favorites> favorites;
    private Target viewTarget;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true);
        }
        ButterKnife.bind(this);
        String parcel = (getIntent().hasExtra("listing")) ? "listing" : "favorite";
        final Listing listing = Parcels.unwrap(getIntent().getParcelableExtra(parcel));
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        favorites = Favorites.listAll(Favorites.class);
        mBackground.setVisibility(View.GONE);
        mTitle.setText(listing.getTitle());
        mAuthor.setText(listing.getAuthor());
        mScoreInfo.setText(NumberFormat.getNumberInstance(Locale.US).format(listing.getScore()));
        mCommentInfo.setText(NumberFormat.getNumberInstance(Locale.US).format(listing.getComments()));
        mSubInfo.setText(fromHtml("<a href=\"http://www.reddit.com" + listing.getSubLink() + "\">" + listing.getSubLink() + "</a>"));
        mSubInfo.setMovementMethod(LinkMovementMethod.getInstance());
        mDomainInfo.setText(fromHtml("<a href=\"http://www.reddit.com" + listing.getLink() + "\">Reddit Link</a>"));
        mDomainInfo.setMovementMethod(LinkMovementMethod.getInstance());
        mDateLayout.setText(listing.getFormattedCreatedDate());
        mBackground.setBackgroundColor(ContextCompat.getColor(this, R.color.midnightBlue));
        for (int i = 0; i < favorites.size(); i++) {
            Favorites favorite = favorites.get(i);
            if (favorite.getID().equals(listing.getID())) {
                mFavoriteButton.setChecked(true);
            }
        }
        Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                assert mImage != null;
                image = bitmap;
                if (image != null) {
                    mBackground.setVisibility(View.VISIBLE);
                    mImage.setImageBitmap(bitmap);
                    if (bitmap.getWidth() > 0 && bitmap.getHeight() > 0) {
                        mSize.setText(Integer.toString(bitmap.getWidth()) + " X " + Integer.toString(bitmap.getHeight()));
                        mSizeLayout.setVisibility(View.VISIBLE);
                    }
                    if (bitmap.getByteCount() > 0) {
                        mDimensions.setText(humanReadableByteCount(bitmap.getByteCount(), true));
                        mDimensionsLayout.setVisibility(View.VISIBLE);
                    }
                    Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                        @Override
                        public void onGenerated(Palette palette) {
                            Palette.Swatch textSwatch = palette.getVibrantSwatch();
                            if (textSwatch == null) {
                                return;
                            }
                            mBackground.setBackgroundColor(textSwatch.getRgb());
                        }
                    });
                } else {
                    bitmapError();
                }
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                bitmapError();
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };
        Picasso.with(ImageViewActivity.this).load((parcel.equals("listing")) ? listing.getImageUrl() : listing.getSource()).into(target);
        mImage.setTag(target);
        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setNavigationBarTintEnabled(true);
        tintManager.setStatusBarTintResource(R.color.statusbar_bg);
        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isStoragePermissionGranted()) {
                    File folder = new File(Environment.getExternalStorageDirectory().getPath() + "/Pictures/Wallpapers/");
                    try {
                        boolean isDirectoryCreated = folder.mkdirs();
                        File file = new File(folder, listing.getID() + ".png");
                        FileOutputStream oStream = new FileOutputStream(file);
                        image.compress(Bitmap.CompressFormat.PNG, 100, oStream);
                        oStream.flush();
                        oStream.close();
                        Snackbar snackbar = Snackbar.make(mBackground, view.getResources().getString(R.string.successful), Snackbar.LENGTH_SHORT);
                        snackbar.show();

                    } catch (IOException e) {
                        Log.e("IOException", e.getLocalizedMessage());
                    }
                }
            }
        });
        mFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFavoriteButton.performClick();
            }
        });
        mFavoriteButton.setEventListener(new SparkEventListener() {
            @Override
            public void onEvent(ImageView button, boolean buttonState) {
                String status = (buttonState) ? getResources().getString(R.string.favorite_add) : getResources().getString(R.string.favorite_remove);
                Snackbar snackbar = Snackbar.make(mBackground, status, Snackbar.LENGTH_SHORT);
                if (buttonState) {
                    Favorites favorite = new Favorites(listing);
                    favorite.save();
                } else {
                    for (int i = 0; i < favorites.size(); i++) {
                        List<Favorites> favorite = Favorites.find(Favorites.class, "identifier = ?", listing.getID());
                        if (!favorite.isEmpty()) {
                            favorite.get(0).delete();
                        }
                    }
                }
                snackbar.show();
            }
        });
        mSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WallpaperManager WP = WallpaperManager.getInstance(ImageViewActivity.this);
                try {
                    WP.setBitmap(image);
                    Snackbar snackbar = Snackbar.make(view, view.getResources().getString(R.string.wallpaper_set), Snackbar.LENGTH_SHORT);
                    snackbar.show();
                    Boolean notification = preferences.getBoolean("perform_alert", false);
                    if (notification) {
                        Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
                        notificationIntent.setData(Uri.parse(listing.getLink()));
                        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);

                        PugNotification.with(getApplicationContext())
                                .load()
                                .title(listing.getTitle())
                                .message("New wallpaper")
                                .when(System.currentTimeMillis())
                                .largeIcon(image)
                                .smallIcon(R.drawable.pugnotification_ic_launcher)
                                .autoCancel(true)
                                .flags(DEFAULT_ALL)
                                .button(R.drawable.pugnotification_ic_launcher, view.getResources().getString(R.string.artwork_info), pi)
                                .custom()
                                .setImageLoader(ImageViewActivity.this)
                                .background(listing.getImageUrl())
                                .build();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mSave.performClick();
        }
    }

    @TargetApi(19)
    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    private static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format(Locale.ENGLISH, "%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else {
            return true;
        }
    }

    private void bitmapError() {
        new MaterialDialog.Builder(this)
                .title(R.string.bitmap_error_title)
                .content(R.string.bitmap_error_content)
                .positiveText(R.string.back).onAny(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                finish();
            }
        })
                .show();
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html) {
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html);
        }
        return result;
    }

    @Override
    public void load(String uri, OnImageLoadingCompleted onCompleted) {
        viewTarget = getViewTarget(onCompleted);
        Picasso.with(this).load(uri).into(viewTarget);
    }

    @Override
    public void load(int imageResId, OnImageLoadingCompleted onCompleted) {
        viewTarget = getViewTarget(onCompleted);
        Picasso.with(this).load(imageResId).into(viewTarget);
    }

    private static Target getViewTarget(final OnImageLoadingCompleted onCompleted) {
        return new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                onCompleted.imageLoadingCompleted(bitmap);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };
    }
}
