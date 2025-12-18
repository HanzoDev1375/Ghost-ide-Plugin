package ir.ninjacoder.json5;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import io.github.rosemoe.sora.langs.json.JsonLanguage;
import io.github.rosemoe.sora.widget.EditorPopupWindow;
import android.widget.TextView;
import android.graphics.drawable.GradientDrawable;
import io.github.rosemoe.sora.widget.EditorColorScheme;
import io.github.rosemoe.sora.event.ContentChangeEvent;
import io.noties.markwon.Markwon;
import ir.ninjacoder.ghostide.core.activities.BaseCompat;
import ir.ninjacoder.ghostide.core.activities.FileManagerActivity;
import ir.ninjacoder.ghostide.core.activities.CodeEditorActivity;
import io.github.rosemoe.sora.widget.CodeEditor;
import ir.ninjacoder.ghostide.core.pl.PluginManagerCompat;
import ir.ninjacoder.json5.editor.JSON5Lexer;
import ir.ninjacoder.json5.editor.JSON5Parser;
import ir.ninjacoder.json5.editor.JSON5BaseListener;
import ir.ninjacoder.prograsssheet.listchild.Child;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

public class Json5Lang implements PluginManagerCompat {
  CodeEditor editor;
  FileManagerActivity ac;
  List<Node> node = new ArrayList<>();
  CodeEditorActivity codeEditorActivity;

  @Override
  public void getEditor(CodeEditor arg0) {
    this.editor = arg0;
  }

  @Override
  public String setName() {
    return "Json5";
  }

  @Override
  public boolean hasuseing() {
    return true;
  }

  @Override
  public void getFileManagerAc(FileManagerActivity ac) {
    this.ac = ac;

    String extensions = langModel();

    String firstExtension = extensions;
    if (extensions.contains(",")) {
      firstExtension = extensions.split(",")[0].trim();
    }

    Child child =
        new Child(
            firstExtension, "/storage/emulated/0/GhostWebIDE/plugins/jsonlang/json_official.png");

    ac.addChild(child);

    Toast.makeText(ac, "JSON5 Plugin Loaded: " + extensions, Toast.LENGTH_LONG).show();

    if (extensions.contains(",")) {
      String[] allExtensions = extensions.split(",");
      for (int i = 1; i < allExtensions.length; i++) {
        String ext = allExtensions[i].trim();
        if (!ext.isEmpty()) {
          Child extraChild =
              new Child(ext, "/storage/emulated/0/GhostWebIDE/plugins/jsonlang/json_official.png");
          ac.addChild(extraChild);
        }
      }
    }
  }

  @Override
  public void getCodeEditorAc(CodeEditorActivity ac) {
    this.codeEditorActivity = ac;
    Child child =
        new Child(
            false,
            () -> {
              Log.e(getClass().getSimpleName(), "اینجا خالی باشه بهتره");
              // اینجا رو خالی بزار چون هین ما جا به شدن تب به مشکل میخوری
            },
            1100);
    ac.addChild(child);
    new Handler()
        .postDelayed(
            () -> {
              try {
                if (editor != null) {
                  editor.setEditorLanguage(new JsonLang());
                  editor.analyze(true);

                  Toast.makeText(
                          ac,
                          "JSON language applied for extensions: " + langModel(),
                          Toast.LENGTH_SHORT)
                      .show();

                  // پارس کردن JSON5 بعد از تنظیم زبان
                  parseAndShowErrors();
                }
              } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(ac, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
              }
            },
            500);
  }

