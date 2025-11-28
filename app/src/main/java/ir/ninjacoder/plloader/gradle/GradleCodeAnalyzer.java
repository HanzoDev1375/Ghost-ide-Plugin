package ir.ninjacoder.plloader.gradle;

import android.util.Log;
import io.github.rosemoe.sora.data.Span;
import androidx.core.graphics.ColorUtils;
import android.graphics.Color;
import io.github.rosemoe.sora.interfaces.CodeAnalyzer;
import io.github.rosemoe.sora.text.TextAnalyzeResult;
import io.github.rosemoe.sora.text.TextAnalyzer.AnalyzeThread.Delegate;
import io.github.rosemoe.sora.text.TextStyle;
import io.github.rosemoe.sora.widget.EditorColorScheme;
import io.github.rosemoe.sora.widget.CodeEditor;
import ir.ninjacoder.codesnap.antlr4.gradle.GradleLexer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;

public class GradleCodeAnalyzer implements CodeAnalyzer {
  TextAnalyzeResult result;
  private static final List<DependencyInfo> allDependencies = new ArrayList<>();
  private static boolean firstAnalysis = true;
  private CodeEditor editor;

  public GradleCodeAnalyzer(CodeEditor editor) {
    this.editor = editor;
  }

  public GradleCodeAnalyzer() {
    this.editor = null;
  }

