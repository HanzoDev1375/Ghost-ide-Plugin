package ir.ninjacoder.plloader.theme;

import android.app.Activity;
import android.widget.Toast;
import ir.ninjacoder.ghostide.core.activities.FileManagerActivity;
import io.github.rosemoe.sora.widget.CodeEditor;
import ir.ninjacoder.ghostide.core.activities.CodeEditorActivity;
import ir.ninjacoder.ghostide.core.pl.PluginManagerCompat;
import android.content.SharedPreferences;
import android.content.Context;

public class ThemeLoaderCompat implements PluginManagerCompat {

  @Override
  public void getCodeEditorAc(CodeEditorActivity arg0) {}

  @Override
  public void getEditor(CodeEditor arg0) {}

  @Override
  public void getFileManagerAc(FileManagerActivity arg0) {
    loadSavedTheme(arg0);
  }

  private void loadSavedTheme(FileManagerActivity context) {
    SharedPreferences prefs = context.getSharedPreferences("name", Context.MODE_PRIVATE);
    String savedTheme = prefs.getString("selected_theme_name", "Swamp Green");

    if (!savedTheme.isEmpty()) {
      String filePath = "/storage/emulated/0/GhostWebIDE/plugins/mytheme/" + savedTheme + ".json";
      try {
        JsonTheme.fromFile(context, filePath);
        JsonThemeFactory.applyThemeToActivity(context);
      } catch (Exception e) {
        Toast.makeText(context, e.getLocalizedMessage(), 2).show();
        // Log error if needed
      }
    }
  }

  @Override
  public boolean hasuseing() {
    return true;
  }

  @Override
  public String setName() {
    return "ThemeLoader";
  }

  @Override
  public String langModel() {
    return null;
  }
}
