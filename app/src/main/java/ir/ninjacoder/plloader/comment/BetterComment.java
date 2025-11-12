package ir.ninjacoder.plloader.comment;

import io.github.rosemoe.sora.text.TextAnalyzeResult;
import io.github.rosemoe.sora.text.TextStyle;
import io.github.rosemoe.sora.widget.EditorColorScheme;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class BetterComment {

  public static void betterComments(
      String commentText, int line, int startColumn, TextAnalyzeResult result) {

    int alertColor = EditorColorScheme.tscolormatch1;
    int questionColor = EditorColorScheme.jskeyword;
    int importantColor = EditorColorScheme.javafun;
    int todoColor = EditorColorScheme.tscolormatch3;
    int defaultColor = EditorColorScheme.COMMENT;

    // الگوی ساده‌تر و مطمئن‌تر
    Pattern pattern = Pattern.compile("(\\!|\\?|\\*|\\b(TODO|FIX|BUG|HACK):?)");
    Matcher matcher = pattern.matcher(commentText);

    if (matcher.find()) {
      String symbol = matcher.group();
      int symbolStart = matcher.start();
      int symbolEnd = matcher.end();

      int symbolColor = defaultColor;
      int textColor = defaultColor; // رنگ متن بعد از علامت

      if (symbol.equals("!")) {
        symbolColor = alertColor;
        textColor = alertColor; // کل متن بعد از ! هم قرمز بشه
      } else if (symbol.equals("?")) {
        symbolColor = questionColor;
        textColor = questionColor; // کل متن بعد از ? هم آبی بشه
      } else if (symbol.equals("*")) {
        symbolColor = importantColor;
        textColor = importantColor; // کل متن بعد از * هم نارنجی بشه
      } else if (symbol.startsWith("TODO")
          || symbol.startsWith("FIX")
          || symbol.startsWith("BUG")
          || symbol.startsWith("HACK")) {
        symbolColor = todoColor;
        textColor = todoColor; // کل متن بعد از کلمات کلیدی هم سبز بشه
      }

      // قسمت قبل از علامت (همیشه رنگ پیش‌فرض)
      if (symbolStart > 0) {
        String beforeSymbol = commentText.substring(0, symbolStart);
        addTextWithColor(beforeSymbol, line, startColumn, defaultColor, result);
      }

      // خود علامت
      addTextWithColor(symbol, line, startColumn + symbolStart, symbolColor, result, true);

      // قسمت بعد از علامت با رنگ مخصوص
      if (symbolEnd < commentText.length()) {
        String afterSymbol = commentText.substring(symbolEnd);
        addTextWithColor(afterSymbol, line, startColumn + symbolEnd, textColor, result);
      }
    } else {
      // کل کامنت با رنگ پیش‌فرض
      addTextWithColor(commentText, line, startColumn, defaultColor, result);
    }
  }

  private static void addTextWithColor(
      String text, int line, int startColumn, int color, TextAnalyzeResult result, boolean isbold) {
    for (int i = 0; i < text.length(); i++) {
      result.addIfNeeded(
          line, startColumn + i, TextStyle.makeStyle(color, 0, isbold, false, false));
    }
  }

  private static void addTextWithColor(
      String text, int line, int startColumn, int color, TextAnalyzeResult result) {
    for (int i = 0; i < text.length(); i++) {
      result.addIfNeeded(line, startColumn + i, color);
    }
  }
}
