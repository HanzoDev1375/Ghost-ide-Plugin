package ir.ninjacoder.json;

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
      var lexer = new JsonLexerLexer(CharStreams.fromReader(new StringReader(content.toString())));
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
                + JsonLexerLexer.VOCABULARY.getSymbolicName(type)
                + " Text: '"
                + token.getText()
                + "' State: "
                + state);

        switch (type) {
          case JsonLexerLexer.WS:
            break;
          case JsonLexerLexer.LCURLY:
            helper.handleOpenBracket(result, line, column, true);
            braceStack.push(type);
            state = ParseState.IN_OBJECT_KEY; // وارد object شدیم، منتظر key هستیم
            break;

          case JsonLexerLexer.RCURLY:
            helper.handleCloseBracket(result, line, column, true);
            if (!braceStack.isEmpty()) {
              braceStack.pop();
            }
            state = ParseState.NORMAL;
            break;

          case JsonLexerLexer.LBRACKET:
          case JsonLexerLexer.LPAREN:
            helper.handleOpenBracket(result, line, column, false);
            braceStack.push(type);
            state = ParseState.IN_ARRAY_VALUE; // وارد array شدیم، منتظر value هستیم
            break;

          case JsonLexerLexer.RBRACKET:
          case JsonLexerLexer.RPAREN:
            helper.handleCloseBracket(result, line, column, false);
            if (!braceStack.isEmpty()) {
              braceStack.pop();
            }
            state = ParseState.NORMAL;
            break;

          case JsonLexerLexer.COLON:
            result.addIfNeeded(line, column, EditorColorScheme.javaoprator);
            if (state == ParseState.IN_OBJECT_KEY) {
              state = ParseState.AFTER_COLON; // بعد از colon منتظر value هستیم
            }
            break;

          case JsonLexerLexer.COMMA:
            result.addIfNeeded(line, column, EditorColorScheme.javaoprator);
            if (!braceStack.isEmpty()) {
              if (braceStack.peek() == JsonLexerLexer.LCURLY) {
                // در object هستیم، بعد از کاما منتظر key جدید هستیم
                state = ParseState.IN_OBJECT_KEY;
              } else if (braceStack.peek() == JsonLexerLexer.LBRACKET) {
                // در array هستیم، بعد از کاما منتظر value جدید هستیم
                state = ParseState.IN_ARRAY_VALUE;
              }
            }
            break;

          case JsonLexerLexer.STRING:
          case JsonLexerLexer.HEXCOLOR:
            if (state == ParseState.AFTER_COLON) {
              // این string یک VALUE است
              result.addIfNeeded(line, column, EditorColorScheme.javastring);
              state = ParseState.NORMAL;
            } else if (state == ParseState.IN_OBJECT_KEY) {
              // این string یک KEY است
              result.addIfNeeded(line, column, EditorColorScheme.IDENTIFIER_NAME);
              state = ParseState.NORMAL;
            } else {

              result.addIfNeeded(line, column, EditorColorScheme.javastring);
            }

            if (token.getType() == JsonLexerLexer.STRING) {

              String raw = token.getText();

              if (raw.startsWith("\"") && raw.endsWith("\"")) {

                String inner = raw.substring(1, raw.length() - 1);
                int startCol = column + 1;

                // --- HEX ---
                if (inner.startsWith("#")) {
                  String normalized = normalizeHexColor(inner);
                  if (normalized != null) {
                    try {
                      int color = Color.parseColor(normalized);
                      result.addIfNeeded(line, column, EditorColorScheme.javastring);
                      tryToSpanColor(line, startCol, inner, color);
                    } catch (Exception ignore) {
                    }
                  }
                }
                Integer rgb = parseRgbColor(inner);
                if (rgb != null && (inner.startsWith("rgb(") || inner.startsWith("rgba("))) {

                  result.addIfNeeded(line, column, EditorColorScheme.javastring);

                  spanRgbValuesOnly(line, column + 1, inner, rgb);
                  break;
                }
                Integer hsl = parseHslColor(inner);
                if (hsl != null && (inner.startsWith("hsl(") || inner.startsWith("hsla("))) {

                  result.addIfNeeded(line, column, EditorColorScheme.javastring);

                  spanRgbValuesOnly(line, column + 1, inner, hsl);
                  break;
                }
              }
            }
            if (token.getText().equalsIgnoreCase("red")) {
              tryToSpanColor(line, column, token.getText(), Color.RED);
            }
            break;

          case JsonLexerLexer.NUMBER:
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

          case JsonLexerLexer.LITERAL:
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

          case JsonLexerLexer.IDENTIFIER:
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

          case JsonLexerLexer.SYMBOL:
            result.addIfNeeded(line, column, EditorColorScheme.javaoprator);
            break;

          default:
            result.addIfNeeded(line, column, EditorColorScheme.TEXT_NORMAL);
            break;
        }
      }

      result.determine(lexer.getLine() - 1);
      CommonTokenStream stream = new CommonTokenStream(lexer);
      var parser = new JsonLexerParser(stream);
      parser.removeErrorListeners();
      var loader =
          new JsonLexerBaseListener() {
            @Override
            public void visitErrorNode(ErrorNode node) {
              Utils.setErrorSpan(
                  result, node.getSymbol().getLine(), node.getSymbol().getCharPositionInLine());
            }
          };
      ParseTreeWalker.DEFAULT.walk(loader, parser.json());

    } catch (Exception err) {
      Log.e("JSONAnalyzer", "Error in analyze", err);
    }
  }

  void tryToSpanColor(int line, int column, String token, int color) {
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

    Span middle = Span.obtain(column + token.length(), EditorColorScheme.javastring);
    middle.setBackgroundColorMy(Color.TRANSPARENT);
    result.add(line, middle);

    Span end =
        Span.obtain(column + token.length(), TextStyle.makeStyle(EditorColorScheme.javastring));
    end.setBackgroundColorMy(Color.TRANSPARENT);
    result.add(line, end);
  }

  private String normalizeHexColor(String hex) {
    hex = hex.toUpperCase();

    if (hex.length() == 4) {

      char r = hex.charAt(1);
      char g = hex.charAt(2);
      char b = hex.charAt(3);
      return "#" + r + r + g + g + b + b;
    }

    if (hex.length() == 7) {

      return hex;
    }

    if (hex.length() == 9) {

      return hex;
    }

    return null;
  }

  private Integer parseRgbColor(String text) {
    try {
      boolean isRgba = text.startsWith("rgba");
      int start = text.indexOf('(') + 1;
      int end = text.lastIndexOf(')');
      String[] parts = text.substring(start, end).split(",");

      int r = Integer.parseInt(parts[0].trim());
      int g = Integer.parseInt(parts[1].trim());
      int b = Integer.parseInt(parts[2].trim());

      if (isRgba) {
        float a = Float.parseFloat(parts[3].trim());
        return Color.argb((int) (a * 255), r, g, b);
      }

      return Color.rgb(r, g, b);
    } catch (Exception e) {
      return null;
    }
  }

  private Integer parseHslColor(String text) {
    try {
      boolean isHsla = text.startsWith("hsla");
      int start = text.indexOf('(') + 1;
      int end = text.lastIndexOf(')');
      String[] parts = text.substring(start, end).split(",");

      float h = Float.parseFloat(parts[0].trim());
      float s = Float.parseFloat(parts[1].replace("%", "").trim()) / 100f;
      float l = Float.parseFloat(parts[2].replace("%", "").trim()) / 100f;

      float a = 1f;
      if (isHsla) {
        a = Float.parseFloat(parts[3].trim());
      }

      float[] hsl = new float[] {h, s, l};
      int rgb = ColorUtils.HSLToColor(hsl);
      return ColorUtils.setAlphaComponent(rgb, (int) (a * 255));
    } catch (Exception e) {
      return null;
    }
  }

  void spanRgbValuesOnly(int line, int baseColumn, String inner, int color) {

    int open = inner.indexOf('(');
    int close = inner.lastIndexOf(')');

    if (open == -1 || close == -1 || close <= open) return;

    int start = baseColumn + open + 1;
    int length = close - open - 1;

    int fg =
        ColorUtils.calculateLuminance(color) > 0.5
            ? EditorColorScheme.black
            : EditorColorScheme.white;

    Span span = Span.obtain(start, TextStyle.makeStyle(fg, 0, false, false, false));
    span.setBackgroundColorMy(color);
    result.add(line, span);

    Span reset = Span.obtain(start + length, EditorColorScheme.javastring);
    reset.setBackgroundColorMy(Color.TRANSPARENT);
    result.add(line, reset);
  }
}
