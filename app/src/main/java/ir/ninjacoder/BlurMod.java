package ir.ninjacoder;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.Glide;
import android.util.Log;
import android.widget.ImageView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ir.ninjacoder.codesnap.widget.ghostide.SliderCompat;
import ir.ninjacoder.ghostide.core.activities.BaseCompat;
import ir.ninjacoder.ghostide.core.activities.FileManagerActivity;
import ir.ninjacoder.ghostide.core.activities.CodeEditorActivity;
import io.github.rosemoe.sora.widget.CodeEditor;
import ir.ninjacoder.ghostide.core.glidecompat.GlideCompat;
import ir.ninjacoder.ghostide.core.pl.PluginManagerCompat;
import ir.ninjacoder.ghostide.core.utils.FileUtil;
import ir.ninjacoder.prograsssheet.listchild.ChildIconEditorManager;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.security.MessageDigest;

public class BlurMod implements PluginManagerCompat {
  private ImageView ghosticon;
  public static final String jsonFile =
      "/storage/emulated/0/GhostWebIDE/plugins/blur/object/size.json";
  private String TAG = getClass().getName();
  private CodeEditorActivity currentActivity;

  @Override
  public void getEditor(CodeEditor arg0) {}

  @Override
  public String setName() {
    return getClass().getName();
  }

  @Override
  public boolean hasuseing() {
    return true;
  }

  @Override
  public void getFileManagerAc(FileManagerActivity arg0) {}

  @Override
  public void getCodeEditorAc(CodeEditorActivity base) {
    this.currentActivity = base;
  }

  @Override
  public void getBaseCompat(BaseCompat base) {
    if (base instanceof CodeEditorActivity) {
      CodeEditorActivity activity = (CodeEditorActivity) base;
      this.currentActivity = activity;

      activity
          .getWindow()
          .getDecorView()
          .post(
              new Runnable() {
                @Override
                public void run() {
                  try {
                    Field ghostIconField = CodeEditorActivity.class.getDeclaredField("ghostIcon");
                    ghostIconField.setAccessible(true);
                    ImageView ghostIconView = (ImageView) ghostIconField.get(activity);

                    if (ghostIconView != null && ghostIconView.getContext() != null) {
                      setBlurInWallpaperMobile(activity, ghostIconView);
                      ChildIconEditorManager chid =
                          new ChildIconEditorManager(
                              "/storage/emulated/0/GhostWebIDE/plugins/blur/icon/bluricon.png",
                              (v, pos, id, using) -> {
                                showBlurSettingsDialog(activity, ghostIconView);
                              });
                      activity.addChildManagerEditor(chid);
                    } else {
                      Log.e(TAG, "ghostIcon is null or has no context");
                    }
                  } catch (Exception e) {
                    Log.e(TAG, "Failed to access ghostIcon: " + e.getMessage());
                  }
                }
              });
    }
  }

  @Override
  public String langModel() {
    return "all";
  }

  private void showBlurSettingsDialog(Context context, ImageView targetView) {
    try {
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      BlurSizes currentSettings;
      File jsonFileObj = new File(jsonFile);

      if (!jsonFileObj.exists()) {
        currentSettings = new BlurSizes();
      } else {
        String jsonContent = FileUtil.readFile(jsonFile);
        currentSettings = gson.fromJson(jsonContent, BlurSizes.class);
      }
      LinearLayout layout = new LinearLayout(context);
      layout.setOrientation(LinearLayout.VERTICAL);
      layout.setPadding(50, 50, 50, 50);

      // Blur Size
      TextView blurText = new TextView(context);
      blurText.setText("Blur Size: " + currentSettings.getBlursize());
      blurText.setTextColor(Color.WHITE);

      SliderCompat blurSlider = new SliderCompat(context);
      blurSlider.setValueFrom(1f);
      blurSlider.setValueTo(25f);
      blurSlider.setValue(currentSettings.getBlursize());
      blurSlider.addOnChangeListener(
          new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
              blurText.setText("Blur Size: " + (int) value);
            }
          });

