package ir.ninjacoder.plloader.comment;

import android.util.Log;
import android.widget.Toast;
import io.github.rosemoe.sora.interfaces.CodeAnalyzer;
import io.github.rosemoe.sora.langs.javascript.JavaScriptLanguage;
import io.github.rosemoe.sora.widget.CodeEditor;
import ir.ninjacoder.ghostide.core.activities.CodeEditorActivity;
import ir.ninjacoder.ghostide.core.activities.FileManagerActivity;
import ir.ninjacoder.ghostide.core.pl.PluginManagerCompat;

public class JsBetterCommentPlugin implements PluginManagerCompat {

  private CodeEditor currentEditor;
  private CodeEditorActivity currentActivity;

  @Override
  public void getCodeEditorAc(CodeEditorActivity arg0) {
    currentActivity = arg0;
    Log.d("JsPlugin", "✅ Activity received: " + (arg0 != null));
  }

  @Override
  public void getEditor(CodeEditor editor) {
    Log.d("JsPlugin", "🎯 SIMPLE VERSION - getEditor called");
    this.currentEditor = editor;

    if (editor == null) return;

    // فقط روی context ادیتور حساب کن
    editor.postDelayed(
        () -> {
          try {
            if (editor.getContext() instanceof CodeEditorActivity) {
              CodeEditorActivity activity = (CodeEditorActivity) editor.getContext();
              String fileType = activity.getcurrentFileType();

              if (fileType != null && fileType.endsWith(".js")) {
                Toast.makeText(activity, "JS Plugin Activated!", Toast.LENGTH_SHORT).show();
                applyCustomLanguage();
              }
            }
          } catch (Exception e) {
            Log.e("JsPlugin", "Error: " + e.getMessage());
          }
        },
        1000);
  }

  private void applyCustomLanguage() {
    Log.d("JsPlugin", "🌈 applyCustomLanguage called");

    if (currentEditor == null) {
      Log.e("JsPlugin", "❌ Editor is null in applyCustomLanguage");
      return;
    }

    try {
      // ایجاد زبان کاستوم
      JavaScriptLanguage customLang =
          new JavaScriptLanguage() {
            @Override
            public CodeAnalyzer getAnalyzer() {
              Log.d("JsPlugin", "🔧 Returning custom JavaScript analyzer");
              return new JavaScriptCodeAnalyzer(); // آنالایزر کاستوم تو
            }
          };

      // ست کردن زبان
      currentEditor.setEditorLanguage(customLang);
      Log.d("JsPlugin", "✅ Custom language set");

      // فورس ری‌آنالایز بعد از تأخیر
      currentEditor.postDelayed(
          () -> {
            if (currentEditor != null) {
              Log.d("JsPlugin", "🔄 Analyzing code...");
              currentEditor.analyze(true);
              currentEditor.invalidate();
              Log.d("JsPlugin", "✅ Analysis complete");
            }
          },
          1000);

    } catch (Exception e) {
      Log.e("JsPlugin", "💥 Error applying custom language: " + e.getMessage());
      e.printStackTrace();
    }
  }

  @Override
  public void getFileManagerAc(FileManagerActivity arg0) {}

  @Override
  public boolean hasuseing() {
    return true;
  }

  @Override
  public String setName() {
    return "Smart Comment Highlighter";
  }

  @Override
  public String langModel() {
    return ".js";
  }
}
