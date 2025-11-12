package ir.ninjacoder.plloader;

import android.graphics.drawable.GradientDrawable;
import android.view.View;
import com.google.android.material.slider.Slider;
import io.github.rosemoe.sora.event.ContentChangeEvent;
import io.github.rosemoe.sora.widget.CodeEditor;
import io.github.rosemoe.sora.widget.EditorPopupWindow;
import android.widget.TextView;
import io.github.rosemoe.sora.widget.EditorColorScheme;

public class EditorPopUp {
  public static void showPowerMenuAtCursor(CodeEditor editor, String message) {
    try {

      EditorPopupWindow popupWindow =
          new EditorPopupWindow(
              editor,
              EditorPopupWindow.FEATURE_SCROLL_AS_CONTENT
                  | EditorPopupWindow.FEATURE_SHOW_OUTSIDE_VIEW_ALLOWED);
      TextView textView = new TextView(editor.getContext());
      textView.setSingleLine(true);
      textView.setPadding(32, 16, 32, 16);
      GradientDrawable f = new GradientDrawable();
      f.setColor(editor.getColorScheme().getColor(EditorColorScheme.AUTO_COMP_PANEL_BG));
      f.setStroke(1, editor.getColorScheme().getColor(EditorColorScheme.AUTO_COMP_PANEL_CORNER));
      f.setCornerRadius(25);
      textView.setBackground(f);
      textView.setTextSize(14);
      textView.setTextColor(editor.getColorScheme().getColor(EditorColorScheme.TEXT_NORMAL));
      textView.setText(message);
      popupWindow.setContentView(textView);
      popupWindow.setOutsideTouchable(true);

      textView.measure(
          View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
          View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

      int width = textView.getMeasuredWidth();
      int height = textView.getMeasuredHeight();
      popupWindow.setSize(width, height);
      var selection = editor.getCursor().left();
      float charX = editor.getCharOffsetX(selection.getLine(), selection.getColumn());
      float charY =
          editor.getCharOffsetY(selection.getLine(), selection.getColumn()) - editor.getRowHeight();

      var locationBuffer = new int[2];
      editor.getLocationInWindow(locationBuffer);
      float restAbove = charY + locationBuffer[1];
      float restBottom = editor.getHeight() - charY - editor.getRowHeight();

      boolean completionShowing = editor.getAutoCompleteWindow().isShowing();
      float windowY;
      if (restAbove > restBottom || completionShowing) {
        windowY = charY - popupWindow.getHeight();
      } else {
        windowY = charY + editor.getRowHeight() * 1.5f;
      }

      if (completionShowing && windowY < 0) {
        return;
      }

      float windowX = Math.max(charX - popupWindow.getWidth() / 2f, 0f);
      popupWindow.setLocationAbsolutely((int) windowX, (int) windowY);
      popupWindow.show();
      editor.subscribeEvent(
          ContentChangeEvent.class,
          (event, sub) -> {
            if (event.getAction() == ContentChangeEvent.ACTION_DELETE
                || event.getAction() == ContentChangeEvent.ACTION_INSERT
                || event.getAction() == ContentChangeEvent.ACTION_SET_NEW_TEXT) {
              popupWindow.dismiss();
            } else popupWindow.show();
          });
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void showCustomViewAtCursor(CodeEditor editor, View customView) {
    try {
      EditorPopupWindow popupWindow =
          new EditorPopupWindow(
              editor,
              EditorPopupWindow.FEATURE_SCROLL_AS_CONTENT
                  | EditorPopupWindow.FEATURE_SHOW_OUTSIDE_VIEW_ALLOWED);

      popupWindow.setContentView(customView);
      popupWindow.setOutsideTouchable(true);

      customView.measure(
          View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
          View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

      int width = customView.getMeasuredWidth();
      int height = customView.getMeasuredHeight();
      popupWindow.setSize(width, height);

      var selection = editor.getCursor().left();
      float charX = editor.getCharOffsetX(selection.getLine(), selection.getColumn());
      float charY =
          editor.getCharOffsetY(selection.getLine(), selection.getColumn()) - editor.getRowHeight();

      var locationBuffer = new int[2];
      editor.getLocationInWindow(locationBuffer);
      float restAbove = charY + locationBuffer[1];
      float restBottom = editor.getHeight() - charY - editor.getRowHeight();

      boolean completionShowing = editor.getAutoCompleteWindow().isShowing();
      float windowY;
      if (restAbove > restBottom || completionShowing) {
        windowY = charY - popupWindow.getHeight();
      } else {
        windowY = charY + editor.getRowHeight() * 1.5f;
      }

      if (completionShowing && windowY < 0) {
        popupWindow.dismiss();
      }

      GradientDrawable draw = new GradientDrawable();
      draw.setColor(editor.getColorScheme().getColor(EditorColorScheme.AUTO_COMP_PANEL_BG));
      draw.setStroke(2, editor.getColorScheme().getColor(EditorColorScheme.AUTO_COMP_PANEL_CORNER));
      draw.setCornerRadius(20);
      customView.setBackground(draw);
      float windowX = Math.max(charX - popupWindow.getWidth() / 2f, 0f);
      popupWindow.setLocationAbsolutely((int) windowX, (int) windowY);
      popupWindow.show();
      if (editor.getCursor().isSelected()) {
        popupWindow.dismiss();
      }
      editor.subscribeEvent(
          ContentChangeEvent.class,
          (event, sub) -> {
            if (event.getAction() == ContentChangeEvent.ACTION_DELETE
                || event.getAction() == ContentChangeEvent.ACTION_INSERT
                || event.getAction() == ContentChangeEvent.ACTION_SET_NEW_TEXT) {
              popupWindow.dismiss();
            } else popupWindow.show();
          });

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
