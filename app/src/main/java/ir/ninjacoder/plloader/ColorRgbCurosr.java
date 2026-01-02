package ir.ninjacoder.plloader;

import android.graphics.Color;
import com.blankj.utilcode.util.ClipboardUtils;
import io.github.rosemoe.sora.data.Span;
import io.github.rosemoe.sora.event.ContentChangeEvent;
import io.github.rosemoe.sora.event.SelectionChangeEvent;
import io.github.rosemoe.sora.text.TextStyle;
import io.github.rosemoe.sora.widget.CodeEditor;
import io.github.rosemoe.sora.widget.EditorColorScheme;
import ir.ninjacoder.ghostide.core.activities.BaseCompat;
import ir.ninjacoder.ghostide.core.activities.CodeEditorActivity;
import ir.ninjacoder.ghostide.core.activities.FileManagerActivity;
import ir.ninjacoder.ghostide.core.pl.PluginManagerCompat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorRgbCurosr implements PluginManagerCompat {

  private boolean isProcessing = false;
  private long lastProcessTime = 0;
  private final int PROCESS_DELAY = 150;
  private int originalCursorColor;
  private boolean isCursorColorChanged = false;
  private CodeEditor currentEditor;
  private CodeEditorActivity codeEditorActivity;
  private boolean isCssFile = false;
  private TabLayout tabLayout;

  // الگوهای شناسایی رنگ
  private static final Pattern HEX_PATTERN = Pattern.compile("#[0-9A-Fa-f]{3,8}\\b");
  private static final Pattern RGB_PATTERN = Pattern.compile("rgb\\s*\\([^)]+\\)");
  private static final Pattern HSL_PATTERN = Pattern.compile("hsl[a]?\\s*\\([^)]+\\)");

  @Override
  public void getCodeEditorAc(CodeEditorActivity arg0) {
    codeEditorActivity = arg0;
    setupTabChangeListener();
  }

  @Override
  public void getEditor(CodeEditor editor) {
    currentEditor = editor;

    if (editor == null) return;

    // ذخیره رنگ اصلی کرسر
    originalCursorColor = editor.getColorScheme().getColor(EditorColorScheme.SELECTION_INSERT);

    editor.postDelayed(
        () -> {
          try {
            if (editor.getContext() instanceof CodeEditorActivity) {
              codeEditorActivity = (CodeEditorActivity) editor.getContext();
              setupTabChangeListener();

              // فوراً فایل تایپ رو چک کن و event listener رو ست کن
              updateFileType();
              setupEventListeners(editor);

              // لاگ برای دیباگ
              android.util.Log.d("ColorPlugin", "🔧 Plugin initialized - isCssFile: " + isCssFile);
            }
          } catch (Exception e) {
            android.util.Log.e("ColorPlugin", "❌ Error in getEditor: " + e.getMessage());
            e.printStackTrace();
          }
        },
        3000); // تأخیر کمتر
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
                android.util.Log.d("ColorPlugin", "🔄 Tab changed - updating file type");
                updateFileType();
                restoreOriginalCursorColor(); // رنگ رو ریست کن

                // فورس چک موقعیت کرسر
                if (currentEditor != null) {
                  currentEditor.postDelayed(
                      () -> {
                        try {
                          int cursorLine = currentEditor.getCursor().getLeftLine();
                          int cursorColumn = currentEditor.getCursor().getLeftColumn();
                          String currentLine = currentEditor.getText().getLineString(cursorLine);
                          checkColorAtCursor(cursorLine, cursorColumn, currentLine);
                        } catch (Exception e) {
                          // ignore
                        }
                      },
                      900);
                }
              }

              @Override
              public void onTabUnselected(TabLayout.Tab tab) {}

              @Override
              public void onTabReselected(TabLayout.Tab tab) {}
            });

        // همین الان هم فایل تایپ رو چک کن
        updateFileType();
      }
    } catch (Exception e) {
      android.util.Log.e("ColorPlugin", "❌ Error setting up tab listener: " + e.getMessage());
    }
  }

  private void updateFileType() {
    try {
      if (codeEditorActivity == null) return;

      // استفاده از متد getcurrentFileType که در CodeEditorActivity هست
      Method method = codeEditorActivity.getClass().getMethod("getcurrentFileType");
      String fileType = (String) method.invoke(codeEditorActivity);

      boolean wasCssFile = isCssFile;
      isCssFile = fileType != null && fileType.endsWith(".css") || fileType.endsWith(".html");

      android.util.Log.d("ColorPlugin", "📁 File type: " + fileType + " - isCssFile: " + isCssFile);

      // اگر وضعیت تغییر کرد، کرسر رو ریست کن
      if (wasCssFile != isCssFile) {
        restoreOriginalCursorColor();
      }

    } catch (Exception e) {
      android.util.Log.e("ColorPlugin", "❌ Error updating file type: " + e.getMessage());
      isCssFile = false;
    }
  }

  private void setupEventListeners(CodeEditor editor) {
    editor.subscribeEvent(
        SelectionChangeEvent.class,
        (event, unsubscribe) -> {
          if (isProcessing) return;

          // هر بار چک کن که فایل هنوز CSS هست یا نه
          updateFileType();

          // فقط برای فایل‌های CSS پردازش کن
          if (!isCssFile) {
            restoreOriginalCursorColor();
            return;
          }

          long currentTime = System.currentTimeMillis();
          if (currentTime - lastProcessTime < PROCESS_DELAY) {
            return;
          }
          lastProcessTime = currentTime;

          try {
            int cursorLine = editor.getCursor().getLeftLine();
            int cursorColumn = editor.getCursor().getLeftColumn();
            String currentLine = editor.getText().getLineString(cursorLine);

            checkColorAtCursor(cursorLine, cursorColumn, currentLine);

          } catch (Exception e) {
            // در صورت خطا، رنگ کرسر را به حالت عادی برگردان
            restoreOriginalCursorColor();
          }
        });

    editor.subscribeEvent(
        ContentChangeEvent.class,
        (event, sub) -> {
          if (event.getAction() == ContentChangeEvent.ACTION_DELETE
              || event.getAction() == ContentChangeEvent.ACTION_INSERT
              || event.getAction() == ContentChangeEvent.ACTION_SET_NEW_TEXT) {
            isProcessing = false;
            // پس از تغییر محتوا، رنگ کرسر را بررسی کن
            restoreOriginalCursorColor();
          }
        });
  }

  private void checkColorAtCursor(int cursorLine, int cursorColumn, String currentLine) {
    List<ColorHelper> colorList = getColorsFromLine(currentLine, cursorLine);
    boolean foundColor = false;

    for (ColorHelper helper : colorList) {
      if (isCursorOnColor(helper, cursorLine, cursorColumn)) {
        changeCursorColor(currentEditor, helper);
        showColorPicker(currentEditor, helper);
        foundColor = true;
        break;
      }
    }

    // اگر روی رنگ نیست، رنگ کرسر را به حالت عادی برگردان
    if (!foundColor) {
      restoreOriginalCursorColor();
    }
  }

  private List<ColorHelper> getColorsFromLine(String lineText, int lineNumber) {
    List<ColorHelper> colorList = new ArrayList<>();

    if (lineText == null || lineText.isEmpty()) {
      return colorList;
    }

    // شناسایی رنگ‌های HEX
    Matcher hexMatcher = HEX_PATTERN.matcher(lineText);
    while (hexMatcher.find()) {
      String color = expandShortHex(hexMatcher.group());
      colorList.add(new ColorHelper(lineNumber, hexMatcher.start(), color, "HEX"));
    }

    // شناسایی رنگ‌های RGB
    Matcher rgbMatcher = RGB_PATTERN.matcher(lineText);
    while (rgbMatcher.find()) {
      colorList.add(new ColorHelper(lineNumber, rgbMatcher.start(), rgbMatcher.group(), "RGB"));
    }

    // شناسایی رنگ‌های HSL
    Matcher hslMatcher = HSL_PATTERN.matcher(lineText);
    while (hslMatcher.find()) {
      colorList.add(new ColorHelper(lineNumber, hslMatcher.start(), hslMatcher.group(), "HSL"));
    }

    return colorList;
  }

  private boolean isCursorOnColor(ColorHelper helper, int cursorLine, int cursorColumn) {
    if (helper.getLine() != cursorLine) return false;

    int start = helper.getCol();
    int end = start + helper.getColorHex().length();
    return cursorColumn >= start && cursorColumn <= end;
  }

  private void changeCursorColor(CodeEditor editor, ColorHelper colorHelper) {
    try {
      if (!isCssFile || editor == null) {
        restoreOriginalCursorColor();
        return;
      }

      // گرفتن موقعیت کرسر
      int cursorLine = editor.getCursor().getLeftLine();
      int cursorColumn = editor.getCursor().getLeftColumn();

      // گرفتن Spans برای خط فعلی
      List<Span> spans = editor.getSpansForLine(cursorLine);

      if (spans != null && !spans.isEmpty()) {
        // پیدا کردن Span در موقعیت کرسر
        Span spanAtCursor = null;
        for (int i = 0; i < spans.size(); i++) {
          Span span = spans.get(i);
          int spanStart = span.column;
          int spanEnd = (i + 1 < spans.size()) ? spans.get(i + 1).column : Integer.MAX_VALUE;

          if (cursorColumn >= spanStart && cursorColumn < spanEnd) {
            spanAtCursor = span;
            break;
          }
        }

        if (spanAtCursor != null) {
          // گرفتن ID رنگ از Span
          int colorId = TextStyle.getForegroundColorId(spanAtCursor.style);

          // گرفتن رنگ واقعی از ColorScheme
          EditorColorScheme scheme = editor.getColorScheme();
          int spanColor = scheme.getColor(colorId);

          // اعمال رنگ Span به کرسر
          scheme.setColor(EditorColorScheme.SELECTION_INSERT, spanColor);
          isCursorColorChanged = true;
          editor.invalidate();

          android.util.Log.d(
              "ColorPlugin",
              "🎨 Applied SPAN color to cursor:"
                  + "\n  Cursor position: line="
                  + cursorLine
                  + ", col="
                  + cursorColumn
                  + "\n  Span color ID: "
                  + colorId
                  + "\n  Actual color: #"
                  + Integer.toHexString(spanColor).toUpperCase()
                  + "\n  Color name: "
                  + getColorName(colorId));
        } else {
          // اگر Span پیدا نشد، از رنگ CSS استفاده کن
          applyCssColor(editor, colorHelper);
        }
      } else {
        // اگر Spans موجود نبود، از رنگ CSS استفاده کن
        applyCssColor(editor, colorHelper);
      }

    } catch (Exception e) {
      restoreOriginalCursorColor();
      android.util.Log.e("ColorPlugin", "❌ Error changing cursor color: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private void applyCssColor(CodeEditor editor, ColorHelper colorHelper) {
    try {
      // پارس کردن رنگ CSS
      int color;
      String colorText = colorHelper.getColorHex();

      if (colorHelper.getType().equals("RGB")) {
        color = parseRGB(colorText);
      } else if (colorHelper.getType().equals("HSL")) {
        color = parseHSL(colorText);
      } else {
        color = Color.parseColor(colorText);
      }

      // اعمال رنگ CSS
      editor.getColorScheme().setColor(EditorColorScheme.SELECTION_INSERT, color);
      isCursorColorChanged = true;
      editor.invalidate();

      android.util.Log.d("ColorPlugin", "🎨 Applied CSS color: " + colorText);
    } catch (Exception e) {
      throw new RuntimeException("Failed to apply CSS color", e);
    }
  }

  // تابع برای گرفتن نام رنگ از ID (اختیاری - برای دیباگ)
  private String getColorName(int colorId) {
    // فقط ID های اصلی
    switch (colorId) {
      case EditorColorScheme.TEXT_NORMAL:
        return "TEXT_NORMAL";
      case EditorColorScheme.KEYWORD:
        return "KEYWORD";
      case EditorColorScheme.IDENTIFIER_VAR:
        return "IDENTIFIER_VAR";
      case EditorColorScheme.IDENTIFIER_NAME:
        return "IDENTIFIER_NAME";
      case EditorColorScheme.OPERATOR:
        return "OPERATOR";
      case EditorColorScheme.COMMENT:
        return "COMMENT";

      case EditorColorScheme.LITERAL:
        return "LITERAL";
      case EditorColorScheme.FUNCTION_NAME:
        return "FUNCTION_NAME";
      case EditorColorScheme.ANNOTATION:
        return "ANNOTATION";
      case EditorColorScheme.ATTRIBUTE_NAME:
        return "ATTRIBUTE_NAME";
      case EditorColorScheme.ATTRIBUTE_VALUE:
        return "ATTRIBUTE_VALUE";
      case EditorColorScheme.HTML_TAG:
        return "HTML_TAG";
      case EditorColorScheme.SELECTION_INSERT:
        return "SELECTION_INSERT";
      case EditorColorScheme.SELECTED_TEXT_BACKGROUND:
        return "SELECTED_TEXT_BACKGROUND";
      case EditorColorScheme.CURRENT_LINE:
        return "CURRENT_LINE";
      default:
        return "UNKNOWN (" + colorId + ")";
    }
  }

  private void showColorPicker(CodeEditor editor, ColorHelper colorHelper) {
    try {
      // دوباره چک کن که فایل هنوز CSS هست
      if (!isCssFile) return;

      CompactColorPickerView colorPicker = new CompactColorPickerView(editor.getContext());
      setInitialColor(colorPicker, colorHelper);

      colorPicker.setOnColorChangedListener(
          (color, hexColor) -> {
            String newColor = convertColorFormat(color, colorHelper);
            replaceColorInEditor(editor, colorHelper, newColor);
          });

      EditorPopUp.showCustomViewAtCursor(editor, colorPicker);
    } catch (Exception e) {
      // ignore
    }
  }

  private void setInitialColor(CompactColorPickerView colorPicker, ColorHelper colorHelper) {
    try {
      int color;
      String colorText = colorHelper.getColorHex();

      if (colorHelper.getType().equals("RGB")) {
        color = parseRGB(colorText);
      } else if (colorHelper.getType().equals("HSL")) {
        color = parseHSL(colorText);
      } else {
        color = Color.parseColor(colorText);
      }
      colorPicker.setInitialColor(color);
    } catch (Exception e) {
      colorPicker.setInitialColor(Color.BLACK);
    }
  }

  private int parseRGB(String rgbString) {
    try {
      String clean = rgbString.replace("rgb(", "").replace(")", "").replace(" ", "");
      String[] parts = clean.split(",");
      int r = Integer.parseInt(parts[0]);
      int g = Integer.parseInt(parts[1]);
      int b = Integer.parseInt(parts[2]);
      return Color.rgb(r, g, b);
    } catch (Exception e) {
      return Color.BLACK;
    }
  }

  private int parseHSL(String hslString) {
    try {
      String clean =
          hslString
              .replace("hsl(", "")
              .replace("hsla(", "")
              .replace(")", "")
              .replace("%", "")
              .replace(" ", "");
      String[] parts = clean.split(",");

      float h = Float.parseFloat(parts[0]);
      float s = Float.parseFloat(parts[1]) / 100.0f;
      float l = Float.parseFloat(parts[2]) / 100.0f;
      float alpha = parts.length > 3 ? Float.parseFloat(parts[3]) : 1.0f;

      return HSLToColor(h, s, l, alpha);
    } catch (Exception e) {
      return Color.BLACK;
    }
  }

  private String convertColorFormat(int color, ColorHelper colorHelper) {
    switch (colorHelper.getType()) {
      case "RGB":
        return String.format(
            "rgb(%d,%d,%d)", Color.red(color), Color.green(color), Color.blue(color));

      case "HSL":
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        int h = (int) hsv[0];
        int s = (int) (hsv[1] * 100);
        int l = (int) (hsv[2] * 100);

        if (colorHelper.getColorHex().contains("hsla")) {
          float alpha = Color.alpha(color) / 255.0f;
          return String.format("hsla(%d,%d%%,%d%%,%.1f)", h, s, l, alpha);
        } else {
          return String.format("hsl(%d,%d%%,%d%%)", h, s, l);
        }

      default:
        return String.format("#%08X", color);
    }
  }

  private void replaceColorInEditor(CodeEditor editor, ColorHelper colorHelper, String newColor) {
    try {
      isProcessing = true;

      int line = colorHelper.getLine();
      int start = colorHelper.getCol();
      int end = start + colorHelper.getColorHex().length();

      editor.getText().replace(line, start, line, end, newColor);
    } catch (Exception e) {
      // ignore
    } finally {
      // بعد از جایگزینی، رنگ کرسر را به حالت عادی برگردان
      editor.postDelayed(
          () -> {
            isProcessing = false;
            restoreOriginalCursorColor();
          },
          200);
    }
  }

  private void restoreOriginalCursorColor() {
    if (isCursorColorChanged && currentEditor != null) {
      currentEditor
          .getColorScheme()
          .setColor(EditorColorScheme.SELECTION_INSERT, originalCursorColor);
      isCursorColorChanged = false;
      currentEditor.invalidate();
    }
  }

  private int HSLToColor(float h, float s, float l, float alpha) {
    float r, g, b;

    if (s == 0f) {
      r = g = b = l;
    } else {
      float q = l < 0.5f ? l * (1 + s) : l + s - l * s;
      float p = 2 * l - q;
      r = hueToRGB(p, q, h + 1f / 3f);
      g = hueToRGB(p, q, h);
      b = hueToRGB(p, q, h - 1f / 3f);
    }

    return Color.argb(
        Math.round(alpha * 255), Math.round(r * 255), Math.round(g * 255), Math.round(b * 255));
  }

  private float hueToRGB(float p, float q, float t) {
    if (t < 0f) t += 1f;
    if (t > 1f) t -= 1f;
    if (t < 1f / 6f) return p + (q - p) * 6f * t;
    if (t < 1f / 2f) return q;
    if (t < 2f / 3f) return p + (q - p) * (2f / 3f - t) * 6f;
    return p;
  }

  private String expandShortHex(String shortHex) {
    if (shortHex.length() == 4 && shortHex.startsWith("#")) {
      return "#"
          + shortHex.charAt(1)
          + shortHex.charAt(1)
          + shortHex.charAt(2)
          + shortHex.charAt(2)
          + shortHex.charAt(3)
          + shortHex.charAt(3);
    }
    return shortHex;
  }

  static class ColorHelper {
    final int line, col;
    final String colorHex, type;

    public ColorHelper(int line, int col, String colorHex, String type) {
      this.line = line;
      this.col = col;
      this.colorHex = colorHex;
      this.type = type;
    }

    public int getLine() {
      return line;
    }

    public int getCol() {
      return col;
    }

    public String getColorHex() {
      return colorHex;
    }

    public String getType() {
      return type;
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
    return "Color Rgba finder (Optimized)";
  }

  @Override
  public String langModel() {
    return ".css,.html";
  }

  @Override
  public void getBaseCompat(BaseCompat arg0) {}
}
