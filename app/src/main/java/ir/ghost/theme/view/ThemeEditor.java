package ir.ghost.theme.view;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import io.github.rosemoe.sora.event.ClickEvent;
import io.github.rosemoe.sora.event.ContentChangeEvent;
import io.github.rosemoe.sora.event.SelectionChangeEvent;
import io.github.rosemoe.sora.text.CharPosition;
import io.github.rosemoe.sora.text.Content;
import io.github.rosemoe.sora.text.ContentLine;
import ir.ninjacoder.ghostide.core.activities.BaseCompat;
import ir.ninjacoder.ghostide.core.activities.FileManagerActivity;
import ir.ninjacoder.ghostide.core.activities.CodeEditorActivity;
import io.github.rosemoe.sora.widget.CodeEditor;
import ir.ninjacoder.ghostide.core.pl.PluginManagerCompat;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class ThemeEditor implements PluginManagerCompat {

  private static final Map<Character, Character> SYMBOL_PAIRS =
      new HashMap<Character, Character>() {
        {
          put('(', ')');
          put('{', '}');
          put('[', ']');
          put('"', '"');
          put('\'', '\'');
          put('<', '>');
        }
      };

  private boolean isAutoInserting = false;
  private boolean isAutoDeleting = false;
  private CharPosition lastCursorPosition;
  private String lastTypedSymbol = "";

  @Override
  public void getEditor(CodeEditor editor) {
    if (editor == null) return;

    // ثبت همه رویدادهای لازم
    editor.subscribeEvent(
        ContentChangeEvent.class,
        (event, unsubscribe) -> {
          handleContentChange(event, editor);
        });

    editor.subscribeEvent(
        SelectionChangeEvent.class,
        (event, unsubscribe) -> {
          handleSelectionChange(event, editor);
        });

    new Handler(Looper.getMainLooper())
        .post(
            () -> {
              Toast.makeText(
                      editor.getContext(),
                      "Smart Symbol Pairing Activated!\n"
                          + "Features:\n"
                          + "• Auto-pair: (, {, [, \", ', <\n"
                          + "• Smart delete\n"
                          + "• Skip over closing symbols",
                      Toast.LENGTH_LONG)
                  .show();
            });
  }

  private void handleContentChange(ContentChangeEvent event, CodeEditor editor) {
    if (isAutoInserting || isAutoDeleting) {
      return;
    }

    Content content = editor.getText();
    int action = event.getAction();

    if (action == ContentChangeEvent.ACTION_INSERT) {
      CharSequence inserted = event.getChangedText();

      if (inserted.length() == 1) {
        char ch = inserted.charAt(0);

        // اگر سمبل باز تایپ شد
        if (SYMBOL_PAIRS.containsKey(ch)) {
          handleOpeningSymbol(ch, event, editor);
        }
        // اگر سمبل بسته تایپ شد
        else if (SYMBOL_PAIRS.containsValue(ch)) {
          handleClosingSymbolTyped(ch, editor);
        }

        lastTypedSymbol = String.valueOf(ch);
      }

    } else if (action == ContentChangeEvent.ACTION_DELETE) {
      CharSequence deleted = event.getChangedText();

      if (deleted.length() == 1) {
        handleSingleCharacterDelete(deleted.charAt(0), event, editor);
      }
    }
  }

  private void handleSelectionChange(SelectionChangeEvent event, CodeEditor editor) {
    // ذخیره آخرین موقعیت مکان‌نما
    lastCursorPosition = event.getLeft();
  }


  private void handleOpeningSymbol(char openingChar, ContentChangeEvent event, CodeEditor editor) {
    Content content = editor.getText();
    CharPosition start = event.getChangeStart();

    // بررسی آیا بین یک جفت سمبل هستیم
    if (isCursorBetweenMatchingPair(start.line, start.column, openingChar, content)) {
      // فقط مکان‌نما را حرکت بده
      editor.moveSelectionRight();
      return;
    }

    // درج خودکار سمبل بسته
    isAutoInserting = true;
    try {
      char closingChar = SYMBOL_PAIRS.get(openingChar);

      // برای کوتیشن و آپستروف، منطق خاص
      if (openingChar == '"' || openingChar == '\'') {
        handleQuoteInsertion(openingChar, closingChar, start, editor);
      } else {
        handleRegularSymbolInsertion(openingChar, closingChar, start, editor);
      }

    } finally {
      isAutoInserting = false;
    }
  }

  private void handleQuoteInsertion(
      char quoteChar, char closingChar, CharPosition pos, CodeEditor editor) {
    Content content = editor.getText();

    // بررسی کاراکتر قبلی و بعدی
    boolean hasSpaceBefore =
        pos.column == 0 || Character.isWhitespace(content.charAt(pos.line, pos.column - 1));
    boolean hasSpaceAfter =
        pos.column >= content.getColumnCount(pos.line)
            || Character.isWhitespace(content.charAt(pos.line, pos.column));

    // اگر در کلمات هستیم، کوتیشن دوم را اضافه کن
    if (!hasSpaceBefore && hasSpaceAfter) {
      // احتمالاً کوتیشن پایانی یک کلمه است
      content.insert(pos.line, pos.column, String.valueOf(closingChar));
      editor.setSelection(pos.line, pos.column);
    } else {
      // کوتیشن شروع یک کلمه است - هر دو را اضافه کن
      content.insert(pos.line, pos.column, String.valueOf(closingChar));
      editor.setSelection(pos.line, pos.column);
    }
  }

  private void handleRegularSymbolInsertion(
      char openingChar, char closingChar, CharPosition pos, CodeEditor editor) {
    Content content = editor.getText();

    // درج سمبل بسته
    content.insert(pos.line, pos.column, String.valueOf(closingChar));

    // برگشت مکان‌نما بین سمبل‌ها
    editor.setSelection(pos.line, pos.column);
  }

  private void handleClosingSymbolTyped(char typedChar, CodeEditor editor) {
    CharPosition cursorPos = editor.getCursor().left();
    Content content = editor.getText();

    // بررسی آیا سمبل بسته در سمت راست وجود دارد
    if (cursorPos.column < content.getColumnCount(cursorPos.line)) {
      char nextChar = content.charAt(cursorPos.line, cursorPos.column);
      if (nextChar == typedChar) {
        // فقط مکان‌نما را حرکت بده
        editor.moveSelectionRight();
      }
    }
  }

  private void handleSingleCharacterDelete(
      char deletedChar, ContentChangeEvent event, CodeEditor editor) {
    if (isAutoDeleting) return;

    Content content = editor.getText();
    CharPosition start = event.getChangeStart();
    CharPosition end = event.getChangeEnd();

    // فقط وقتی که یک کاراکتر حذف شده باشد
    if (start.line == end.line && start.column + 1 == end.column) {

      // اگر سمبل باز حذف شد
      if (SYMBOL_PAIRS.containsKey(deletedChar)) {
        char closingChar = SYMBOL_PAIRS.get(deletedChar);

        // بررسی آیا سمبل بسته در سمت راست وجود دارد
        if (start.column < content.getColumnCount(start.line)) {
          char nextChar = content.charAt(start.line, start.column);
          if (nextChar == closingChar) {
            // حذف خودکار سمبل بسته
            isAutoDeleting = true;
            try {
              content.delete(start.line, start.column, start.line, start.column + 1);
            } finally {
              isAutoDeleting = false;
            }
          }
        }
      }
      // اگر سمبل بسته حذف شد
      else if (SYMBOL_PAIRS.containsValue(deletedChar)) {
        char openingChar = findOpeningChar(deletedChar);

        // بررسی آیا سمبل باز در سمت چپ وجود دارد
        if (start.column > 1) { // چون یک کاراکتر حذف شده
          char prevChar = content.charAt(start.line, start.column - 2);
          if (prevChar == openingChar) {
            // حذف خودکار سمبل باز
            isAutoDeleting = true;
            try {
              content.delete(start.line, start.column - 2, start.line, start.column - 1);
            } finally {
              isAutoDeleting = false;
            }
          }
        }
      }
    }
  }

  private char findOpeningChar(char closingChar) {
    for (Map.Entry<Character, Character> entry : SYMBOL_PAIRS.entrySet()) {
      if (entry.getValue() == closingChar) {
        return entry.getKey();
      }
    }
    return closingChar;
  }

  private boolean isCursorBetweenMatchingPair(
      int line, int column, char openingChar, Content content) {
    if (column > 0 && column < content.getColumnCount(line)) {
      char leftChar = content.charAt(line, column - 1);
      char rightChar = content.charAt(line, column);

      char expectedClosing = SYMBOL_PAIRS.get(openingChar);

      return leftChar == openingChar && rightChar == expectedClosing;
    }
    return false;
  }

  private void checkIfBetweenPairs(CharPosition pos, CodeEditor editor) {
    Content content = editor.getText();

    for (Map.Entry<Character, Character> pair : SYMBOL_PAIRS.entrySet()) {
      char opening = pair.getKey();
      char closing = pair.getValue();

      if (isCursorBetweenMatchingPair(pos.line, pos.column, opening, content)) {
        // کاربر بین یک جفت سمبل کلیک کرده
        // می‌توانیم یک نشانگر ویژوال اضافه کنیم
        highlightBetweenSymbols(pos.line, pos.column - 1, pos.column, editor);
        break;
      }
    }
  }

  private void highlightBetweenSymbols(int line, int startCol, int endCol, CodeEditor editor) {
    // اینجا می‌توانیم یک هایلایت موقت اضافه کنیم
    // اما فعلاً فقط لاگ می‌کنیم
    System.out.println(
        "Cursor is between symbols at line " + line + ", columns " + startCol + "-" + endCol);
  }

  @Override
  public String setName() {
    return "SmartSymbolPairPlugin";
  }

  @Override
  public boolean hasuseing() {
    return true;
  }

  @Override
  public void getFileManagerAc(FileManagerActivity arg0) {}

  @Override
  public void getCodeEditorAc(CodeEditorActivity ac) {}

  @Override
  public void getBaseCompat(BaseCompat base) {
    if (base instanceof CodeEditorActivity) {
      try {
        CodeEditorActivity codeEditorActivity = (CodeEditorActivity) base;
        Field f = CodeEditorActivity.class.getDeclaredField("editor");
        f.setAccessible(true);
        CodeEditor editor = (CodeEditor) f.get(codeEditorActivity);

        if (editor != null) {
          

          var theme =
              new ThemePreview(
                  codeEditorActivity,
                  "/storage/emulated/0/GhostWebIDE/theme/AsDrak/AsDrak.ghost",
                  null);
          theme.show();

          // پیغام راهنمای استفاده
          new Handler(Looper.getMainLooper())
              .postDelayed(
                  () -> {
                    Toast.makeText(
                            codeEditorActivity,
                            "Tip: Type '(' to get '()'\n" + "Delete '(' to delete both '()'",
                            Toast.LENGTH_LONG)
                        .show();
                  },
                  1000);
        }
      } catch (Exception err) {
        new Handler(Looper.getMainLooper())
            .post(
                () -> {
                  Toast.makeText(base, err.getMessage(), Toast.LENGTH_SHORT).show();
                });
      }
    }
  }

  @Override
  public String langModel() {
    return ".html,.js,.css";
  }
}
