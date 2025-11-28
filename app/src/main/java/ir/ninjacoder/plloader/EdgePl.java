package ir.ninjacoder.plloader;

import android.os.Build;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.view.Gravity;
import android.view.Window;
import android.view.View;
import android.graphics.Color;
import android.view.WindowManager;
import android.util.TypedValue;
import android.content.Context;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import ir.ninjacoder.ghostide.core.widget.ExrtaFab;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.activity.EdgeToEdge;
import androidx.core.view.ViewCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.WindowInsetsCompat;
import ir.ninjacoder.ghostide.core.activities.BaseCompat;
import ir.ninjacoder.ghostide.core.activities.FileManagerActivity;
import ir.ninjacoder.ghostide.core.activities.CodeEditorActivity;
import io.github.rosemoe.sora.widget.CodeEditor;
import ir.ninjacoder.ghostide.core.pl.PluginManagerCompat;

import java.lang.reflect.Field;

public class EdgePl implements PluginManagerCompat {

  private int originalMarginBottom;
  private int originalMarginTop;
  private CoordinatorLayout.LayoutParams originalParams;
  private ImageView imageView; // ذخیره رفرنس ImageView
  private View fab; // ذخیره رفرنس FAB

  @Override
  public void getEditor(CodeEditor arg0) {}

  @Override
  public String setName() {
    return "EdgeToEdgePlugin";
  }

  @Override
  public boolean hasuseing() {
    return true;
  }

  @Override
  public void getFileManagerAc(FileManagerActivity ac) {}

  @Override
  public void getCodeEditorAc(CodeEditorActivity ac) {}

  @Override
  public void getBaseCompat(BaseCompat ac) {
    if (ac instanceof CodeEditorActivity) {
      CodeEditorActivity codeEditorActivity = (CodeEditorActivity) ac;

      EdgeToEdge.enable(ac);
      setupWindowForKeyboard(ac.getWindow());
      transparentNavigationBar(ac.getWindow());

      ac.getWindow()
          .getDecorView()
          .post(
              () -> {
                adjustFabPosition(codeEditorActivity);
                setupKeyboardListener(codeEditorActivity);
              });
    }
  }

  @Override
  public String langModel() {
    return "all";
  }

  private void setupWindowForKeyboard(Window window) {
    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
  }

  private void adjustFabPosition(CodeEditorActivity activity) {
    if (activity == null || activity.isFinishing()) return;

    try {
      Field fabField = CodeEditorActivity.class.getDeclaredField("_fab");
      fabField.setAccessible(true);
      fab = (View) fabField.get(activity);

      if (fab != null) {
        CoordinatorLayout.LayoutParams params =
            (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
        originalParams = params;
        originalMarginBottom = params.bottomMargin;
        originalMarginTop = params.topMargin;

        int marginBottom = dpToPx(activity, 42);
        int marginTop = dpToPx(activity, 26);
        int marginHorizontal = dpToPx(activity, 16);

        params.setMargins(marginHorizontal, marginTop, marginHorizontal, marginBottom);
        fab.post(() -> fab.setLayoutParams(params));

        if (fab.getVisibility() != View.VISIBLE) {
          fab.setVisibility(View.VISIBLE);
        }

        // ایجاد ImageView بالای FAB
        createImageViewAboveFab(activity, fab);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void createImageViewAboveFab(CodeEditorActivity activity, View fab) {
    imageView = new ImageView(activity);
    imageView.setId(View.generateViewId());

    Glide.with(imageView.getContext())
        .load("/storage/emulated/0/Download/item1.png")
        .fitCenter()
        .diskCacheStrategy(DiskCacheStrategy.NONE)
        .override(50,50)
        .into(imageView);

    int size = dpToPx(activity, 40);
    CoordinatorLayout.LayoutParams p = new CoordinatorLayout.LayoutParams(size, size);

    CoordinatorLayout parent = findCoordinatorLayout(activity);
    if (parent == null) return;

    parent.addView(imageView, p);

    // موقعیت اولیه رو تنظیم کن
    updateImagePosition();
  }

  // متد برای آپدیت موقعیت ImageView
  private void updateImagePosition() {
    if (fab == null || imageView == null) return;

    fab.post(
        () -> {
          int size = dpToPx(imageView.getContext(), 40);
          int fabX = (int) (fab.getX() + fab.getWidth() / 2f - size / 2f);
          // تغییر این خط - اضافه کردن 3 dp به Y موقعیت
          int fabY = (int) (fab.getY() - size - dpToPx(imageView.getContext(), -3)); // حتی پایین‌تر
          imageView.setX(fabX);
          imageView.setY(fabY);
          
        });
  }

  private CoordinatorLayout findCoordinatorLayout(CodeEditorActivity activity) {
    View rootView = activity.getWindow().getDecorView();
    return findCoordinatorLayoutRecursive((ViewGroup) rootView);
  }

  private CoordinatorLayout findCoordinatorLayoutRecursive(ViewGroup viewGroup) {
    for (int i = 0; i < viewGroup.getChildCount(); i++) {
      View child = viewGroup.getChildAt(i);
      if (child instanceof CoordinatorLayout) {
        return (CoordinatorLayout) child;
      } else if (child instanceof ViewGroup) {
        CoordinatorLayout result = findCoordinatorLayoutRecursive((ViewGroup) child);
        if (result != null) {
          return result;
        }
      }
    }
    return null;
  }

  private void setupKeyboardListener(CodeEditorActivity activity) {
    if (activity == null || activity.isFinishing()) return;

    try {
      Field fabField = CodeEditorActivity.class.getDeclaredField("_fab");
      fabField.setAccessible(true);
      fab = (View) fabField.get(activity);

      if (fab == null) return;

      View rootView = activity.getWindow().getDecorView();

      ViewCompat.setOnApplyWindowInsetsListener(
          rootView,
          (v, insets) -> {
            try {
              Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
              Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());

              if (fab != null && originalParams != null) {
                CoordinatorLayout.LayoutParams params =
                    (CoordinatorLayout.LayoutParams) fab.getLayoutParams();

                if (ime.bottom > 0) {
                  int keyboardHeight = ime.bottom - systemBars.bottom;
                  int extraSpace = dpToPx(activity, 60);
                  params.setMargins(
                      params.leftMargin,
                      params.topMargin,
                      params.rightMargin,
                      originalMarginBottom + keyboardHeight + extraSpace);
                } else {
                  params.setMargins(
                      params.leftMargin,
                      params.topMargin,
                      params.rightMargin,
                      originalMarginBottom);
                }

                fab.post(
                    () -> {
                      fab.setLayoutParams(params);
                      // موقعیت ImageView رو هم آپدیت کن
                      updateImagePosition();
                    });
              }
            } catch (Exception e) {
              e.printStackTrace();
            }

            return insets;
          });

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  void transparentNavigationBar(Window window) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      window.setNavigationBarContrastEnforced(false);
    }
    window.setNavigationBarColor(Color.TRANSPARENT);
  }

  private int dpToPx(Context context, int dp) {
    return (int)
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
  }
}
