package ir.ninjacoder.plloader;

import android.graphics.Color;
import io.github.rosemoe.sora.event.ContentChangeEvent;
import io.github.rosemoe.sora.event.SelectionChangeEvent;
import io.github.rosemoe.sora.widget.CodeEditor;
import ir.ninjacoder.ghostide.activities.CodeEditorActivity;
import ir.ninjacoder.ghostide.activities.FileManagerActivity;
import ir.ninjacoder.ghostide.pl.PluginManagerCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorRgbCurosr implements PluginManagerCompat {

  private boolean isProcessing = false;
  private long lastProcessTime = 0;
  private final int PROCESS_DELAY = 150; 

  
  private static final Pattern HEX_PATTERN = Pattern.compile("#[0-9A-Fa-f]{3,8}\\b");
  private static final Pattern RGB_PATTERN = Pattern.compile("rgb\\s*\\([^)]+\\)");
  private static final Pattern HSL_PATTERN = Pattern.compile("hsl[a]?\\s*\\([^)]+\\)");

  @Override
  public void getCodeEditorAc(CodeEditorActivity arg0) {}

  @Override
  public void getEditor(CodeEditor editor) {
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
            int cursorLine = editor.getCursor().getLeftLine();
            int cursorColumn = editor.getCursor().getLeftColumn();
            String currentLine = editor.getText().getLineString(cursorLine);
            List<ColorHelper> colorList = getColorsFromLine(currentLine, cursorLine);

            for (ColorHelper helper : colorList) {
              if (isCursorOnColor(helper, cursorLine, cursorColumn)) {
                showColorPicker(editor, helper);
                return;
              }
            }
          } catch (Exception e) {
            
          }
        });

    editor.subscribeEvent(
        ContentChangeEvent.class,
        (event, sub) -> {
          if (event.getAction() == ContentChangeEvent.ACTION_DELETE
              || event.getAction() == ContentChangeEvent.ACTION_INSERT
              || event.getAction() == ContentChangeEvent.ACTION_SET_NEW_TEXT) {
            isProcessing = false;
          }
        });
  }

  private List<ColorHelper> getColorsFromLine(String lineText, int lineNumber) {
    List<ColorHelper> colorList = new ArrayList<>();

    if (lineText == null || lineText.isEmpty()) {
      return colorList;
    }

    
    Matcher hexMatcher = HEX_PATTERN.matcher(lineText);
    while (hexMatcher.find()) {
      String color = expandShortHex(hexMatcher.group());
      colorList.add(new ColorHelper(lineNumber, hexMatcher.start(), color, "HEX"));
    }

    
    Matcher rgbMatcher = RGB_PATTERN.matcher(lineText);
    while (rgbMatcher.find()) {
      colorList.add(new ColorHelper(lineNumber, rgbMatcher.start(), rgbMatcher.group(), "RGB"));
    }

    
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

  private void showColorPicker(CodeEditor editor, ColorHelper colorHelper) {
    try {
      CompactColorPickerView colorPicker = new CompactColorPickerView(editor.getContext());
      setInitialColor(colorPicker, colorHelper);

      colorPicker.setOnColorChangedListener(
          (color, hexColor) -> {
            String newColor = convertColorFormat(color, colorHelper);
            replaceColorInEditor(editor, colorHelper, newColor);
          });

      EditorPopUp.showCustomViewAtCursor(editor, colorPicker);
    } catch (Exception e) {
      
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
      
    } finally {
      
      editor.postDelayed(() -> isProcessing = false, 200);
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
}