      // Sampling
      TextView samplingText = new TextView(context);
      samplingText.setText("Sampling: " + currentSettings.getSampling());
      samplingText.setTextColor(Color.WHITE);

      SliderCompat samplingSlider = new SliderCompat(context);
      samplingSlider.setValueFrom(1f);
      samplingSlider.setValueTo(8f);
      samplingSlider.setValue(currentSettings.getSampling());
      samplingSlider.addOnChangeListener(
          new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
              samplingText.setText("Sampling: " + (int) value);
            }
          });

      // Brightness
      TextView brightnessText = new TextView(context);
      brightnessText.setText("Brightness: " + currentSettings.getBrightness());
      brightnessText.setTextColor(Color.WHITE);

      SliderCompat brightnessSlider = new SliderCompat(context);
      brightnessSlider.setValueFrom(-100f);
      brightnessSlider.setValueTo(100f);
      brightnessSlider.setValue(currentSettings.getBrightness());
      brightnessSlider.addOnChangeListener(
          new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
              brightnessText.setText("Brightness: " + (int) value);
            }
          });

      // Contrast
      TextView contrastText = new TextView(context);
      contrastText.setText("Contrast: " + currentSettings.getContrast());
      contrastText.setTextColor(Color.WHITE);

      SliderCompat contrastSlider = new SliderCompat(context);
      contrastSlider.setValueFrom(-100f);
      contrastSlider.setValueTo(100f);
      contrastSlider.setValue(currentSettings.getContrast());
      contrastSlider.addOnChangeListener(
          new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
              contrastText.setText("Contrast: " + (int) value);
            }
          });

      // Saturation
      TextView saturationText = new TextView(context);
      saturationText.setText("Saturation: " + currentSettings.getSaturation());
      saturationText.setTextColor(Color.WHITE);

      SliderCompat saturationSlider = new SliderCompat(context);
      saturationSlider.setValueFrom(0f);
      saturationSlider.setValueTo(2f);
      saturationSlider.setValue(currentSettings.getSaturation());
      saturationSlider.addOnChangeListener(
          new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
              saturationText.setText("Saturation: " + String.format("%.1f", value));
            }
          });

      // Hue
      TextView hueText = new TextView(context);
      hueText.setText("Hue: " + currentSettings.getHue());
      hueText.setTextColor(Color.WHITE);

      SliderCompat hueSlider = new SliderCompat(context);
      hueSlider.setValueFrom(0f);
      hueSlider.setValueTo(1f);
      hueSlider.setValue(currentSettings.getHue());
      hueSlider.addOnChangeListener(
          new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
              hueText.setText("Hue: " + String.format("%.2f", value));
            }
          });

      // Opacity
      TextView opacityText = new TextView(context);
      opacityText.setText("Opacity: " + currentSettings.getOpacity());
      opacityText.setTextColor(Color.WHITE);

      SliderCompat opacitySlider = new SliderCompat(context);
      opacitySlider.setValueFrom(0f);
      opacitySlider.setValueTo(1f);
      opacitySlider.setValue(currentSettings.getOpacity());
      opacitySlider.addOnChangeListener(
          new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
              opacityText.setText("Opacity: " + String.format("%.2f", value));
            }
          });

      // Add all views to layout
      layout.addView(blurText);
      layout.addView(blurSlider);
      layout.addView(samplingText);
      layout.addView(samplingSlider);
      layout.addView(brightnessText);
      layout.addView(brightnessSlider);
      layout.addView(contrastText);
      layout.addView(contrastSlider);
      layout.addView(saturationText);
      layout.addView(saturationSlider);
      layout.addView(hueText);
      layout.addView(hueSlider);
      layout.addView(opacityText);
      layout.addView(opacitySlider);

      MaterialAlertDialogBuilder dialogBuilder =
          new MaterialAlertDialogBuilder(context)
              .setTitle("Blur Settings")
              .setView(layout)
              .setPositiveButton(
                  "OK",
                  (dialog, which) -> {
                    int newBlurSize = (int) blurSlider.getValue();
                    int newSampling = (int) samplingSlider.getValue();
                    float newBrightness = brightnessSlider.getValue();
                    int newContrast = (int) contrastSlider.getValue();
                    float newSaturation = saturationSlider.getValue();
                    float newHue = hueSlider.getValue();
                    float newOpacity = opacitySlider.getValue();

                    saveBlurSettings(
                        newBlurSize,
                        newSampling,
                        newBrightness,
                        newContrast,
                        newSaturation,
                        newHue,
                        newOpacity);

                    if (currentActivity != null) {
                      try {
                        Field ghostIconField =
                            CodeEditorActivity.class.getDeclaredField("ghostIcon");
                        ghostIconField.setAccessible(true);
                        ImageView ghostIconView = (ImageView) ghostIconField.get(currentActivity);
                        if (ghostIconView != null) {
                          setBlurInWallpaperMobile(context, ghostIconView);
                        }
                      } catch (Exception e) {
                        Log.e(TAG, "Error applying new blur settings: " + e.getMessage());
                      }
                    }

                    Toast.makeText(context, "Blur settings updated", Toast.LENGTH_SHORT).show();
                  })
              .setNegativeButton(
                  "Cancel",
                  (dialog, which) -> {
                    dialog.dismiss();
                  });

      dialogBuilder.show();

    } catch (Exception e) {
      Log.e(TAG, "Error showing blur settings dialog: " + e.getMessage());
      Toast.makeText(context, "Error showing settings: " + e.getMessage(), Toast.LENGTH_SHORT)
          .show();
    }
  }

  private void saveBlurSettings(
      int blurSize,
      int sampling,
      float brightness,
      int contrast,
      float saturation,
      float hue,
      float opacity) {
    try {
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      BlurSizes newSettings = new BlurSizes();
      newSettings.setBlursize(blurSize);
      newSettings.setSampling(sampling);
      newSettings.setBrightness((int) brightness);
      newSettings.setContrast(contrast);
      newSettings.setSaturation(saturation);
      newSettings.setHue(hue);
      newSettings.setOpacity(opacity);

      String jsonContent = gson.toJson(newSettings);
      FileWriter writer = new FileWriter(jsonFile);
      writer.write(jsonContent);
      writer.flush();
      writer.close();

      Log.d(TAG, "Blur settings saved: " + jsonContent);
    } catch (Exception e) {
      Log.e(TAG, "Error saving blur settings: " + e.getMessage());
    }
  }

  void setBlurInWallpaperMobile(Context context, ImageView view) {
    try {
      SharedPreferences getvb = context.getSharedPreferences("getvb", Context.MODE_PRIVATE);
      String imagePath = getvb.getString("dir", "");
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      BlurSizes code;
      File jsonFileObj = new File(jsonFile);

      if (!jsonFileObj.exists()) {
        jsonFileObj.getParentFile().mkdirs();
        code = new BlurSizes();
        String jsonContent = gson.toJson(code);
        FileWriter writer = new FileWriter(jsonFileObj);
        writer.write(jsonContent);
        writer.flush();
        writer.close();
        Log.d(TAG, "File created with content: " + jsonContent);
      } else {
        String jsonContent = FileUtil.readFile(jsonFile);
        code = gson.fromJson(jsonContent, BlurSizes.class);
        Log.d(TAG, "File read with content: " + jsonContent);
      }
      Toast.makeText(context, imagePath, Toast.LENGTH_SHORT).show();
      if (imagePath.endsWith(".gif")) {
        Glide.with(context)
            .asGif()
            .load(imagePath)
            .placeholder(GlideCompat.CircelPrograssBar())
            .transform(
                new WorkingBlurTransformation(
                    code.getBlursize(),
                    code.getSampling(),
                    code.getBrightness(),
                    code.getContrast(),
                    code.getSaturation(),
                    code.getHue(),
                    code.getTintColor(),
                    code.getOpacity()))
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .error(new ColorDrawable(Color.BLACK))
            .into(view);
      } else {
        Glide.with(context)
            .load(imagePath)
            .placeholder(GlideCompat.CircelPrograssBar())
            .transform(
                new WorkingBlurTransformation(
                    code.getBlursize(),
                    code.getSampling(),
                    code.getBrightness(),
                    code.getContrast(),
                    code.getSaturation(),
                    code.getHue(),
                    code.getTintColor(),
                    code.getOpacity()))
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .error(new ColorDrawable(Color.BLACK))
            .into(view);
      }
    } catch (Exception e) {
      Log.e(TAG, "Error in setBlurInWallpaperMobile: " + e.getMessage());
    }
  }

  class FastBlur {
    public static Bitmap blur(Bitmap sentBitmap, int radius, boolean canReuseInBitmap) {
      Bitmap bitmap;
      if (canReuseInBitmap) {
        bitmap = sentBitmap;
      } else {
        bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);
      }

      if (radius < 1) {
        return (null);
      }

      int w = bitmap.getWidth();
      int h = bitmap.getHeight();

      int[] pix = new int[w * h];
      bitmap.getPixels(pix, 0, w, 0, 0, w, h);

      int wm = w - 1;
      int hm = h - 1;
      int wh = w * h;
      int div = radius + radius + 1;

      int r[] = new int[wh];
      int g[] = new int[wh];
      int b[] = new int[wh];
      int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
      int vmin[] = new int[Math.max(w, h)];

      var divsum = (div + 1) >> 1;
      divsum *= divsum;
      int dv[] = new int[256 * divsum];
      for (i = 0; i < 256 * divsum; i++) {
        dv[i] = (i / divsum);
      }

      yw = yi = 0;

      int[][] stack = new int[div][3];
      int stackpointer;
      int stackstart;
      int[] sir;
      int rbs;
      var r1 = radius + 1;
      int routsum, goutsum, boutsum;
      int rinsum, ginsum, binsum;

      for (y = 0; y < h; y++) {
        rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
        for (i = -radius; i <= radius; i++) {
          p = pix[yi + Math.min(wm, Math.max(i, 0))];
          sir = stack[i + radius];
          sir[0] = (p & 0xff0000) >> 16;
          sir[1] = (p & 0x00ff00) >> 8;
          sir[2] = (p & 0x0000ff);
          rbs = r1 - Math.abs(i);
          rsum += sir[0] * rbs;
          gsum += sir[1] * rbs;
          bsum += sir[2] * rbs;
          if (i > 0) {
            rinsum += sir[0];
            ginsum += sir[1];
            binsum += sir[2];
          } else {
            routsum += sir[0];
            goutsum += sir[1];
            boutsum += sir[2];
          }
        }
        stackpointer = radius;

        for (x = 0; x < w; x++) {

          r[yi] = dv[rsum];
          g[yi] = dv[gsum];
          b[yi] = dv[bsum];

          rsum -= routsum;
          gsum -= goutsum;
          bsum -= boutsum;

          stackstart = stackpointer - radius + div;
          sir = stack[stackstart % div];

          routsum -= sir[0];
          goutsum -= sir[1];
          boutsum -= sir[2];

          if (y == 0) {
            vmin[x] = Math.min(x + radius + 1, wm);
          }
          p = pix[yw + vmin[x]];

          sir[0] = (p & 0xff0000) >> 16;
          sir[1] = (p & 0x00ff00) >> 8;
          sir[2] = (p & 0x0000ff);

          rinsum += sir[0];
          ginsum += sir[1];
          binsum += sir[2];

          rsum += rinsum;
          gsum += ginsum;
          bsum += binsum;

          stackpointer = (stackpointer + 1) % div;
          sir = stack[(stackpointer) % div];

          routsum += sir[0];
          goutsum += sir[1];
          boutsum += sir[2];

          rinsum -= sir[0];
          ginsum -= sir[1];
          binsum -= sir[2];

          yi++;
        }
        yw += w;
      }
      for (x = 0; x < w; x++) {
        rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
        yp = -radius * w;
        for (i = -radius; i <= radius; i++) {
          yi = Math.max(0, yp) + x;

          sir = stack[i + radius];

          sir[0] = r[yi];
          sir[1] = g[yi];
          sir[2] = b[yi];

          rbs = r1 - Math.abs(i);

          rsum += sir[0] * rbs;
          gsum += sir[1] * rbs;
          bsum += sir[2] * rbs;

          if (i > 0) {
            rinsum += sir[0];
            ginsum += sir[1];
            binsum += sir[2];
          } else {
            routsum += sir[0];
            goutsum += sir[1];
            boutsum += sir[2];
          }

          if (i < hm) {
            yp += w;
          }
        }
        yi = x;
        stackpointer = radius;
        for (y = 0; y < h; y++) {

          pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

          rsum -= routsum;
          gsum -= goutsum;
          bsum -= boutsum;

          stackstart = stackpointer - radius + div;
          sir = stack[stackstart % div];

          routsum -= sir[0];
          goutsum -= sir[1];
          boutsum -= sir[2];

          if (x == 0) {
            vmin[y] = Math.min(y + r1, hm) * w;
          }
          p = x + vmin[y];

          sir[0] = r[p];
          sir[1] = g[p];
          sir[2] = b[p];

          rinsum += sir[0];
          ginsum += sir[1];
          binsum += sir[2];

          rsum += rinsum;
          gsum += ginsum;
          bsum += binsum;

          stackpointer = (stackpointer + 1) % div;
          sir = stack[stackpointer];

          routsum += sir[0];
          goutsum += sir[1];
          boutsum += sir[2];

          rinsum -= sir[0];
          ginsum -= sir[1];
          binsum -= sir[2];

          yi += w;
        }
      }

      bitmap.setPixels(pix, 0, w, 0, 0, w, h);
      return (bitmap);
    }
  }

  public class WorkingBlurTransformation extends BitmapTransformation {

    private static final String ID = "ir.ninjacoder.WorkingBlurTransformation.v4";
    private final int radius;
    private final int sampling;
    private final float brightness;
    private final int contrast;
    private final float saturation;
    private final float hue;
    private final int tintColor;
    private final float opacity;

    public WorkingBlurTransformation(
        int radius,
        int sampling,
        float brightness,
        int contrast,
        float saturation,
        float hue,
        int tintColor,
        float opacity) {
      this.radius = radius;
      this.sampling = sampling;
      this.brightness = brightness;
      this.contrast = contrast;
      this.saturation = saturation;
      this.hue = hue;
      this.tintColor = tintColor;
      this.opacity = opacity;
    }

    @Override
    protected Bitmap transform(
        @NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {

      int scaledWidth = toTransform.getWidth() / sampling;
      int scaledHeight = toTransform.getHeight() / sampling;

      Bitmap scaledBitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888);

      Canvas canvas = new Canvas(scaledBitmap);
      canvas.scale(1f / sampling, 1f / sampling);

      Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
      canvas.drawBitmap(toTransform, 0, 0, paint);

      try {
        Bitmap blurred = FastBlur.blur(scaledBitmap, radius, true);

        // اعمال تمام افکت‌ها روی bitmap نهایی
        if (brightness != 0
            || contrast != 0
            || saturation != 1.0f
            || hue != 0f
            || tintColor != 0
            || opacity != 1.0f) {
          Bitmap finalBitmap = applyAllEffects(blurred);
          if (blurred != finalBitmap && !blurred.isRecycled()) {
            blurred.recycle();
          }
          return finalBitmap;
        }

        return blurred;
      } catch (Throwable e) {
        Log.e("Blur", "error = " + e.getMessage());
        return toTransform;
      }
    }

    private Bitmap applyAllEffects(Bitmap bitmap) {
      Bitmap resultBitmap =
          Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
      Canvas canvas = new Canvas(resultBitmap);

      Paint paint = new Paint();
      ColorMatrix colorMatrix = new ColorMatrix();

      // Brightness
      float normalizedBrightness = brightness / 100f;
      ColorMatrix brightnessMatrix = new ColorMatrix();
      brightnessMatrix.set(
          new float[] {
            1, 0, 0, 0, normalizedBrightness * 255,
            0, 1, 0, 0, normalizedBrightness * 255,
            0, 0, 1, 0, normalizedBrightness * 255,
            0, 0, 0, 1, 0
          });

      // Contrast
      float contrastFactor = (contrast + 100) / 100f;
      float contrastTranslate = (-0.5f * contrastFactor + 0.5f) * 255f;
      ColorMatrix contrastMatrix = new ColorMatrix();
      contrastMatrix.set(
          new float[] {
            contrastFactor,
            0,
            0,
            0,
            contrastTranslate,
            0,
            contrastFactor,
            0,
            0,
            contrastTranslate,
            0,
            0,
            contrastFactor,
            0,
            contrastTranslate,
            0,
            0,
            0,
            1,
            0
          });

      // Saturation
      ColorMatrix saturationMatrix = new ColorMatrix();
      saturationMatrix.setSaturation(saturation);

      // Hue Rotation
      ColorMatrix hueMatrix = new ColorMatrix();
      hueMatrix.setRotate(0, hue * 360); // Red
      hueMatrix.setRotate(1, hue * 360); // Green
      hueMatrix.setRotate(2, hue * 360); // Blue

      // Combine all color matrices
      colorMatrix.postConcat(brightnessMatrix);
      colorMatrix.postConcat(contrastMatrix);
      colorMatrix.postConcat(saturationMatrix);
      colorMatrix.postConcat(hueMatrix);

      paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));

      // Opacity
      paint.setAlpha((int) (opacity * 255));

      // Tint Color
      if (tintColor != 0) {
        paint.setColorFilter(new PorterDuffColorFilter(tintColor, PorterDuff.Mode.SRC_ATOP));
      }

      canvas.drawBitmap(bitmap, 0, 0, paint);

      return resultBitmap;
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
      messageDigest.update(
          (ID + radius + sampling + brightness + contrast + saturation + hue + tintColor + opacity)
              .getBytes(CHARSET));
    }
  }

  class BlurSizes {
    int blursize = 3, sampling = 2, brightness = 0;
    int contrast = 0;
    float saturation = 1.0f;
    float hue = 0f;
    int tintColor = 0;
    float opacity = 1.0f;

    public int getBlursize() {
      return this.blursize;
    }

    public void setBlursize(int blursize) {
      this.blursize = blursize;
    }

    public int getSampling() {
      return this.sampling;
    }

    public void setSampling(int sampling) {
      this.sampling = sampling;
    }

    public int getBrightness() {
      return this.brightness;
    }

    public void setBrightness(int brightness) {
      this.brightness = brightness;
    }

    public int getContrast() {
      return this.contrast;
    }

    public void setContrast(int contrast) {
      this.contrast = contrast;
    }

    public float getSaturation() {
      return this.saturation;
    }

    public void setSaturation(float saturation) {
      this.saturation = saturation;
    }

    public float getHue() {
      return this.hue;
    }

    public void setHue(float hue) {
      this.hue = hue;
    }

    public int getTintColor() {
      return this.tintColor;
    }

    public void setTintColor(int tintColor) {
      this.tintColor = tintColor;
    }

    public float getOpacity() {
      return this.opacity;
    }

    public void setOpacity(float opacity) {
      this.opacity = opacity;
    }
  }
}
