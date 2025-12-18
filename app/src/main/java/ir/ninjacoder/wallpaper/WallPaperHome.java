package ir.ninjacoder.wallpaper;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;
import com.blankj.utilcode.util.ClipboardUtils;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.Glide;
import android.util.Log;
import android.widget.ImageView;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.shape.MaterialShapes;
import com.google.android.material.shape.ShapeAppearanceModel;
import android.graphics.drawable.GradientDrawable;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ir.ninjacoder.codesnap.widget.ghostide.SliderCompat;
import ir.ninjacoder.ghostide.core.activities.BaseCompat;
import ir.ninjacoder.ghostide.core.activities.FileManagerActivity;
import ir.ninjacoder.ghostide.core.activities.CodeEditorActivity;
import io.github.rosemoe.sora.widget.CodeEditor;
import ir.ninjacoder.ghostide.core.glidecompat.GlideCompat;
import ir.ninjacoder.ghostide.core.layoutmanager.NavigationViewCompnet;
import ir.ninjacoder.ghostide.core.pl.PluginManagerCompat;
import ir.ninjacoder.ghostide.core.terminal.TerminalActivity;
import ir.ninjacoder.ghostide.core.utils.DataUtil;
import ir.ninjacoder.ghostide.core.utils.FileUtil;
import ir.ninjacoder.ghostide.core.utils.ObjectUtils;
import ir.ninjacoder.ghostide.core.widget.ExrtaFab;
import ir.ninjacoder.prograsssheet.listchild.ChildIconEditorManager;
import ir.ninjacoder.prograsssheet.perfence.ListItemView;
import ir.ninjacoder.prograsssheet.perfence.PerfenceLayoutSubTitle;
import ir.ninjacoder.prograsssheet.perfence.PreferenceSwitchGroup;
import java.io.File;
import java.io.FileWriter;
import java.security.MessageDigest;
import java.util.List;
import java.util.ArrayList;
import android.graphics.drawable.RippleDrawable;
import com.google.android.material.shape.MaterialShapeDrawable;

public class WallPaperHome implements PluginManagerCompat {
  private ImageView ghosticon;
  private ExrtaFab fab;
  public static final String jsonFile =
      "/storage/emulated/0/GhostWebIDE/plugins/wallpaper/object/size.json";
  private String TAG = getClass().getName();
  private BaseCompat currentBase;
  private FileManagerActivity filemanager;
  private CustomFab customFab;

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
  public void getFileManagerAc(FileManagerActivity arg0) {
    this.filemanager = arg0;
  }

  @Override
  public void getCodeEditorAc(CodeEditorActivity base) {}

  @Override
  public void getBaseCompat(BaseCompat base) {
    this.currentBase = base;
    if (base instanceof CodeEditorActivity) {
      return;
    }
    if (base instanceof TerminalActivity term) {
      var tab = term.getTabLayoutTerminal();
      var backgroundColor = tab.getBackgroundTintList();
      if (backgroundColor != null) {
        int defaultColor = backgroundColor.getDefaultColor();
        int colorWithAlpha = ColorUtils.setAlphaComponent(defaultColor, 128);
        tab.setBackgroundColor(colorWithAlpha);
      }
    }
    if (base instanceof FileManagerActivity as) {
      as.getWindow()
          .getDecorView()
          .postDelayed(
              () -> {
                base.getWindow().setNavigationBarColor(Color.TRANSPARENT);
                base.getWindow().setStatusBarColor(Color.TRANSPARENT);
                try {
                  var f = FileManagerActivity.class.getDeclaredField("mview");
                  f.setAccessible(true);
                  var view = (View) f.get(as);
                  view.setBackground(null);
                  view.setBackgroundColor(Color.TRANSPARENT);
                } catch (Exception err) {
                  DataUtil.showMessage(as, err.getLocalizedMessage());
                }
              },
              1000);
    }
    var rootview = base.getWindow().getDecorView().findViewById(android.R.id.content);
    base.getWindow().setNavigationBarColor(Color.TRANSPARENT);
    base.getWindow().setStatusBarColor(Color.TRANSPARENT);
    base.getWindow()
        .getDecorView()
        .post(
            () -> {
              try {
                applySystemWallpaper(base);
                addCustomFabToLayout(base, (ViewGroup) rootview);
              } catch (Exception e) {
                Log.e(TAG, "Failed to initialize wallpaper plugin: " + e.getMessage());
              }
            });
  }

  @Override
  public String langModel() {
    return "all";
  }

  private ExrtaFab findMainFab(ViewGroup viewGroup) {
    for (int i = 0; i < viewGroup.getChildCount(); i++) {
      View child = viewGroup.getChildAt(i);
      if (child instanceof ExrtaFab) {
        return (ExrtaFab) child;
      } else if (child instanceof ViewGroup) {
        ExrtaFab fab = findMainFab((ViewGroup) child);
        if (fab != null) {
          return fab;
        }
      }
    }
    return null;
  }

  private ViewGroup findCoordinatorLayout(ViewGroup view) {
    if (view.getClass().getSimpleName().equals("CoordinatorLayout")) {
      return view;
    }

    for (int i = 0; i < view.getChildCount(); i++) {
      View child = view.getChildAt(i);
      if (child instanceof ViewGroup) {
        ViewGroup result = findCoordinatorLayout((ViewGroup) child);
        if (result != null && result.getClass().getSimpleName().equals("CoordinatorLayout")) {
          return result;
        }
      }
    }
    return null;
  }

  private void addCustomFabToLayout(BaseCompat base, ViewGroup rootView) {
    try {
      ExrtaFab mainFab = findMainFab(rootView);
      if (mainFab == null) return;

      ViewGroup parent = (ViewGroup) mainFab.getParent();
      if (parent == null) return;

      customFab = new CustomFab(base);

      int size = dp(base, 56);

      ViewGroup.MarginLayoutParams correct = new ViewGroup.MarginLayoutParams(size, size);

      if (mainFab.getLayoutParams() instanceof ViewGroup.MarginLayoutParams mp) {
        correct.leftMargin = mp.leftMargin;
        correct.rightMargin = mp.rightMargin;
        correct.topMargin = mp.topMargin;
        correct.bottomMargin = mp.bottomMargin + dp(base, 70);
      }

      if (mainFab.getLayoutParams() instanceof FrameLayout.LayoutParams fp) {
        FrameLayout.LayoutParams finalLp = new FrameLayout.LayoutParams(correct);
        finalLp.gravity = fp.gravity;
        parent.addView(customFab, finalLp);
      } else if (mainFab.getLayoutParams() instanceof CoordinatorLayout.LayoutParams cp) {
        CoordinatorLayout.LayoutParams finalLp = new CoordinatorLayout.LayoutParams(correct);
        finalLp.gravity = cp.gravity;
        parent.addView(customFab, finalLp);
      } else {
        parent.addView(customFab, correct);
      }

      customFab.bringToFront();
      customFab.setVisibility(View.VISIBLE);
      customFab.setAlpha(1f);

      customFab.setOnClickListener(v -> showWallpaperSettingsDialog(base));

    } catch (Exception ignored) {
    }
  }

  private int dp(Context c, int v) {
    return (int) (v * c.getResources().getDisplayMetrics().density);
  }

