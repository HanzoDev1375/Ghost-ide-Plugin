package ir.php;

import android.util.Log;
import io.github.rosemoe.sora.data.RainbowBracketHelper;
import io.github.rosemoe.sora.data.Span;
import androidx.core.graphics.ColorUtils;
import android.graphics.Color;
import io.github.rosemoe.sora.interfaces.CodeAnalyzer;
import io.github.rosemoe.sora.text.TextAnalyzeResult;
import io.github.rosemoe.sora.text.TextAnalyzer.AnalyzeThread.Delegate;
import io.github.rosemoe.sora.text.TextStyle;
import io.github.rosemoe.sora.widget.EditorColorScheme;
import ir.ninjacoder.ghostide.core.marco.RegexUtilCompat;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;

public class PhpCodeAnalyzer implements CodeAnalyzer {
  private TextAnalyzeResult result;

  @Override
  public void analyze(CharSequence content, TextAnalyzeResult result, Delegate del) {
    try {
      this.result = result;
      var rb = new RainbowBracketHelper("");
      var lexer = new PhpLexer(CharStreams.fromReader(new StringReader(content.toString())));
      int line, col;
      int lastline = 1;
      Token token;
      int pretoken = -1;
      int type;
      while (del.shouldAnalyze()) {
        token = lexer.nextToken();
        if (token == null) break;
        if (token.getType() == PhpLexer.EOF) {
          lastline = token.getLine() - 1;
          break;
        }
        line = token.getLine() - 1;
        type = token.getType();
        String text1 = token.getText();
        col = token.getCharPositionInLine();
        if (type == PhpLexer.EOF) {
          lastline = line;
          break;
        }
        switch (type) {
          case PhpLexer.Whitespace:
            result.addNormalIfNull();
            break;
          case PhpLexer.Abstract:
          case PhpLexer.Array:
          case PhpLexer.As:
          case PhpLexer.BinaryCast:
          case PhpLexer.BoolType:
          case PhpLexer.BooleanConstant:
          case PhpLexer.Break:
          case PhpLexer.Callable:
          case PhpLexer.Case:
          case PhpLexer.Catch:
          case PhpLexer.Class:
          case PhpLexer.Clone:
          case PhpLexer.Const:
          case PhpLexer.Continue:
          case PhpLexer.Declare:
          case PhpLexer.Default:
          case PhpLexer.Do:
          case PhpLexer.DoubleCast:
          case PhpLexer.DoubleType:
          case PhpLexer.Echo:
          case PhpLexer.Else:
          case PhpLexer.ElseIf:
          case PhpLexer.Empty:
          case PhpLexer.Enum_:
          case PhpLexer.EndDeclare:
          case PhpLexer.EndFor:
          case PhpLexer.EndForeach:
          case PhpLexer.EndIf:
          case PhpLexer.EndSwitch:
          case PhpLexer.EndWhile:
          case PhpLexer.Eval:
          case PhpLexer.Exit:
          case PhpLexer.Extends:
          case PhpLexer.Final:
          case PhpLexer.Finally:
          case PhpLexer.FloatCast:
          case PhpLexer.For:
          case PhpLexer.Foreach:
          case PhpLexer.Function_:
          case PhpLexer.Global:
          case PhpLexer.Goto:
          case PhpLexer.If:
          case PhpLexer.Implements:
          case PhpLexer.Import:
          case PhpLexer.Include:
          case PhpLexer.IncludeOnce:
          case PhpLexer.InstanceOf:
          case PhpLexer.InsteadOf:
          case PhpLexer.Int8Cast:
          case PhpLexer.Int16Cast:
          case PhpLexer.Int64Type:
          case PhpLexer.IntType:
          case PhpLexer.Interface:
          case PhpLexer.IsSet:
          case PhpLexer.List:
          case PhpLexer.LogicalAnd:
          case PhpLexer.LogicalOr:
          case PhpLexer.LogicalXor:
          case PhpLexer.Match_:
          case PhpLexer.Namespace:
          case PhpLexer.New:
          case PhpLexer.Null:
          case PhpLexer.ObjectType:
          case PhpLexer.Parent_:
          case PhpLexer.Partial:
          case PhpLexer.Print:
          case PhpLexer.Private:
          case PhpLexer.Protected:
          case PhpLexer.Public:
          case PhpLexer.Readonly:
          case PhpLexer.Require:
          case PhpLexer.RequireOnce:
          case PhpLexer.Resource:
          case PhpLexer.Return:
          case PhpLexer.Static:
          case PhpLexer.StringType:
          case PhpLexer.Switch:
          case PhpLexer.Throw:
          case PhpLexer.Trait:
          case PhpLexer.Try:
          case PhpLexer.Typeof:
          case PhpLexer.UnicodeCast:
          case PhpLexer.Unset:
          case PhpLexer.Use:
          case PhpLexer.Var:
          case PhpLexer.While:
          case PhpLexer.Yield:
          case PhpLexer.From:
          case PhpLexer.LambdaFn:
          case PhpLexer.Ticks:
          case PhpLexer.Encoding:
          case PhpLexer.StrictTypes:
            putColor(EditorColorScheme.javakeyword, true, false, line, col);
            break;
          case PhpLexer.Spaceship:
          case PhpLexer.DoubleArrow:
          case PhpLexer.Inc:
          case PhpLexer.Dec:
          case PhpLexer.IsIdentical:
          case PhpLexer.IsNoidentical:
          case PhpLexer.IsEqual:
          case PhpLexer.IsNotEq:
          case PhpLexer.IsSmallerOrEqual:
          case PhpLexer.IsGreaterOrEqual:
          case PhpLexer.PlusEqual:
          case PhpLexer.MinusEqual:
          case PhpLexer.MulEqual:
          case PhpLexer.Pow:
          case PhpLexer.PowEqual:
          case PhpLexer.DivEqual:
          case PhpLexer.Concaequal:
          case PhpLexer.ModEqual:
          case PhpLexer.ShiftLeftEqual:
          case PhpLexer.ShiftRightEqual:
          case PhpLexer.AndEqual:
          case PhpLexer.OrEqual:
          case PhpLexer.XorEqual:
          case PhpLexer.BooleanOr:
          case PhpLexer.BooleanAnd:
          case PhpLexer.NullCoalescing:
          case PhpLexer.NullCoalescingEqual:
          case PhpLexer.ShiftLeft:
          case PhpLexer.ShiftRight:
          case PhpLexer.DoubleColon:
          case PhpLexer.ObjectOperator:
          case PhpLexer.NamespaceSeparator:
          case PhpLexer.Ellipsis:
          case PhpLexer.Ampersand:
          case PhpLexer.Pipe:
          case PhpLexer.Bang:
          case PhpLexer.Caret:
          case PhpLexer.Plus:
          case PhpLexer.Minus:
          case PhpLexer.Asterisk:
          case PhpLexer.Percent:
          case PhpLexer.Divide:
          case PhpLexer.Tilde:
          case PhpLexer.SuppressWarnings:
          case PhpLexer.Dollar:
          case PhpLexer.Dot:
          case PhpLexer.QuestionMark:
          case PhpLexer.Comma:
          case PhpLexer.Colon:
          case PhpLexer.SemiColon:
          case PhpLexer.Eq:
          case PhpLexer.Quote:
          case PhpLexer.BackQuote:
            putColor(EditorColorScheme.javaoprator, line, col);
            break;

          case PhpLexer.String_:
            {
              CodeHighlighter.highlightFStringphp(
                  text1, line, col, result, EditorColorScheme.jsoprator);

              break;
            }
          case PhpLexer.Number:
            putColor(EditorColorScheme.javanumber, line, col);
            break;
          case PhpLexer.Greater:
          case PhpLexer.CloseSquareBracket:
          case PhpLexer.PhpEnd:
          case PhpLexer.CloseRoundBracket:
            rb.handleCloseBracket(result, line, col, false);
            break;
          case PhpLexer.OpenRoundBracket:
          case PhpLexer.OpenSquareBracket:
          case PhpLexer.PhpStart:
            rb.handleOpenBracket(result, line, col, false);
            break;
          case PhpLexer.Less:
          case PhpLexer.OpenCurlyBracket:
            rb.handleOpenBracket(result, line, col, true);
            break;
          case PhpLexer.CloseCurlyBracket:
          case PhpLexer.LessEndHtml:
            rb.handleCloseBracket(result, line, col, true);
            break;
          case PhpLexer.HASHComment:
          case PhpLexer.SingleLineComment:
          case PhpLexer.MultiLineComment:
            putColor(EditorColorScheme.COMMENT, line, col);
            break;
          case PhpLexer.HexColor:
            {
              if (RegexUtilCompat.RegexSelect(
                  "(\\#[a-zA-F0-9]{8})|(\\#[a-zA-F0-9]{6})|(\\#[a-zA-F0-9]{3})", text1)) {

                var colors = result;
                String colorString = text1;

                // تبدیل رنگ سه رقمی به شش رقمی
                if (colorString.length() == 4) { // اگر رنگ سه رقمی باشد
                  String red = colorString.substring(1, 2);
                  String green = colorString.substring(2, 3);
                  String blue = colorString.substring(3, 4);
                  colorString = "#" + red + red + green + green + blue + blue; // تبدیل به شش رقمی
                }

                try {
                  int color = Color.parseColor(colorString);
                  colors.addIfNeeded(line, col, EditorColorScheme.white);
                  if (ColorUtils.calculateLuminance(color) > 0.5) {
                    Span span =
                        Span.obtain(
                            col,
                            TextStyle.makeStyle(
                                EditorColorScheme.black));
                    if (span != null) {
                      span.setBackgroundColorMy(color);
                      colors.add(line, span);
                    }
                  } else {
                    Span span =
                        Span.obtain(
                            col,
                            TextStyle.makeStyle(
                                EditorColorScheme.white));
                    if (span != null) {
                      span.setBackgroundColorMy(color);
                      colors.add(line, span);
                    }
                  }

                  Span middle = Span.obtain(col + text1.length(), EditorColorScheme.black);
                  middle.setBackgroundColorMy(Color.TRANSPARENT);
                  colors.add(line, middle);

                  Span end =
                      Span.obtain(
                          col + text1.length(),
                          TextStyle.makeStyle(EditorColorScheme.white));
                  end.setBackgroundColorMy(Color.TRANSPARENT);
                  colors.add(line, end);
                  break;
                } catch (Exception ignore) {
                  ignore.printStackTrace();
                }
              } else {
                result.addIfNeeded(line, col, EditorColorScheme.javastring);
              }

              result.addIfNeeded(line, col, EditorColorScheme.javastring);
              break;
            }
          case PhpLexer.Identifier:
            {
              int id = EditorColorScheme.TEXT_NORMAL;
              boolean isBold = false, isItalic = false;

              if (pretoken == PhpLexer.Class
                  || pretoken == PhpLexer.Interface
                  || pretoken == PhpLexer.Trait) {

                id = EditorColorScheme.javaoprator;
                isBold = true;
              } else if (pretoken == PhpLexer.Function_) {
                id = EditorColorScheme.javaoprator;
                isBold = true;
                isItalic = true;
              } else if (pretoken == PhpLexer.Namespace
                  || pretoken == PhpLexer.Use
                  || pretoken == PhpLexer.From) {

                id = EditorColorScheme.phpcolormatch2;
              } else if (pretoken == PhpLexer.DoubleColon) {
                id = EditorColorScheme.phpcolormatch1;
              } else if (pretoken == PhpLexer.ObjectOperator) {
                id = EditorColorScheme.phpcolormatch1;
              } else if (pretoken == PhpLexer.Less || pretoken == PhpLexer.LessEndHtml) {
                id = EditorColorScheme.javakeywordoprator;
                isBold = true;
              } else if (pretoken == PhpLexer.Extends) {
                id = EditorColorScheme.phpcolormatch4;
              } else if (lexer._input.LA(1) == ':'
                  || lexer._input.LA(2) == ':'
                  || lexer._input.LA(1) == '-') {
                id = EditorColorScheme.tscolormatch2;
              } else if (pretoken == PhpLexer.Dollar) {
                id = EditorColorScheme.phpattr;
              } else if (pretoken == PhpLexer.Number) {
                id = EditorColorScheme.javafun;
              } else if (pretoken == PhpLexer.PhpStart || pretoken == PhpLexer.PhpEnd) {
                id = EditorColorScheme.phphtmlattr;
              } else if (lexer._input.LA(1) == '('
                  || lexer._input.LA(2) == '('
                  || lexer._input.LA(1) == ')'
                  || lexer._input.LA(2) == ')') {
                id = EditorColorScheme.tssymbols;
              } else if (Character.isUpperCase(text1.charAt(0))) {
                Pattern pattern = Pattern.compile("^[A-Z][a-zA-Z0-9_]*$");
                if (pattern.matcher(text1).matches()) {
                  id = EditorColorScheme.phpcolormatch6;
                }
              }

              putColor(id, isBold, isItalic, line, col);
              break;
            }
          default:
            putColor(EditorColorScheme.TEXT_NORMAL, line, col);
            break;
        }
        if (type != PhpLexer.Whitespace) {
          pretoken = type;
        }
      }
      result.determine(lastline);
    } catch (Exception err) {
      Log.e("ErrorToLoadPhp", err.getMessage());
    }
  }

  void putColor(int color, boolean hasbold, boolean hasitalic, int line, int col) {
    result.addIfNeeded(line, col, TextStyle.makeStyle(color, 0, hasbold, hasitalic, false));
  }

  void putColor(int color, boolean hasbold, int line, int col) {
    result.addIfNeeded(line, col, TextStyle.makeStyle(color, 0, hasbold, false, false));
  }

  void putColor(int color, int line, int col) {
    result.addIfNeeded(line, col, TextStyle.makeStyle(color, 0, false, false, false));
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
                true,
                false));
    span.setBackgroundColorMy(color);
    result.add(line, span);

    Span middle = Span.obtain(column + token.length(), EditorColorScheme.TEXT_NORMAL);
    middle.setBackgroundColorMy(Color.TRANSPARENT);
    result.add(line, middle);

    Span end =
        Span.obtain(column + token.length(), TextStyle.makeStyle(EditorColorScheme.TEXT_NORMAL));
    end.setBackgroundColorMy(Color.TRANSPARENT);
    result.add(line, end);
  }
}
