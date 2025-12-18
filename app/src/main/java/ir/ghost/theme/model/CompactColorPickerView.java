package ir.ghost.theme.model;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;
import com.google.android.material.slider.Slider;

public class CompactColorPickerView extends LinearLayout {

  private HuePicker huePicker;
  private SaturationValuePicker saturationValuePicker;
  private Slider alphaSlider;
  private TextView colorPreview;
  private Button applyButton;
  private OnColorChangedListener listener;

  private float hue = 0f;
  private float saturation = 1f;
  private float value = 1f;
  private int alpha = 255;

  private Bitmap hueBitmapCache;
  private Bitmap satValBitmapCache;
  private boolean needsHueRedraw = true;
  private boolean needsSatValRedraw = true;

  public interface OnColorChangedListener {
    void onColorChanged(int color, String hexColor);
  }

  public CompactColorPickerView(Context context) {
    super(context);
    init();
  }

  public CompactColorPickerView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  private void init() {
    setOrientation(VERTICAL);
    setPadding(16, 16, 16, 16);
    setBackgroundColor(0xFFFAFAFA);

    colorPreview = new TextView(getContext());
    colorPreview.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 80));
    colorPreview.setBackgroundColor(getCurrentColor());
    colorPreview.setText(getHexColor());
    colorPreview.setTextColor(Color.WHITE);
    colorPreview.setGravity(Gravity.CENTER);
    colorPreview.setTextSize(12);
    addView(colorPreview);

    huePicker = new HuePicker(getContext());
    huePicker.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 60));
    addView(huePicker);

    saturationValuePicker = new SaturationValuePicker(getContext());
    saturationValuePicker.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 200));
    addView(saturationValuePicker);

    alphaSlider = new Slider(getContext());
    alphaSlider.setLayoutParams(
        new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    alphaSlider.setValueFrom(0);
    alphaSlider.setValueTo(255);
    alphaSlider.setValue(255);
    alphaSlider.addOnChangeListener(
        new Slider.OnChangeListener() {
          @Override
          public void onValueChange(Slider slider, float progress, boolean fromUser) {
            alpha = (int) progress;
            updateColor();
          }
        });
    addView(alphaSlider);

    applyButton = new Button(getContext());
    applyButton.setLayoutParams(
        new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    applyButton.setText("اعمال رنگ");
    applyButton.setOnClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            if (listener != null) {
              listener.onColorChanged(getCurrentColor(), getHexColor());
            }
          }
        });
    addView(applyButton);
  }

  private void updateColor() {
    int color = getCurrentColor();
    colorPreview.setBackgroundColor(color);
    colorPreview.setText(getHexColor());

    saturationValuePicker.needsRedraw = true;
    saturationValuePicker.invalidate();
  }

  public int getCurrentColor() {

    int rgb = HSVToRGB(hue, saturation, value);
    return Color.argb(alpha, Color.red(rgb), Color.green(rgb), Color.blue(rgb));
  }

  private int HSVToRGB(float h, float s, float v) {
    h %= 360f;
    if (h < 0) h += 360f;

    s = Math.max(0, Math.min(1, s));
    v = Math.max(0, Math.min(1, v));

    float c = v * s;
    float x = c * (1 - Math.abs((h / 60f) % 2 - 1));
    float m = v - c;

    float r, g, b;
    if (h < 60) {
      r = c;
      g = x;
      b = 0;
    } else if (h < 120) {
      r = x;
      g = c;
      b = 0;
    } else if (h < 180) {
      r = 0;
      g = c;
      b = x;
    } else if (h < 240) {
      r = 0;
      g = x;
      b = c;
    } else if (h < 300) {
      r = x;
      g = 0;
      b = c;
    } else {
      r = c;
      g = 0;
      b = x;
    }

    return Color.rgb((int) ((r + m) * 255), (int) ((g + m) * 255), (int) ((b + m) * 255));
  }

  public String getHexColor() {
    int color = getCurrentColor();
    return String.format("#%08X", color);
  }

  public void setOnColorChangedListener(OnColorChangedListener listener) {
    this.listener = listener;
  }

  public void setInitialColor(int color) {

    float[] hsv = RGBToHSV(Color.red(color), Color.green(color), Color.blue(color));

    this.hue = hsv[0];
    this.saturation = hsv[1];
    this.value = hsv[2];
    this.alpha = Color.alpha(color);

    this.alphaSlider.setValue(this.alpha);

    if (this.huePicker != null) {
      this.huePicker.selectorPosition = this.hue / 360f;
      this.huePicker.needsRedraw = true;
    }
    if (this.saturationValuePicker != null) {
      this.saturationValuePicker.satPos = this.saturation;
      this.saturationValuePicker.valPos = this.value;
      this.saturationValuePicker.needsRedraw = true;
    }

    updateColor();
  }

  private float[] RGBToHSV(int r, int g, int b) {
    float[] hsv = new float[3];
    float rf = r / 255f;
    float gf = g / 255f;
    float bf = b / 255f;

    float max = Math.max(rf, Math.max(gf, bf));
    float min = Math.min(rf, Math.min(gf, bf));
    float delta = max - min;

    hsv[2] = max;

    if (max > 0) {
      hsv[1] = delta / max;
    } else {
      hsv[1] = 0;
    }

    if (delta > 0) {
      if (max == rf) {
        hsv[0] = (gf - bf) / delta;
      } else if (max == gf) {
        hsv[0] = 2f + (bf - rf) / delta;
      } else {
        hsv[0] = 4f + (rf - gf) / delta;
      }
      hsv[0] *= 60f;
      if (hsv[0] < 0) hsv[0] += 360f;
    } else {
      hsv[0] = 0;
    }

    return hsv;
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();

    if (hueBitmapCache != null) {
      hueBitmapCache.recycle();
      hueBitmapCache = null;
    }
    if (satValBitmapCache != null) {
      satValBitmapCache.recycle();
      satValBitmapCache = null;
    }
  }

  private class HuePicker extends View {
    private Paint paint;
    private float selectorPosition = 0f;
    private boolean needsRedraw = true;

    public HuePicker(Context context) {
      super(context);
      init();
    }

    private void init() {
      paint = new Paint();
      paint.setAntiAlias(true);
      paint.setDither(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
      int width = getWidth();
      int height = getHeight();

      if (width <= 0 || height <= 0) return;

      if (needsRedraw
          || hueBitmapCache == null
          || hueBitmapCache.getWidth() != width
          || hueBitmapCache.getHeight() != height) {

        if (hueBitmapCache != null) {
          hueBitmapCache.recycle();
        }

        hueBitmapCache = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas tempCanvas = new Canvas(hueBitmapCache);

        for (int x = 0; x < width; x++) {
          float hue = (float) x / width * 360f;
          paint.setColor(HSVToRGB(hue, 1f, 1f));
          tempCanvas.drawRect(x, 0, x + 1, height, paint);
        }
        needsRedraw = false;
      }

      canvas.drawBitmap(hueBitmapCache, 0, 0, paint);

      paint.setColor(Color.WHITE);
      paint.setStrokeWidth(3);
      float pos = selectorPosition * width;
      canvas.drawLine(pos, 0, pos, height, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
      if (event.getAction() == MotionEvent.ACTION_DOWN
          || event.getAction() == MotionEvent.ACTION_MOVE) {

        float x = event.getX();
        selectorPosition = Math.max(0, Math.min(1, x / getWidth()));
        hue = selectorPosition * 360f;

        if (saturationValuePicker != null) {
          saturationValuePicker.needsRedraw = true;
        }

        updateColor();
        invalidate();

        return true;
      }
      return true;
    }
  }

  private class SaturationValuePicker extends View {
    private Paint paint;
    private float satPos = 1f, valPos = 1f;
    private boolean needsRedraw = true;

    public SaturationValuePicker(Context context) {
      super(context);
      init();
    }

    private void init() {
      paint = new Paint();
      paint.setAntiAlias(true);
      paint.setDither(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
      int width = getWidth();
      int height = getHeight();

      if (width <= 0 || height <= 0) return;

      if (needsRedraw
          || satValBitmapCache == null
          || satValBitmapCache.getWidth() != width
          || satValBitmapCache.getHeight() != height) {

        if (satValBitmapCache != null) {
          satValBitmapCache.recycle();
        }

        satValBitmapCache = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas tempCanvas = new Canvas(satValBitmapCache);

        float[] hsv = new float[] {hue, 0, 0};
        for (int y = 0; y < height; y++) {
          float val = 1f - (float) y / height;
          hsv[2] = val;

          for (int x = 0; x < width; x++) {
            float sat = (float) x / width;
            hsv[1] = sat;
            paint.setColor(Color.HSVToColor(hsv));
            tempCanvas.drawPoint(x, y, paint);
          }
        }
        needsRedraw = false;
      }

      canvas.drawBitmap(satValBitmapCache, 0, 0, paint);
      paint.setColor(Color.WHITE);
      paint.setStrokeWidth(3);
      float x = satPos * width;
      float y = (1f - valPos) * height;
      canvas.drawCircle(x, y, 8, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
      if (event.getAction() == MotionEvent.ACTION_DOWN
          || event.getAction() == MotionEvent.ACTION_MOVE) {

        float x = event.getX();
        float y = event.getY();
        satPos = Math.max(0, Math.min(1, x / getWidth()));
        valPos = Math.max(0, Math.min(1, 1f - y / getHeight()));
        saturation = satPos;
        value = valPos;

        updateColor();
        invalidate();

        return true;
      }
      return true;
    }
  }
}