  private void showWallpaperSettingsDialog(BaseCompat base) {
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

      LinearLayout layout = new LinearLayout(base);
      layout.setOrientation(LinearLayout.VERTICAL);
      layout.setPadding(50, 50, 50, 50);

      TextView blurText = new TextView(base);
      blurText.setText("Blur Size: " + currentSettings.getBlursize());
      blurText.setTextColor(Color.WHITE);

      SliderCompat blurSlider = new SliderCompat(base);
      blurSlider.setValueFrom(1f);
      blurSlider.setValueTo(25f);
      blurSlider.setValue(currentSettings.getBlursize());
      blurSlider.addOnChangeListener(
          (slider, value, fromUser) -> {
            blurText.setText("Blur Size: " + (int) value);
          });

      TextView samplingText = new TextView(base);
      samplingText.setText("Sampling: " + currentSettings.getSampling());
      samplingText.setTextColor(Color.WHITE);

      SliderCompat samplingSlider = new SliderCompat(base);
      samplingSlider.setValueFrom(1f);
      samplingSlider.setValueTo(8f);
      samplingSlider.setValue(currentSettings.getSampling());
      samplingSlider.addOnChangeListener(
          (slider, value, fromUser) -> {
            samplingText.setText("Sampling: " + (int) value);
          });

      TextView brightnessText = new TextView(base);
      brightnessText.setText("Brightness: " + currentSettings.getBrightness());
      brightnessText.setTextColor(Color.WHITE);

      SliderCompat brightnessSlider = new SliderCompat(base);
      brightnessSlider.setValueFrom(-100f);
      brightnessSlider.setValueTo(100f);
      brightnessSlider.setValue(currentSettings.getBrightness());
      brightnessSlider.addOnChangeListener(
          (slider, value, fromUser) -> {
            brightnessText.setText("Brightness: " + (int) value);
          });

      TextView contrastText = new TextView(base);
      contrastText.setText("Contrast: " + currentSettings.getContrast());
      contrastText.setTextColor(Color.WHITE);

      SliderCompat contrastSlider = new SliderCompat(base);
      contrastSlider.setValueFrom(-100f);
      contrastSlider.setValueTo(100f);
      contrastSlider.setValue(currentSettings.getContrast());
      contrastSlider.addOnChangeListener(
          (slider, value, fromUser) -> {
            contrastText.setText("Contrast: " + (int) value);
          });

      TextView saturationText = new TextView(base);
      saturationText.setText("Saturation: " + currentSettings.getSaturation());
      saturationText.setTextColor(Color.WHITE);

      SliderCompat saturationSlider = new SliderCompat(base);
      saturationSlider.setValueFrom(0f);
      saturationSlider.setValueTo(2f);
      saturationSlider.setValue(currentSettings.getSaturation());
      saturationSlider.addOnChangeListener(
          (slider, value, fromUser) -> {
            saturationText.setText("Saturation: " + String.format("%.1f", value));
          });

      TextView opacityText = new TextView(base);
      opacityText.setText("Opacity: " + currentSettings.getOpacity());
      opacityText.setTextColor(Color.WHITE);

      SliderCompat opacitySlider = new SliderCompat(base);
      opacitySlider.setValueFrom(0f);
      opacitySlider.setValueTo(1f);
      opacitySlider.setValue(currentSettings.getOpacity());
      opacitySlider.addOnChangeListener(
          (slider, value, fromUser) -> {
            opacityText.setText("Opacity: " + String.format("%.2f", value));
          });

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
      layout.addView(opacityText);
      layout.addView(opacitySlider);

      MaterialAlertDialogBuilder dialogBuilder =
          new MaterialAlertDialogBuilder(base)
              .setTitle("Wallpaper Settings")
              .setView(layout)
              .setPositiveButton(
                  "Apply",
                  (dialog, which) -> {
                    int newBlurSize = (int) blurSlider.getValue();
                    int newSampling = (int) samplingSlider.getValue();
                    float newBrightness = brightnessSlider.getValue();
                    int newContrast = (int) contrastSlider.getValue();
                    float newSaturation = saturationSlider.getValue();
                    float newOpacity = opacitySlider.getValue();

                    saveWallpaperSettings(
                        newBlurSize,
                        newSampling,
                        newBrightness,
                        newContrast,
                        newSaturation,
                        newOpacity);
                    applySystemWallpaper(base);

                    Toast.makeText(base, "Wallpaper settings updated", Toast.LENGTH_SHORT).show();
                  })
              .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

      dialogBuilder.show();

    } catch (Exception e) {
      Log.e(TAG, "Error showing wallpaper settings dialog: " + e.getMessage());
      Toast.makeText(base, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    }
  }

  private void saveWallpaperSettings(
      int blurSize, int sampling, float brightness, int contrast, float saturation, float opacity) {
    try {
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      BlurSizes newSettings = new BlurSizes();
      newSettings.setBlursize(blurSize);
      newSettings.setSampling(sampling);
      newSettings.setBrightness((int) brightness);
      newSettings.setContrast(contrast);
      newSettings.setSaturation(saturation);
      newSettings.setOpacity(opacity);

      String jsonContent = gson.toJson(newSettings);
      File file = new File(jsonFile);
      file.getParentFile().mkdirs();
      FileWriter writer = new FileWriter(file);
      writer.write(jsonContent);
      writer.close();

      Log.d(TAG, "Wallpaper settings saved");
    } catch (Exception e) {
      Log.e(TAG, "Error saving wallpaper settings: " + e.getMessage());
    }
  }

  private void applySystemWallpaper(BaseCompat base) {
    try {

      WallpaperManager wallpaperManager = WallpaperManager.getInstance(base);
      Drawable wallpaperDrawable = wallpaperManager.getDrawable();

      if (wallpaperDrawable != null) {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        BlurSizes settings;
        File jsonFileObj = new File(jsonFile);

        if (!jsonFileObj.exists()) {
          settings = new BlurSizes();
        } else {
          String jsonContent = FileUtil.readFile(jsonFile);
          settings = gson.fromJson(jsonContent, BlurSizes.class);
        }

        Glide.with(base)
            .load(wallpaperDrawable)
            .transform(
                new WallpaperTransformation(
                    settings.getBlursize(),
                    settings.getSampling(),
                    settings.getBrightness(),
                    settings.getContrast(),
                    settings.getSaturation(),
                    settings.getOpacity()))
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(
                new CustomTarget<Drawable>() {
                  @Override
                  public void onResourceReady(
                      @NonNull Drawable resource, Transition<? super Drawable> transition) {

                    base.getWindow().getDecorView().setBackground(resource);

                    findAndApplyToToolbar(base);
                  }

                  @Override
                  public void onLoadCleared(Drawable placeholder) {}
                });
      }
    } catch (Exception e) {
      Log.e(TAG, "Error applying system wallpaper: " + e.getMessage());
      Toast.makeText(base, "Error applying wallpaper", Toast.LENGTH_SHORT).show();
    }
  }

  private List<TextInputLayout> findAllTextInputLayouts(ViewGroup viewGroup) {
    List<TextInputLayout> result = new ArrayList<>();
    findAllTextInputLayoutsRecursive(viewGroup, result);
    return result;
  }

  private void findAllTextInputLayoutsRecursive(ViewGroup viewGroup, List<TextInputLayout> result) {
    for (int i = 0; i < viewGroup.getChildCount(); i++) {
      View child = viewGroup.getChildAt(i);

      if (child instanceof TextInputLayout) {
        result.add((TextInputLayout) child);
      } else if (child instanceof ViewGroup) {
        findAllTextInputLayoutsRecursive((ViewGroup) child, result);
      }
    }
  }

