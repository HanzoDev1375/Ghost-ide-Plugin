package ir.ninjacoder.difflang;

import io.github.rosemoe.sora.data.Span;
import io.github.rosemoe.sora.text.TextStyle;
import androidx.core.graphics.ColorUtils;
import android.graphics.Color;
import io.github.rosemoe.sora.interfaces.CodeAnalyzer;
import io.github.rosemoe.sora.text.TextAnalyzeResult;
import io.github.rosemoe.sora.text.TextAnalyzer.AnalyzeThread.Delegate;
import io.github.rosemoe.sora.widget.EditorColorScheme;
import java.io.StringReader;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;

public class DiffAz implements CodeAnalyzer {
  protected TextAnalyzeResult result;

  @Override
  public void analyze(CharSequence content, TextAnalyzeResult result, Delegate del) {
    try {
      this.result = result;
      var lexer = new DiffLexer(CharStreams.fromReader(new StringReader(content.toString())));
      Token token;
      boolean inAddedLine = false;
      boolean inRemovedLine = false;
      boolean inDiffGit = false;
      int currentLine = -1;

      while (del.shouldAnalyze()) {
        token = lexer.nextToken();

        if (token == null || token.getType() == Token.EOF) {
          break;
        }

        int line = token.getLine() - 1;
        int column = token.getCharPositionInLine();
        int type = token.getType();

        // اگر خط جدید شروع شد، حالت‌ها را ریست کن
        if (line != currentLine) {
          currentLine = line;
          inAddedLine = false;
          inRemovedLine = false;
          inDiffGit = false;
        }

        switch (type) {
          case DiffLexer.NEWLINE:
            result.addNormalIfNull();
            inAddedLine = false;
            inRemovedLine = false;
            inDiffGit = false;
            break;
          case DiffLexer.WS:
            // برای فاصله‌ها هم رنگ خط را اعمال کن
            if (inAddedLine) {
              result.addIfNeeded(line, column, EditorColorScheme.green);
              tryToSpanColor(
                  line,
                  column,
                  token,
                  ColorUtils.setAlphaComponent(Color.GREEN, 64),
                  EditorColorScheme.green);
            } else if (inRemovedLine) {
              result.addIfNeeded(line, column, EditorColorScheme.red);
              tryToSpanColor(
                  line,
                  column,
                  token,
                  ColorUtils.setAlphaComponent(Color.RED, 64),
                  EditorColorScheme.red);
            } else if (inDiffGit) {
              result.addIfNeeded(line, column, EditorColorScheme.greenyellow);
              tryToSpanColor(
                  line,
                  column,
                  token,
                  ColorUtils.setAlphaComponent(Color.parseColor("#FFD9FF00"), 64),
                  EditorColorScheme.greenyellow);
            } else {
              result.addNormalIfNull();
            }
            break;
          case DiffLexer.INDEX:
            result.addIfNeeded(line, column, EditorColorScheme.blue);
            tryToSpanColor(
                line,
                column,
                token,
                ColorUtils.setAlphaComponent(Color.BLUE, 64),
                EditorColorScheme.blue);
            break;
          case DiffLexer.DIFF:
            result.addIfNeeded(line, column, EditorColorScheme.greenyellow);
            inRemovedLine = false;
            inAddedLine = false;
            inDiffGit = true;
            tryToSpanColor(
                line,
                column,
                token,
                ColorUtils.setAlphaComponent(Color.parseColor("#FFD9FF00"), 64),
                EditorColorScheme.greenyellow);
            break;
          case DiffLexer.OLD_FILE:
          case DiffLexer.REMOVED_LINE:
            result.addIfNeeded(line, column, EditorColorScheme.red);
            inRemovedLine = true;
            inAddedLine = false;
            inDiffGit = false;
            tryToSpanColor(
                line,
                column,
                token,
                ColorUtils.setAlphaComponent(Color.RED, 64),
                EditorColorScheme.red);
            break;
          case DiffLexer.ADDED_LINE:
          case DiffLexer.NEW_FILE:
            result.addIfNeeded(line, column, EditorColorScheme.green);
            inAddedLine = true;
            inRemovedLine = false;
            inDiffGit = false;
            tryToSpanColor(
                line,
                column,
                token,
                ColorUtils.setAlphaComponent(Color.GREEN, 64),
                EditorColorScheme.green);
            break;
          case DiffLexer.HUNK_HEADER:
            result.addIfNeeded(line, column, EditorColorScheme.gold);
            inAddedLine = false;
            inRemovedLine = false;
            inDiffGit = false;
            tryToSpanColor(
                line,
                column,
                token,
                ColorUtils.setAlphaComponent(Color.parseColor("#FFD700"), 64),
                EditorColorScheme.gold);
            break;
          case DiffLexer.NO_NEWLINE:
            result.addIfNeeded(line, column, EditorColorScheme.magenta);
            tryToSpanColor(
                line,
                column,
                token,
                ColorUtils.setAlphaComponent(Color.MAGENTA, 64),
                EditorColorScheme.magenta);
            break;
          case DiffLexer.ID:
          default:
            // همه توکن‌های دیگر (شامل ID و هر چیز دیگر)
            if (inAddedLine) {
              result.addIfNeeded(line, column, EditorColorScheme.green);
              tryToSpanColor(
                  line,
                  column,
                  token,
                  ColorUtils.setAlphaComponent(Color.GREEN, 64),
                  EditorColorScheme.green);
            } else if (inRemovedLine) {
              result.addIfNeeded(line, column, EditorColorScheme.red);
              tryToSpanColor(
                  line,
                  column,
                  token,
                  ColorUtils.setAlphaComponent(Color.RED, 64),
                  EditorColorScheme.red);
            } else if (inDiffGit) {
              result.addIfNeeded(line, column, EditorColorScheme.greenyellow);
              tryToSpanColor(
                  line,
                  column,
                  token,
                  ColorUtils.setAlphaComponent(Color.parseColor("#FFD9FF00"), 64),
                  EditorColorScheme.greenyellow);
            } else {
              result.addIfNeeded(line, column, EditorColorScheme.TEXT_NORMAL);
            }
            break;
        }
      }
      result.determine(lexer.getLine() - 1);
    } catch (Exception err) {
      err.printStackTrace();
    }
  }

  void tryToSpanColor(int line, int column, Token token, int color, int colorState) {
    Span span = Span.obtain(column, TextStyle.makeStyle(colorState, 0, false, false, false));
    span.setBackgroundColorMy(color);
    result.add(line, span);

    Span middle = Span.obtain(column + token.getText().length(), EditorColorScheme.TEXT_NORMAL);
    middle.setBackgroundColorMy(Color.TRANSPARENT);
    result.add(line, middle);

    Span end =
        Span.obtain( 
            column + token.getText().length(), TextStyle.makeStyle(EditorColorScheme.TEXT_NORMAL));
    end.setBackgroundColorMy(Color.TRANSPARENT);
    result.add(line, end);
  }
}
