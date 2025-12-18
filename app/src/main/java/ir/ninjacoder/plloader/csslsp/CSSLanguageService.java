package ir.ninjacoder.plloader.csslsp;

import android.content.Context;
import android.util.Log;

import com.caoccao.javet.interop.NodeRuntime;
import com.caoccao.javet.interop.V8Host;
import com.caoccao.javet.values.reference.V8ValueFunction;
import com.caoccao.javet.values.reference.V8ValueArray;
import com.caoccao.javet.values.reference.V8ValueObject;

import io.github.rosemoe.sora.data.CompletionItem;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CSSLanguageService {

  private static final String TAG = "CSSLanguageService";
  private static final String BUNDLE_URL =
      "https://cdn.jsdelivr.net/npm/tern@0.24.3/lib/tern.js"; // Tern.js
  private static final String LOCAL_PATH =
      "/storage/emulated/0/GhostWebIDE/plugins/csslsp/data/tern.js";

  private NodeRuntime nodeRuntime;
  private V8ValueFunction doCompleteFn;
  private boolean isReady = false;

  public CSSLanguageService(Context ctx) {
    init();
  }

  private void init() {
    new Thread(
            () -> {
              try {
                nodeRuntime = V8Host.getNodeInstance().createV8Runtime();
                downloadAndSetup();
                Log.d(TAG, "CSS Language Service initialized");
              } catch (Throwable e) {
                Log.e(TAG, "INIT ERROR", e);
              }
            })
        .start();
  }

  private void downloadAndSetup() {
    try {
      File bundleFile = new File(LOCAL_PATH);

      if (!bundleFile.exists()) {
        downloadBundle(bundleFile);
        Thread.sleep(1000);
      }

      if (bundleFile.exists()) {
        loadBundle(bundleFile);
        setupService();
        isReady = true;
        Log.d(TAG, "✅ CSS Language Service ready");
      }

    } catch (Exception e) {
      Log.e(TAG, "SETUP ERROR", e);
    }
  }

  private void downloadBundle(File outputFile) {
    try {
      Log.d(TAG, "📥 Downloading Tern.js bundle...");

      URL url = new URL(BUNDLE_URL);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");
      connection.setConnectTimeout(30000);
      connection.setReadTimeout(30000);

      File dir = outputFile.getParentFile();
      if (!dir.exists()) dir.mkdirs();

      try (InputStream input = connection.getInputStream();
          FileOutputStream output = new FileOutputStream(outputFile)) {

        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
          output.write(buffer, 0, bytesRead);
        }
      }

      Log.d(TAG, "✅ Download completed: " + outputFile.length() + " bytes");

    } catch (Exception e) {
      Log.e(TAG, "❌ DOWNLOAD ERROR", e);
    }
  }

  private void loadBundle(File bundleFile) throws Exception {
    String src = readFileToString(bundleFile);
    nodeRuntime.getExecutor(src).executeVoid();
    Log.d(TAG, "✅ Tern.js bundle loaded successfully");
  }

  private void setupService() throws Exception {
    String script =
        "try {\n"
            + "    const server = new tern.Server({ defs: [] });\n"
            + "    function doCSSComplete(cssText, offset) {\n"
            + "        try {\n"
            + "            if (!cssText || offset < 0) return [];\n"
            + "            const doc = { text: cssText, name: 'file.css' };\n"
            + "            const completions = server.complete(doc, { line: 0, ch: offset });\n"
            + "            return completions ? completions.completions : [];\n"
            + "        } catch(e) { console.error('❌ CSS Completion error:', e); return []; }\n"
            + "    }\n"
            + "    globalThis.doCSSComplete = doCSSComplete;\n"
            + "    console.log('✅ CSS Language Service setup completed');\n"
            + "} catch(e) {\n"
            + "    console.error('❌ Setup error:', e);\n"
            + "}";

    nodeRuntime.getExecutor(script).executeVoid();
    doCompleteFn = nodeRuntime.getGlobalObject().get("doCSSComplete");

    if (doCompleteFn != null) {
      Log.d(TAG, "✅ CSS Completion function ready");
    } else {
      Log.e(TAG, "❌ Failed to create completion function");
    }
  }

  public List<CompletionItem> doComplete(String cssCode, int offset) {
    List<CompletionItem> completions = new ArrayList<>();
    try {
      if (!isReady || doCompleteFn == null || cssCode == null) return completions;

      Object result = doCompleteFn.call(null, cssCode, offset);

      if (result instanceof V8ValueArray) {
        V8ValueArray items = (V8ValueArray) result;
        for (int i = 0; i < items.getLength(); i++) {
          Object obj = items.get(i);
          if (obj instanceof V8ValueObject) {
            V8ValueObject item = (V8ValueObject) obj;
            String label = item.has("name") ? item.getString("name") : "item";
            completions.add(new CompletionItem(label, label, "CSS"));
          }
        }
      }

    } catch (Exception e) {
      Log.e(TAG, "❌ COMPLETION ERROR", e);
    }
    return completions;
  }

  public boolean isReady() {
    return isReady && doCompleteFn != null;
  }

  private String readFileToString(File file) throws Exception {
    try (InputStream in = new FileInputStream(file)) {
      byte[] buf = new byte[(int) file.length()];
      int read = in.read(buf);
      return new String(buf, 0, read, StandardCharsets.UTF_8);
    }
  }

  public void destroy() {
    try {
      if (doCompleteFn != null) doCompleteFn.close();
      if (nodeRuntime != null) nodeRuntime.close();
    } catch (Exception e) {
      Log.e(TAG, "❌ DESTROY ERROR", e);
    }
  }
}
