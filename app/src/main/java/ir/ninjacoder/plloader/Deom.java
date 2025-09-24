package ir.ninjacoder.plloader;

import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.color.MaterialColors;
import io.github.rosemoe.sora.langs.html.HTMLLanguage;
import io.github.rosemoe.sora.widget.CodeEditor;
import io.github.rosemoe.sora.widget.EditorColorScheme;
import ir.ninjacoder.ghostide.utils.ObjectUtils;

public class Deom {

  public void getEditor(CodeEditor editor) {
    editor
        .getColorScheme()
        .setColor(EditorColorScheme.WHOLE_BACKGROUND, Color.parseColor("#fff702"));
    editor.getColorScheme().setColor(EditorColorScheme.TEXT_NORMAL, Color.CYAN);
    var sheet = new BottomSheetDialog(editor.getContext());
    TextView t = new TextView(editor.getContext());
    t.setTextColor(MaterialColors.getColor(t, ObjectUtils.TvColor, 0));
    t.setText("Hello to dex run");
    t.setTextSize(20f);
    t.setGravity(Gravity.CENTER);
    t.setLayoutParams(
        new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    sheet.setContentView(t);
    if (sheet != null) {
      sheet.show();
    }
    if (editor.getTextAsString().equals("<html")) {
      Toast.makeText(editor.getContext(), "This html lang", 2).show();
    }
  }

  public String setName() {
    return "Test";
  }

  public boolean hasuseing() {
    return true;
  }
}
