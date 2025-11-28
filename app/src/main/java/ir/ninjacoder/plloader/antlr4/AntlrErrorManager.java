package ir.ninjacoder.plloader.antlr4;

import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import io.github.rosemoe.sora.event.ContentChangeEvent;
import io.github.rosemoe.sora.langs.javascript.JavaScriptLanguage;
import io.github.rosemoe.sora.langs.javascript.JavaScriptLexer;
import io.github.rosemoe.sora.langs.javascript.JavaScriptParserBaseListener;
import io.github.rosemoe.sora.langs.xml.analyzer.Utils;
import io.github.rosemoe.sora.widget.CodeEditor;
import io.github.rosemoe.sora.event.SelectionChangeEvent;
import io.github.rosemoe.sora.widget.EditorColorScheme;
import ir.ninjacoder.ghostide.core.activities.BaseCompat;
import ir.ninjacoder.ghostide.core.activities.FileManagerActivity;
import ir.ninjacoder.ghostide.core.activities.CodeEditorActivity;
import ir.ninjacoder.ghostide.core.glidecompat.GlideCompat;
import ir.ninjacoder.ghostide.core.pl.PluginManagerCompat;
import ir.ninjacoder.plloader.EditorPopUp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import io.github.rosemoe.sora.langs.javascript.JavaScriptParser;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

public class AntlrErrorManager implements PluginManagerCompat {
  private boolean hascolorChange = false;
  private CodeEditor editors;
  private ImageView errorIcon;
  private List<AntlrError> logcat = new ArrayList<>();
  private Set<Integer> errorLines = new HashSet<>();
  private int originalCursorColor;
  private boolean isCursorColorChanged = false;

  @Override
  public void getCodeEditorAc(CodeEditorActivity editor) {
    ViewGroup view = editor.getWindow().getDecorView().findViewById(android.R.id.content);
    if (editors.getEditorLanguage() instanceof JavaScriptLanguage) {

      errorIcon = new ImageView(editor);
      ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(70, 70);
      errorIcon.setLayoutParams(params);
      errorIcon.setX(155);
      errorIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
      errorIcon.setY(60);
      errorIcon.setZ(25);
      updateIconColor();

      GlideCompat.GlideNormals(
          errorIcon, "/storage/emulated/0/GhostWebIDE/plugins/antlr4error/error.png");

      editors.subscribeEvent(
          ContentChangeEvent.class,
          (ev, suv) -> {
            showErrorContent(editors);
            updateIconColor();
          });
      errorIcon.setOnClickListener(
          v -> {
            showErrorDialog();
          });

      view.addView(errorIcon);
    }
  }

  private void showErrorDialog() {
    if (editors == null || editors.getContext() == null) return;

    List<Map<String, String>> data = new ArrayList<>();
    for (AntlrError error : logcat) {
      Map<String, String> map = new HashMap<>();
      map.put("line", "Line: " + error.getLine());
      map.put("col", "Col: " + error.getCol());
      map.put("msg", "Error: " + error.getMsg());
      data.add(map);
    }

    SimpleAdapter adapter =
        new SimpleAdapter(
            editors.getContext(),
            data,
            android.R.layout.simple_list_item_2,
            new String[] {"line", "msg"},
            new int[] {android.R.id.text1, android.R.id.text2});

    ListView list = new ListView(editors.getContext());
    list.setAdapter(adapter);

    // اضافه کردن کلیک لیستنر برای لیست
    list.setOnItemClickListener(
        (parent, view, position, id) -> {
          if (position < logcat.size()) {
            AntlrError error = logcat.get(position);
            // رفتن به خط خطا
            editors.setSelection(error.getLine() - 1, error.getCol());
          }
        });

    new MaterialAlertDialogBuilder(editors.getContext())
        .setTitle("Errors (" + logcat.size() + ")")
        .setView(list)
        .setPositiveButton("OK", null)
        .show();
  }

  private void updateIconColor() {
    if (errorIcon == null) return;

    if (hascolorChange) {
      errorIcon.setColorFilter(Color.RED);
    } else {
      errorIcon.clearColorFilter();
    }
  }

  @Override
  public void getEditor(CodeEditor arg0) {
    this.editors = arg0;
    editors.setCursorWidth(2.4f);
    // ذخیره رنگ اصلی کرسر
    originalCursorColor = editors.getColorScheme().getColor(EditorColorScheme.SELECTION_INSERT);
    editors.subscribeEvent(
        SelectionChangeEvent.class,
        (event, unsubscribe) -> {
          handleCursorMovement();
        });
  }