  @Override
  public void analyze(CharSequence content, TextAnalyzeResult result, Delegate del) {
    try {
      this.result = result;

      if (firstAnalysis) {
        firstAnalysis = false;
        allDependencies.clear();
        checkAllDependencies(content.toString());
      }

      int type, lastLine = 1;
      int line, column;
      Token token;
      int pretoken = -1;
      var lexer = new GradleLexer(CharStreams.fromReader(new StringReader(content.toString())));
      while (del.shouldAnalyze()) {
        token = lexer.nextToken();
        if (token == null) break;
        if (token.getType() == GradleLexer.EOF) {
          lastLine = token.getLine() - 1;
          break;
        }
        line = token.getLine() - 1;
        type = token.getType();
        column = token.getCharPositionInLine();
        if (type == GradleLexer.EOF) {
          lastLine = line;
          break;
        }
        switch (type) {
          case GradleLexer.WS:
            result.addNormalIfNull();
            break;

          case GradleLexer.APPLY:
          case GradleLexer.TASK:
          case GradleLexer.BUILDSCRIPT:
          case GradleLexer.REPOSITORIES:
          case GradleLexer.MAVEN:
          case GradleLexer.GOOGLE:
          case GradleLexer.GRADLE:
          case GradleLexer.PROJECT:
          case GradleLexer.SETTINGS:
          case GradleLexer.FILE_TREE:
          case GradleLexer.FILES:
          case GradleLexer.DO_LAST:
          case GradleLexer.FROM:
          case GradleLexer.INTO:
            result.addIfNeeded(
                line,
                column,
                TextStyle.makeStyle(EditorColorScheme.javakeyword, 0, true, false, false));
            break;

          case GradleLexer.IMPLEMENTATION:
          case GradleLexer.COMPILE_ONLY:
          case GradleLexer.RUNTIME_ONLY:
          case GradleLexer.TEST_IMPLEMENTATION:
          case GradleLexer.ANDROID_TEST_IMPLEMENTATION:
          case GradleLexer.API:
          case GradleLexer.KAPT:
          case GradleLexer.ANNOTATION_PROCESSOR:
          case GradleLexer.COMPILE:
          case GradleLexer.RUNTIME:
          case GradleLexer.TEST_COMPILE:
          case GradleLexer.TEST_RUNTIME:
          case GradleLexer.VARIANT_CONFIGURATION:
            result.addIfNeeded(
                line,
                column,
                TextStyle.makeStyle(EditorColorScheme.javakeywordoprator, 0, true, false, false));
            break;

          case GradleLexer.SINGLE_QUOTE_STRING:
          case GradleLexer.DOUBLE_QUOTE_STRING:
          case GradleLexer.TRIPLE_QUOTE_STRING:
            String tokenText = token.getText();
            if (isDependencyString(tokenText)) {
              DependencyInfo dep = extractDependencyFromString(tokenText);
              if (dep != null) {

                boolean shouldHighlight =
                    RepositoryManager.isDependencyOutdated(dep.group, dep.artifact, dep.version);

                if (shouldHighlight) {
                  tryToSpanColor(line, column, token, Color.parseColor("#FFFFE0B2"));
                } else {
                  result.addIfNeeded(
                      line,
                      column,
                      TextStyle.makeStyle(EditorColorScheme.javastring, 0, true, false, false));
                }
              } else {
                result.addIfNeeded(
                    line,
                    column,
                    TextStyle.makeStyle(EditorColorScheme.javastring, 0, true, false, false));
              }
            } else {
              result.addIfNeeded(
                  line,
                  column,
                  TextStyle.makeStyle(EditorColorScheme.javastring, 0, true, false, false));
            }
            break;

          case GradleLexer.DEPENDENCIES:
          case GradleLexer.PLUGINS:
            result.addIfNeeded(
                line,
                column,
                TextStyle.makeStyle(EditorColorScheme.javafield, 0, true, false, false));
            break;

          case GradleLexer.INTEGER:
          case GradleLexer.FLOAT:
            result.addIfNeeded(
                line,
                column,
                TextStyle.makeStyle(EditorColorScheme.javanumber, 0, true, false, false));
            break;
          case GradleLexer.ASSIGN:
          case GradleLexer.PLUS:
          case GradleLexer.MINUS:
          case GradleLexer.MULT:
          case GradleLexer.DIV:
          case GradleLexer.DOT:
          case GradleLexer.COLON:
          case GradleLexer.COMMA:
          case GradleLexer.SEMICOLON:
          case GradleLexer.LPAREN:
          case GradleLexer.RPAREN:
          case GradleLexer.LBRACE:
          case GradleLexer.RBRACE:
          case GradleLexer.LBRACKET:
          case GradleLexer.RBRACKET:
            result.addIfNeeded(
                line,
                column,
                TextStyle.makeStyle(EditorColorScheme.javaoprator, 0, true, false, false));
            break;
          case GradleLexer.TRUE:
          case GradleLexer.FALSE:
          case GradleLexer.NULL:
            result.addIfNeeded(
                line, column, TextStyle.makeStyle(EditorColorScheme.jsfun, 0, true, false, false));
            break;

          case GradleLexer.AS:
          case GradleLexer.ASSERT:
          case GradleLexer.BREAK:
          case GradleLexer.CASE:
          case GradleLexer.CATCH:
          case GradleLexer.CLASS:
          case GradleLexer.CONST:
          case GradleLexer.CONTINUE:
          case GradleLexer.DEF:
          case GradleLexer.DEFAULT:
          case GradleLexer.DO:
          case GradleLexer.ELSE:
          case GradleLexer.ENUM:
          case GradleLexer.EXTENDS:
          case GradleLexer.FINALLY:
          case GradleLexer.FOR:
          case GradleLexer.GOTO:
          case GradleLexer.IF:
          case GradleLexer.IMPLEMENTS:
          case GradleLexer.IMPORT:
          case GradleLexer.IN:
          case GradleLexer.INSTANCEOF:
          case GradleLexer.INTERFACE:
          case GradleLexer.NEW:
          case GradleLexer.PACKAGE:
          case GradleLexer.RETURN:
          case GradleLexer.SUPER:
          case GradleLexer.SWITCH:
          case GradleLexer.THIS:
          case GradleLexer.THROW:
          case GradleLexer.THROWS:
          case GradleLexer.TRAIT:
          case GradleLexer.TRY:
          case GradleLexer.WHILE:
            result.addIfNeeded(
                line,
                column,
                TextStyle.makeStyle(EditorColorScheme.jskeyword, 0, true, false, false));
            break;
          case GradleLexer.LINE_COMMENT:
          case GradleLexer.BLOCK_COMMENT:
          case GradleLexer.DOC_COMMENT:
            result.addIfNeeded(
                line,
                column,
                TextStyle.makeStyle(EditorColorScheme.COMMENT, 0, true, false, false));
            break;

          case GradleLexer.IDENTIFIER:
            {
              int colorNormal = EditorColorScheme.TEXT_NORMAL;
              boolean isBold = false;

              if (pretoken == GradleLexer.TASK) {
                colorNormal = EditorColorScheme.tscolormatch1;
                isBold = true;
              } else if (pretoken == GradleLexer.PROJECT
                  || pretoken == GradleLexer.APPLY
                  || pretoken == GradleLexer.REPOSITORIES
                  || pretoken == GradleLexer.BUILDSCRIPT) {
                colorNormal = EditorColorScheme.tscolormatch2;
              } else if (pretoken == GradleLexer.IMPLEMENTATION
                  || pretoken == GradleLexer.COMPILE_ONLY
                  || pretoken == GradleLexer.RUNTIME_ONLY
                  || pretoken == GradleLexer.TEST_IMPLEMENTATION) {
                colorNormal = EditorColorScheme.tscolormatch3;
                isBold = true;
              } else if (pretoken == GradleLexer.DEF
                  || pretoken == GradleLexer.CLASS
                  || pretoken == GradleLexer.IMPORT
                  || pretoken == GradleLexer.IMPLEMENTS) {
                colorNormal = EditorColorScheme.tscolormatch4;
              } else if (lexer._input.LA(2) == '{' || lexer._input.LA("{".length()) == '{') {
                colorNormal = EditorColorScheme.pycolormatch1;
                isBold = true;
              } else if (lexer._input.LA(1) == '.') {
                colorNormal = EditorColorScheme.pycolormatch2;
              } else if (pretoken == GradleLexer.DOT) {
                colorNormal = EditorColorScheme.pycolormatch3;
              } else if (Character.isUpperCase(token.getText().charAt(0))) {
                Pattern pattern = Pattern.compile("^[A-Z][a-zA-Z0-9_]*$");
                if (pattern.matcher(token.getText()).matches()) {
                  colorNormal = EditorColorScheme.pycolormatch4;
                }
              }

              result.addIfNeeded(
                  line, column, TextStyle.makeStyle(colorNormal, 0, true, false, false));
              break;
            }
          default:
            result.addIfNeeded(
                line,
                column,
                TextStyle.makeStyle(EditorColorScheme.TEXT_NORMAL, 0, false, false, false));
            break;
        }
        if (type != GradleLexer.WS) {
          pretoken = type;
        }
      }

      result.determine(lastLine);
    } catch (Exception err) {
      Log.e("GradleAnalyzer", "Error in analyze: " + err.getMessage());
    }
  }

