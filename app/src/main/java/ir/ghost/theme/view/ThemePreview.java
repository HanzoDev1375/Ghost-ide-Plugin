package ir.ghost.theme.view;

import android.content.Context;
import android.graphics.Color;
import io.github.rosemoe.sora.widget.CodeEditor;
import java.lang.reflect.Field;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.widget.LinearLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ir.ghost.theme.model.CompactColorPickerView;
import ir.ghost.theme.model.GhostTheme;
import ir.ghost.theme.model.SpacingItemDecoration;
import ir.ninjacoder.ghostide.core.utils.FileUtil;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ThemePreview {
  private Context context;
  private String themepath;
  private GhostTheme theme;
  private Gson gson = new GsonBuilder().setPrettyPrinting().create();
  private BottomSheetDialog dialog;
  private RecyclerView rv;
  private ThemePreviewAdapter adapter;
  private TextInputLayout searchLayout;
  private TextInputEditText searchEditText;
  private CodeEditor editor;

  public ThemePreview(Context context, String themepath, CodeEditor editor) {
    this.context = context;
    this.themepath = themepath;
    this.editor = editor; // ممکن است null باشد
    theme = gson.fromJson(FileUtil.readFile(themepath), GhostTheme.class);
    rv = new RecyclerView(context);
    dialog = new BottomSheetDialog(context);
    adapter = new ThemePreviewAdapter(context, theme);

    init();
  }

  void init() {
    // تنظیمات RecyclerView
    rv.setLayoutManager(new LinearLayoutManager(context));
    rv.setAdapter(adapter);
    rv.setHasFixedSize(true);
    rv.setClipToPadding(false);
    rv.addItemDecoration(new SpacingItemDecoration(2, 8, context));

    // تنظیم کلیک‌لیسنر برای آیتم‌ها
    adapter.setOnItemClickListener(
        new ThemePreviewAdapter.OnItemClickListener() {
          @Override
          public void onItemClick(ThemePreviewAdapter.ThemeItem item) {
            showColorPickerDialog(item);
          }
        });

    // ساخت Search Input
    searchLayout = new TextInputLayout(context);
    searchLayout.setLayoutParams(
        new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    searchLayout.setHint("Search colors...");
    searchLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);

    searchEditText = new TextInputEditText(searchLayout.getContext());
    searchEditText.setLayoutParams(
        new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    searchEditText.setSingleLine(true);

    searchLayout.addView(searchEditText);

    // اضافه کردن TextWatcher برای جستجوی زنده
    searchEditText.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {
            adapter.filter(s.toString());
          }

          @Override
          public void afterTextChanged(Editable s) {}
        });

    // ساخت layout به صورت برنامه‌نویسی
    LinearLayout layout = new LinearLayout(context);
    layout.setOrientation(LinearLayout.VERTICAL);
    LinearLayout.LayoutParams par =
        new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
    par.gravity = Gravity.CENTER_HORIZONTAL;
    layout.setLayoutParams(par);

    // اضافه کردن Search و RecyclerView به layout
    int padding = dpToPx(16);
    layout.setPadding(padding, padding, padding, 0);

    layout.addView(searchLayout);

    LinearLayout.LayoutParams rvParams =
        new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    rvParams.setMargins(0, dpToPx(16), 0, 0);
    rv.setLayoutParams(rvParams);

    layout.addView(rv);

    // تنظیم content view برای dialog
    dialog.setContentView(layout);
  }

  private void showColorPickerDialog(ThemePreviewAdapter.ThemeItem item) {
    // ایجاد ColorPicker
    CompactColorPickerView colorPicker = new CompactColorPickerView(context);

    try {
      // تنظیم رنگ اولیه
      var color = Color.parseColor(item.getHexColor());
      colorPicker.setInitialColor(color);
    } catch (IllegalArgumentException e) {
      // در صورت خطا در پارس کردن رنگ
      int defaultColor = android.graphics.Color.parseColor("#FF0000");
      colorPicker.setInitialColor(defaultColor);
    }

    // ساخت AlertDialog با استایل متریال
    AlertDialog dialog =
        new MaterialAlertDialogBuilder(context)
            .setTitle("Edit Color: " + item.getName())
            .setView(colorPicker)
            .setNegativeButton("Cancel", null)
            .setPositiveButton(
                "Save",
                (d, which) -> {
                  // ذخیره رنگ جدید
                  String newHexColor = colorPicker.getHexColor();
                  updateThemeColor(item.getName(), newHexColor);

                  // آپدیت آداپتور
                  adapter.updateColor(item.getName(), newHexColor);

                  // آپدیت editor اگر null نباشد
                  if (editor != null) {
                    editor.postDelayed(
                        new Runnable() {
                          @Override
                          public void run() {
                            try {
                              editor.invalidate();
                            } catch (Exception err) {
                              // خطا را نادیده بگیر
                            }
                          }
                        },
                        1000);
                  }
                })
            .create();

    // اضافه کردن Listener برای ColorPicker (اختیاری - برای نمایش تغییرات لحظه‌ای)
    colorPicker.setOnColorChangedListener(
        new CompactColorPickerView.OnColorChangedListener() {
          @Override
          public void onColorChanged(int color, String hexColor) {
            // می‌توانید تغییرات لحظه‌ای را اینجا هندل کنید
          }
        });

    dialog.show();
  }

  private void updateThemeColor(String fieldName, String newHexColor) {
    try {
      // آپدیت تم در حافظه
      Field field = GhostTheme.class.getDeclaredField(fieldName);
      field.setAccessible(true);
      field.set(theme, newHexColor);

      // ذخیره در فایل
      saveThemeToFile();

    } catch (NoSuchFieldException | IllegalAccessException e) {
      e.printStackTrace();
    }
  }

  private void saveThemeToFile() {
    try {
      String json = gson.toJson(theme);
      File file = new File(themepath);

      FileWriter writer = new FileWriter(file);
      writer.write(json);
      writer.flush();
      writer.close();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private int dpToPx(int dp) {
    float density = context.getResources().getDisplayMetrics().density;
    return Math.round(dp * density);
  }

  public void show() {
    if (!dialog.isShowing()) {
      dialog.show();
      // پاک کردن متن جستجو هنگام نمایش
      searchEditText.setText("");
    }
  }

  public void dismiss() {
    if (dialog.isShowing()) {
      dialog.dismiss();
    }
  }
}
