package ir.ninjacoder.plloader.csslsp;

import android.content.Context;
import android.util.Log;

import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.interop.NodeRuntime;
import com.caoccao.javet.interop.V8Host;
import com.caoccao.javet.values.reference.V8ValueFunction;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class CSSBeautifier {

  private static final String TAG = "CSSBeautifier";
  private static final String BEAUTIFY_URL =
      "https://cdnjs.cloudflare.com/ajax/libs/js-beautify/1.15.4/beautify-css.min.js";
  private static final String LOCAL_PATH =
      "/storage/emulated/0/GhostWebIDE/plugins/csslsp/data/beautify-css.min.js";

  private NodeRuntime nodeRuntime;
  private V8ValueFunction beautifyFunction;
  private Context context;

  public CSSBeautifier(Context ctx) {
    this.context = ctx;
    initNode();
  }

  private void initNode() {
    try {
      nodeRuntime = V8Host.getNodeInstance().createV8Runtime();

      // اول دانلود کن اگر فایل وجود نداره
      File beautifyFile = new File(LOCAL_PATH);
      if (!beautifyFile.exists()) {
        downloadBeautifyCSS();
      }

      setupBeautify();
      Log.d(TAG, "CSS Beautifier initialized");
    } catch (Throwable e) {
      Log.e(TAG, "INIT ERROR", e);
    }
  }

  private void downloadBeautifyCSS() {
    new Thread(
            () -> {
              try {
                Log.d(TAG, "Downloading beautify-css...");

                URL url = new URL(BEAUTIFY_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(30000);
                connection.setReadTimeout(30000);

                // ایجاد پوشه اگر وجود نداره
                File dir = new File("/storage/emulated/0/GhostWebIDE/plugins/csslsp/data/");
                if (!dir.exists()) {
                  dir.mkdirs();
                }

                File outputFile = new File(LOCAL_PATH);

                try (InputStream input = connection.getInputStream();
                    FileOutputStream output = new FileOutputStream(outputFile)) {

                  byte[] buffer = new byte[4096];
                  int bytesRead;
                  while ((bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                  }
                }

                Log.d(TAG, "Download completed: " + outputFile.length() + " bytes");

              } catch (Exception e) {
                Log.e(TAG, "DOWNLOAD ERROR", e);
              }
            })
        .start();
  }

  private String readFileToString(File f) throws Exception {
    try (InputStream in = new BufferedInputStream(new FileInputStream(f))) {
      byte[] buf = new byte[(int) f.length()];
      int read = in.read(buf);
      return new String(buf, 0, read, StandardCharsets.UTF_8);
    }
  }

  private void setupBeautify() throws Exception {
    File beautifyFile = new File(LOCAL_PATH);

    // اگر فایل وجود نداره صبر کن دانلود بشه
    if (!beautifyFile.exists()) {
      Thread.sleep(2000); // 2 ثانیه صبر کن
      if (!beautifyFile.exists()) {
        Log.e(TAG, "Beautify file not found after download attempt");
        return;
      }
    }

    String src = readFileToString(beautifyFile);

    // Load beautify-css
    nodeRuntime.getExecutor(src).executeVoid();

    // Setup beautify function
    String setupFn =
        "function beautifyCSS(cssCode) {\n"
            + "  try {\n"
            + "    const options = {\n"
            + "      indent_size: 2,\n"
            + "      indent_char: ' ',\n"
            + "      indent_with_tabs: false,\n"
            + "      end_with_newline: true,\n"
            + "      space_around_selector_separator: true\n"
            + "    };\n"
            + "    return globalThis.css_beautify(cssCode, options);\n"
            + "  } catch(e) {\n"
            + "    console.error('Beautify error:', e);\n"
            + "    return cssCode;\n"
            + "  }\n"
            + "}";

    nodeRuntime.getExecutor(setupFn).executeVoid();
    beautifyFunction = nodeRuntime.getGlobalObject().get("beautifyCSS");

    if (beautifyFunction != null) {
      Log.d(TAG, "CSS beautify function setup successfully");
    }
  }

  public String beautify(String cssCode) {
    try {
      if (beautifyFunction == null || cssCode == null) {
        return cssCode;
      }

      Object result = beautifyFunction.call(null, cssCode);
      return result != null ? result.toString() : cssCode;

    } catch (Exception e) {
      Log.e(TAG, "BEAUTIFY ERROR", e);
      return cssCode;
    }
  }

  public boolean isReady() {
    return beautifyFunction != null;
  }

  public void destroy() {
    try {
      if (beautifyFunction != null) {
        beautifyFunction.close();
      }
      if (nodeRuntime != null) {
        nodeRuntime.close();
      }
    } catch (Exception e) {
      Log.e(TAG, "DESTROY ERROR", e);
    }
  }
}
