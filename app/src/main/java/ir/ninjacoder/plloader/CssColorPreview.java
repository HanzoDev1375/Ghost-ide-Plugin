package ir.ninjacoder.plloader;

import android.graphics.Color;
import android.widget.TextView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.rosemoe.sora.event.SelectionChangeEvent;
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

  @Override
  public void getCodeEditorAc(CodeEditorActivity arg0) {}

  @Override
  public void getEditor(CodeEditor editor) {
    
    try {
      colorList =
          new Gson()
              .fromJson(
                  new StringReader(CssColors.getJsonColor()),
                  new TypeToken<List<ColorPreview>>() {}.getType());

      
      if (colorList != null && !colorList.isEmpty()) {
        StringBuilder patternBuilder = new StringBuilder();
        patternBuilder.append("\\b(");
        for (int i = 0; i < colorList.size(); i++) {
          if (i > 0) patternBuilder.append("|");
          patternBuilder.append(colorList.get(i).getColorName());
        }
        patternBuilder.append(")\\b");
        colorPattern = Pattern.compile(patternBuilder.toString(), Pattern.CASE_INSENSITIVE);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    editor.subscribeEvent(
        SelectionChangeEvent.class,
        (event, unsubscribe) -> {
          try {
            if (colorList == null || colorPattern == null) return;

            int cursorLine = editor.getCursor().getLeftLine();
            int cursorColumn = editor.getCursor().getLeftColumn();
            String currentLine = editor.getText().getLineString(cursorLine);

            if (currentLine == null || currentLine.isEmpty()) return;

            
            Matcher matcher = colorPattern.matcher(currentLine);
            while (matcher.find()) {
              int start = matcher.start();
              int end = matcher.end();
              String foundColorName = matcher.group(1);

              
              if (cursorColumn >= start && cursorColumn <= end) {
                
                for (ColorPreview color : colorList) {
                  if (color.getColorName().equalsIgnoreCase(foundColorName)) {
                    showColorPreview(color, editor);
                    return; 
                  }
                }
              }
            }
          } catch (Exception e) {
            
          }
        });
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
    } catch (Exception e) {
      
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
}
