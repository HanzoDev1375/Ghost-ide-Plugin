package ir.ghost.theme.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import ir.ghost.theme.model.GhostTheme;
import ir.ninjacoder.ghostide.core.utils.AnimUtils;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ThemePreviewAdapter extends RecyclerView.Adapter<ThemePreviewAdapter.ThemeViewHolder> {

  private Context context;
  private List<ThemeItem> originalThemeItems;
  private List<ThemeItem> filteredThemeItems;
  private String currentFilter = "";
  private OnItemClickListener itemClickListener;

  public interface OnItemClickListener {
    void onItemClick(ThemeItem item);
  }

  public void setOnItemClickListener(OnItemClickListener listener) {
    this.itemClickListener = listener;
  }

  public ThemePreviewAdapter(Context context, GhostTheme theme) {
    this.context = context;
    this.originalThemeItems = extractThemeItems(theme);
    this.filteredThemeItems = new ArrayList<>(originalThemeItems);
  }

  private List<ThemeItem> extractThemeItems(GhostTheme theme) {
    List<ThemeItem> items = new ArrayList<>();

    // استفاده از Reflection برای دریافت تمام فیلدها
    Field[] fields = GhostTheme.class.getDeclaredFields();

    for (Field field : fields) {
      if (field.getType() == String.class) {
        try {
          field.setAccessible(true);
          String value = (String) field.get(theme);
          if (value != null && value.startsWith("#")) {
            items.add(new ThemeItem(field.getName(), value));
          }
        } catch (IllegalAccessException e) {
          e.printStackTrace();
        }
      }
    }

    return items;
  }

  // متد فیلتر کردن
  public void filter(String query) {
    currentFilter = query.toLowerCase().trim();
    filteredThemeItems.clear();

    if (currentFilter.isEmpty()) {
      filteredThemeItems.addAll(originalThemeItems);
    } else {
      for (ThemeItem item : originalThemeItems) {
        if (item.getName().toLowerCase().contains(currentFilter)
            || item.getHexColor().toLowerCase().contains(currentFilter)) {
          filteredThemeItems.add(item);
        }
      }
    }
    notifyDataSetChanged();
  }

  // متد برای آپدیت رنگ
  public void updateColor(String fieldName, String newHexColor) {
    // آپدیت در لیست اصلی
    for (ThemeItem item : originalThemeItems) {
      if (item.getName().equals(fieldName)) {
        item.setHexColor(newHexColor);
        break;
      }
    }

    // آپدیت در لیست فیلتر شده
    for (ThemeItem item : filteredThemeItems) {
      if (item.getName().equals(fieldName)) {
        item.setHexColor(newHexColor);
        break;
      }
    }

    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public ThemeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    // ساخت layout به صورت برنامه‌نویسی
    MaterialCardView cardView = new MaterialCardView(context);

    ViewGroup.MarginLayoutParams layoutParams =
        new ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    AnimUtils.Sacla(cardView);
    int verticalMargin = dpToPx(4);
    int horizontalMargin = dpToPx(0);
    layoutParams.setMargins(horizontalMargin, verticalMargin, horizontalMargin, verticalMargin);

    cardView.setLayoutParams(layoutParams);
    cardView.setCardElevation(2);
    cardView.setRadius(20);
    cardView.setContentPadding(8, 8, 8, 8);

    LinearLayout mainLayout = new LinearLayout(context);
    mainLayout.setLayoutParams(
        new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    mainLayout.setOrientation(LinearLayout.HORIZONTAL);
    mainLayout.setPadding(8, 8, 8, 8);

    // رنگ‌نمای کوچک
    View colorView = new View(context);
    LinearLayout.LayoutParams colorParams = new LinearLayout.LayoutParams(dpToPx(30), dpToPx(30));
    colorParams.setMargins(dpToPx(3), 0, dpToPx(3), 0);
    colorView.setLayoutParams(colorParams);
    colorView.setId(View.generateViewId());

    // Layout برای متن‌ها
    LinearLayout textLayout = new LinearLayout(context);
    LinearLayout.LayoutParams textParams =
        new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
    textLayout.setLayoutParams(textParams);
    textLayout.setOrientation(LinearLayout.VERTICAL);
    textLayout.setPadding(dpToPx(8), 0, 0, 0);

    // TextView برای نام متغیر
    TextView valueName = new TextView(context);
    valueName.setId(View.generateViewId());
    valueName.setLayoutParams(
        new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    valueName.setTextSize(15);
    valueName.setPadding(dpToPx(3), dpToPx(3), dpToPx(3), dpToPx(3));

    // TextView برای کد هگز
    TextView hexName = new TextView(context);
    hexName.setId(View.generateViewId());
    hexName.setLayoutParams(
        new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    hexName.setTextSize(12);
    hexName.setPadding(dpToPx(2), dpToPx(2), dpToPx(2), dpToPx(2));

    // اضافه کردن viewها به layoutها
    textLayout.addView(valueName);
    textLayout.addView(hexName);
    mainLayout.addView(colorView);
    mainLayout.addView(textLayout);
    cardView.addView(mainLayout);

    // ایجاد ViewHolder
    ThemeViewHolder viewHolder = new ThemeViewHolder(cardView, colorView, valueName, hexName);

    // اضافه کردن کلیک‌لیسنر
    cardView.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            int position = viewHolder.getAdapterPosition();
            if (position != RecyclerView.NO_POSITION && itemClickListener != null) {
              ThemeItem item = filteredThemeItems.get(position);
              itemClickListener.onItemClick(item);
            }
          }
        });

    return viewHolder;
  }

  @Override
  public void onBindViewHolder(@NonNull ThemeViewHolder holder, int position) {
    ThemeItem item = filteredThemeItems.get(position);
    holder.bind(item, currentFilter);
  }

  @Override
  public int getItemCount() {
    return filteredThemeItems.size();
  }

  private int dpToPx(int dp) {
    float density = context.getResources().getDisplayMetrics().density;
    return Math.round(dp * density);
  }

  // کلاس برای نگهداری آیتم‌های تم
  public static class ThemeItem {
    private String name;
    private String hexColor;

    public ThemeItem(String name, String hexColor) {
      this.name = name;
      this.hexColor = hexColor;
    }

    public String getName() {
      return name;
    }

    public String getHexColor() {
      return hexColor;
    }

    public void setHexColor(String hexColor) {
      this.hexColor = hexColor;
    }
  }

  // ViewHolder کلاس
  public static class ThemeViewHolder extends RecyclerView.ViewHolder {
    private View colorView;
    private TextView valueName;
    private TextView hexName;

    public ThemeViewHolder(
        @NonNull View itemView, View colorView, TextView valueName, TextView hexName) {
      super(itemView);
      this.colorView = colorView;
      this.valueName = valueName;
      this.hexName = hexName;
    }

    public void bind(ThemeItem item, String filter) {
      try {
        // تنظیم رنگ background
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(dpToPx(itemView.getContext(), 20));
        drawable.setColor(Color.parseColor(item.getHexColor()));
        colorView.setBackground(drawable);

        // تنظیم متن‌ها با هایلایت
        if (!filter.isEmpty()) {
          // هایلایت نام متغیر
          SpannableString spannableName = getHighlightedText(item.getName(), filter);
          valueName.setText(spannableName);

          // هایلایت کد هگز
          SpannableString spannableHex = getHighlightedText(item.getHexColor(), filter);
          hexName.setText(spannableHex);
        } else {
          valueName.setText(item.getName());
          hexName.setText(item.getHexColor());
        }
      } catch (Exception e) {
        valueName.setText(item.getName() + " (Invalid Color)");
        hexName.setText(item.getHexColor());
      }
    }

    private SpannableString getHighlightedText(String text, String filter) {
      SpannableString spannableString = new SpannableString(text);

      if (!filter.isEmpty() && !text.isEmpty()) {
        String lowerText = text.toLowerCase();
        String lowerFilter = filter.toLowerCase();

        int startIndex = lowerText.indexOf(lowerFilter);
        while (startIndex >= 0) {
          int endIndex = startIndex + lowerFilter.length();
          spannableString.setSpan(
              new BackgroundColorSpan(Color.parseColor("#FFFFE0B2")),
              startIndex,
              endIndex,
              SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
          startIndex = lowerText.indexOf(lowerFilter, endIndex);
        }
      }

      return spannableString;
    }

    private int dpToPx(Context context, int dp) {
      float density = context.getResources().getDisplayMetrics().density;
      return Math.round(dp * density);
    }
  }
}
