package ir.ninjacoder.plloader.comment;

import android.util.Log;
import io.github.rosemoe.sora.text.TextAnalyzeResult;
import io.github.rosemoe.sora.text.TextStyle;
import io.github.rosemoe.sora.widget.EditorColorScheme;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BetterComment {

  private static final Pattern LINE_COMMENT_PATTERN = Pattern.compile("^\\s*//\\s*(.*)$");
  private static final Pattern BLOCK_COMMENT_START = Pattern.compile("^\\s*/\\*.*$");
  private static final Pattern BLOCK_COMMENT_LINE = Pattern.compile("^\\s*\\*\\s*(.*)$");
  private static final Pattern BLOCK_COMMENT_END = Pattern.compile(".*\\*/\\s*$");
  private static final Pattern KEYWORD_PATTERN =
      Pattern.compile("^(?:!|\\?|\\*|TODO|FIX|BUG|HACK)\\b[: -]?(.*)?", Pattern.CASE_INSENSITIVE);

  private static final Pattern HTML_TAG_OPEN = Pattern.compile("<\\??([^/>\\s]+)");
  private static final Pattern HTML_TAG_CLOSE = Pattern.compile("</\\s*([^>\\s]+)\\s*>");
  private static final Pattern HTML_ATTR_NAME = Pattern.compile("([^='\"</>\\s:]+)(?=[:=]|\\s|$)");
  private static final Pattern HTML_ATTR_VALUE = Pattern.compile("([\"'])(.*?)\\1");
  private static final Pattern HTML_NAMESPACE = Pattern.compile("(xmlns:)([^='\"\\s]+)");

  public static void betterComments(
      String commentText, int line, int startColumn, TextAnalyzeResult result) {

    String trimmed = commentText.trim();

    if (BLOCK_COMMENT_START.matcher(trimmed).find()
        || BLOCK_COMMENT_LINE.matcher(trimmed).find()
        || BLOCK_COMMENT_END.matcher(trimmed).find()) {
      handleBlockCommentLine(commentText, line, startColumn, result);
      return;
    }

    Matcher lineMatcher = LINE_COMMENT_PATTERN.matcher(commentText);
    if (lineMatcher.find()) {
      String content = lineMatcher.group(1).trim();

      if (content.matches(".*</?\\w+.*")) {
        getHtmlComment(commentText, line, startColumn, result);
        return;
      }

      long style = detectKeywordStyle(content);
      addStyledText(commentText, line, startColumn, style, result);
      return;
    }

    addStyledText(commentText, line, startColumn, defaultStyle(), result);
  }

  private static long detectKeywordStyle(String content) {
    Matcher matcher = KEYWORD_PATTERN.matcher(content);
    if (matcher.find()) {
      String keyword = matcher.group(0).toUpperCase();

      if (keyword.startsWith("!"))
        return TextStyle.makeStyle(EditorColorScheme.tscolormatch1, 0, true, false, false);
      if (keyword.startsWith("?"))
        return TextStyle.makeStyle(EditorColorScheme.jskeyword, 0, false, true, false);
      if (keyword.startsWith("*"))
        return TextStyle.makeStyle(EditorColorScheme.javafun, 0, true, true, false);
      if (keyword.contains("TODO")
          || keyword.contains("FIX")
          || keyword.contains("BUG")
          || keyword.contains("HACK"))
        return TextStyle.makeStyle(EditorColorScheme.tscolormatch3, 0, false, false, false);
    }

    return defaultStyle();
  }

  private static void handleBlockCommentLine(
      String text, int line, int startColumn, TextAnalyzeResult result) {
    String trimmed = text.trim();
    String inner = trimmed.replaceFirst("^(/\\*+|\\*+|\\*/+)", "").trim();
  }

  static void getHtmlComment(String text, int line, int col, TextAnalyzeResult result) {
    try {
      addStyledText(text, line, col, defaultStyle(), result);

      highlightMatches(text, line, col, HTML_TAG_OPEN, EditorColorScheme.HTML_TAG, result);
      highlightMatches(text, line, col, HTML_TAG_CLOSE, EditorColorScheme.HTML_TAG, result);
      highlightMatches(text, line, col, HTML_ATTR_NAME, EditorColorScheme.ATTRIBUTE_NAME, result);
      highlightMatches(text, line, col, HTML_ATTR_VALUE, EditorColorScheme.LITERAL, result);
      highlightMatches(text, line, col, HTML_NAMESPACE, EditorColorScheme.OPERATOR, result);

    } catch (Throwable err) {
      Log.e("BetterHTML", "Highlight Error: " + err);
    }
  }

  private static void highlightMatches(
      String text, int line, int col, Pattern pattern, int color, TextAnalyzeResult result) {
    Matcher matcher = pattern.matcher(text);
    while (matcher.find()) {
      int start = matcher.start();
      int end = matcher.end();
      for (int i = start; i < end; i++) {
        result.addIfNeeded(line, col + i, TextStyle.makeStyle(color, 0, false, false, false));
      }
    }
  }

  private static void addStyledText(
      String text, int line, int startColumn, long style, TextAnalyzeResult result) {
    for (int i = 0; i < text.length(); i++) {
      result.addIfNeeded(line, startColumn + i, style);
    }
  }

  private static long defaultStyle() {
    return TextStyle.makeStyle(EditorColorScheme.COMMENT, 0, false, false, false);
  }
}
