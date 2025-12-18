package ir.ninjacoder.plloader.betterhtml;

import android.util.Log;

import com.caoccao.javet.interop.NodeRuntime;
import com.caoccao.javet.interop.V8Host;
import com.caoccao.javet.values.reference.V8ValueFunction;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class HTMLBeautifier {

  private static final String TAG = "FullHTMLBeautifier";

  private static final String BASE_URL =
      "https://cdnjs.cloudflare.com/ajax/libs/js-beautify/1.15.4/";
  private static final String LOCAL_DIR =
      "/storage/emulated/0/GhostWebIDE/plugins/betterhtm/beautifier/";
  private static final String HTML_FILE = LOCAL_DIR + "beautify-html.min.js";
  private static final String CSS_FILE = LOCAL_DIR + "beautify-css.min.js";
  private static final String JS_FILE = LOCAL_DIR + "beautify.min.js";

  private NodeRuntime runtime;
  private V8ValueFunction htmlFn, cssFn, jsFn;
  private static final Pattern STYLE_PATTERN =
      Pattern.compile("(?i)<style\\b[^>]*>(.*?)</style>", Pattern.DOTALL);

  private static final Pattern SCRIPT_PATTERN =
      Pattern.compile("(?i)<script\\b[^>]*>(.*?)</script>", Pattern.DOTALL);

  public HTMLBeautifier() {
    init();
  }

  private void init() {
    try {
      runtime = V8Host.getNodeInstance().createV8Runtime();

      File dir = new File(LOCAL_DIR);
      if (!dir.exists()) dir.mkdirs();

      downloadIfNeeded("beautify-html.min.js", HTML_FILE);
      downloadIfNeeded("beautify-css.min.js", CSS_FILE);
      downloadIfNeeded("beautify.min.js", JS_FILE);

      Thread.sleep(1500);

      loadScripts();
      setupJSFunctions();

    } catch (Throwable e) {
      Log.e(TAG, "INIT ERROR", e);
    }
  }

  private void downloadIfNeeded(String fileName, String savePath) {
    if (new File(savePath).exists()) return;

    new Thread(
            () -> {
              try {
                URL url = new URL(BASE_URL + fileName);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setConnectTimeout(20000);
                conn.setReadTimeout(20000);

                try (InputStream in = conn.getInputStream();
                    FileOutputStream out = new FileOutputStream(savePath)) {

                  byte[] buf = new byte[4096];
                  int read;

                  while ((read = in.read(buf)) != -1) {
                    out.write(buf, 0, read);
                  }
                }

                Log.i(TAG, "Downloaded: " + fileName);

              } catch (Exception e) {
                Log.e(TAG, "DOWNLOAD ERROR " + fileName, e);
              }
            })
        .start();
  }

  private String read(String path) throws Exception {
    File f = new File(path);
    try (InputStream in = new FileInputStream(f)) {
      byte[] buf = new byte[(int) f.length()];
      int r = in.read(buf);
      return new String(buf, 0, r, StandardCharsets.UTF_8);
    }
  }

  private void loadScripts() throws Exception {
    runtime.getExecutor(read(HTML_FILE)).executeVoid();
    runtime.getExecutor(read(CSS_FILE)).executeVoid();
    runtime.getExecutor(read(JS_FILE)).executeVoid();
  }

  private void setupJSFunctions() throws Exception {

    String fn =
        "function run_html(code) {\n"
            + "    return html_beautify(code, {\n"
            + "        indent_size: 2,\n"
            + "        indent_char: ' ',\n"
            + "        max_preserve_newlines: 1,\n"
            + "        preserve_newlines: true,\n"
            + "        keep_array_indentation: false,\n"
            + "        break_chained_methods: false,\n"
            + "        indent_scripts: 'normal',\n"
            + "        brace_style: 'collapse',\n"
            + "        space_before_conditional: true,\n"
            + "        unescape_strings: false,\n"
            + "        jslint_happy: false,\n"
            + "        end_with_newline: false,\n"
            + "        wrap_line_length: 0,\n"
            + "        indent_inner_html: true,\n"
            + "        comma_first: false,\n"
            + "        e4x: false,\n"
            + "        indent_empty_lines: false\n"
            + "    });\n"
            + "}\n"
            + "function run_css(code) {\n"
            + "    return css_beautify(code, {\n"
            + "        indent_size: 2,\n"
            + "        indent_char: ' ',\n"
            + "        selector_separator_newline: true,\n"
            + "        end_with_newline: false,\n"
            + "        newline_between_rules: true\n"
            + "    });\n"
            + "}\n"
            + "function run_js(code) {\n"
            + "    return js_beautify(code, {\n"
            + "        indent_size: 2,\n"
            + "        indent_char: ' ',\n"
            + "        preserve_newlines: true,\n"
            + "        max_preserve_newlines: 2,\n"
            + "        space_in_paren: false,\n"
            + "        jslint_happy: false,\n"
            + "        keep_array_indentation: false,\n"
            + "        break_chained_methods: false,\n"
            + "        eval_code: false,\n"
            + "        unescape_strings: false,\n"
            + "        wrap_line_length: 0,\n"
            + "        end_with_newline: false\n"
            + "    });\n"
            + "}";

    runtime.getExecutor(fn).executeVoid();

    htmlFn = runtime.getGlobalObject().get("run_html");
    cssFn = runtime.getGlobalObject().get("run_css");
    jsFn = runtime.getGlobalObject().get("run_js");
  }

  private String beautifyHTML(String str) {
    try {
      return htmlFn.call(null, str).toString();
    } catch (Exception e) {
      Log.e(TAG, "HTML beautify error", e);
      return str;
    }
  }

  private String beautifyCSS(String str) {
    try {
      return cssFn.call(null, str).toString();
    } catch (Exception e) {
      Log.e(TAG, "CSS beautify error", e);
      return str;
    }
  }

  private String beautifyJS(String str) {
    try {
      return jsFn.call(null, str).toString();
    } catch (Exception e) {
      Log.e(TAG, "JS beautify error", e);
      return str;
    }
  }

  public String beautifyHTMLMixed(String html) {
    if (html == null || html.trim().isEmpty()) {
      return html;
    }

    if (!isReady()) {
      Log.w(TAG, "Beautifier not ready yet");
      return html;
    }

    try {
      StringBuilder result = new StringBuilder(html);
      processStyleTags(result);
      processScriptTags(result);
      String finalHtml = beautifyHTML(result.toString());
      Log.i(TAG, "HTML beautification completed successfully");
      return finalHtml;
    } catch (Exception e) {
      Log.e(TAG, "BEAUTIFY MIXED ERROR", e);
      return html;
    }
  }

  private void processStyleTags(StringBuilder html) {
    Matcher matcher = STYLE_PATTERN.matcher(html);
    StringBuffer sb = new StringBuffer();

    while (matcher.find()) {
      String fullMatch = matcher.group(0);
      String cssContent = matcher.group(1);

      if (cssContent != null && !cssContent.trim().isEmpty()) {
        String beautifiedCSS = beautifyCSS(cssContent);

        String replacement = fullMatch.replace(cssContent, "\n" + beautifiedCSS + "\n");
        matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
      } else {
        matcher.appendReplacement(sb, Matcher.quoteReplacement(fullMatch));
      }
    }
    matcher.appendTail(sb);

    html.setLength(0);
    html.append(sb.toString());
  }

  private void processScriptTags(StringBuilder html) {
    Matcher matcher = SCRIPT_PATTERN.matcher(html);
    StringBuffer sb = new StringBuffer();

    while (matcher.find()) {
      String fullMatch = matcher.group(0);
      String jsContent = matcher.group(1);

      if (jsContent != null && !jsContent.trim().isEmpty()) {

        String tagStart = fullMatch.substring(0, fullMatch.indexOf(">") + 1);
        if (!tagStart.contains("type=")
            || tagStart.contains("type=\"text/javascript\"")
            || tagStart.contains("type='text/javascript'")
            || tagStart.contains("type=text/javascript")) {

          String beautifiedJS = beautifyJS(jsContent);

          String replacement = fullMatch.replace(jsContent, "\n" + beautifiedJS + "\n");
          matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        } else {
          matcher.appendReplacement(sb, Matcher.quoteReplacement(fullMatch));
        }
      } else {
        matcher.appendReplacement(sb, Matcher.quoteReplacement(fullMatch));
      }
    }
    matcher.appendTail(sb);

    html.setLength(0);
    html.append(sb.toString());
  }

  public boolean isReady() {
    return htmlFn != null && cssFn != null && jsFn != null && runtime != null;
  }

  public void destroy() {
    try {
      if (htmlFn != null && !htmlFn.isClosed()) htmlFn.close();
      if (cssFn != null && !cssFn.isClosed()) cssFn.close();
      if (jsFn != null && !jsFn.isClosed()) jsFn.close();
      if (runtime != null && !runtime.isClosed()) runtime.close();
    } catch (Exception e) {
      Log.e(TAG, "Error during cleanup", e);
    }
  }
}
