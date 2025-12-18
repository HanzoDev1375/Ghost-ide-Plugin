package ir.ninjacoder;

import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import androidx.core.text.StringKt;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayout.Tab;
import io.github.rosemoe.sora.widget.EditorColorScheme;
import ir.ninjacoder.ghostide.core.activities.BaseCompat;
import ir.ninjacoder.ghostide.core.activities.FileManagerActivity;
import ir.ninjacoder.ghostide.core.activities.CodeEditorActivity;
import io.github.rosemoe.sora.widget.CodeEditor;
import ir.ninjacoder.ghostide.core.pl.PluginManagerCompat;
import io.github.rosemoe.sora.event.SelectionChangeEvent;
import io.github.rosemoe.sora.event.ContentChangeEvent;
import io.github.rosemoe.sora.text.Cursor;

public class SearchTop implements PluginManagerCompat {

  private String lastSearchedWord = "";
  private CodeEditor currentEditor;
  private Handler handler = new Handler(Looper.getMainLooper());
  private Runnable searchRunnable;
  private boolean isTyping = false;
  private final int TYPING_DELAY = 500;

  @Override
  public void getEditor(CodeEditor editor) {
    this.currentEditor = editor;

    editor.subscribeEvent(
        ContentChangeEvent.class,
        (event, unsubscribe) -> {
          if (event.getAction() == ContentChangeEvent.ACTION_INSERT
              || event.getAction() == ContentChangeEvent.ACTION_DELETE) {
            handler.removeCallbacksAndMessages(null);
            editor.getSearcher().stopSearch();
            isTyping = true;

            handler.postDelayed(
                () -> {
                  isTyping = false;
                  performDelayedSearch();
                },
                TYPING_DELAY);
          }
        });

    editor.subscribeEvent(
        SelectionChangeEvent.class,
        (event, unsubscribe) -> {
          if (!isTyping && !event.isSelected()) {
            handler.removeCallbacksAndMessages(null);

            handler.postDelayed(
                () -> {
                  performSearch();
                },
                100);
          } else if (event.isSelected()) {

            handler.removeCallbacksAndMessages(null);
            editor.getSearcher().stopSearch();
            lastSearchedWord = "";
          }
        });
  }

  private void performDelayedSearch() {
    if (currentEditor == null || isTyping) return;

    Cursor cursor = currentEditor.getCursor();
    int line = cursor.getLeftLine();
    int column = cursor.getLeftColumn();
    String currentLine = currentEditor.getText().getLineString(line);
    if (currentLine.trim().isEmpty()) {
      currentEditor.getSearcher().stopSearch();
      lastSearchedWord = "";
      return;
    }

    String wordAtCursor = getWordAtPosition(currentLine, column);
    performSearchWithWord(wordAtCursor);
  }

  private void performSearch() {
    if (currentEditor == null || isTyping) return;
    Cursor cursor = currentEditor.getCursor();
    int line = cursor.getLeftLine();
    int column = cursor.getLeftColumn();
    String currentLine = currentEditor.getText().getLineString(line);
    if (currentLine.trim().isEmpty()) {
      currentEditor.getSearcher().stopSearch();
      lastSearchedWord = "";
      return;
    }

    String wordAtCursor = getWordAtPosition(currentLine, column);
    performSearchWithWord(wordAtCursor);
  }

  private void performSearchWithWord(String wordAtCursor) {
    if (!wordAtCursor.isEmpty() && !wordAtCursor.equals(lastSearchedWord)) {
      currentEditor.getSearcher().search(wordAtCursor);

      lastSearchedWord = wordAtCursor;

    } else if (wordAtCursor.isEmpty() && !lastSearchedWord.isEmpty()) {
      currentEditor.getSearcher().stopSearch();
      lastSearchedWord = "";
    }
  }

  private String getWordAtPosition(String line, int position) {
    if (line == null || line.isEmpty() || position < 0 || position > line.length()) {
      return "";
    }

    int start = position;
    while (start > 0 && isWordCharacter(line.charAt(start - 1))) {
      start--;
    }

    int end = position;
    while (end < line.length() && isWordCharacter(line.charAt(end))) {
      end++;
    }

    if (start < end) {
      String word = line.substring(start, end);
      return (word.length() >= 1) ? word : "";
    }

    return "";
  }

  private boolean isWordCharacter(char c) {
    return Character.isLetterOrDigit(c) || c == '_' || c == '$';
  }

  @Override
  public String setName() {
    return "Search top";
  }

  @Override
  public boolean hasuseing() {
    return true;
  }

  @Override
  public void getFileManagerAc(FileManagerActivity arg0) {}

  @Override
  public void getCodeEditorAc(CodeEditorActivity ac) {

    ac.getEditorTabLayout()
        .addOnTabSelectedListener(
            new TabLayout.OnTabSelectedListener() {

              @Override
              public void onTabReselected(TabLayout.Tab arg0) {}

              @Override
              public void onTabSelected(TabLayout.Tab arg0) {
                handler.removeCallbacksAndMessages(null);
                currentEditor.getSearcher().stopSearch();
                isTyping = true;

                handler.postDelayed(
                    () -> {
                      isTyping = false;
                      performDelayedSearch();
                    },
                    TYPING_DELAY);
              }

              @Override
              public void onTabUnselected(TabLayout.Tab arg0) {}
            });
  }

  @Override
  public void getBaseCompat(BaseCompat arg0) {}

  @Override
  public String langModel() {
    return ".html,.js,.c,.cpp,.cs,.rb,.g4,.java,.kt";
  }
}