  void parseAndShowErrors() {
    new Thread(
            () -> {
              try {
                var lexer = new JSON5Lexer(CharStreams.fromString(editor.getTextAsString()));
                CommonTokenStream stream = new CommonTokenStream(lexer);
                var parser = new JSON5Parser(stream);
                parser.removeErrorListeners();

                final List<Node> errors = new ArrayList<>();

                var loader =
                    new JSON5BaseListener() {
                      @Override
                      public void visitErrorNode(ErrorNode nodes) {
                        errors.add(
                            new Node(
                                nodes.getSymbol().getLine(),
                                nodes.getSymbol().getCharPositionInLine(),
                                "JSON5 Syntax Error: " + nodes.getText()));
                      }
                    };
                ParseTreeWalker.DEFAULT.walk(loader, parser.json5());

                // برگردون به main thread برای نمایش
                new Handler(Looper.getMainLooper())
                    .post(
                        () -> {
                          if (!errors.isEmpty()) {
                            // نمایش اولین خطا
                            Node firstError = errors.get(0);
                            showPowerMenuAtCursor(editor, firstError.getMsg());

                            // یا نمایش همه خطاها
                            // for (Node error : errors) {
                            //     showPowerMenuAtCursor(editor, error.getMsg());
                            // }
                          }
                        });
              } catch (Exception err) {
                Log.e("ErrorToParserJson5", err.getMessage());
              }
            })
        .start();
  }

  @Override
  public void getBaseCompat(BaseCompat base) {}

  @Override
  public String langModel() {

    return ".kson,.json5,.fson,.pathghost";
  }

  /** چون نمیخام وابسته گی باشه برای همین کپی کردم و مارک داون هم میخام لود کنم */
  void showPowerMenuAtCursor(CodeEditor editor, String message) {
    try {

      EditorPopupWindow popupWindow =
          new EditorPopupWindow(
              editor,
              EditorPopupWindow.FEATURE_SCROLL_AS_CONTENT
                  | EditorPopupWindow.FEATURE_SHOW_OUTSIDE_VIEW_ALLOWED);
      TextView textView = new TextView(editor.getContext());
      Markwon md = Markwon.create(textView.getContext());
      textView.setSingleLine(true);
      textView.setPadding(32, 16, 32, 16);
      GradientDrawable f = new GradientDrawable();
      f.setColor(editor.getColorScheme().getColor(EditorColorScheme.AUTO_COMP_PANEL_BG));
      f.setStroke(1, editor.getColorScheme().getColor(EditorColorScheme.AUTO_COMP_PANEL_CORNER));
      f.setCornerRadius(25);
      textView.setBackground(f);
      textView.setTextSize(14);
      textView.setTextColor(editor.getColorScheme().getColor(EditorColorScheme.TEXT_NORMAL));
      md.setMarkdown(textView, message);
      popupWindow.setContentView(textView);
      popupWindow.setOutsideTouchable(true);

      textView.measure(
          View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
          View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

      int width = textView.getMeasuredWidth();
      int height = textView.getMeasuredHeight();
      popupWindow.setSize(width, height);
      var selection = editor.getCursor().left();
      float charX = editor.getCharOffsetX(selection.getLine(), selection.getColumn());
      float charY =
          editor.getCharOffsetY(selection.getLine(), selection.getColumn()) - editor.getRowHeight();

      var locationBuffer = new int[2];
      editor.getLocationInWindow(locationBuffer);
      float restAbove = charY + locationBuffer[1];
      float restBottom = editor.getHeight() - charY - editor.getRowHeight();

      boolean completionShowing = editor.getAutoCompleteWindow().isShowing();
      float windowY;
      if (restAbove > restBottom || completionShowing) {
        windowY = charY - popupWindow.getHeight();
      } else {
        windowY = charY + editor.getRowHeight() * 1.5f;
      }

      if (completionShowing && windowY < 0) {
        return;
      }

      float windowX = Math.max(charX - popupWindow.getWidth() / 2f, 0f);
      popupWindow.setLocationAbsolutely((int) windowX, (int) windowY);
      popupWindow.show();
      editor.subscribeEvent(
          ContentChangeEvent.class,
          (event, sub) -> {
            if (event.getAction() == ContentChangeEvent.ACTION_DELETE
                || event.getAction() == ContentChangeEvent.ACTION_INSERT
                || event.getAction() == ContentChangeEvent.ACTION_SET_NEW_TEXT) {
              popupWindow.dismiss();
            } else popupWindow.show();
          });
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  class Node {
    int line, col;
    String msg;

    public Node(int line, int col, String msg) {
      this.line = line;
      this.col = col;
      this.msg = msg;
    }

    public int getLine() {
      return this.line;
    }

    public int getCol() {
      return this.col;
    }

    public String getMsg() {
      return this.msg;
    }
  }
}
