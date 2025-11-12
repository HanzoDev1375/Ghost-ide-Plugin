package ir.ninjacoder.plloader;

import android.graphics.Color;
import android.widget.TextView;
import android.util.Log;
import android.os.Handler;
import android.os.Looper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.rosemoe.sora.event.SelectionChangeEvent;
import io.github.rosemoe.sora.widget.EditorColorScheme;
import ir.ninjacoder.ghostide.core.activities.FileManagerActivity;
import io.github.rosemoe.sora.widget.CodeEditor;
import ir.ninjacoder.ghostide.core.activities.CodeEditorActivity;
import ir.ninjacoder.ghostide.core.pl.PluginManagerCompat;
import java.io.StringReader;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CssColorPreview implements PluginManagerCompat {

  private List<ColorPreview> colorList;
  private Pattern colorPattern;
  private int originalCursorColor;
  private boolean isCursorColorChanged = false;
  private CodeEditor currentEditor;

  private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
  private Future<?> currentTask;
  private final Handler mainHandler = new Handler(Looper.getMainLooper());

  @Override
  public void getCodeEditorAc(CodeEditorActivity arg0) {}

  @Override
  public void getEditor(CodeEditor editor) {
    currentEditor = editor;

    Log.d("CssColorPreview", "Plugin loaded for editor");

    boolean shouldApply = typeLangApply(editor);
    Log.d("CssColorPreview", "Should apply: " + shouldApply);

    if (shouldApply) {
      Log.d("CssColorPreview", "Initializing plugin for supported language");

      originalCursorColor = editor.getColorScheme().getColor(EditorColorScheme.SELECTION_INSERT);
      Log.d("CssColorPreview", "Original cursor color: " + originalCursorColor);

      // بارگذاری اولیه رنگ‌ها در پس‌زمینه
      loadColorsInBackground();

      editor.subscribeEvent(
          SelectionChangeEvent.class,
          (event, unsubscribe) -> {
            if (colorList == null || colorPattern == null) {
              return;
            }

            // لغو تسک قبلی
            cancelCurrentTask();

            // شروع تسک جدید
            currentTask =
                executor.submit(
                    () -> {
                      detectAndUpdateColor();
                    });
          });

      Log.d("CssColorPreview", "Event subscription completed");
    } else {
      Log.d("CssColorPreview", "Language not supported, plugin disabled");
    }
  }

  private void loadColorsInBackground() {
    executor.submit(
        () -> {
          try {
            colorList =
                new Gson()
                    .fromJson(
                        new StringReader(CssColors.getJsonColor()),
                        new TypeToken<List<ColorPreview>>() {}.getType());

            if (colorList != null && !colorList.isEmpty()) {
              Log.d("CssColorPreview", "Loaded " + colorList.size() + " colors from JSON");

              StringBuilder patternBuilder = new StringBuilder();
              patternBuilder.append("\\b(");
              for (int i = 0; i < colorList.size(); i++) {
                if (i > 0) patternBuilder.append("|");
                patternBuilder.append(colorList.get(i).getColorName());
              }
              patternBuilder.append(")\\b");
              colorPattern = Pattern.compile(patternBuilder.toString(), Pattern.CASE_INSENSITIVE);
              Log.d("CssColorPreview", "Pattern created: " + colorPattern.pattern());
            }
          } catch (Exception e) {
            Log.e("CssColorPreview", "Error loading colors: " + e.getMessage());
          }
        });
  }

  private void detectAndUpdateColor() {
    if (Thread.currentThread().isInterrupted()) {
      return;
    }

    try {
      // گرفتن اطلاعات در background thread
      final int cursorLine = currentEditor.getCursor().getLeftLine();
      final int cursorColumn = currentEditor.getCursor().getLeftColumn();
      final String currentLine = currentEditor.getText().getLineString(cursorLine);

      if (currentLine == null || currentLine.isEmpty()) {
        mainHandler.post(this::restoreOriginalCursorColor);
        return;
      }

      // جستجوی رنگ در background
      ColorPreview detectedColor = null;
      Matcher matcher = colorPattern.matcher(currentLine);

      while (matcher.find() && !Thread.currentThread().isInterrupted()) {
        int start = matcher.start();
        int end = matcher.end();
        String foundColorName = matcher.group(1);

        if (cursorColumn >= start && cursorColumn <= end) {
          for (ColorPreview color : colorList) {
            if (Thread.currentThread().isInterrupted()) break;
            if (color.getColorName().equalsIgnoreCase(foundColorName)) {
              detectedColor = color;
              break;
            }
          }
          if (detectedColor != null) break;
        }
      }

      final ColorPreview finalColor = detectedColor;
      mainHandler.post(
          () -> {
            if (finalColor != null) {
              changeCursorColor(finalColor, currentEditor);
            } else {
              restoreOriginalCursorColor();
            }
          });

    } catch (Exception e) {
      Log.e("CssColorPreview", "Error in color detection: " + e.getMessage());
      mainHandler.post(this::restoreOriginalCursorColor);
    }
  }

  private void cancelCurrentTask() {
    if (currentTask != null && !currentTask.isDone()) {
      currentTask.cancel(true);
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
    return "CSS Color Preview";
  }

  void showColorPreview(ColorPreview color, CodeEditor editor) {
    try {
      editor.post(
          () -> {
            TextView tv = new TextView(editor.getContext());
            tv.setPadding(32, 16, 32, 16);
            tv.setText(color.getColorName() + ": " + color.getCssColor());
            tv.setTextColor(Color.parseColor(color.getCssColor()));
            tv.setTextSize(14);
            EditorPopUp.showCustomViewAtCursor(editor, tv);
          });
    } catch (Exception e) {
      Log.e("CssColorPreview", "Error showing color preview: " + e.getMessage());
    }
  }

  void changeCursorColor(ColorPreview color, CodeEditor editor) {
    try {
      int newColor = Color.parseColor(color.getCssColor());
      editor.getColorScheme().setColor(EditorColorScheme.SELECTION_INSERT, newColor);
      isCursorColorChanged = true;
      showColorPreview(color, editor);
      editor.invalidate();
    } catch (Exception e) {
      Log.e("CssColorPreview", "Error changing cursor color: " + e.getMessage());
      restoreOriginalCursorColor();
    }
  }

  void restoreOriginalCursorColor() {
    if (isCursorColorChanged && currentEditor != null) {
      currentEditor
          .getColorScheme()
          .setColor(EditorColorScheme.SELECTION_INSERT, originalCursorColor);
      isCursorColorChanged = false;
      currentEditor.invalidate();
    }
  } 

  public static class ColorPreview {
    private String colorName;
    private String cssColor;

    public String getColorName() {
      return colorName;
    }

    public String getCssColor() {
      return cssColor;
    }
  }

  boolean typeLangApply(CodeEditor editor) {
    return true;
  }

  @Override
  public String langModel() {
    return ".html,.css";
  }
}