  private void applyAlphaToAllSwitchGroupsInRecyclerView(RecyclerView recyclerView) {
    if (recyclerView == null || recyclerView.getAdapter() == null) return;

    int childCount = recyclerView.getChildCount();
    for (int i = 0; i < childCount; i++) {
      View child = recyclerView.getChildAt(i);
      findAndApplyAlphaToSwitchGroupsInView(child);
    }
  }

  private List<PerfenceLayoutSubTitle> findAllPerfenceLayoutSubTitles(ViewGroup viewGroup) {
    List<PerfenceLayoutSubTitle> result = new ArrayList<>();
    findAllPerfenceLayoutSubTitlesRecursive(viewGroup, result);
    return result;
  }

  private void applyAlphaToTextInputLayout(TextInputLayout textInputLayout) {
    try {

      int boxBackgroundMode = textInputLayout.getBoxBackgroundMode();

      if (boxBackgroundMode == TextInputLayout.BOX_BACKGROUND_OUTLINE) {
        applyAlphaToOutlinedTextInputLayout(textInputLayout);
      } else if (boxBackgroundMode == TextInputLayout.BOX_BACKGROUND_FILLED) {
        applyAlphaToFilledTextInputLayout(textInputLayout);
      } else if (boxBackgroundMode == TextInputLayout.BOX_BACKGROUND_NONE) {
        applyAlphaToNoBackgroundTextInputLayout(textInputLayout);
      } else {

        applyAlphaToDefaultTextInputLayout(textInputLayout);
      }

      applyAlphaToTextInputLayoutTextColors(textInputLayout);

      applyAlphaToEditTextInside(textInputLayout);

    } catch (Exception e) {
      Log.e(TAG, "Error applying alpha to TextInputLayout: " + e.getMessage());
    }
  }

