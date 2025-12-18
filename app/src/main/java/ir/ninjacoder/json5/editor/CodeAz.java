package ir.ninjacoder.json5.editor;

import androidx.core.graphics.ColorUtils;
import io.github.rosemoe.sora.data.Span;
import io.github.rosemoe.sora.langs.xml.analyzer.Utils;
import io.github.rosemoe.sora.text.TextStyle;
import android.graphics.Color;
import io.github.rosemoe.sora.widget.EditorColorScheme;
import io.github.rosemoe.sora.interfaces.CodeAnalyzer;
import io.github.rosemoe.sora.text.TextAnalyzeResult;
import io.github.rosemoe.sora.text.TextAnalyzer.AnalyzeThread.Delegate;
import android.util.Log;
import ir.ninjacoder.ghostide.core.marco.RegexUtilCompat;
import java.io.StringReader;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import java.util.Stack;
import io.github.rosemoe.sora.data.RainbowBracketHelper;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

public class CodeAz implements CodeAnalyzer {
  RainbowBracketHelper helper;
  TextAnalyzeResult result;

  private enum ParseState {
    IN_OBJECT_KEY, // منتظر key در object هستیم
    AFTER_COLON, // بعد از colon منتظر value هستیم
    IN_ARRAY_VALUE, // در array منتظر value هستیم
    NORMAL
  }

  private ParseState state = ParseState.NORMAL;
  private Stack<Integer> braceStack = new Stack<>();

