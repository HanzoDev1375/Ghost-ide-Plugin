package ir.ninjacoder.plloader.img;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.reflect.TypeToken;
import io.github.rosemoe.sora.event.ContentChangeEvent;
import java.util.List;
import com.google.gson.Gson;
import io.github.rosemoe.sora.event.SelectionChangeEvent;
import io.github.rosemoe.sora.widget.CodeEditor;
import io.github.rosemoe.sora.widget.EditorColorScheme;
import ir.ninjacoder.ghostide.core.activities.BaseCompat;
import ir.ninjacoder.ghostide.core.activities.CodeEditorActivity;
import ir.ninjacoder.ghostide.core.activities.FileManagerActivity;
import ir.ninjacoder.ghostide.core.pl.PluginManagerCompat;
import ir.ninjacoder.plloader.EditorPopUp;
import java.io.File;
import java.lang.reflect.Field;
import com.google.android.material.tabs.TabLayout;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.HashSet;

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
  private HashSet<Integer> linesWithIcons = new HashSet<>();
  private String lastProcessedFilePath = "";

  // آیکون پیش‌فرض برای نمایش
  private static final String DEFAULT_IMAGE_ICON = "ic_image_icon.png"; // مسیر آیکون در assets
  private static final int IMAGE_ICON_COLOR_FILTER = 0;

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
              updateFileType();
              setupEventListeners(editor);
              scanAndAddIconsForAllLines(editor);
              int lineCount = editor.getLineCount();
              float mergingsize;
              if (lineCount > 1000) {
                mergingsize = 10;
              } else if (lineCount > 100) {
                mergingsize = 8.2f;
              } else if (lineCount > 10) {
                mergingsize = 7.3f;
              } else if (lineCount > 5) {
                mergingsize = 5.1f;
              } else {
                mergingsize = 1.0f;
              }
              editor.setDividerMargin(mergingsize);
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

      Field field = codeEditorActivity.getClass().getDeclaredField("tablayouteditor");
      field.setAccessible(true);
      tabLayout = (TabLayout) field.get(codeEditorActivity);

      if (tabLayout != null) {
        tabLayout.addOnTabSelectedListener(
            new TabLayout.OnTabSelectedListener() {
              @Override
              public void onTabSelected(TabLayout.Tab tab) {
                updateFileType();
                if (currentEditor != null) {
                  scanAndAddIconsForAllLines(currentEditor);
                }
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

      String fileType = codeEditorActivity.getcurrentFileType();
      boolean wasHtmlFile = isHtmlFile;
      isHtmlFile = fileType != null && (fileType.endsWith(".html") || fileType.endsWith(".htm"));

      if (currentEditor != null) {
        currentEditor.post(
            () -> {
              try {
                if (isHtmlFile) {
                  scanAndAddIconsForAllLines(currentEditor);
                } else {
                  removeAllIcons();
                }
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

  private void scanAndAddIconsForAllLines(CodeEditor editor) {
    try {
      if (!isHtmlFile || editor == null) {
        removeAllIcons();
        return;
      }

      removeAllIcons();
      linesWithIcons.clear();

      String text = editor.getText().toString();
      String[] lines = text.split("\n");

      for (int i = 0; i < lines.length; i++) {
        String line = lines[i];
        if (containsImagePath(line)) {
          addIconToLine(editor, i, line);
        }
      }

      editor.invalidate();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private boolean containsImagePath(String lineText) {
    if (lineText == null || lineText.isEmpty()) {
      return false;
    }

    Matcher matcher = IMAGE_PATH_PATTERN.matcher(lineText);
    while (matcher.find()) {
      String foundPath = matcher.group();
      String cleanedPath = cleanImagePath(foundPath);
      if (isValidImagePath(cleanedPath)) {
        return true;
      }
    }

    return false;
  }

  private void addIconToLine(CodeEditor editor, int lineNumber, String lineText) {
    try {
      // پیدا کردن اولین مسیر تصویر در خط
      Matcher matcher = IMAGE_PATH_PATTERN.matcher(lineText);
      while (matcher.find()) {
        String foundPath = matcher.group();
        String cleanedPath = cleanImagePath(foundPath);

        if (isValidImagePath(cleanedPath)) {
          String fullPath = resolveFilePath(cleanedPath);
          if (fullPath != null && new File(fullPath).exists()) {

            editor.setLineNumberAlign(Paint.Align.LEFT);
            editor.addLineIcons(lineNumber + 1, IMAGE_ICON_COLOR_FILTER, fullPath, true);
            linesWithIcons.add(lineNumber);
            break;
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void removeAllIcons() {
    try {
      if (currentEditor != null) {
        for (int lineNumber : linesWithIcons) {
          try {
            currentEditor.removeLineIcon(lineNumber + 1);
          } catch (Exception e) {
            // ignore
          }
        }
        linesWithIcons.clear();
        currentEditor.invalidate();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void setupEventListeners(CodeEditor editor) {
    originalCursorColor = editor.getColorScheme().getColor(EditorColorScheme.SELECTION_INSERT);

    editor.subscribeEvent(
        ContentChangeEvent.class,
        (event, unsubscribe) -> {
          if (!isHtmlFile) return;

          editor.postDelayed(
              () -> {
                int lineCount = editor.getLineCount();
                float mergingsize;
                if (lineCount > 1000) {
                  mergingsize = 10;
                } else if (lineCount > 100) {
                  mergingsize = 8.2f;
                } else if (lineCount > 10) {
                  mergingsize = 7.3f;
                } else if (lineCount > 5) {
                  mergingsize = 5.1f;
                } else {
                  mergingsize = 1.0f;
                }

                editor.setDividerMargin(mergingsize);
              },
              400);
          editor.postDelayed(
              () -> {
                scanAndAddIconsForAllLines(editor);
              },
              300);
        });

    // لیسنر برای حرکت کرسر (همانند قبل)
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
            updateFileType();

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
      if (!isHtmlFile) return;

      String fullPath = resolveFilePath(filePath);

      if (fullPath != null && new File(fullPath).exists()) {
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

      SharedPreferences shp =
          codeEditorActivity.getSharedPreferences("shp", android.content.Context.MODE_PRIVATE);
      if (!shp.contains("path")) return null;

      String pathJson = shp.getString("path", "");
      if (pathJson.isEmpty()) return null;

      var type = new TypeToken<List<HashMap<String, Object>>>() {}.getType();
      List<HashMap<String, Object>> tabsList = new Gson().fromJson(pathJson, type);

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
    return "Image Preview with Icons";
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
  public void getBaseCompat(BaseCompat base) {
    
  }

}