  private void checkAllDependencies(String content) {
    new Thread(
            () -> {
              try {
                String[] lines = content.split("\n");
                for (int i = 0; i < lines.length; i++) {
                  String line = lines[i];
                  if (isDependencyLine(line)) {
                    DependencyInfo dep = extractDependencyFromLine(line);
                    if (dep != null) {
                      dep =
                          new DependencyInfo(
                              i, dep.start, dep.group, dep.artifact, dep.version, dep.fullMatch);
                      allDependencies.add(dep);

                      // به صورت غیرهمزمان چک کن برای آپدیت‌های آینده
                      checkDependencyUpdateAsync(dep);
                    }
                  }
                }
              } catch (Exception e) {
                Log.e("GradleAnalyzer", "Error checking all dependencies: " + e.getMessage());
              }
            })
        .start();
  }

  private void checkDependencyUpdateAsync(DependencyInfo dep) {
    RepositoryManager.getLatestVersion(
        dep.group,
        dep.artifact,
        new RepositoryManager.VersionCheckCallback() {
          @Override
          public void onVersionFound(String latestVersion) {
            if (editor != null && latestVersion != null && !latestVersion.equals(dep.version)) {
              editor.post(editor::invalidate);
            }
          }

          @Override
          public void onError(String error) {
            Log.d("GradleAnalyzer", "No update available for: " + dep.group + ":" + dep.artifact);
          }
        });
  }

  private boolean isDependencyLine(String line) {
    return line.matches(".*(implementation|api|compile|runtimeOnly|annotationProcessor).*");
  }

  private DependencyInfo extractDependencyFromLine(String lineText) {
    Pattern pattern =
        Pattern.compile(
            "(implementation|api|compile|runtimeOnly|annotationProcessor)\\s*(?:\\(?['\"]([^:]+):([^:]+):([^)'\"]+)['\"]\\)?)");

    Matcher matcher = pattern.matcher(lineText);
    if (matcher.find()) {
      return new DependencyInfo(
          0,
          matcher.start(),
          matcher.group(2),
          matcher.group(3),
          matcher.group(4),
          matcher.group(0));
    }
    return null;
  }

  private boolean isDependencyString(String text) {
    return text.matches("['\"].*:.*:.*['\"]");
  }

  private DependencyInfo extractDependencyFromString(String text) {
    String cleanText = text.replaceAll("^['\"]|['\"]$", "");
    Pattern pattern = Pattern.compile("([^:]+):([^:]+):([^:]+)");
    Matcher matcher = pattern.matcher(cleanText);
    if (matcher.find()) {
      return new DependencyInfo(0, 0, matcher.group(1), matcher.group(2), matcher.group(3), text);
    }
    return null;
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
                true,
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