  @Override
  public void analyze(CharSequence content, TextAnalyzeResult result, Delegate del) {
    try {
      this.result = result;
      var lexer = new JSON5Lexer(CharStreams.fromReader(new StringReader(content.toString())));
      helper = new RainbowBracketHelper("");
      Token token;
      state = ParseState.NORMAL;
      braceStack.clear();

      while (del.shouldAnalyze()) {
        token = lexer.nextToken();

        if (token == null || token.getType() == Token.EOF) {
          break;
        }

        int line = token.getLine() - 1;
        int column = token.getCharPositionInLine();
        int type = token.getType();

        // لاگ برای دیباگ
        Log.d(
            "JSON5Debug",
            "Token: "
                + JSON5Lexer.VOCABULARY.getSymbolicName(type)
                + " Text: '"
                + token.getText()
                + "' State: "
                + state);

        switch (type) {
          case JSON5Lexer.WS:
            break;

          case JSON5Lexer.SINGLE_LINE_COMMENT:
          case JSON5Lexer.MULTI_LINE_COMMENT:
            result.addIfNeeded(line, column, EditorColorScheme.COMMENT);
            break;

          case JSON5Lexer.LCURLY:
            helper.handleOpenBracket(result, line, column, true);
            braceStack.push(type);
            state = ParseState.IN_OBJECT_KEY; // وارد object شدیم، منتظر key هستیم
            break;

          case JSON5Lexer.RCURLY:
            helper.handleCloseBracket(result, line, column, true);
            if (!braceStack.isEmpty()) {
              braceStack.pop();
            }
            state = ParseState.NORMAL;
            break;

          case JSON5Lexer.LBRACKET:
          case JSON5Lexer.LPAREN:
            helper.handleOpenBracket(result, line, column, false);
            braceStack.push(type);
            state = ParseState.IN_ARRAY_VALUE; // وارد array شدیم، منتظر value هستیم
            break;

          case JSON5Lexer.RBRACKET:
          case JSON5Lexer.RPAREN:
            helper.handleCloseBracket(result, line, column, false);
            if (!braceStack.isEmpty()) {
              braceStack.pop();
            }
            state = ParseState.NORMAL;
            break;

          case JSON5Lexer.COLON:
            result.addIfNeeded(line, column, EditorColorScheme.javaoprator);
            if (state == ParseState.IN_OBJECT_KEY) {
              state = ParseState.AFTER_COLON; // بعد از colon منتظر value هستیم
            }
            break;

          case JSON5Lexer.COMMA:
            result.addIfNeeded(line, column, EditorColorScheme.javaoprator);
            if (!braceStack.isEmpty()) {
              if (braceStack.peek() == JSON5Lexer.LCURLY) {
                // در object هستیم، بعد از کاما منتظر key جدید هستیم
                state = ParseState.IN_OBJECT_KEY;
              } else if (braceStack.peek() == JSON5Lexer.LBRACKET) {
                // در array هستیم، بعد از کاما منتظر value جدید هستیم
                state = ParseState.IN_ARRAY_VALUE;
              }
            }
            break;

          case JSON5Lexer.STRING:
          case JSON5Lexer.HEXCOLOR:
            if (state == ParseState.AFTER_COLON) {
              // این string یک VALUE است
              result.addIfNeeded(line, column, EditorColorScheme.javastring);
              state = ParseState.NORMAL;
            } else if (state == ParseState.IN_OBJECT_KEY) {
              // این string یک KEY است
              result.addIfNeeded(line, column, EditorColorScheme.IDENTIFIER_NAME);
              state = ParseState.NORMAL;
            } else {
              // حالت عادی
              result.addIfNeeded(line, column, EditorColorScheme.javastring);
            }
            if(token.getText().startsWith("\"#")) {
            	int color = Color.parseColor(token.getText().substring(1, token.getText().length() - 1));
              tryToSpanColor(line,column,token,color);
            }
            if(token.getText().equals("red")) {
            	tryToSpanColor(line,column,token,Color.RED);
            }
            break;

          case JSON5Lexer.NUMBER:
            if (state == ParseState.AFTER_COLON) {
              // عدد به عنوان VALUE
              result.addIfNeeded(line, column, EditorColorScheme.javanumber);
              state = ParseState.NORMAL;
            } else if (state == ParseState.IN_OBJECT_KEY) {
              // عدد به عنوان KEY
              result.addIfNeeded(line, column, EditorColorScheme.IDENTIFIER_NAME);
              state = ParseState.NORMAL;
            } else {
              // حالت عادی
              result.addIfNeeded(line, column, EditorColorScheme.javanumber);
            }
            break;

          case JSON5Lexer.LITERAL:
          case JSON5Lexer.NUMERIC_LITERAL:
            if (state == ParseState.AFTER_COLON) {
              // literal به عنوان VALUE
              result.addIfNeeded(line, column, EditorColorScheme.javakeyword);
              state = ParseState.NORMAL;
            } else if (state == ParseState.IN_OBJECT_KEY) {
              // literal به عنوان KEY
              result.addIfNeeded(line, column, EditorColorScheme.IDENTIFIER_NAME);
              state = ParseState.NORMAL;
            } else {
              // حالت عادی
              result.addIfNeeded(line, column, EditorColorScheme.javakeyword);
            }
            break;

          case JSON5Lexer.IDENTIFIER:
            if (state == ParseState.AFTER_COLON) {
              // identifier به عنوان VALUE
              result.addIfNeeded(line, column, EditorColorScheme.TEXT_NORMAL);
              state = ParseState.NORMAL;
            } else if (state == ParseState.IN_OBJECT_KEY) {
              // identifier به عنوان KEY
              result.addIfNeeded(line, column, EditorColorScheme.IDENTIFIER_NAME);
              state = ParseState.NORMAL;
            } else {
              // حالت عادی
              result.addIfNeeded(line, column, EditorColorScheme.TEXT_NORMAL);
            }
            break;

          case JSON5Lexer.SYMBOL:
            result.addIfNeeded(line, column, EditorColorScheme.javaoprator);
            break;

          default:
            result.addIfNeeded(line, column, EditorColorScheme.TEXT_NORMAL);
            break;
        }
      }

      result.determine(lexer.getLine() - 1);
      CommonTokenStream stream = new CommonTokenStream(lexer);
      var parser = new JSON5Parser(stream);
      parser.removeErrorListeners();
      var loader =
          new JSON5BaseListener() {
            @Override
            public void visitErrorNode(ErrorNode node) {
              Utils.setErrorSpan(
                  result, node.getSymbol().getLine(), node.getSymbol().getCharPositionInLine());
            }
          };
      ParseTreeWalker.DEFAULT.walk(loader, parser.json5());

    } catch (Exception err) {
      Log.e("JSON5Analyzer", "Error in analyze", err);
    }
  }

  void tryToSpanColor(int line, int column, Token token, int color) {
    Span span =
        Span.obtain(
            column,
            TextStyle.makeStyle(
                ColorUtils.calculateLuminance(color) > 0.5
                    ? EditorColorScheme.black
                    : EditorColorScheme.white,
                0,
                false,
                false,
                false));
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
