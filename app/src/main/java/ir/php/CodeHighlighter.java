package ir.php;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.rosemoe.sora.data.RainbowBracketHelper;
import io.github.rosemoe.sora.text.TextAnalyzeResult;
import io.github.rosemoe.sora.text.TextStyle;
import io.github.rosemoe.sora.widget.EditorColorScheme;

public class CodeHighlighter {

  public static void highlightFStringphp(
      String fstringtext, int line, int column, TextAnalyzeResult result, int innerColor) {

    // اگر رشته شامل تگ HTML باشد → ابتدا آن را رنگ می‌کنیم
    if (fstringtext.contains("<") && fstringtext.contains(">")) {
      highlightHtmlInsideString(fstringtext, line, column, result);
      return;
    }

    Pattern pattern =
        Pattern.compile("(\\{[^}]+\\})|(\\$\\w+)|(<)|(>[^<]*)|(&\\w+;)|(\\b\\w+\\s*=)");
    Matcher matcher = pattern.matcher(fstringtext);
    var rbcolorHelper = new RainbowBracketHelper(fstringtext);

    int lastIndex = 0;
    int curLine = line;
    int curCol = column;

    while (matcher.find()) {
      int matchStart = matcher.start();

      if (matchStart > lastIndex) {
        String normalText = fstringtext.substring(lastIndex, matchStart);
        addTextWithColor(normalText, curLine, curCol, EditorColorScheme.javastring, result);
        curCol += normalText.length();
      }

      String matchedText = matcher.group();

      if (matchedText.startsWith("{")) {
        rbcolorHelper.handleOpenBracket(result, curLine, curCol, false);
        curCol++;
        String content = matchedText.substring(1, matchedText.length() - 1);
        if (!content.isEmpty()) {
          addTextWithColor(content, curLine, curCol, innerColor, result);
          curCol += content.length();
        }
        rbcolorHelper.handleCloseBracket(result, curLine, curCol, false);
        curCol++;

      } else if (matchedText.startsWith("$")) {
        addTextWithColor(matchedText, curLine, curCol, EditorColorScheme.javanumber, result, true);
        curCol += matchedText.length();

      } else if (matchedText.equals("</")) {
        addTextWithColor("</", curLine, curCol, EditorColorScheme.htmltag, result, true);
        curCol += 2;

      } else if (matchedText.startsWith(">")) {
        addTextWithColor(">", curLine, curCol, EditorColorScheme.tscolormatch2, result, true);
        curCol++;

        if (matchedText.length() > 1) {
          String textAfter = matchedText.substring(1);
          addTextWithColor(textAfter, curLine, curCol, EditorColorScheme.ANNOTATION, result);
          curCol += textAfter.length();
        }

      } else if (matchedText.startsWith("&")) {
        addTextWithColor(matchedText, curLine, curCol, EditorColorScheme.htmlattr, result);
        curCol += matchedText.length();

      } else if (matchedText.endsWith("=")) {
        addTextWithColor(matchedText, curLine, curCol, EditorColorScheme.htmlattrname, result);
        curCol += matchedText.length();
      }

      lastIndex = matcher.end();
    }

    if (lastIndex < fstringtext.length()) {
      String remainingText = fstringtext.substring(lastIndex);
      addTextWithColor(remainingText, curLine, curCol, EditorColorScheme.javastring, result);
    }
  }

  // ---------------------------
  //       HTML Highlighter
  // ---------------------------
  private static void highlightHtmlInsideString(
      String text, int line, int column, TextAnalyzeResult result) {

    Pattern htmlTagPattern = Pattern.compile("(<[^>]+>)");
    Matcher matcher = htmlTagPattern.matcher(text);

    int last = 0;
    int col = column;

    while (matcher.find()) {

      if (matcher.start() > last) {
        String normal = text.substring(last, matcher.start());
        addTextWithColor(normal, line, col, EditorColorScheme.javastring, result);
        col += normal.length();
      }

      String tag = matcher.group();

      // ---------------------
      //  تگ بسته: </div>
      // ---------------------
      if (tag.startsWith("</")) {

        addTextWithColor("</", line, col, EditorColorScheme.htmlblockhash, result, true);
        col += 2;

        String tagName = tag.substring(2, tag.length() - 1);
        addTextWithColor(tagName, line, col, EditorColorScheme.htmltag, result);
        col += tagName.length();

        addTextWithColor(">", line, col, EditorColorScheme.htmlblockhash, result, true);
        col++;

        last = matcher.end();
        continue;
      }

      // ---------------------
      //   تگ باز <div ...>
      // ---------------------
      addTextWithColor("<", line, col, EditorColorScheme.htmltag, result, true);
      col++;

      String inside = tag.substring(1, tag.length() - 1);

      Pattern attrPattern = Pattern.compile("(\\w+)(=)(\"[^\"]+\")?");
      Matcher m2 = attrPattern.matcher(inside);

      int lastInner = 0;

      while (m2.find()) {

        if (m2.start() > lastInner) {
          String txt = inside.substring(lastInner, m2.start());
          addTextWithColor(txt, line, col, EditorColorScheme.jsoprator, result);
          col += txt.length();
        }

        String attrName = m2.group(1);
        String eq = m2.group(2);
        String attrValue = m2.group(3);

        addTextWithColor(attrName, line, col, EditorColorScheme.htmlattrname, result, true);
        col += attrName.length();

        addTextWithColor(eq, line, col, EditorColorScheme.htmlattr, result);
        col++;

        if (attrValue != null) {
          addTextWithColor(attrValue, line, col, EditorColorScheme.tscolormatch2, result);
          col += attrValue.length();
        }

        lastInner = m2.end();
      }

      if (lastInner < inside.length()) {
        String txt = inside.substring(lastInner);
        addTextWithColor(txt, line, col, EditorColorScheme.ANNOTATION, result);
        col += txt.length();
      }

      addTextWithColor(">", line, col, EditorColorScheme.htmltag, result, true);
      col++;

      last = matcher.end();
    }

    if (last < text.length()) {
      String remaining = text.substring(last);
      addTextWithColor(remaining, line, col, EditorColorScheme.javastring, result);
    }
  }

  // ---------------------------
  //       Color Helpers
  // ---------------------------
  private static void addTextWithColor(
      String text, int line, int startColumn, int color, TextAnalyzeResult result, boolean bold) {
    for (int i = 0; i < text.length(); i++) {
      result.addIfNeeded(line, startColumn + i, TextStyle.makeStyle(color, 0, bold, false, false));
    }
  }

  private static void addTextWithColor(
      String text, int line, int startColumn, int color, TextAnalyzeResult result) {
    for (int i = 0; i < text.length(); i++) {
      result.addIfNeeded(line, startColumn + i, color);
    }
  }
}
