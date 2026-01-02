package ir.ninjacoder;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.Toast;
import android.graphics.drawable.BitmapDrawable;
import android.app.WallpaperManager;
import android.util.DisplayMetrics;
import android.graphics.Matrix;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.app.ProgressDialog;
import android.app.AlertDialog;
import android.view.View;

import com.blankj.utilcode.util.ClipboardUtils;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import com.blankj.utilcode.util.ThreadUtils;
import ir.ninjacoder.ghostide.core.activities.BaseCompat;
import ir.ninjacoder.ghostide.core.activities.FileManagerActivity;
import ir.ninjacoder.ghostide.core.activities.CodeEditorActivity;
import io.github.rosemoe.sora.widget.CodeEditor;
import ir.ninjacoder.ghostide.core.activities.SetHomeWallpActivity;
import ir.ninjacoder.ghostide.core.pl.PluginManagerCompat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class HomeWallpaper implements PluginManagerCompat {

  private SetHomeWallpActivity currentActivity;
  private ImageView imageView; // ZoomageView extends ImageView
  private ExtendedFloatingActionButton fab;

  @Override
  public void getEditor(CodeEditor arg0) {}

  @Override
  public String setName() {
    return "HomeWallpaper";
  }

  @Override
  public boolean hasuseing() {
    return true;
  }

  @Override
  public void getFileManagerAc(FileManagerActivity arg0) {}

  @Override
  public void getCodeEditorAc(CodeEditorActivity arg0) {}

  @Override
  public void getBaseCompat(BaseCompat base) {
    if (base instanceof SetHomeWallpActivity) {
      currentActivity = (SetHomeWallpActivity) base;
      new Handler()
          .postDelayed(
              () -> {
                try {

                  // دسترسی مستقیم به فیلدها با reflection
                  Field imageViewField = base.getClass().getDeclaredField("imageView");
                  imageViewField.setAccessible(true);
                  imageView = (android.widget.ImageView) imageViewField.get(base);

                  Field fabField = base.getClass().getDeclaredField("fab");
                  fabField.setAccessible(true);
                  fab = (ExtendedFloatingActionButton) fabField.get(base);
                  fab.postDelayed(
                      () -> {
                        fab.shrink();
                      },
                      100);

                  // حذف listener قبلی و اضافه کردن listener جدید
                  fab.setOnClickListener(
                      v -> {
                        if (currentActivity != null) {
                          _showWallpaperDialog();
                        }
                      });

                  Toast.makeText(base, "Wallpaper plugin loaded!", Toast.LENGTH_SHORT).show();

                } catch (Exception e) {
                  e.printStackTrace();
                  ClipboardUtils.copyText(e.getLocalizedMessage());
                  Toast.makeText(base, "Failed to load wallpaper plugin", Toast.LENGTH_SHORT)
                      .show();
                }
              },
              600);
    }
  }

  @Override
  public String langModel() {
    return "all";
  }

  // متد جدید: گرفتن bitmap از ImageView با حفظ zoom
  private Bitmap getBitmapFromImageView(ImageView imageView) {
    try {
      // روش 1: گرفتن bitmap از drawable فعلی
      Drawable drawable = imageView.getDrawable();
      if (drawable == null) {
        return null;
      }

      // ابتدا bitmap اصلی رو بگیر
      Bitmap originalBitmap;
      if (drawable instanceof BitmapDrawable) {
        originalBitmap = ((BitmapDrawable) drawable).getBitmap();
      } else {
        // برای drawableهای دیگر
        originalBitmap =
            Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(originalBitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
      }

      // الان باید transformهای اعمال شده روی ImageView رو اعمال کنیم
      // این شامل scale، translate و rotation میشه

      // گرفتن matrix فعلی ImageView
      Matrix matrix = imageView.getImageMatrix();

      // ایجاد bitmap جدید با سایز view
      Bitmap resultBitmap =
          Bitmap.createBitmap(imageView.getWidth(), imageView.getHeight(), Bitmap.Config.ARGB_8888);

      Canvas canvas = new Canvas(resultBitmap);

      // اعمال matrix روی bitmap اصلی
      canvas.drawBitmap(originalBitmap, matrix, new Paint(Paint.FILTER_BITMAP_FLAG));

      return resultBitmap;

    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  private void setWallpaper(int type) {
    try {
      if (imageView == null) {
        Toast.makeText(currentActivity, "Image view not found", Toast.LENGTH_SHORT).show();
        return;
      }

      // استفاده از متد جدید برای گرفتن bitmap با zoom
      Bitmap bitmap = getBitmapFromImageView(imageView);
      if (bitmap == null) {
        Toast.makeText(currentActivity, "Failed to get image", Toast.LENGTH_SHORT).show();
        return;
      }

      WallpaperManager wallpaperManager = WallpaperManager.getInstance(currentActivity);

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        if (type == 0) { // Home
          wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM);
        } else if (type == 1) { // Lock
          wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK);
        } else if (type == 2) { // Both
          wallpaperManager.setBitmap(
              bitmap, null, true, WallpaperManager.FLAG_SYSTEM | WallpaperManager.FLAG_LOCK);
        }
      } else {
        wallpaperManager.setBitmap(bitmap);
      }

      Toast.makeText(currentActivity, "Wallpaper set successfully", Toast.LENGTH_SHORT).show();

    } catch (Exception e) {
      e.printStackTrace();
      Toast.makeText(currentActivity, "Failed to set wallpaper", Toast.LENGTH_SHORT).show();
    }
  }

  public void _showWallpaperDialog() {
    if (currentActivity == null) return;

    String[] items = {"Home Screen", "Lock Screen", "Both"};

    new AlertDialog.Builder(currentActivity)
        .setTitle("Set Wallpaper")
        .setItems(
            items,
            (dialog, which) -> {
              if (which == 0) {
                setAutoFitWallpaper(0);
              } else if (which == 1) {
                setAutoFitWallpaper(1);
              } else {
                setAutoFitWallpaper(2);
              }
            })
        .show();
  }

  private Bitmap getAutoFitBitmap(Bitmap src) {
    if (currentActivity == null) return src;

    DisplayMetrics dm = new DisplayMetrics();
    currentActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);

    int sw = dm.widthPixels;
    int sh = dm.heightPixels;

    int bw = src.getWidth();
    int bh = src.getHeight();

    float scale;
    float dx = 0, dy = 0;

    if (bw * sh > sw * bh) {
      scale = (float) sh / bh;
      dx = (sw - bw * scale) / 2f;
    } else {
      scale = (float) sw / bw;
      dy = (sh - bh * scale) / 2f;
    }

    Matrix matrix = new Matrix();
    matrix.setScale(scale, scale);
    matrix.postTranslate(dx, dy);

    Bitmap out = Bitmap.createBitmap(sw, sh, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(out);
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

    canvas.drawBitmap(src, matrix, paint);
    return out;
  }

  private void setAutoFitWallpaper(int type) {
    if (currentActivity == null || imageView == null) return;

    ProgressDialog pd = new ProgressDialog(currentActivity);
    pd.setMessage("Setting wallpaper...");
    pd.setCancelable(false);
    pd.show();

    new Thread(
            () -> {
              try {
                // استفاده از متد جدید برای گرفتن bitmap با zoom
                Bitmap srcBitmap = getBitmapFromImageView(imageView);
                if (srcBitmap == null) {
                  ThreadUtils.runOnUiThread(
                      () -> {
                        pd.dismiss();
                        Toast.makeText(currentActivity, "No image selected", Toast.LENGTH_SHORT)
                            .show();
                      });
                  return;
                }

                Bitmap finalBitmap = getAutoFitBitmap(srcBitmap);

                WallpaperManager wm = WallpaperManager.getInstance(currentActivity);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                  if (type == 0) {
                    wm.setBitmap(finalBitmap, null, true, WallpaperManager.FLAG_SYSTEM);
                  } else if (type == 1) {
                    wm.setBitmap(finalBitmap, null, true, WallpaperManager.FLAG_LOCK);
                  } else {
                    wm.setBitmap(
                        finalBitmap,
                        null,
                        true,
                        WallpaperManager.FLAG_SYSTEM | WallpaperManager.FLAG_LOCK);
                  }
                } else {
                  wm.setBitmap(finalBitmap);
                }

                ThreadUtils.runOnUiThread(
                    () -> {
                      pd.dismiss();
                      Toast.makeText(
                              currentActivity, "Wallpaper set successfully!", Toast.LENGTH_SHORT)
                          .show();
                    });

              } catch (Exception e) {
                e.printStackTrace();
                ThreadUtils.runOnUiThread(
                    () -> {
                      pd.dismiss();
                      Toast.makeText(currentActivity, "Failed to set wallpaper", Toast.LENGTH_SHORT)
                          .show();
                    });
              }
            })
        .start();
  }
}
