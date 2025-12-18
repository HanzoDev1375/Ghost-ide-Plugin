package ir.ninjacoder.plloader.csslsp;

import android.content.Context;
import android.util.Log;
import com.blankj.utilcode.util.ThreadUtils;
import com.caoccao.javet.interop.NodeRuntime;
import com.caoccao.javet.interop.V8Host;
import com.caoccao.javet.values.reference.V8ValueArray;
import com.caoccao.javet.values.reference.V8ValueFunction;
import com.caoccao.javet.values.reference.V8ValueObject;
import io.github.rosemoe.sora.data.CompletionItem;
import ir.ninjacoder.ghostide.core.utils.DataUtil;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class JavaScriptAutoCompleter {

  private static final String TAG = "EmmetJavet";
  private static final String LOCAL_EMMET_PATH =
      "/storage/emulated/0/apk/node_modules/emmet/dist/emmet.js";
  private NodeRuntime nodeRuntime;
  private V8ValueFunction extractFunction;
  private V8ValueFunction expandFunction;

  public JavaScriptAutoCompleter(Context ctx) {
    initNode();
    ThreadUtils.runOnUiThread(
        () -> {
          DataUtil.showMessage(ctx, "Emmet Loaded");
        });
  }

  private void initNode() {
    try {
      nodeRuntime = V8Host.getNodeInstance().createV8Runtime();
      setupEmmet();
    } catch (Exception e) {
      Log.e(TAG, "خطا در مقداردهی اولیه نود: " + e.getMessage());
    }
  }

  private void setupEmmet() throws Exception {
    File emmetFile = new File(LOCAL_EMMET_PATH);
    if (!emmetFile.exists()) {
      Log.e(TAG, "فایل Emmet یافت نشد در: " + LOCAL_EMMET_PATH);
      return;
    }

    // خواندن کد Emmet
    FileInputStream fis = new FileInputStream(emmetFile);
    byte[] data = new byte[(int) emmetFile.length()];
    fis.read(data);
    fis.close();
    String emmetSource = new String(data, StandardCharsets.UTF_8);

    // تزریق Emmet به محیط
    String script =
        "var exports = {}; var module = { exports: exports };\n"
            + emmetSource
            + "\n"
            + "globalThis.emmet = (module.exports.expand) ? module.exports : exports;";

    nodeRuntime.getExecutor(script).executeVoid();

    // ایجاد تابع‌های پل
    String bridgeScript =
        "globalThis.emmetComplete = function(line, cursorPos) {\n"
            + "  try {\n"
            + "    // ۱. استخراج مخفف Emmet از موقعیت کرسر\n"
            + "    const extracted = globalThis.emmet.extract(line, cursorPos, {\n"
            + "      type: 'markup', // یا 'stylesheet' برای CSS\n"
            + "      lookAhead: true,\n"
            + "      prefix: ''\n"
            + "    });\n"
            + "\n"
            + "    if (!extracted || !extracted.abbreviation) {\n"
            + "      return [];\n"
            + "    }\n"
            + "\n"
            + "    const abbr = extracted.abbreviation;\n"
            + "    const start = extracted.start;\n"
            + "    const end = extracted.end;\n"
            + "\n"
            + "    // ۲. گسترش مخفف\n"
            + "    const options = {\n"
            + "      type: 'markup',\n"
            + "      options: { \n"
            + "        'output.field': (index, placeholder) => placeholder || '',\n"
            + "        'output.format': false // برای تکمیل کد، فرمت‌بندی لازم نیست\n"
            + "      }\n"
            + "    };\n"
            + "\n"
            + "    const expanded = globalThis.emmet.expand(abbr, options);\n"
            + "\n"
            + "    // ۳. بازگشت اطلاعات برای تکمیل کد\n"
            + "    return [{\n"
            + "      abbreviation: abbr,\n"
            + "      expanded: expanded,\n"
            + "      start: start,\n"
            + "      end: end\n"
            + "    }];\n"
            + "  } catch (e) {\n"
            + "    console.error('Emmet error:', e);\n"
            + "    return [];\n"
            + "  }\n"
            + "};\n"
            + "\n"
            + "globalThis.emmetExpand = function(abbreviation) {\n"
            + "  try {\n"
            + "    const options = {\n"
            + "      type: 'markup',\n"
            + "      options: { \n"
            + "        'output.field': (index, placeholder) => placeholder || '',\n"
            + "        'output.format': true\n"
            + "      }\n"
            + "    };\n"
            + "    return globalThis.emmet.expand(abbreviation, options);\n"
            + "  } catch (e) {\n"
            + "    console.error('Emmet expand error:', e);\n"
            + "    return '';\n"
            + "  }\n"
            + "};";

    nodeRuntime.getExecutor(bridgeScript).executeVoid();

    // گرفتن رفرنس توابع
    extractFunction = nodeRuntime.getGlobalObject().get("emmetComplete");
    expandFunction = nodeRuntime.getGlobalObject().get("emmetExpand");
  }

  /** برای تکمیل کد (Auto-completion) - زمانی که کاربر در حال تایپ است */
  public List<CompletionItem> complete(String lineText, int cursorPosition) {
    List<CompletionItem> items = new ArrayList<>();

    if (extractFunction == null || lineText == null) {
      return items;
    }

    try {
      // فراخوانی تابع استخراج
      Object result = extractFunction.call(null, lineText, cursorPosition);

      if (result instanceof V8ValueArray) {
        V8ValueArray v8Array = (V8ValueArray) result;

        for (int i = 0; i < v8Array.getLength(); i++) {
          Object item = v8Array.get(i);

          if (item instanceof V8ValueObject) {
            V8ValueObject obj = (V8ValueObject) item;

            String abbreviation = obj.getString("abbreviation");
            String expanded = obj.getString("expanded");
            int start = obj.getInteger("start");
            int end = obj.getInteger("end");

            if (abbreviation != null && expanded != null) {
              // ساخت آیتم تکمیل کد
              CompletionItem completionItem =
                  new CompletionItem(
                      abbreviation + " → Emmet", expanded, "تبدیل Emmet: " + abbreviation);

              // تنظیم offset برای جایگزینی دقیق
              // completionItem.setOffset(start); // اگر API Sora Editor پشتیبانی کند

              items.add(completionItem);
            }

            obj.close();
          }
        }
        v8Array.close();
      }
    } catch (Exception e) {
      Log.e(TAG, "خطا در تکمیل کد Emmet: " + e.getMessage());
    }

    return items;
  }

  /** برای گسترش مستقیم یک مخفف (مثلاً با کلید میانبر) */
  public String expandAbbreviation(String abbreviation) {
    if (expandFunction == null || abbreviation == null || abbreviation.trim().isEmpty()) {
      return "";
    }

    try {
      Object result = expandFunction.call(null, abbreviation);
      return result.toString();
    } catch (Exception e) {
      Log.e(TAG, "خطا در گسترش مخفف: " + e.getMessage());
      return "";
    }
  }

  /** بررسی می‌کند آیا در موقعیت فعلی مخفف Emmet وجود دارد */
  public boolean hasEmmetAbbreviation(String lineText, int cursorPosition) {
    if (extractFunction == null) {
      return false;
    }

    try {
      Object result = extractFunction.call(null, lineText, cursorPosition);

      if (result instanceof V8ValueArray) {
        V8ValueArray v8Array = (V8ValueArray) result;
        boolean hasResult = v8Array.getLength() > 0;
        v8Array.close();
        return hasResult;
      }
    } catch (Exception e) {
      Log.e(TAG, "خطا در بررسی Emmet: " + e.getMessage());
    }

    return false;
  }

  public void destroy() {
    try {
      if (extractFunction != null && !extractFunction.isClosed()) {
        extractFunction.close();
      }
      if (expandFunction != null && !expandFunction.isClosed()) {
        expandFunction.close();
      }
      if (nodeRuntime != null && !nodeRuntime.isClosed()) {
        nodeRuntime.close();
      }
    } catch (Exception e) {
      Log.e(TAG, "خطا در بستن منابع: " + e.getMessage());
    }
  }
}