  private void handleCursorMovement() {
    if (editors == null || logcat.isEmpty()) {
      restoreOriginalCursorColor();
      return;
    }

    int cursorLine = editors.getCursor().getLeftLine() + 1; // تبدیل به 1-based

    // پیدا کردن خطاهای مربوط به خط فعلی
    List<AntlrError> currentLineErrors = new ArrayList<>();
    for (AntlrError error : logcat) {
      if (error.getLine() == cursorLine) {
        currentLineErrors.add(error);
      }
    }

    // اگر خطایی در این خط وجود دارد، رنگ کرسر را تغییر بده و پاپ‌آپ نشان بده
    if (!currentLineErrors.isEmpty()) {
      changeCursorToErrorColor();

      // نمایش پیام خطاها - فقط اولین خطا رو نشون بده
      AntlrError firstError = currentLineErrors.get(0);
      String errorMessage =
          "خطا در خط: "
              + firstError.getLine()
              + "\nموقعیت: "
              + firstError.getCol()
              + "\nمشکل: "
              + firstError.getMsg();

      // همیشه پاپ‌آپ رو نمایش بده
      EditorPopUp.showPowerMenuAtCursor(editors, errorMessage);

    } else {
      restoreOriginalCursorColor();
    }
  }

  private void changeCursorToErrorColor() {
    if (editors != null && !isCursorColorChanged) {
      editors.getColorScheme().setColor(EditorColorScheme.SELECTION_INSERT, Color.RED);
      isCursorColorChanged = true;
      editors.invalidate();
    }
  }

  private void restoreOriginalCursorColor() {
    if (editors != null && isCursorColorChanged) {
      editors.getColorScheme().setColor(EditorColorScheme.SELECTION_INSERT, originalCursorColor);
      isCursorColorChanged = false;
      editors.invalidate();
    }
  }

  @Override
  public void getFileManagerAc(FileManagerActivity arg0) {}

  @Override
  public boolean hasuseing() {
    return true;
  }

  @Override
  public String langModel() {
    return ".js";
  }

  @Override
  public String setName() {
    return getClass().getName();
  }

  void showErrorContent(CodeEditor editor) {
    try {
      ANTLRInputStream input = new ANTLRInputStream(editor.getText().toString());
      JavaScriptLexer lexer = new JavaScriptLexer(input);
      CommonTokenStream stream = new CommonTokenStream(lexer);
      JavaScriptParser parser = new JavaScriptParser(stream);

      Set<Integer> currentErrorLines = new HashSet<>();
      hascolorChange = false;
      logcat.clear();

      JavaScriptParserBaseListener listener =
          new JavaScriptParserBaseListener() {
            @Override
            public void visitErrorNode(ErrorNode node) {
              hascolorChange = true;
              int line = node.getSymbol().getLine();
              int col = node.getSymbol().getCharPositionInLine();
              String msg = node.getSymbol().getText();

              logcat.add(new AntlrError(line, col, msg));
              currentErrorLines.add(line);
              Utils.setErrorSpan(editor.getTextAnalyzeResult(), line, col);
            }
          };
      ParseTreeWalker walker = new ParseTreeWalker();
      walker.walk(listener, parser.program());

      // حذف آیکون از خطوطی که دیگه خطا ندارن
      for (Integer line : errorLines) {
        if (!currentErrorLines.contains(line)) {
          editor.removeLineIcon(line);
        }
      }

      errorLines = currentErrorLines;

    } catch (Exception e) {
      hascolorChange = true;
      for (Integer line : errorLines) {
        editor.removeLineIcon(line);
      }
      errorLines.clear();
      logcat.clear();
    }
  }

  class AntlrError {
    int line, col;
    String msg;

    public AntlrError(int line, int col, String msg) {
      this.line = line;
      this.col = col;
      this.msg = msg;
    }

    @Override
    public String toString() {
      return "AntlrError[line=" + line + ", col=" + col + ", msg=" + msg + "]";
    }

    public int getLine() {
      return this.line;
    }

    public int getCol() {
      return this.col;
    }

    public void setCol(int col) {
      this.col = col;
    }

    public String getMsg() {
      return this.msg;
    }
  }

  @Override
  public void getBaseCompat(BaseCompat arg0) {}
}
