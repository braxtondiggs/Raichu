package com.cymbit.raichu;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.WallpaperManager;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ImageViewActivity extends AppCompatActivity {
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true);
        }
        ButterKnife.bind(this);
        final Listing listing = Parcels.unwrap(getIntent().getParcelableExtra("listing"));
        mTitle.setText(listing.getTitle());
        mAuthor.setText(listing.getAuthor());
        mBackground.setBackgroundColor(ContextCompat.getColor(this, R.color.midnightBlue));
        Target target = new Target() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                assert mImage != null;
                image = bitmap;
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
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };
        Picasso.with(ImageViewActivity.this).load(listing.getImageUrl()).into(target);//diskCacheStrategy(DiskCacheStrategy.ALL)
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
                        folder.mkdirs();
                        File file = new File(folder, listing.getID() + ".png");
                        FileOutputStream ostream = new FileOutputStream(file);
                        image.compress(Bitmap.CompressFormat.PNG, 100, ostream);
                        ostream.flush();
                        ostream.close();
                        Snackbar snackbar = Snackbar.make(mBackground, "Saved Successfully!", Snackbar.LENGTH_SHORT);
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
                String status = (buttonState) ? "Removed from Favorites" : "Added to Favorites";
                Snackbar snackbar = Snackbar.make(mBackground, status, Snackbar.LENGTH_SHORT);
                snackbar.show();
            }
        });
        mSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WallpaperManager WP = WallpaperManager.getInstance(ImageViewActivity.this);
                try {
                    WP.setBitmap(image);
                    Snackbar snackbar = Snackbar.make(view, "Wallpaper Set!", Snackbar.LENGTH_SHORT);
                    snackbar.show();
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
}
