package ir.ninjacoder.plloader;

import android.graphics.Color;
import android.widget.TextView;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.rosemoe.sora.event.SelectionChangeEvent;
import io.github.rosemoe.sora.langs.css3.CSS3Language;
import io.github.rosemoe.sora.langs.html.HTMLLanguage;
import io.github.rosemoe.sora.langs.php.PHPLanguage;
import io.github.rosemoe.sora.widget.EditorColorScheme;
import ir.ninjacoder.ghostide.activities.FileManagerActivity;
import io.github.rosemoe.sora.widget.CodeEditor;
import ir.ninjacoder.ghostide.activities.CodeEditorActivity;
import ir.ninjacoder.ghostide.pl.PluginManagerCompat;
import java.io.StringReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CssColorPreview implements PluginManagerCompat {

  private List<ColorPreview> colorList;
  private Pattern colorPattern;
  private int originalCursorColor;
  private boolean isCursorColorChanged = false;
  private CodeEditor currentEditor;

  @Override
  public void getCodeEditorAc(CodeEditorActivity arg0) {}

  @Override
  public void getEditor(CodeEditor editor) {
    currentEditor = editor;

    Log.d("CssColorPreview", "Plugin loaded for editor");

    // تست نوع زبان
    boolean shouldApply = typeLangApply(editor);
    Log.d("CssColorPreview", "Should apply: " + shouldApply);
    Log.d(
        "CssColorPreview",
        "Language class: "
            + (editor.getEditorLanguage() != null
                ? editor.getEditorLanguage().getClass().getName()
                : "null"));

    if (shouldApply) {
      Log.d("CssColorPreview", "Initializing plugin for supported language");

      // ذخیره رنگ اصلی کرسر
      originalCursorColor = editor.getColorScheme().getColor(EditorColorScheme.SELECTION_INSERT);
      Log.d("CssColorPreview", "Original cursor color: " + originalCursorColor);

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
        } else {
          Log.d("CssColorPreview", "Color list is null or empty");
        }
      } catch (Exception e) {
        Log.e("CssColorPreview", "Error loading colors: " + e.getMessage());
        e.printStackTrace();
      }

      editor.subscribeEvent(
          SelectionChangeEvent.class,
          (event, unsubscribe) -> {
            try {
              Log.d("CssColorPreview", "Selection change event triggered");

              if (colorList == null || colorPattern == null) {
                Log.d("CssColorPreview", "Color list or pattern is null");
                return;
              }

              int cursorLine = editor.getCursor().getLeftLine();
              int cursorColumn = editor.getCursor().getLeftColumn();
              String currentLine = editor.getText().getLineString(cursorLine);

              Log.d(
                  "CssColorPreview", "Cursor at line: " + cursorLine + ", column: " + cursorColumn);
              Log.d("CssColorPreview", "Current line: " + currentLine);

              if (currentLine == null || currentLine.isEmpty()) {
                Log.d("CssColorPreview", "Empty line, restoring cursor color");
                restoreOriginalCursorColor();
                return;
              }

              boolean foundColor = false;

              // جستجو برای نام رنگ در خط جاری
              Matcher matcher = colorPattern.matcher(currentLine);
              while (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();
                String foundColorName = matcher.group(1);

                Log.d(
                    "CssColorPreview",
                    "Found color name: " + foundColorName + " at " + start + "-" + end);

                // بررسی آیا کرسر روی نام رنگ قرار دارد
                if (cursorColumn >= start && cursorColumn <= end) {
                  Log.d("CssColorPreview", "Cursor is on color: " + foundColorName);

                  // پیدا کردن رنگ متناظر
                  for (ColorPreview color : colorList) {
                    if (color.getColorName().equalsIgnoreCase(foundColorName)) {
                      Log.d("CssColorPreview", "Changing cursor color to: " + color.getCssColor());
                      changeCursorColor(color, editor);
                      foundColor = true;
                      return;
                    }
                  }
                }
              }

              // اگر روی رنگ نیست، رنگ کرسر را به حالت عادی برگردان
              if (!foundColor) {
                Log.d("CssColorPreview", "No color found under cursor, restoring original");
                restoreOriginalCursorColor();
              }
            } catch (Exception e) {
              Log.e("CssColorPreview", "Error in selection event: " + e.getMessage());
              restoreOriginalCursorColor();
            }
          });

      Log.d("CssColorPreview", "Event subscription completed");
    } else {
      Log.d("CssColorPreview", "Language not supported, plugin disabled");
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
      TextView tv = new TextView(editor.getContext());
      tv.setPadding(32, 16, 32, 16);
      tv.setText(color.getColorName() + ": " + color.getCssColor());
      tv.setTextColor(Color.parseColor(color.getCssColor()));
      tv.setTextSize(14);
      EditorPopUp.showCustomViewAtCursor(editor, tv);
      Log.d("CssColorPreview", "Color preview shown: " + color.getColorName());
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
      Log.d("CssColorPreview", "Cursor color changed to: " + color.getCssColor());
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
      Log.d("CssColorPreview", "Cursor color restored to original");
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
    return ".html";
  }
}
