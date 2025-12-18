package ir.ninjacoder.plloader.csslsp;

import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ScrollView;
import com.caoccao.javet.interop.V8Runtime;
import android.os.Bundle;
import android.app.Dialog;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.widget.LinearLayout;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.inputmethod.EditorInfo;
import android.view.KeyEvent;
import android.view.Gravity;
import android.widget.Button;
import com.caoccao.javet.interop.V8Host;
import com.caoccao.javet.values.V8Value;
import android.view.View;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class NodeTerminal extends BottomSheetDialogFragment {

  private EditText inputEditText;
  private TextView outputTextView;
  private ScrollView scrollView;
  private V8Runtime v8Runtime;
  private ScaleGestureDetector scaleGestureDetector;
  private float scaleFactor = 1.0f;

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {

    BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
    LinearLayout mainLayout = new LinearLayout(requireContext());
    mainLayout.setOrientation(LinearLayout.VERTICAL);
    mainLayout.setBackgroundColor(Color.BLACK);
    mainLayout.setPadding(0, 0, 0, 0);
    scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
    LinearLayout background = new LinearLayout(requireContext());
    background.setOrientation(LinearLayout.VERTICAL);
    background.setBackgroundColor(Color.BLACK);
    background.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
    background.setLayoutParams(
        new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

    // ============================
    // ScrollView
    // ============================
    scrollView = new ScrollView(requireContext());
    scrollView.setLayoutParams(
        new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

    // ============================
    // terminal container
    // ============================
    LinearLayout terminalBackground = new LinearLayout(requireContext());
    terminalBackground.setOrientation(LinearLayout.VERTICAL);
    terminalBackground.setPadding(8, 8, 8, 8);
    terminalBackground.setLayoutParams(
        new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

    // ============================
    // Welcome Text (like XML)
    // ============================
    outputTextView = new TextView(requireContext());
    outputTextView.setText("Welcome to NodeJs\n");
    outputTextView.setTextColor(Color.WHITE);
    outputTextView.setTextSize(11);
    outputTextView.setTypeface(Typeface.MONOSPACE);
    outputTextView.setGravity(Gravity.START);

    // ============================
    // Input line layout (~ $)
    // ============================
    LinearLayout inputLayout = new LinearLayout(requireContext());
    inputLayout.setOrientation(LinearLayout.HORIZONTAL);
    inputLayout.setGravity(Gravity.START | Gravity.TOP);

    TextView prefix = new TextView(requireContext());
    prefix.setText("~ $ ");
    prefix.setTextColor(Color.WHITE);
    prefix.setTextSize(11);
    prefix.setTypeface(Typeface.MONOSPACE);

    inputEditText = new EditText(requireContext());
    inputEditText.setLayoutParams(
        new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    inputEditText.setBackgroundColor(Color.TRANSPARENT);
    inputEditText.setTextColor(Color.WHITE);
    inputEditText.setHintTextColor(Color.GRAY);
    inputEditText.setTextSize(11);
    inputEditText.setTypeface(Typeface.MONOSPACE);
    inputEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
    inputEditText.setSingleLine(true);
    inputEditText.setHint("Type JavaScript command...");

    mainLayout.setOnTouchListener(
        new View.OnTouchListener() {

          @Override
          public boolean onTouch(View arg0, MotionEvent event) {
            if (scaleGestureDetector != null) {
              return scaleGestureDetector.onTouchEvent(event);
            }
            return false;
          }
        });
    // روش اول: استفاده از OnEditorActionListener برای IME_ACTION_DONE
    inputEditText.setOnEditorActionListener(
        new TextView.OnEditorActionListener() {
          @Override
          public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
              executeCommand();
              return true;
            }
            return false;
          }
        });

    // روش دوم: استفاده از OnKeyListener برای KEYCODE_ENTER
    inputEditText.setOnKeyListener(
        new View.OnKeyListener() {
          @Override
          public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
              executeCommand();
              return true;
            }
            return false;
          }
        });
    inputLayout.addView(prefix);
    inputLayout.addView(inputEditText);

    terminalBackground.addView(outputTextView);
    terminalBackground.addView(inputLayout);

    scrollView.addView(terminalBackground);
    background.addView(scrollView);
    mainLayout.addView(background);
    dialog.setContentView(mainLayout);
    dialog.setOnShowListener(
        dialogInterface -> {
          inputEditText.requestFocus();
        });

    initializeV8();
    return dialog;
  }

  private void initializeV8() {
    try {
      v8Runtime = V8Host.getNodeInstance().createV8Runtime();
      appendOutput("V8 runtime initialized...\n");
    } catch (Exception e) {
      appendOutput("Error initializing V8: " + e.getMessage() + "\n");
    }
  }

  private void executeCommand() {
    String cmd = inputEditText.getText().toString().trim();

    if (cmd.isEmpty()) {
      appendOutput("~ $ \n");
      inputEditText.setText("");
      return;
    }

    appendOutput("~ $ " + cmd + "\n");

    if (cmd.equals("exit")) {
      dismiss();
      return;
    }

    if (cmd.equals("clear")) {
      outputTextView.setText("");
      inputEditText.setText("");
      return;
    }

    try {
      V8Value result = v8Runtime.getExecutor(cmd).execute();
      if (result != null) {
        appendOutput(result.toString() + "\n");
        result.close();
      } else {
        appendOutput("undefined\n");
      }
    } catch (Throwable t) {
      appendOutput("Error: " + t.getMessage() + "\n");
    }

    inputEditText.setText("");

    // اسکرول به پایین
    scrollView.postDelayed(
        () -> {
          scrollView.fullScroll(View.FOCUS_DOWN);
        },
        100);
  }

  private void appendOutput(String text) {
    if (outputTextView != null) {
      outputTextView.append(text);
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    try {
      if (v8Runtime != null) {
        v8Runtime.close();
      }
    } catch (Exception e) {
      // ignore
    }
  }

  class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
    @Override
    public boolean onScale(ScaleGestureDetector detector) {
      float currentSize =
          outputTextView.getTextSize() / getResources().getDisplayMetrics().scaledDensity;
      float newSize = currentSize * detector.getScaleFactor();
      newSize = Math.max(5, Math.min(newSize, 45));
      outputTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, newSize);
      inputEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, newSize);

      return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
      float finalSize =
          outputTextView.getTextSize() / getResources().getDisplayMetrics().scaledDensity;
      super.onScaleEnd(detector);
    }
  }
}