  private void applyAlphaToOutlinedTextInputLayout(TextInputLayout textInputLayout) {
    try {

      textInputLayout.setBoxBackgroundColor(Color.TRANSPARENT);

      int boxStrokeColorStateList = textInputLayout.getBoxStrokeColor();

      int alphaDefault = ColorUtils.setAlphaComponent(boxStrokeColorStateList, 128);
      int alphaFocused = ColorUtils.setAlphaComponent(boxStrokeColorStateList, 128);

      ColorStateList newStrokeColorStateList =
          new ColorStateList(
              new int[][] {new int[] {android.R.attr.state_focused}, new int[] {}},
              new int[] {alphaFocused, alphaDefault});

      textInputLayout.setBoxStrokeColorStateList(newStrokeColorStateList);

      ColorStateList hintColor = textInputLayout.getDefaultHintTextColor();
      if (hintColor != null) {
        int hintDefaultColor = hintColor.getDefaultColor();
        int alphaHintColor = ColorUtils.setAlphaComponent(hintDefaultColor, 180);
        textInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(alphaHintColor));
      }

    } catch (Exception e) {
      Log.e(TAG, "Error in applyAlphaToOutlinedTextInputLayout: " + e.getMessage());
    }
  }

  private void applyAlphaToFilledTextInputLayout(TextInputLayout textInputLayout) {
    try {

      int boxBackgroundColorStateList = textInputLayout.getBoxBackgroundColor();

      int alphaColor = ColorUtils.setAlphaComponent(boxBackgroundColorStateList, 128);

      ColorStateList newBoxBackgroundColorStateList =
          new ColorStateList(
              new int[][] {new int[] {android.R.attr.state_focused}, new int[] {}},
              new int[] {alphaColor, alphaColor});

      textInputLayout.setBoxBackgroundColorStateList(newBoxBackgroundColorStateList);

      int boxStrokeColorStateList = textInputLayout.getBoxStrokeColor();
      int alphaStrokeColor = ColorUtils.setAlphaComponent(boxStrokeColorStateList, 128);
      textInputLayout.setBoxStrokeColor(alphaStrokeColor);

    } catch (Exception e) {
      Log.e(TAG, "Error in applyAlphaToFilledTextInputLayout: " + e.getMessage());
    }
  }

  private void applyAlphaToNoBackgroundTextInputLayout(TextInputLayout textInputLayout) {
    try {

      textInputLayout.setBoxBackgroundColor(Color.TRANSPARENT);

      applyAlphaToTextInputLayoutTextColors(textInputLayout);

    } catch (Exception e) {
      Log.e(TAG, "Error in applyAlphaToNoBackgroundTextInputLayout: " + e.getMessage());
    }
  }

  private void applyAlphaToDefaultTextInputLayout(TextInputLayout textInputLayout) {
    try {

      Drawable originalBg = textInputLayout.getBackground();

      if (originalBg == null) {

        int backgroundColor =
            MaterialColors.getColor(
                textInputLayout, com.google.android.material.R.attr.colorSurfaceContainer);
        if (backgroundColor == 0) {
          backgroundColor =
              MaterialColors.getColor(
                  textInputLayout, com.google.android.material.R.attr.colorSurface);
        }
        if (backgroundColor == 0) {
          backgroundColor = Color.WHITE;
        }

        int alphaColor = ColorUtils.setAlphaComponent(backgroundColor, 128);
        textInputLayout.setBackgroundColor(alphaColor);
        return;
      }

      if (originalBg instanceof InsetDrawable) {
        InsetDrawable insetDrawable = (InsetDrawable) originalBg;
        Drawable innerDrawable = insetDrawable.getDrawable();

        if (innerDrawable instanceof MaterialShapeDrawable) {
          applyAlphaToMaterialShapeDrawableBackground(
              textInputLayout, (MaterialShapeDrawable) innerDrawable);
        } else if (innerDrawable instanceof GradientDrawable) {
          applyAlphaToGradientDrawableBackground(textInputLayout, (GradientDrawable) innerDrawable);
        } else if (innerDrawable instanceof ColorDrawable) {
          applyAlphaToColorDrawableBackground(textInputLayout, (ColorDrawable) innerDrawable);
        }
      } else if (originalBg instanceof MaterialShapeDrawable) {
        applyAlphaToMaterialShapeDrawableBackground(
            textInputLayout, (MaterialShapeDrawable) originalBg);
      } else if (originalBg instanceof GradientDrawable) {
        applyAlphaToGradientDrawableBackground(textInputLayout, (GradientDrawable) originalBg);
      } else if (originalBg instanceof ColorDrawable) {
        applyAlphaToColorDrawableBackground(textInputLayout, (ColorDrawable) originalBg);
      } else if (originalBg instanceof RippleDrawable) {

        RippleDrawable rippleDrawable = (RippleDrawable) originalBg;
        Drawable backgroundDrawable = rippleDrawable.getDrawable(0);

        if (backgroundDrawable instanceof MaterialShapeDrawable) {
          applyAlphaToMaterialShapeDrawableBackground(
              textInputLayout, (MaterialShapeDrawable) backgroundDrawable);
        } else if (backgroundDrawable instanceof GradientDrawable) {
          applyAlphaToGradientDrawableBackground(
              textInputLayout, (GradientDrawable) backgroundDrawable);
        }
      }

    } catch (Exception e) {
      Log.e(TAG, "Error in applyAlphaToDefaultTextInputLayout: " + e.getMessage());
    }
  }

  private void applyAlphaToMaterialShapeDrawableBackground(
      TextInputLayout textInputLayout, MaterialShapeDrawable shapeDrawable) {
    ShapeAppearanceModel shapeModel = shapeDrawable.getShapeAppearanceModel();

    MaterialShapeDrawable newShapeDrawable = new MaterialShapeDrawable();
    newShapeDrawable.setShapeAppearanceModel(shapeModel);

    ColorStateList fillColor = shapeDrawable.getFillColor();
    if (fillColor != null) {
      int color = fillColor.getDefaultColor();
      int alphaColor = ColorUtils.setAlphaComponent(color, 128);
      newShapeDrawable.setFillColor(ColorStateList.valueOf(alphaColor));
    }

    if (shapeDrawable.getStrokeWidth() > 0) {
      ColorStateList strokeColor = shapeDrawable.getStrokeColor();
      if (strokeColor != null) {
        int strokeDefaultColor = strokeColor.getDefaultColor();
        int alphaStrokeColor = ColorUtils.setAlphaComponent(strokeDefaultColor, 128);
        newShapeDrawable.setStroke(shapeDrawable.getStrokeWidth(), alphaStrokeColor);
      }
    }

    newShapeDrawable.setElevation(shapeDrawable.getElevation());

    textInputLayout.setBackground(newShapeDrawable);
  }

  private void applyAlphaToGradientDrawableBackground(
      TextInputLayout textInputLayout, GradientDrawable gradientDrawable) {
    GradientDrawable newGradientDrawable = new GradientDrawable();

    float[] radii = gradientDrawable.getCornerRadii();
    if (radii != null && radii.length == 8) {
      newGradientDrawable.setCornerRadii(radii);
    } else {
      newGradientDrawable.setCornerRadius(gradientDrawable.getCornerRadius());
    }

    ColorStateList color = gradientDrawable.getColor();
    if (color != null) {
      int defaultColor = color.getDefaultColor();
      int alphaColor = ColorUtils.setAlphaComponent(defaultColor, 128);
      newGradientDrawable.setColor(alphaColor);
    } else {
      newGradientDrawable.setColor(ColorUtils.setAlphaComponent(Color.WHITE, 128));
    }

    textInputLayout.setBackground(newGradientDrawable);
  }

  private void applyAlphaToColorDrawableBackground(
      TextInputLayout textInputLayout, ColorDrawable colorDrawable) {
    int color = colorDrawable.getColor();
    int newColor = ColorUtils.setAlphaComponent(color, 128);
    textInputLayout.setBackground(new ColorDrawable(newColor));
  }

  private void applyAlphaToTextInputLayoutTextColors(TextInputLayout textInputLayout) {
    try {

      ColorStateList hintColor = textInputLayout.getDefaultHintTextColor();
      if (hintColor != null) {
        int hintDefaultColor = hintColor.getDefaultColor();
        int alphaHintColor = ColorUtils.setAlphaComponent(hintDefaultColor, 180);
        textInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(alphaHintColor));
      }

      ColorStateList counterTextColor = textInputLayout.getCounterTextColor();
      if (counterTextColor != null) {
        int counterDefaultColor = counterTextColor.getDefaultColor();
        int alphaCounterColor = ColorUtils.setAlphaComponent(counterDefaultColor, 180);
        textInputLayout.setCounterTextColor(ColorStateList.valueOf(alphaCounterColor));
      }

    } catch (Exception e) {
      Log.e(TAG, "Error applying alpha to TextInputLayout text colors: " + e.getMessage());
    }
  }

  private void applyAlphaToEditTextInside(TextInputLayout textInputLayout) {
    try {
      var editText = textInputLayout.getEditText();
      if (editText != null) {

        Drawable editTextBg = editText.getBackground();
        if (editTextBg != null) {
          if (editTextBg instanceof ColorDrawable) {
            ColorDrawable cd = (ColorDrawable) editTextBg;
            int color = cd.getColor();
            int newColor = ColorUtils.setAlphaComponent(color, 128);
            editText.setBackground(new ColorDrawable(newColor));
          }
        }
        ColorStateList editTextHintColor = editText.getHintTextColors();
        if (editTextHintColor != null) {
          int editHintDefaultColor = editTextHintColor.getDefaultColor();
          int alphaEditHintColor = ColorUtils.setAlphaComponent(editHintDefaultColor, 180);
          editText.setHintTextColor(ColorStateList.valueOf(alphaEditHintColor));
        }
        ColorStateList editTextTextColor = editText.getTextColors();
        if (editTextTextColor != null) {
          int editTextDefaultColor = editTextTextColor.getDefaultColor();
          int alphaEditTextColor = ColorUtils.setAlphaComponent(editTextDefaultColor, 200);
          editText.setTextColor(ColorStateList.valueOf(alphaEditTextColor));
        }
      }
    } catch (Exception e) {
      Log.e(TAG, "Error applying alpha to EditText inside TextInputLayout: " + e.getMessage());
    }
  }

  private void findAllPerfenceLayoutSubTitlesRecursive(
      ViewGroup viewGroup, List<PerfenceLayoutSubTitle> result) {
    for (int i = 0; i < viewGroup.getChildCount(); i++) {
      View child = viewGroup.getChildAt(i);

      if (child instanceof PerfenceLayoutSubTitle) {
        result.add((PerfenceLayoutSubTitle) child);
      } else if (child instanceof ViewGroup) {
        findAllPerfenceLayoutSubTitlesRecursive((ViewGroup) child, result);
      }
    }
  }

  private void applyAlphaToPerfenceLayoutSubTitle(PerfenceLayoutSubTitle subTitle) {
    Drawable originalBg = subTitle.getBackground();

    if (originalBg == null) {
      subTitle.updateBackground();
      originalBg = subTitle.getBackground();
    }

    if (originalBg instanceof RippleDrawable) {
      RippleDrawable rippleDrawable = (RippleDrawable) originalBg;
      Drawable backgroundDrawable = rippleDrawable.getDrawable(0);

      if (backgroundDrawable instanceof MaterialShapeDrawable) {
        MaterialShapeDrawable shapeDrawable = (MaterialShapeDrawable) backgroundDrawable;

        ColorStateList fillColor = shapeDrawable.getFillColor();
        if (fillColor != null) {
          int color = fillColor.getDefaultColor();
          int alphaColor = ColorUtils.setAlphaComponent(color, 128);

          MaterialShapeDrawable newShapeDrawable =
              new MaterialShapeDrawable(shapeDrawable.getShapeAppearanceModel());
          newShapeDrawable.setFillColor(ColorStateList.valueOf(alphaColor));

          int rippleColor =
              MaterialColors.getColor(subTitle, com.google.android.material.R.attr.colorOnSurface);
          RippleDrawable newRippleDrawable =
              new RippleDrawable(ColorStateList.valueOf(rippleColor), newShapeDrawable, null);

          subTitle.setBackground(newRippleDrawable);
        }
      }
    } else if (originalBg instanceof ColorDrawable) {
      ColorDrawable cd = (ColorDrawable) originalBg;
      int color = cd.getColor();
      int newColor = ColorUtils.setAlphaComponent(color, 128);
      subTitle.setBackground(new ColorDrawable(newColor));
    }
  }

  private void findAndApplyToToolbar(BaseCompat base) {
    ViewGroup rootView = (ViewGroup) base.getWindow().getDecorView();

    AppBarLayout appBarLayout = findAppBarLayout(rootView);
    if (appBarLayout != null) {
      appBarLayout.setBackgroundColor(Color.TRANSPARENT);
      appBarLayout.setBackground(null);
      appBarLayout.setElevation(0f);
      List<TextInputLayout> textInputLayouts = findAllTextInputLayouts(rootView);
      for (TextInputLayout textInputLayout : textInputLayouts) {
        applyAlphaToTextInputLayout(textInputLayout);
      }
      CollapsingToolbarLayout collapsingToolbarLayout = findCollapsingToolbarLayout(appBarLayout);
      if (collapsingToolbarLayout != null) {
        collapsingToolbarLayout.setBackground(new ColorDrawable(Color.TRANSPARENT));
        collapsingToolbarLayout.setContentScrim(null);
        collapsingToolbarLayout.setStatusBarScrim(null);
      }
    }
    List<PerfenceLayoutSubTitle> subTitles = findAllPerfenceLayoutSubTitles(rootView);
    for (PerfenceLayoutSubTitle subTitle : subTitles) {
      applyAlphaToPerfenceLayoutSubTitle(subTitle);
    }
    Toolbar toolbar = findToolbar(rootView);
    if (toolbar != null) {
      toolbar.setBackground(new ColorDrawable(Color.TRANSPARENT));
    }

    ExtendedFloatingActionButton fab1 = findFab(rootView);
    if (fab1 != null) applyAlphaToExtendedFab(fab1);

    NavigationViewCompnet nav = findNav(rootView);
    if (nav != null) {
      applyAlphaToNavigationView(nav);
    }

    List<RecyclerView> recyclerViews = findAllRecyclerViews(rootView);
    for (RecyclerView recyclerView : recyclerViews) {
      setupRecyclerViewScrollListener(recyclerView);
      applyAlphaToAllViewsInRecyclerView(recyclerView);
    }

    List<PreferenceSwitchGroup> switchGroups = findAllSwitchGroups(rootView);
    for (PreferenceSwitchGroup sw : switchGroups) {
      applyAlphaToSwitchGroup(sw);
    }

    List<MaterialCardView> cardViews = findAllMaterialCardViews(rootView);
    for (MaterialCardView cardView : cardViews) {
      applyAlphaToMaterialCardView(cardView);
    }

    List<ListItemView> listItemViews = findAllListItemViews(rootView);
    for (ListItemView listItemView : listItemViews) {
      applyAlphaToListItemView(listItemView);
    }
  }

  private void applyAlphaToAllViewsInRecyclerView(RecyclerView recyclerView) {
    if (recyclerView == null) return;

    int childCount = recyclerView.getChildCount();
    for (int i = 0; i < childCount; i++) {
      View child = recyclerView.getChildAt(i);
      processViewForAlpha(child);
    }
  }

  private void processViewForAlpha(View view) {
    if (view == null) return;

    if (view instanceof PreferenceSwitchGroup) {
      applyAlphaToSwitchGroup((PreferenceSwitchGroup) view);
    } else if (view instanceof PerfenceLayoutSubTitle) {
      applyAlphaToPerfenceLayoutSubTitle((PerfenceLayoutSubTitle) view);
    } else if (view instanceof com.google.android.material.textfield.TextInputLayout) {
      applyAlphaToTextInputLayout((com.google.android.material.textfield.TextInputLayout) view);
    } else if (view instanceof ListItemView) {
      applyAlphaToListItemView((ListItemView) view);
    } else if (view instanceof MaterialCardView) {
      applyAlphaToMaterialCardView((MaterialCardView) view);
    } else if (view instanceof ExtendedFloatingActionButton) {
      applyAlphaToExtendedFab((ExtendedFloatingActionButton) view);
    }

    if (view instanceof ViewGroup) {
      ViewGroup viewGroup = (ViewGroup) view;
      for (int i = 0; i < viewGroup.getChildCount(); i++) {
        processViewForAlpha(viewGroup.getChildAt(i));
      }
    }
  }

  private void setupRecyclerViewScrollListener(RecyclerView recyclerView) {
    recyclerView.addOnScrollListener(
        new RecyclerView.OnScrollListener() {
          @Override
          public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
              applyAlphaToAllViewsInRecyclerView(recyclerView);
            }
          }

          @Override
          public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            applyAlphaToAllViewsInRecyclerView(recyclerView);
          }
        });
  }

  private void applyAlphaToListItemView(ListItemView listItemView) {
    try {
      Drawable originalBg = listItemView.getBackground();

      if (originalBg instanceof RippleDrawable) {
        RippleDrawable rippleDrawable = (RippleDrawable) originalBg;
        Drawable backgroundDrawable = rippleDrawable.getDrawable(0);

        if (backgroundDrawable instanceof GradientDrawable) {
          GradientDrawable gradientDrawable = (GradientDrawable) backgroundDrawable;

          float[] radii = gradientDrawable.getCornerRadii();

          int color = Color.TRANSPARENT;
          ColorStateList colorList = gradientDrawable.getColor();
          if (colorList != null) {
            color = colorList.getDefaultColor();
          }

          int alphaColor = ColorUtils.setAlphaComponent(color, 128);

          GradientDrawable newGradientDrawable = new GradientDrawable();
          if (radii != null) {
            newGradientDrawable.setCornerRadii(radii);
          } else {
            newGradientDrawable.setCornerRadius(gradientDrawable.getCornerRadius());
          }
          newGradientDrawable.setColor(alphaColor);

          int rippleColor =
              MaterialColors.getColor(
                  listItemView, com.google.android.material.R.attr.colorOnSurfaceVariant);
          if (rippleColor == 0) {
            rippleColor = Color.LTGRAY;
          }

          RippleDrawable newRippleDrawable =
              new RippleDrawable(ColorStateList.valueOf(rippleColor), newGradientDrawable, null);

          listItemView.setBackground(newRippleDrawable);
        }
      }
    } catch (Exception e) {
      Log.e(TAG, "Error applying alpha to ListItemView: " + e.getMessage());
    }
  }

  private void applyAlphaToMaterialCardView(MaterialCardView cardView) {
    try {

      ColorStateList cardBackground = cardView.getCardBackgroundColor();
      if (cardBackground != null) {
        int color = cardBackground.getDefaultColor();
        int alphaColor = ColorUtils.setAlphaComponent(color, 128);
        cardView.setCardBackgroundColor(alphaColor);
      }

      if (cardView.getStrokeWidth() > 0) {
        int strokeColor = cardView.getStrokeColor();
        int alphaStrokeColor = ColorUtils.setAlphaComponent(strokeColor, 128);
        cardView.setStrokeColor(alphaStrokeColor);
      }
    } catch (Exception e) {
      Log.e(TAG, "Error applying alpha to MaterialCardView: " + e.getMessage());
    }
  }

  private AppBarLayout findAppBarLayout(ViewGroup viewGroup) {
    for (int i = 0; i < viewGroup.getChildCount(); i++) {
      View child = viewGroup.getChildAt(i);

      if (child instanceof AppBarLayout) {
        return (AppBarLayout) child;
      } else if (child instanceof ViewGroup) {
        AppBarLayout result = findAppBarLayout((ViewGroup) child);
        if (result != null) return result;
      }
    }
    return null;
  }

  private CollapsingToolbarLayout findCollapsingToolbarLayout(ViewGroup viewGroup) {
    for (int i = 0; i < viewGroup.getChildCount(); i++) {
      View child = viewGroup.getChildAt(i);

      if (child instanceof CollapsingToolbarLayout) {
        return (CollapsingToolbarLayout) child;
      } else if (child instanceof ViewGroup) {
        CollapsingToolbarLayout result = findCollapsingToolbarLayout((ViewGroup) child);
        if (result != null) return result;
      }
    }
    return null;
  }

  private List<RecyclerView> findAllRecyclerViews(ViewGroup viewGroup) {
    List<RecyclerView> result = new ArrayList<>();
    findAllRecyclerViewsRecursive(viewGroup, result);
    return result;
  }

  private void findAllRecyclerViewsRecursive(ViewGroup viewGroup, List<RecyclerView> result) {
    for (int i = 0; i < viewGroup.getChildCount(); i++) {
      View child = viewGroup.getChildAt(i);

      if (child instanceof RecyclerView) {
        result.add((RecyclerView) child);
      } else if (child instanceof ViewGroup) {
        findAllRecyclerViewsRecursive((ViewGroup) child, result);
      }
    }
  }

  private void findAndApplyAlphaToSwitchGroupsInView(View view) {
    if (view instanceof PreferenceSwitchGroup) {
      applyAlphaToSwitchGroup((PreferenceSwitchGroup) view);
    } else if (view instanceof PerfenceLayoutSubTitle) {
      applyAlphaToPerfenceLayoutSubTitle((PerfenceLayoutSubTitle) view);
    } else if (view instanceof TextInputLayout) {
      applyAlphaToTextInputLayout((TextInputLayout) view);
    } else if (view instanceof ViewGroup) {
      ViewGroup viewGroup = (ViewGroup) view;
      for (int i = 0; i < viewGroup.getChildCount(); i++) {
        findAndApplyAlphaToSwitchGroupsInView(viewGroup.getChildAt(i));
      }
    }
  }

  private List<com.google.android.material.card.MaterialCardView> findAllMaterialCardViews(
      ViewGroup viewGroup) {
    List<MaterialCardView> result = new ArrayList<>();
    findAllMaterialCardViewsRecursive(viewGroup, result);
    return result;
  }

  private void findAllMaterialCardViewsRecursive(
      ViewGroup viewGroup, List<MaterialCardView> result) {
    for (int i = 0; i < viewGroup.getChildCount(); i++) {
      View child = viewGroup.getChildAt(i);

      if (child instanceof MaterialCardView) {
        result.add((MaterialCardView) child);
      } else if (child instanceof ViewGroup) {
        findAllMaterialCardViewsRecursive((ViewGroup) child, result);
      }
    }
  }

  private List<ListItemView> findAllListItemViews(ViewGroup viewGroup) {
    List<ListItemView> result = new ArrayList<>();
    findAllListItemViewsRecursive(viewGroup, result);
    return result;
  }

  private void findAllListItemViewsRecursive(ViewGroup viewGroup, List<ListItemView> result) {
    for (int i = 0; i < viewGroup.getChildCount(); i++) {
      View child = viewGroup.getChildAt(i);

      if (child instanceof ListItemView) {
        result.add((ListItemView) child);
      } else if (child instanceof ViewGroup) {
        findAllListItemViewsRecursive((ViewGroup) child, result);
      }
    }
  }

  private List<PreferenceSwitchGroup> findAllSwitchGroups(ViewGroup viewGroup) {
    List<PreferenceSwitchGroup> switchGroups = new ArrayList<>();
    findAllSwitchGroupsRecursive(viewGroup, switchGroups);
    return switchGroups;
  }

  private void findAllSwitchGroupsRecursive(
      ViewGroup viewGroup, List<PreferenceSwitchGroup> result) {
    for (int i = 0; i < viewGroup.getChildCount(); i++) {
      View child = viewGroup.getChildAt(i);

      if (child instanceof PreferenceSwitchGroup) {
        result.add((PreferenceSwitchGroup) child);
      } else if (child instanceof ViewGroup) {
        findAllSwitchGroupsRecursive((ViewGroup) child, result);
      }
    }
  }

  private void applyAlphaToExtendedFab(ExtendedFloatingActionButton fab) {
    Drawable originalBg = fab.getBackground();

    if (originalBg == null) {
      int fallback = ColorUtils.setAlphaComponent(Color.parseColor("#FF6200EE"), 128);
      fab.setBackgroundColor(fallback);
      return;
    }

    if (originalBg instanceof MaterialShapeDrawable) {
      MaterialShapeDrawable shapeDrawable = (MaterialShapeDrawable) originalBg;
      ShapeAppearanceModel shapeModel = shapeDrawable.getShapeAppearanceModel();

      ColorStateList fillColor = shapeDrawable.getFillColor();
      if (fillColor != null) {
        int color = fillColor.getDefaultColor();
        int alphaColor = ColorUtils.setAlphaComponent(color, 128);

        MaterialShapeDrawable newShapeDrawable = new MaterialShapeDrawable();
        newShapeDrawable.setShapeAppearanceModel(shapeModel);
        newShapeDrawable.setFillColor(ColorStateList.valueOf(alphaColor));

        if (shapeDrawable.getStrokeWidth() > 0) {
          newShapeDrawable.setStroke(
              shapeDrawable.getStrokeWidth(), shapeDrawable.getStrokeColor());
        }

        newShapeDrawable.setElevation(shapeDrawable.getElevation());

        fab.setBackground(newShapeDrawable);
      }
    } else if (originalBg instanceof RippleDrawable) {

      RippleDrawable rippleDrawable = (RippleDrawable) originalBg;
      Drawable backgroundDrawable = rippleDrawable.getDrawable(0);

      if (backgroundDrawable instanceof MaterialShapeDrawable) {
        MaterialShapeDrawable shapeDrawable = (MaterialShapeDrawable) backgroundDrawable;
        ShapeAppearanceModel shapeModel = shapeDrawable.getShapeAppearanceModel();

        ColorStateList fillColor = shapeDrawable.getFillColor();
        if (fillColor != null) {
          int color = fillColor.getDefaultColor();
          int alphaColor = ColorUtils.setAlphaComponent(color, 128);

          MaterialShapeDrawable newShapeDrawable = new MaterialShapeDrawable();
          newShapeDrawable.setShapeAppearanceModel(shapeModel);
          newShapeDrawable.setFillColor(ColorStateList.valueOf(alphaColor));

          if (shapeDrawable.getStrokeWidth() > 0) {
            newShapeDrawable.setStroke(
                shapeDrawable.getStrokeWidth(), shapeDrawable.getStrokeColor());
          }

          int rippleColor =
              MaterialColors.getColor(fab, com.google.android.material.R.attr.colorOnPrimary);
          RippleDrawable newRippleDrawable =
              new RippleDrawable(ColorStateList.valueOf(rippleColor), newShapeDrawable, null);

          fab.setBackground(newRippleDrawable);
        }
      } else if (backgroundDrawable instanceof GradientDrawable) {

        GradientDrawable gradientDrawable = (GradientDrawable) backgroundDrawable;

        float[] radii = gradientDrawable.getCornerRadii();
        int color =
            gradientDrawable.getColor() != null
                ? gradientDrawable.getColor().getDefaultColor()
                : Color.TRANSPARENT;

        int alphaColor = ColorUtils.setAlphaComponent(color, 128);

        GradientDrawable newGradientDrawable = new GradientDrawable();
        if (radii != null) {
          newGradientDrawable.setCornerRadii(radii);
        } else {
          newGradientDrawable.setCornerRadius(gradientDrawable.getCornerRadius());
        }
        newGradientDrawable.setColor(alphaColor);

        int rippleColor =
            MaterialColors.getColor(fab, com.google.android.material.R.attr.colorOnPrimary);
        RippleDrawable newRippleDrawable =
            new RippleDrawable(ColorStateList.valueOf(rippleColor), newGradientDrawable, null);

        fab.setBackground(newRippleDrawable);
      }
    } else if (originalBg instanceof GradientDrawable) {
      GradientDrawable gradientDrawable = (GradientDrawable) originalBg;

      float[] radii = gradientDrawable.getCornerRadii();
      int color =
          gradientDrawable.getColor() != null
              ? gradientDrawable.getColor().getDefaultColor()
              : Color.TRANSPARENT;

      int alphaColor = ColorUtils.setAlphaComponent(color, 128);

      GradientDrawable newGradientDrawable = new GradientDrawable();
      if (radii != null) {
        newGradientDrawable.setCornerRadii(radii);
      } else {
        newGradientDrawable.setCornerRadius(gradientDrawable.getCornerRadius());
      }
      newGradientDrawable.setColor(alphaColor);

      fab.setBackground(newGradientDrawable);
    } else if (originalBg instanceof ColorDrawable) {
      ColorDrawable cd = (ColorDrawable) originalBg;
      int color = cd.getColor();
      int newColor = ColorUtils.setAlphaComponent(color, 128);
      fab.setBackground(new ColorDrawable(newColor));
    } else {

      int primaryColor = MaterialColors.getColor(fab, ObjectUtils.colorPrimary);
      if (primaryColor == 0) {
        primaryColor = Color.parseColor("#FF6200EE");
      }
      int alphaColor = ColorUtils.setAlphaComponent(primaryColor, 128);

      MaterialShapeDrawable shapeDrawable =
          new MaterialShapeDrawable(ShapeAppearanceModel.builder().setAllCornerSizes(16f).build());
      shapeDrawable.setFillColor(ColorStateList.valueOf(alphaColor));
      shapeDrawable.setElevation(fab.getElevation());

      fab.setBackground(shapeDrawable);
    }
  }

  private void applyAlphaToNavigationView(NavigationViewCompnet navView) {
    Drawable originalBg = navView.getBackground();

    if (originalBg == null) {
      int fallback = ColorUtils.setAlphaComponent(Color.parseColor("#FF14181B"), 128);
      navView.setBackground(new ColorDrawable(fallback));
      return;
    }

    if (originalBg instanceof MaterialShapeDrawable) {
      MaterialShapeDrawable shapeDrawable = (MaterialShapeDrawable) originalBg;
      ShapeAppearanceModel shapeModel = shapeDrawable.getShapeAppearanceModel();

      ColorStateList fillColor = shapeDrawable.getFillColor();
      if (fillColor != null) {
        int color = fillColor.getDefaultColor();
        int alphaColor = ColorUtils.setAlphaComponent(color, 128);

        MaterialShapeDrawable newShapeDrawable = new MaterialShapeDrawable();
        newShapeDrawable.setShapeAppearanceModel(shapeModel);
        newShapeDrawable.setFillColor(ColorStateList.valueOf(alphaColor));

        newShapeDrawable.setStroke(shapeDrawable.getStrokeWidth(), shapeDrawable.getStrokeColor());
        newShapeDrawable.setElevation(shapeDrawable.getElevation());

        navView.setBackground(newShapeDrawable);
      }
    } else if (originalBg instanceof RippleDrawable) {

      RippleDrawable rippleDrawable = (RippleDrawable) originalBg;
      Drawable backgroundDrawable = rippleDrawable.getDrawable(0);

      if (backgroundDrawable instanceof MaterialShapeDrawable) {
        MaterialShapeDrawable shapeDrawable = (MaterialShapeDrawable) backgroundDrawable;
        ShapeAppearanceModel shapeModel = shapeDrawable.getShapeAppearanceModel();

        ColorStateList fillColor = shapeDrawable.getFillColor();
        if (fillColor != null) {
          int color = fillColor.getDefaultColor();
          int alphaColor = ColorUtils.setAlphaComponent(color, 128);

          MaterialShapeDrawable newShapeDrawable = new MaterialShapeDrawable();
          newShapeDrawable.setShapeAppearanceModel(shapeModel);
          newShapeDrawable.setFillColor(ColorStateList.valueOf(alphaColor));

          int rippleColor =
              MaterialColors.getColor(navView, com.google.android.material.R.attr.colorOnSurface);
          RippleDrawable newRippleDrawable =
              new RippleDrawable(ColorStateList.valueOf(rippleColor), newShapeDrawable, null);

          navView.setBackground(newRippleDrawable);
        }
      }
    } else if (originalBg instanceof GradientDrawable) {

      GradientDrawable gradientDrawable = (GradientDrawable) originalBg;

      float[] radii = gradientDrawable.getCornerRadii();
      int color =
          gradientDrawable.getColor() != null
              ? gradientDrawable.getColor().getDefaultColor()
              : Color.TRANSPARENT;

      int alphaColor = ColorUtils.setAlphaComponent(color, 128);

      GradientDrawable newGradientDrawable = new GradientDrawable();
      if (radii != null) {
        newGradientDrawable.setCornerRadii(radii);
      } else {
        newGradientDrawable.setCornerRadius(gradientDrawable.getCornerRadius());
      }
      newGradientDrawable.setColor(alphaColor);

      navView.setBackground(newGradientDrawable);
    } else if (originalBg instanceof ColorDrawable) {
      ColorDrawable cd = (ColorDrawable) originalBg;
      int color = cd.getColor();
      int newColor = ColorUtils.setAlphaComponent(color, 128);
      navView.setBackground(new ColorDrawable(newColor));
    } else {

      int fallback = ColorUtils.setAlphaComponent(Color.parseColor("#FF14181B"), 128);
      navView.setBackground(new ColorDrawable(fallback));
    }
  }

  private void applyAlphaToSwitchGroup(PreferenceSwitchGroup switchGroup) {
    Drawable originalBg = switchGroup.getBackground();

    if (originalBg == null) {

      switchGroup.updateBackground();
      originalBg = switchGroup.getBackground();
    }

    if (originalBg instanceof RippleDrawable) {
      RippleDrawable rippleDrawable = (RippleDrawable) originalBg;
      Drawable backgroundDrawable = rippleDrawable.getDrawable(0);

      if (backgroundDrawable instanceof MaterialShapeDrawable) {
        MaterialShapeDrawable shapeDrawable = (MaterialShapeDrawable) backgroundDrawable;

        ColorStateList fillColor = shapeDrawable.getFillColor();
        if (fillColor != null) {
          int color = fillColor.getDefaultColor();
          int alphaColor = ColorUtils.setAlphaComponent(color, 128);

          MaterialShapeDrawable newShapeDrawable =
              new MaterialShapeDrawable(shapeDrawable.getShapeAppearanceModel());
          newShapeDrawable.setFillColor(ColorStateList.valueOf(alphaColor));

          int rippleColor =
              MaterialColors.getColor(
                  switchGroup, com.google.android.material.R.attr.colorOnSurface);
          RippleDrawable newRippleDrawable =
              new RippleDrawable(ColorStateList.valueOf(rippleColor), newShapeDrawable, null);

          switchGroup.setBackground(newRippleDrawable);
        }
      }
    } else if (originalBg instanceof ColorDrawable) {
      ColorDrawable cd = (ColorDrawable) originalBg;
      int color = cd.getColor();
      int newColor = ColorUtils.setAlphaComponent(color, 128);
      switchGroup.setBackground(new ColorDrawable(newColor));
    }
  }

  private void applyAlphaToSubTile(PerfenceLayoutSubTitle switchGroup) {
    Drawable originalBg = switchGroup.getBackground();

    if (originalBg == null) {

      switchGroup.updateBackground();
      originalBg = switchGroup.getBackground();
    }

    if (originalBg instanceof RippleDrawable) {
      RippleDrawable rippleDrawable = (RippleDrawable) originalBg;
      Drawable backgroundDrawable = rippleDrawable.getDrawable(0);

      if (backgroundDrawable instanceof MaterialShapeDrawable) {
        MaterialShapeDrawable shapeDrawable = (MaterialShapeDrawable) backgroundDrawable;

        ColorStateList fillColor = shapeDrawable.getFillColor();
        if (fillColor != null) {
          int color = fillColor.getDefaultColor();
          int alphaColor = ColorUtils.setAlphaComponent(color, 128);

          MaterialShapeDrawable newShapeDrawable =
              new MaterialShapeDrawable(shapeDrawable.getShapeAppearanceModel());
          newShapeDrawable.setFillColor(ColorStateList.valueOf(alphaColor));

          int rippleColor =
              MaterialColors.getColor(
                  switchGroup, com.google.android.material.R.attr.colorOnSurface);
          RippleDrawable newRippleDrawable =
              new RippleDrawable(ColorStateList.valueOf(rippleColor), newShapeDrawable, null);

          switchGroup.setBackground(newRippleDrawable);
        }
      }
    } else if (originalBg instanceof ColorDrawable) {
      ColorDrawable cd = (ColorDrawable) originalBg;
      int color = cd.getColor();
      int newColor = ColorUtils.setAlphaComponent(color, 128);
      switchGroup.setBackground(new ColorDrawable(newColor));
    }
  }

  private void applyAlphaToBackground(View view) {
    Drawable bg = view.getBackground();

    if (bg instanceof ColorDrawable) {
      ColorDrawable cd = (ColorDrawable) bg;
      int color = cd.getColor();
      int newColor = ColorUtils.setAlphaComponent(color, 128);
      view.setBackground(new ColorDrawable(newColor));
    }
  }

  ExtendedFloatingActionButton findFab(ViewGroup viewGroup) {
    for (int i = 0; i < viewGroup.getChildCount(); i++) {
      View child = viewGroup.getChildAt(i);

      if (child instanceof ExtendedFloatingActionButton) {
        return (ExtendedFloatingActionButton) child;
      } else if (child instanceof ViewGroup) {
        ExtendedFloatingActionButton nav = findFab((ViewGroup) child);
        if (nav != null) {
          return nav;
        }
      }
    }
    return null;
  }

  NavigationViewCompnet findNav(ViewGroup viewGroup) {
    for (int i = 0; i < viewGroup.getChildCount(); i++) {
      View child = viewGroup.getChildAt(i);

      if (child instanceof NavigationViewCompnet) {
        return (NavigationViewCompnet) child;
      } else if (child instanceof ViewGroup) {
        NavigationViewCompnet nav = findNav((ViewGroup) child);
        if (nav != null) {
          return nav;
        }
      }
    }
    return null;
  }

  private Toolbar findToolbar(ViewGroup viewGroup) {
    for (int i = 0; i < viewGroup.getChildCount(); i++) {
      View child = viewGroup.getChildAt(i);

      if (child instanceof Toolbar) {
        return (Toolbar) child;
      } else if (child instanceof ViewGroup) {
        Toolbar toolbar = findToolbar((ViewGroup) child);
        if (toolbar != null) {
          return toolbar;
        }
      }
    }
    return null;
  }

  public class WallpaperTransformation extends BitmapTransformation {
    private static final String ID = "ir.ninjacoder.WallpaperTransformation";
    private final int radius;
    private final int sampling;
    private final float brightness;
    private final int contrast;
    private final float saturation;
    private final float opacity;

    public WallpaperTransformation(
        int radius, int sampling, float brightness, int contrast, float saturation, float opacity) {
      this.radius = radius;
      this.sampling = sampling;
      this.brightness = brightness;
      this.contrast = contrast;
      this.saturation = saturation;
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
        return applyEffects(blurred);
      } catch (Exception e) {
        Log.e("Wallpaper", "Error in transformation: " + e.getMessage());
        return toTransform;
      }
    }

    private Bitmap applyEffects(Bitmap bitmap) {
      Bitmap resultBitmap =
          Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
      Canvas canvas = new Canvas(resultBitmap);
      Paint paint = new Paint();
      ColorMatrix colorMatrix = new ColorMatrix();

      float normalizedBrightness = brightness / 100f;
      ColorMatrix brightnessMatrix = new ColorMatrix();
      brightnessMatrix.set(
          new float[] {
            1, 0, 0, 0, normalizedBrightness * 255,
            0, 1, 0, 0, normalizedBrightness * 255,
            0, 0, 1, 0, normalizedBrightness * 255,
            0, 0, 0, 1, 0
          });

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

      ColorMatrix saturationMatrix = new ColorMatrix();
      saturationMatrix.setSaturation(saturation);

      colorMatrix.postConcat(brightnessMatrix);
      colorMatrix.postConcat(contrastMatrix);
      colorMatrix.postConcat(saturationMatrix);

      paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
      paint.setAlpha((int) (opacity * 255));

      canvas.drawBitmap(bitmap, 0, 0, paint);
      return resultBitmap;
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
      messageDigest.update(
          (ID + radius + sampling + brightness + contrast + saturation + opacity)
              .getBytes(CHARSET));
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

      float normalizedBrightness = brightness / 100f;
      ColorMatrix brightnessMatrix = new ColorMatrix();
      brightnessMatrix.set(
          new float[] {
            1, 0, 0, 0, normalizedBrightness * 255,
            0, 1, 0, 0, normalizedBrightness * 255,
            0, 0, 1, 0, normalizedBrightness * 255,
            0, 0, 0, 1, 0
          });

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

      ColorMatrix saturationMatrix = new ColorMatrix();
      saturationMatrix.setSaturation(saturation);

      ColorMatrix hueMatrix = new ColorMatrix();
      hueMatrix.setRotate(0, hue * 360);
      hueMatrix.setRotate(1, hue * 360);
      hueMatrix.setRotate(2, hue * 360);

      colorMatrix.postConcat(brightnessMatrix);
      colorMatrix.postConcat(contrastMatrix);
      colorMatrix.postConcat(saturationMatrix);
      colorMatrix.postConcat(hueMatrix);

      paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));

      paint.setAlpha((int) (opacity * 255));

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
