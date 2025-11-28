package ir.ninjacoder.plloader.img;

import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import io.github.rosemoe.sora.event.SelectionChangeEvent;
import io.github.rosemoe.sora.langs.html.HTMLLanguage;
import io.github.rosemoe.sora.widget.CodeEditor;
import io.github.rosemoe.sora.widget.EditorColorScheme;
import ir.ninjacoder.ghostide.core.IdeEditor;
import ir.ninjacoder.ghostide.core.activities.BaseCompat;
import ir.ninjacoder.ghostide.core.activities.CodeEditorActivity;
import ir.ninjacoder.ghostide.core.activities.FileManagerActivity;
import ir.ninjacoder.ghostide.core.pl.PluginManagerCompat;
import ir.ninjacoder.plloader.EditorPopUp;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class ImagePreview implements PluginManagerCompat {

  private boolean isProcessing = false;
  private long lastProcessTime = 0;
  private final int PROCESS_DELAY = 100;
  private int originalCursorColor;
  private boolean isCursorColorChanged = false;
  private CodeEditor currentEditor;
  private CodeEditorActivity codeEditorActivity;
  private String lastImagePath = "";
  private boolean isHtmlFile = false;
  private TabLayout tabLayout;

  private static final Pattern IMAGE_PATH_PATTERN =
      Pattern.compile(
          "([a-zA-Z0-9_\\-./\\\\]+)\\.(png|jpg|jpeg|gif|bmp|webp|svg)", Pattern.CASE_INSENSITIVE);

  @Override
  public void getCodeEditorAc(CodeEditorActivity arg0) {
    codeEditorActivity = arg0;
    setupTabChangeListener();
  }

  @Override
  public void getEditor(CodeEditor editor) {
    this.currentEditor = editor;

    if (editor == null) return;

    editor.postDelayed(
        () -> {
          try {
            if (editor.getContext() instanceof CodeEditorActivity) {
              codeEditorActivity = (CodeEditorActivity) editor.getContext();
              setupTabChangeListener();
              updateFileType(); // اولین بار فایل تایپ رو چک کن
              setupEventListeners(editor);
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
        },
        500);
  }

  private void setupTabChangeListener() {
    try {
      if (codeEditorActivity == null) return;

      // پیدا کردن TabLayout با ریفلکشن
      Field field = codeEditorActivity.getClass().getDeclaredField("tablayouteditor");
      field.setAccessible(true);
      tabLayout = (TabLayout) field.get(codeEditorActivity);

      if (tabLayout != null) {
        tabLayout.addOnTabSelectedListener(
            new TabLayout.OnTabSelectedListener() {
              @Override
              public void onTabSelected(TabLayout.Tab tab) {
                // وقتی تب عوض شد، فایل تایپ رو آپدیت کن
                updateFileType();
              }

              @Override
              public void onTabUnselected(TabLayout.Tab tab) {}

              @Override
              public void onTabReselected(TabLayout.Tab tab) {}
            });
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void updateFileType() {
    try {
      if (codeEditorActivity == null) return;

      // استفاده از متد getcurrentFileType که در CodeEditorActivity هست
      Method method = codeEditorActivity.getClass().getMethod("getcurrentFileType");
      String fileType = (String) method.invoke(codeEditorActivity);

      isHtmlFile = fileType != null && (fileType.endsWith(".html"));

      // اگر فایل HTML شد، زبان رو ست کن
      if (isHtmlFile && currentEditor != null) {
        currentEditor.post(
            () -> {
              try {
                currentEditor.invalidate();
              } catch (Exception e) {
                e.printStackTrace();
              }
            });
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void setupEventListeners(CodeEditor editor) {
    // ذخیره رنگ اصلی کرسر
    originalCursorColor = editor.getColorScheme().getColor(EditorColorScheme.SELECTION_INSERT);

    // تنظیم image preview event
    editor.subscribeEvent(
        SelectionChangeEvent.class,
        (event, unsubscribe) -> {
          if (isProcessing) return;

          long currentTime = System.currentTimeMillis();
          if (currentTime - lastProcessTime < PROCESS_DELAY) {
            return;
          }
          lastProcessTime = currentTime;

          try {
            // هر بار چک کن که فایل هنوز HTML هست یا نه
            updateFileType();

            // فقط برای فایل‌های HTML پردازش کن
            if (!isHtmlFile) {
              restoreOriginalCursorColor();
              return;
            }

            int cursorLine = event.getLeft().getLine();
            int cursorColumn = event.getLeft().getColumn();
            String currentLine = editor.getText().getLineString(cursorLine);

            ImagePathResult result = findImagePathAtPosition(currentLine, cursorColumn);

            if (result != null && result.filePath != null) {
              changeCursorColorToGreen(editor);
              showImagePreview(editor, result.filePath);
              lastImagePath = result.filePath;
            } else {
              restoreOriginalCursorColor();
              lastImagePath = "";
            }
          } catch (Exception e) {
            restoreOriginalCursorColor();
          }
        });
  }

  private void showImagePreview(CodeEditor editor, String filePath) {
    try {
      // دوباره چک کن که فایل هنوز HTML هست
      if (!isHtmlFile) return;

      String fullPath = resolveFilePath(filePath);

      if (fullPath != null && new File(fullPath).exists()) {
        // ایجاد ImageView
        MaterialCardView cardView = new MaterialCardView(editor.getContext());
        MaterialCardView.LayoutParams cardParams =
            new MaterialCardView.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(16, 16, 16, 16);
        cardView.setLayoutParams(cardParams);
        cardView.setRadius(24);
        cardView.setCardElevation(3);
        cardView.setUseCompatPadding(true);

        LinearLayout linearLayout = new LinearLayout(editor.getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(16, 16, 16, 16);

        ImageView imageView = new ImageView(editor.getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(300, 300);
        imageView.setLayoutParams(params);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setAdjustViewBounds(true);

        Glide.with(imageView.getContext())
            .load(fullPath)
            .error(android.R.drawable.ic_delete)
            .into(imageView);

        linearLayout.addView(imageView);
        cardView.addView(linearLayout);

        EditorPopUp.showCustomViewAtCursor(editor, cardView);

      } else {
        Toast.makeText(editor.getContext(), "فایل یافت نشد: " + filePath, Toast.LENGTH_SHORT)
            .show();
      }
    } catch (Exception e) {
      Toast.makeText(editor.getContext(), "خطا: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    }
  }

  private ImagePathResult findImagePathAtPosition(String lineText, int cursorColumn) {
    if (lineText == null || lineText.isEmpty()) {
      return null;
    }

    Matcher matcher = IMAGE_PATH_PATTERN.matcher(lineText);
    while (matcher.find()) {
      int start = matcher.start();
      int end = matcher.end();
      String foundPath = matcher.group();

      if (cursorColumn >= start && cursorColumn <= end) {
        String cleanedPath = cleanImagePath(foundPath);
        if (isValidImagePath(cleanedPath)) {
          return new ImagePathResult(cleanedPath, start, end);
        }
      }
    }

    return null;
  }

  private String cleanImagePath(String path) {
    String cleaned = path.trim();

    if ((cleaned.startsWith("\"") && cleaned.endsWith("\""))
        || (cleaned.startsWith("'") && cleaned.endsWith("'"))
        || (cleaned.startsWith("(") && cleaned.endsWith(")"))) {
      cleaned = cleaned.substring(1, cleaned.length() - 1);
    }

    while (cleaned.endsWith(",") || cleaned.endsWith(";") || cleaned.endsWith(")")) {
      cleaned = cleaned.substring(0, cleaned.length() - 1);
    }

    return cleaned.trim();
  }

  private boolean isValidImagePath(String path) {
    if (path == null || path.isEmpty()) return false;

    String lowerPath = path.toLowerCase();
    boolean hasValidExtension =
        lowerPath.endsWith(".png")
            || lowerPath.endsWith(".jpg")
            || lowerPath.endsWith(".jpeg")
            || lowerPath.endsWith(".gif")
            || lowerPath.endsWith(".bmp")
            || lowerPath.endsWith(".webp")
            || lowerPath.endsWith(".svg");

    return hasValidExtension;
  }

  private String resolveFilePath(String filePath) {
    if (filePath == null) return null;

    if (filePath.startsWith("/")) {
      return filePath;
    }

    String resolvedPath = resolveRelativePath(filePath);
    if (resolvedPath != null) {
      return resolvedPath;
    }

    return null;
  }

  private String resolveRelativePath(String relativePath) {
    try {
      if (codeEditorActivity == null) return null;

      android.content.SharedPreferences shp =
          codeEditorActivity.getSharedPreferences("shp", android.content.Context.MODE_PRIVATE);
      if (!shp.contains("path")) return null;

      String pathJson = shp.getString("path", "");
      if (pathJson.isEmpty()) return null;

      java.lang.reflect.Type type =
          new com.google.gson.reflect.TypeToken<ArrayList<HashMap<String, Object>>>() {}.getType();
      ArrayList<HashMap<String, Object>> tabsList =
          new com.google.gson.Gson().fromJson(pathJson, type);

      if (tabsList == null || tabsList.isEmpty()) return null;

      int currentTab = getCurrentTabPosition();
      if (currentTab >= 0 && currentTab < tabsList.size()) {
        String currentFilePath = tabsList.get(currentTab).get("path").toString();
        if (currentFilePath != null && !currentFilePath.isEmpty()) {
          File currentFile = new File(currentFilePath);
          File parentDir = currentFile.getParentFile();
          if (parentDir != null) {
            File resolvedFile = new File(parentDir, relativePath);
            return resolvedFile.getAbsolutePath();
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  private int getCurrentTabPosition() {
    try {
      if (tabLayout != null) {
        return tabLayout.getSelectedTabPosition();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return -1;
  }

  private void changeCursorColorToGreen(CodeEditor editor) {
    try {
      if (!isCursorColorChanged) {
        editor.getColorScheme().setColor(EditorColorScheme.SELECTION_INSERT, Color.GREEN);
        isCursorColorChanged = true;
        editor.invalidate();
      }
    } catch (Exception e) {
      restoreOriginalCursorColor();
    }
  }

  private void restoreOriginalCursorColor() {
    if (isCursorColorChanged && currentEditor != null) {
      try {
        currentEditor
            .getColorScheme()
            .setColor(EditorColorScheme.SELECTION_INSERT, originalCursorColor);
        isCursorColorChanged = false;
        currentEditor.invalidate();
      } catch (Exception e) {
        // ignore
      }
    }
  }

  @Override
  public void getFileManagerAc(FileManagerActivity arg0) {}

  @Override
  public boolean hasuseing() {
    return true;
  }

  @Override
  public String setName() {
    return "Image Preview";
  }

  @Override
  public String langModel() {
    return ".html,.htm";
  }

  private static class ImagePathResult {
    String filePath;
    int start;
    int end;

    ImagePathResult(String filePath, int start, int end) {
      this.filePath = filePath;
      this.start = start;
      this.end = end;
    }
  }

  @Override
  public void getBaseCompat(BaseCompat arg0) {}
}
