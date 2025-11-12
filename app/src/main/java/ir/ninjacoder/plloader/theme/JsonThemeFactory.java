package ir.ninjacoder.plloader.theme;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.loadingindicator.LoadingIndicator;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.MaterialShapes;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.slider.Slider;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.chip.Chip;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;
import ir.ninjacoder.ghostide.core.glidecompat.GlideCompat;
import ir.ninjacoder.ghostide.core.utils.ObjectUtils;
import ir.ninjacoder.ghostide.core.widget.ExrtaFab;

import ir.ninjacoder.prograsssheet.perfence.ListItemView;
import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.Field;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class JsonThemeFactory {

  static int get(Context context, int attr) {
    return MaterialColors.getColor(context, attr, 0);
  }

  public static void applyThemeToActivity(Activity activity) {
    if (activity == null) return;

    View rootView = activity.getWindow().getDecorView().findViewById(android.R.id.content);

    rootView.post(
        new Runnable() {
          @Override
          public void run() {
            applyThemeToViewTree((ViewGroup) rootView);
            try {
              activity
                  .getWindow()
                  .getDecorView()
                  .setBackgroundColor(
                      JsonTheme.color("surface", get(activity, ObjectUtils.colorSurface)));
              activity
                  .getWindow()
                  .setNavigationBarColor(
                      JsonTheme.color("surface", get(activity, ObjectUtils.colorSurface)));
              activity
                  .getWindow()
                  .setStatusBarColor(
                      JsonTheme.color("surface", get(activity, ObjectUtils.colorSurface)));
            } catch (Exception err) {
              err.printStackTrace();
            }
          }
        });
  }

  static void applyThemeToViewTree(ViewGroup viewGroup) {
    if (viewGroup == null) return;
    if (viewGroup.getId() == android.R.id.content) {
      ImageView themeIcon = new ImageView(viewGroup.getContext());
      GlideCompat.GlideNormals(
          themeIcon, "/storage/emulated/0/GhostWebIDE/plugins/mytheme/dark_mode.png");
      themeIcon.setColorFilter(
          JsonTheme.color("primary", get(themeIcon.getContext(), ObjectUtils.colorPrimary)));
      themeIcon.setBackground(MaterialShapes.createShapeDrawable(MaterialShapes.CLOVER_4));
      themeIcon.setBackgroundTintList(
          ColorStateList.valueOf(
              JsonTheme.color(
                  "onPrimary", get(themeIcon.getContext(), ObjectUtils.colorOnPrimary))));
      ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(100, 100);
      themeIcon.setLayoutParams(params);
      themeIcon.setPadding(20, 20, 20, 20);
      themeIcon.setX(120);
      themeIcon.setZ(20);
      themeIcon.setY(25);

      themeIcon.setOnClickListener(
          v -> {
            List<String> themes = new ArrayList<>();
            themes.add("Swamp Green");
            themes.add("Ocean Blue");
            themes.add("Black & Gold");
            themes.add("Dracula");

            SharedPreferences prefs =
                v.getContext().getSharedPreferences("name", Context.MODE_PRIVATE);
            int currentSelection = prefs.getInt("selected_theme_index", -1);

            new MaterialAlertDialogBuilder(v.getContext())
                .setTitle("Select theme")
                .setSingleChoiceItems(
                    themes.toArray(new String[0]),
                    currentSelection,
                    (dialog, which) -> {
                      String selectedTheme = themes.get(which);
                      String filePath =
                          "/storage/emulated/0/GhostWebIDE/plugins/mytheme/"
                              + selectedTheme
                              + ".json";
                      try {
                        JsonTheme.fromFile(v.getContext(), filePath);
                        if (v.getContext() instanceof Activity) {
                          JsonThemeFactory.applyThemeToActivity((Activity) v.getContext());
                        }

                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putInt("selected_theme_index", which);
                        editor.putString("selected_theme_name", selectedTheme);
                        editor.apply();

                        Toast.makeText(
                                v.getContext(),
                                "تم " + selectedTheme + " اعمال شد",
                                Toast.LENGTH_SHORT)
                            .show();
                      } catch (Exception e) {
                        Toast.makeText(v.getContext(), "خطا در اعمال تم", Toast.LENGTH_SHORT)
                            .show();
                      }
                      ((Activity) v.getContext()).recreate();

                      // آپدیت کامل همه ویوها بعد از تغییر تم
                      viewGroup.postDelayed(
                          () -> {
                            View updatedRootView =
                                ((Activity) v.getContext()).getWindow().getDecorView();
                            ViewGroup updatedContentView =
                                updatedRootView.findViewById(android.R.id.content);
                            if (updatedContentView != null) {
                              forceUpdateAllViews(updatedContentView);
                            }
                          },
                          800);
                      dialog.dismiss();
                    })
                .setNegativeButton("لغو", null)
                .show();
          });

      viewGroup.addView(themeIcon);
    }
    for (int i = 0; i < viewGroup.getChildCount(); i++) {
      View child = viewGroup.getChildAt(i);
      applyTheme(child);
      if (child instanceof ViewGroup) {
        applyThemeToViewTree((ViewGroup) child);
      }
    }

    reapplySpecificComponents(viewGroup);
  }

  // متد جدید برای فورس آپدیت همه ویوها
  static void forceUpdateAllViews(ViewGroup viewGroup) {
    if (viewGroup == null) return;

    for (int i = 0; i < viewGroup.getChildCount(); i++) {
      View child = viewGroup.getChildAt(i);

      // اعمال تم روی خود ویو
      applyThemeDeep(child);

      // اگر MaterialCardView هست، عمیق‌تر آپدیت کن
      if (child instanceof MaterialCardView) {
        forceUpdateCardViewChildren((MaterialCardView) child);
      }
      // اگر RecyclerView هست
      else if (child instanceof RecyclerView) {
        forceUpdateRecyclerView((RecyclerView) child);
      }
      // اگر ViewGroup معمولی هست
      else if (child instanceof ViewGroup) {
        forceUpdateAllViews((ViewGroup) child);
      }
    }
  }

  // آپدیت عمیق برای هر ویو
  static void applyThemeDeep(View view) {
    if (view == null) return;

    applyTheme(view);

    // برای TextViewها اطمینان از آپدیت رنگ متن
    if (view instanceof TextView) {
      TextView textView = (TextView) view;
      textView.setTextColor(
          JsonTheme.color("onSurface", get(textView.getContext(), ObjectUtils.colorOnSurface)));
    }

    // برای MaterialCardView
    if (view instanceof MaterialCardView) {
      applyMaterialCardViewTheme((MaterialCardView) view);
    }
  }

  // آپدیت فرزندان MaterialCardView
  static void forceUpdateCardViewChildren(MaterialCardView cardView) {
    if (cardView == null) return;

    // آپدیت خود کارت
    applyMaterialCardViewTheme(cardView);

    // آپدیت همه فرزندان
    for (int i = 0; i < cardView.getChildCount(); i++) {
      View child = cardView.getChildAt(i);
      applyThemeDeep(child);

      // اگر فرزند خودش ViewGroup هست، بازگشتی برو
      if (child instanceof ViewGroup) {
        forceUpdateAllViews((ViewGroup) child);
      }
    }
  }

  static void forceUpdateRecyclerView(RecyclerView recyclerView) {
    if (recyclerView == null) return;

    // آپدیت RecyclerView
    recyclerView.setBackgroundColor(
        JsonTheme.color("surface", get(recyclerView.getContext(), ObjectUtils.colorSurface)));

    // آپدیت ویوهای قابل مشاهده
    for (int i = 0; i < recyclerView.getChildCount(); i++) {
      View itemView = recyclerView.getChildAt(i);
      forceUpdateListItem(itemView);
    }

    // آپدیت ویوهای کش شده در RecycledViewPool
    try {
      RecyclerView.RecycledViewPool pool = recyclerView.getRecycledViewPool();
      for (int i = 0; i < pool.getRecycledViewCount(0); i++) {
        RecyclerView.ViewHolder holder = pool.getRecycledView(0);
        if (holder != null) {
          forceUpdateListItem(holder.itemView);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    // فورس ریدراو
    recyclerView.getAdapter().notifyDataSetChanged();
    recyclerView.invalidate();
  }

  // آپدیت آیتم‌های لیست
  static void forceUpdateListItem(View itemView) {
    if (itemView == null) return;

    // اعمال تم روی خود آیتم
    applyThemeDeep(itemView);

    // اگر ListItemView خاص هست
    if (itemView instanceof ListItemView) {
      applyListItem((ListItemView) itemView);
    }

    // آپدیت همه فرزندان آیتم
    if (itemView instanceof ViewGroup) {
      forceUpdateAllViews((ViewGroup) itemView);
    }
  }

  static void reapplySpecificComponents(ViewGroup viewGroup) {
    for (int i = 0; i < viewGroup.getChildCount(); i++) {
      View child = viewGroup.getChildAt(i);

      if (child instanceof RecyclerView) {
        forceUpdateRecyclerView((RecyclerView) child);
      } else if (child instanceof MaterialSwitch) {
        applySwitchMaterialTheme((MaterialSwitch) child);
      } else if (child instanceof MaterialCheckBox) {
        applyMaterialCheckBoxTheme((MaterialCheckBox) child);
      } else if (child instanceof MaterialRadioButton) {
        applyMaterialRadioButtonTheme((MaterialRadioButton) child);
      } else if (child instanceof MaterialButton) {
        applyMaterialButtonTheme((MaterialButton) child);
      } else if (child instanceof Chip) {
        applyChipTheme((Chip) child);
      } else if (child instanceof NavigationView) {
        applyNavigationViewTheme((NavigationView) child);
      } else if (child instanceof MaterialCardView) {
        forceUpdateCardViewChildren((MaterialCardView) child);
      } else if (child instanceof Button) {
        applyButtonTheme((Button) child);
      } else if (child instanceof CheckBox) {
        applyCheckBoxTheme((CheckBox) child);
      } else if (child instanceof FloatingActionButton) {
        applyFloatingActionButtonTheme((FloatingActionButton) child);
      } else if (child instanceof ExtendedFloatingActionButton) {
        applyExtendedFABTheme((ExtendedFloatingActionButton) child);
      } else if (child instanceof ListItemView) {
        applyListItem((ListItemView) child);
      }

      if (child instanceof ViewGroup) {
        reapplySpecificComponents((ViewGroup) child);
      }
    }
  }

  static void applyTheme(View v) {
    if (v == null) return;

    try {
      Context context = v.getContext();

      if (v instanceof RecyclerView) {
        forceUpdateRecyclerView((RecyclerView) v);
      } else if (v instanceof TextView) {
        applyTextTheme((TextView) v);
      } else if (v instanceof MaterialButton) {
        applyMaterialButtonTheme((MaterialButton) v);
      } else if (v instanceof Button) {
        applyButtonTheme((Button) v);
      } else if (v instanceof TextInputLayout) {
        applyTextInputLayoutTheme((TextInputLayout) v);
      } else if (v instanceof EditText) {
        applyEditTextTheme((EditText) v);
      } else if (v instanceof MaterialCheckBox) {
        applyMaterialCheckBoxTheme((MaterialCheckBox) v);
      } else if (v instanceof CheckBox) {
        applyCheckBoxTheme((CheckBox) v);
      } else if (v instanceof MaterialRadioButton) {
        applyMaterialRadioButtonTheme((MaterialRadioButton) v);
      } else if (v instanceof RadioButton) {
        applyRadioButtonTheme((RadioButton) v);
      } else if (v instanceof MaterialSwitch) {
        applySwitchMaterialTheme((MaterialSwitch) v);
      } else if (v instanceof Switch) {
        applySwitchTheme((Switch) v);
      } else if (v instanceof LinearProgressIndicator) {
        applyLinearProgressIndicatorTheme((LinearProgressIndicator) v);
      } else if (v instanceof CircularProgressIndicator) {
        applyCircularProgressIndicatorTheme((CircularProgressIndicator) v);
      } else if (v instanceof ProgressBar) {
        applyProgressBarTheme((ProgressBar) v);
      } else if (v instanceof SeekBar) {
        applySeekBarTheme((SeekBar) v);
      } else if (v instanceof MaterialCardView) {
        forceUpdateCardViewChildren((MaterialCardView) v);
      } else if (v instanceof FloatingActionButton) {
        applyFloatingActionButtonTheme((FloatingActionButton) v);
      } else if (v instanceof ExtendedFloatingActionButton) {
        applyExtendedFABTheme((ExtendedFloatingActionButton) v);
      } else if (v instanceof MaterialToolbar) {
        applyMaterialToolbarTheme((MaterialToolbar) v);
      } else if (v instanceof NavigationView) {
        applyNavigationViewTheme((NavigationView) v);
      } else if (v instanceof BottomNavigationView) {
        applyBottomNavigationViewTheme((BottomNavigationView) v);
      } else if (v instanceof TabLayout) {
        applyTabLayoutTheme((TabLayout) v);
      } else if (v instanceof Chip) {
        applyChipTheme((Chip) v);
      } else if (v instanceof Slider) {
        applySliderTheme((Slider) v);
      } else if (v instanceof ImageView) {
        applyImageView((ImageView) v);
      } else if (v instanceof Toolbar) {
        applyToolBar((Toolbar) v);
      } else if (v instanceof LoadingIndicator) {
        applyLoadingIndicatorTheme((LoadingIndicator) v);
      } else if (v instanceof ListItemView) {
        applyListItem((ListItemView) v);
      } else {
        v.setBackgroundColor(JsonTheme.color("surface", get(context, ObjectUtils.colorSurface)));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  static void applyTextTheme(TextView textView) {
    textView.setTextColor(
        JsonTheme.color("onSurface", get(textView.getContext(), ObjectUtils.colorOnSurface)));
  }

  static void applyEditTextTheme(EditText editText) {
    Context context = editText.getContext();
    editText.setTextColor(JsonTheme.color("onSurface", get(context, ObjectUtils.colorOnSurface)));
    editText.setHintTextColor(
        JsonTheme.color("onSurfaceVariant", get(context, ObjectUtils.colorOnSurfaceVariant)));
    editText.setBackgroundTintList(
        ColorStateList.valueOf(JsonTheme.color("primary", get(context, ObjectUtils.colorPrimary))));
  }

  static void applyMaterialButtonTheme(MaterialButton button) {
    Context context = button.getContext();
    button.setBackgroundTintList(
        ColorStateList.valueOf(JsonTheme.color("primary", get(context, ObjectUtils.colorPrimary))));
    button.setTextColor(JsonTheme.color("onPrimary", get(context, ObjectUtils.colorOnPrimary)));
    button.setRippleColor(
        ColorStateList.valueOf(
            JsonTheme.color("onPrimary", get(context, ObjectUtils.colorOnPrimary))));

    try {
      ShapeAppearanceModel shapeAppearanceModel =
          new ShapeAppearanceModel().toBuilder().setAllCorners(CornerFamily.ROUNDED, 20).build();
      button.setShapeAppearanceModel(shapeAppearanceModel);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  static void applyButtonTheme(Button button) {
    Context context = button.getContext();
    button.setBackgroundColor(JsonTheme.color("primary", get(context, ObjectUtils.colorPrimary)));
    button.setTextColor(JsonTheme.color("onPrimary", get(context, ObjectUtils.colorOnPrimary)));
  }

  static void applyTextInputLayoutTheme(TextInputLayout textInputLayout) {
    Context context = textInputLayout.getContext();

    textInputLayout.setHintTextColor(
        ColorStateList.valueOf(
            JsonTheme.color("onSurfaceVariant", get(context, ObjectUtils.colorOnSurfaceVariant))));
    textInputLayout.setBoxStrokeColor(
        JsonTheme.color("outline", get(context, ObjectUtils.colorOutline)));
    textInputLayout.setDefaultHintTextColor(
        ColorStateList.valueOf(
            JsonTheme.color("onSurfaceVariant", get(context, ObjectUtils.colorOnSurfaceVariant))));

    ColorStateList boxStrokeColorStateList =
        new ColorStateList(
            new int[][] {
              new int[] {android.R.attr.state_focused},
              new int[] {android.R.attr.state_hovered},
              new int[] {}
            },
            new int[] {
              JsonTheme.color("primary", get(context, ObjectUtils.colorPrimary)),
              JsonTheme.color("primary", get(context, ObjectUtils.colorPrimary)),
              JsonTheme.color("outline", get(context, ObjectUtils.colorOutline))
            });

    textInputLayout.setBoxStrokeColorStateList(boxStrokeColorStateList);
    textInputLayout.setBoxStrokeErrorColor(
        ColorStateList.valueOf(JsonTheme.color("error", get(context, ObjectUtils.colorError))));
    textInputLayout.setErrorTextColor(
        ColorStateList.valueOf(
            JsonTheme.color("onErrorContainer", get(context, ObjectUtils.colorOnErrorContainer))));
    textInputLayout.setErrorIconTintList(
        ColorStateList.valueOf(
            JsonTheme.color("onErrorContainer", get(context, ObjectUtils.colorOnErrorContainer))));
  }

  static void applyMaterialCheckBoxTheme(MaterialCheckBox checkBox) {
    Context context = checkBox.getContext();

    ColorStateList checkBoxColors =
        new ColorStateList(
            new int[][] {
              new int[] {android.R.attr.state_checked, android.R.attr.state_enabled},
              new int[] {android.R.attr.state_enabled},
              new int[] {-android.R.attr.state_enabled}
            },
            new int[] {
              JsonTheme.color("primary", get(context, ObjectUtils.colorPrimary)),
              JsonTheme.color("onSurfaceVariant", get(context, ObjectUtils.colorOnSurfaceVariant)),
              JsonTheme.color("onSurfaceVariant", get(context, ObjectUtils.colorOnSurfaceVariant))
            });

    checkBox.setButtonTintList(checkBoxColors);
    checkBox.setTextColor(JsonTheme.color("onSurface", get(context, ObjectUtils.colorOnSurface)));
  }

  static void applyCheckBoxTheme(CheckBox checkBox) {
    Context context = checkBox.getContext();

    ColorStateList checkBoxColors =
        new ColorStateList(
            new int[][] {
              new int[] {android.R.attr.state_checked, android.R.attr.state_enabled},
              new int[] {android.R.attr.state_enabled},
              new int[] {-android.R.attr.state_enabled}
            },
            new int[] {
              JsonTheme.color("primary", get(context, ObjectUtils.colorPrimary)),
              JsonTheme.color("onSurfaceVariant", get(context, ObjectUtils.colorOnSurfaceVariant)),
              JsonTheme.color("onSurfaceVariant", get(context, ObjectUtils.colorOnSurfaceVariant))
            });

    checkBox.setButtonTintList(checkBoxColors);
    checkBox.setTextColor(JsonTheme.color("onSurface", get(context, ObjectUtils.colorOnSurface)));
  }

  static void applyImageView(ImageView img) {
    Context context = img.getContext();

    // اگر colorFilter وجود داره (که معمولاً برای سفید کردن استفاده میشه)
    if (img.getColorFilter() != null) {
      img.setBackground(MaterialShapes.createShapeDrawable(MaterialShapes.CLOVER_4));
      img.setBackgroundTintList(
          ColorStateList.valueOf(
              JsonTheme.color("onPrimary", get(img.getContext(), ObjectUtils.colorOnPrimary))));
    }
  }

  static void applyMaterialRadioButtonTheme(MaterialRadioButton radioButton) {
    Context context = radioButton.getContext();

    ColorStateList radioButtonColors =
        new ColorStateList(
            new int[][] {
              new int[] {android.R.attr.state_checked, android.R.attr.state_enabled},
              new int[] {android.R.attr.state_enabled},
              new int[] {-android.R.attr.state_enabled}
            },
            new int[] {
              JsonTheme.color("primary", get(context, ObjectUtils.colorPrimary)),
              JsonTheme.color("onSurfaceVariant", get(context, ObjectUtils.colorOnSurfaceVariant)),
              JsonTheme.color("onSurfaceVariant", get(context, ObjectUtils.colorOnSurfaceVariant))
            });

    radioButton.setButtonTintList(radioButtonColors);
    radioButton.setTextColor(
        JsonTheme.color("onSurface", get(context, ObjectUtils.colorOnSurface)));
  }

  static void applyRadioButtonTheme(RadioButton radioButton) {
    Context context = radioButton.getContext();

    ColorStateList radioButtonColors =
        new ColorStateList(
            new int[][] {
              new int[] {android.R.attr.state_checked, android.R.attr.state_enabled},
              new int[] {android.R.attr.state_enabled},
              new int[] {-android.R.attr.state_enabled}
            },
            new int[] {
              JsonTheme.color("primary", get(context, ObjectUtils.colorPrimary)),
              JsonTheme.color("onSurfaceVariant", get(context, ObjectUtils.colorOnSurfaceVariant)),
              JsonTheme.color("onSurfaceVariant", get(context, ObjectUtils.colorOnSurfaceVariant))
            });

    radioButton.setButtonTintList(radioButtonColors);
    radioButton.setTextColor(
        JsonTheme.color("onSurface", get(context, ObjectUtils.colorOnSurface)));
  }

  static void applySwitchMaterialTheme(MaterialSwitch materialSwitch) {
    if (materialSwitch == null) return;
    Context context = materialSwitch.getContext();

    ColorStateList thumbColors =
        new ColorStateList(
            new int[][] {
              new int[] {android.R.attr.state_checked, android.R.attr.state_enabled},
              new int[] {android.R.attr.state_checked},
              new int[] {android.R.attr.state_enabled},
              new int[] {}
            },
            new int[] {
              JsonTheme.color("onPrimary", get(context, ObjectUtils.colorOnPrimary)),
              JsonTheme.color("onPrimary", get(context, ObjectUtils.colorOnPrimary)),
              JsonTheme.color("outline", get(context, ObjectUtils.colorOutline)),
              JsonTheme.color("outline", get(context, ObjectUtils.colorOutline))
            });

    ColorStateList trackColors =
        new ColorStateList(
            new int[][] {
              new int[] {android.R.attr.state_checked, android.R.attr.state_enabled},
              new int[] {android.R.attr.state_checked},
              new int[] {android.R.attr.state_enabled},
              new int[] {}
            },
            new int[] {
              JsonTheme.color("primary", get(context, ObjectUtils.colorPrimary)),
              JsonTheme.color("primary", get(context, ObjectUtils.colorPrimary)),
              JsonTheme.color(
                  "surfaceContainerHighest",
                  get(context, ObjectUtils.colorSurfaceContainerHighest)),
              JsonTheme.color(
                  "surfaceContainerHighest", get(context, ObjectUtils.colorSurfaceContainerHighest))
            });

    ColorStateList trackDecorationColors =
        new ColorStateList(
            new int[][] {new int[] {android.R.attr.state_checked}, new int[] {}},
            new int[] {
              Color.TRANSPARENT, JsonTheme.color("outline", get(context, ObjectUtils.colorOutline))
            });

    materialSwitch.setThumbTintList(thumbColors);
    materialSwitch.setTrackTintList(trackColors);
    materialSwitch.setTrackDecorationTintList(trackDecorationColors);
    materialSwitch.setTextColor(
        JsonTheme.color("onSurface", get(context, ObjectUtils.colorOnSurface)));
  }

  static void applySwitchTheme(Switch switchView) {
    Context context = switchView.getContext();

    ColorStateList thumbColors =
        new ColorStateList(
            new int[][] {
              new int[] {android.R.attr.state_checked, android.R.attr.state_enabled},
              new int[] {android.R.attr.state_checked},
              new int[] {android.R.attr.state_enabled},
              new int[] {}
            },
            new int[] {
              JsonTheme.color("primary", get(context, ObjectUtils.colorPrimary)),
              JsonTheme.color("surfaceVariant", get(context, ObjectUtils.colorSurfaceVariant)),
              JsonTheme.color("surfaceVariant", get(context, ObjectUtils.colorSurfaceVariant)),
              JsonTheme.color("outlineVariant", get(context, ObjectUtils.colorOutlineVariant))
            });

    ColorStateList trackColors =
        new ColorStateList(
            new int[][] {
              new int[] {android.R.attr.state_checked, android.R.attr.state_enabled},
              new int[] {android.R.attr.state_checked},
              new int[] {android.R.attr.state_enabled},
              new int[] {}
            },
            new int[] {
              JsonTheme.color("primaryContainer", get(context, ObjectUtils.colorPrimaryContainer)),
              JsonTheme.color("onSurfaceVariant", get(context, ObjectUtils.colorOnSurfaceVariant)),
              JsonTheme.color("outlineVariant", get(context, ObjectUtils.colorOutlineVariant)),
              JsonTheme.color(
                  "surfaceContainerHighest", get(context, ObjectUtils.colorSurfaceContainerHighest))
            });

    switchView.setThumbTintList(thumbColors);
    switchView.setTrackTintList(trackColors);
    switchView.setTextColor(JsonTheme.color("onSurface", get(context, ObjectUtils.colorOnSurface)));
  }

  static void applyLinearProgressIndicatorTheme(LinearProgressIndicator progress) {
    Context context = progress.getContext();
    progress.setIndicatorColor(JsonTheme.color("primary", get(context, ObjectUtils.colorPrimary)));
    progress.setTrackColor(
        JsonTheme.color("surfaceVariant", get(context, ObjectUtils.colorSurfaceVariant)));
    progress.setTrackCornerRadius(4);
    progress.setTrackThickness(8);
  }

  static void applyCircularProgressIndicatorTheme(CircularProgressIndicator progress) {
    Context context = progress.getContext();
    progress.setIndicatorColor(JsonTheme.color("primary", get(context, ObjectUtils.colorPrimary)));
    progress.setTrackColor(
        JsonTheme.color("surfaceVariant", get(context, ObjectUtils.colorSurfaceVariant)));
    progress.setTrackCornerRadius(4);
    progress.setTrackThickness(8);
  }

  static void applyProgressBarTheme(ProgressBar progressBar) {
    Context context = progressBar.getContext();
    progressBar.setProgressTintList(
        ColorStateList.valueOf(JsonTheme.color("primary", get(context, ObjectUtils.colorPrimary))));
    progressBar.setSecondaryProgressTintList(
        ColorStateList.valueOf(
            JsonTheme.color("primaryContainer", get(context, ObjectUtils.colorPrimaryContainer))));
    progressBar.setBackgroundTintList(
        ColorStateList.valueOf(
            JsonTheme.color("surfaceVariant", get(context, ObjectUtils.colorSurfaceVariant))));
  }

  static void applySeekBarTheme(SeekBar seekBar) {
    Context context = seekBar.getContext();
    seekBar.setProgressTintList(
        ColorStateList.valueOf(JsonTheme.color("primary", get(context, ObjectUtils.colorPrimary))));
    seekBar.setThumbTintList(
        ColorStateList.valueOf(JsonTheme.color("primary", get(context, ObjectUtils.colorPrimary))));
    seekBar.setBackgroundTintList(
        ColorStateList.valueOf(
            JsonTheme.color("surfaceVariant", get(context, ObjectUtils.colorSurfaceVariant))));
  }

  static void applyMaterialCardViewTheme(MaterialCardView cardView) {
    Context context = cardView.getContext();
    cardView.setCardBackgroundColor(
        JsonTheme.color("surface", get(context, ObjectUtils.colorSurface)));
    cardView.setStrokeColor(JsonTheme.color("outline", get(context, ObjectUtils.colorOutline)));
    cardView.setRippleColor(
        ColorStateList.valueOf(
            JsonTheme.color("primaryContainer", get(context, ObjectUtils.colorPrimaryContainer))));

    // آپدیت متن‌های داخل کارت
    for (int i = 0; i < cardView.getChildCount(); i++) {
      View child = cardView.getChildAt(i);
      if (child instanceof TextView) {
        applyTextTheme((TextView) child);
      }
    }
  }

  static void applyExtendedFABTheme(ExtendedFloatingActionButton extendedFab) {
    Context context = extendedFab.getContext();
    if (!(extendedFab instanceof ExrtaFab)) {
      Drawable shapeDrawable = MaterialShapes.createShapeDrawable(MaterialShapes.BOOM);

      if (shapeDrawable instanceof MaterialShapeDrawable) {
        MaterialShapeDrawable materialShape = (MaterialShapeDrawable) shapeDrawable;
        materialShape.setFillColor(
            ColorStateList.valueOf(
                JsonTheme.color(
                    "primaryContainer", get(context, ObjectUtils.colorPrimaryContainer))));
        materialShape.initializeElevationOverlay(context);
      }

      extendedFab.setBackground(shapeDrawable);
      extendedFab.setBackgroundTintList(null);
    }
    extendedFab.setTextColor(
        JsonTheme.color("onPrimaryContainer", get(context, ObjectUtils.colorOnPrimaryContainer)));
    extendedFab.setIconTint(
        ColorStateList.valueOf(
            JsonTheme.color(
                "onPrimaryContainer", get(context, ObjectUtils.colorOnPrimaryContainer))));

    extendedFab.setRippleColor(
        ColorStateList.valueOf(
            JsonTheme.color(
                "onPrimaryContainer", get(context, ObjectUtils.colorOnPrimaryContainer))));
  }

  static void applyFloatingActionButtonTheme(FloatingActionButton fab) {
    Context context = fab.getContext();
    Drawable shapeDrawable = MaterialShapes.createShapeDrawable(MaterialShapes.COOKIE_12);
    if (shapeDrawable instanceof MaterialShapeDrawable) {
      ((MaterialShapeDrawable) shapeDrawable)
          .setFillColor(
              ColorStateList.valueOf(
                  JsonTheme.color("primary", get(context, ObjectUtils.colorPrimary))));
    }
    fab.setBackground(shapeDrawable);
    ColorStateList iconTintList =
        new ColorStateList(
            new int[][] {
              new int[] {-android.R.attr.state_enabled}, // disabled
              new int[] {android.R.attr.state_checkable, android.R.attr.state_checked}, // checked
              new int[] {android.R.attr.state_checkable}, // checkable
              new int[] {} // default
            },
            new int[] {
              JsonTheme.color("onSurface", get(context, ObjectUtils.colorOnSurface)), // disabled
              JsonTheme.color(
                  "onPrimaryContainer",
                  get(context, ObjectUtils.colorOnPrimaryContainer)), // checked
              JsonTheme.color(
                  "onPrimaryContainer",
                  get(context, ObjectUtils.colorOnPrimaryContainer)), // checkable
              JsonTheme.color(
                  "onPrimaryContainer",
                  get(context, ObjectUtils.colorOnPrimaryContainer)) // default
            });

    fab.setImageTintList(iconTintList);

    // Ripple color
    fab.setRippleColor(
        ColorStateList.valueOf(
            JsonTheme.color(
                "onPrimaryContainer", get(context, ObjectUtils.colorOnPrimaryContainer))));
  }

  static void applyMaterialToolbarTheme(MaterialToolbar toolbar) {
    Context context = toolbar.getContext();
    toolbar.setBackgroundColor(JsonTheme.color("surface", get(context, ObjectUtils.colorSurface)));
    toolbar.setTitleTextColor(
        JsonTheme.color("onSurface", get(context, ObjectUtils.colorOnSurface)));
    toolbar.setSubtitleTextColor(
        JsonTheme.color("onSurfaceVariant", get(context, ObjectUtils.colorOnSurfaceVariant)));
  }

  static void applyNavigationViewTheme(NavigationView navigationView) {
    Context context = navigationView.getContext();
    navigationView.setBackgroundColor(
        JsonTheme.color("surface", get(context, ObjectUtils.colorSurface)));
    navigationView.setItemTextColor(createNavColorStateList(context));
    navigationView.setItemIconTintList(createNavColorStateList(context));
  }

  static void applyBottomNavigationViewTheme(BottomNavigationView bottomNav) {
    Context context = bottomNav.getContext();
    bottomNav.setBackgroundColor(
        JsonTheme.color("surface", get(context, ObjectUtils.colorSurface)));
    bottomNav.setItemTextColor(createNavColorStateList(context));
    bottomNav.setItemIconTintList(createNavColorStateList(context));
  }

  static void applyTabLayoutTheme(TabLayout tabLayout) {
    Context context = tabLayout.getContext();
    tabLayout.setBackgroundColor(
        JsonTheme.color("surface", get(context, ObjectUtils.colorSurface)));
    tabLayout.setTabTextColors(
        JsonTheme.color("onSurfaceVariant", get(context, ObjectUtils.colorOnSurfaceVariant)),
        JsonTheme.color("primary", get(context, ObjectUtils.colorPrimary)));
    tabLayout.setSelectedTabIndicatorColor(
        JsonTheme.color("primary", get(context, ObjectUtils.colorPrimary)));
  }

  static void applyChipTheme(Chip chip) {
    Context context = chip.getContext();
    chip.setChipBackgroundColor(
        ColorStateList.valueOf(JsonTheme.color("surface", get(context, ObjectUtils.colorSurface))));
    chip.setTextColor(
        JsonTheme.color("onSurfaceVariant", get(context, ObjectUtils.colorOnSurfaceVariant)));
    chip.setChipStrokeColor(
        ColorStateList.valueOf(JsonTheme.color("outline", get(context, ObjectUtils.colorOutline))));
    chip.setChipStrokeWidth(1f);
    chip.setRippleColor(
        ColorStateList.valueOf(
            JsonTheme.color(
                "onSecondaryContainer", get(context, ObjectUtils.colorOnSecondaryContainer))));
    chip.setChipIconTint(
        ColorStateList.valueOf(
            JsonTheme.color("onSurfaceVariant", get(context, ObjectUtils.colorOnSurfaceVariant))));
    chip.setCheckedIconTint(
        ColorStateList.valueOf(JsonTheme.color("primary", get(context, ObjectUtils.colorPrimary))));
    chip.setCloseIconTint(
        ColorStateList.valueOf(
            JsonTheme.color("onSurfaceVariant", get(context, ObjectUtils.colorOnSurfaceVariant))));
  }

  static void applySliderTheme(Slider slider) {
    Context context = slider.getContext();
    slider.setTrackActiveTintList(
        ColorStateList.valueOf(JsonTheme.color("primary", get(context, ObjectUtils.colorPrimary))));
    slider.setTrackInactiveTintList(
        ColorStateList.valueOf(
            JsonTheme.color("surfaceVariant", get(context, ObjectUtils.colorSurfaceVariant))));
    slider.setThumbTintList(
        ColorStateList.valueOf(JsonTheme.color("primary", get(context, ObjectUtils.colorPrimary))));
    slider.setHaloTintList(
        ColorStateList.valueOf(
            JsonTheme.color("primaryContainer", get(context, ObjectUtils.colorPrimaryContainer))));
  }

  static void applyToolBar(Toolbar toolbar) {
    Context context = toolbar.getContext();
    toolbar.setTitleTextColor(JsonTheme.color("primary", get(context, ObjectUtils.colorPrimary)));
    toolbar.setSubtitleTextColor(
        JsonTheme.color("primary", get(context, ObjectUtils.colorPrimary)));
    toolbar.setBackgroundColor(JsonTheme.color("surface", get(context, ObjectUtils.colorSurface)));
  }

  static void sheet(BottomSheetDialog s) {
    if (s.getWindow() != null) {
      Context context = s.getContext();
      int backgroundColor =
          JsonTheme.color(
              "surfaceContainerHighest", get(context, ObjectUtils.colorSurfaceContainerHighest));

      s.getWindow().setBackgroundDrawable(new ColorDrawable(backgroundColor));
    }
  }

  static void applyLoadingIndicatorTheme(LoadingIndicator loadingIndicator) {
    Context context = loadingIndicator.getContext();
    loadingIndicator.setIndicatorColor(
        JsonTheme.color("primary", get(context, ObjectUtils.colorPrimary)));
    loadingIndicator.setContainerColor(Color.TRANSPARENT);
    loadingIndicator.setIndicatorSize(38);
    loadingIndicator.setContainerWidth(48);
    loadingIndicator.setContainerHeight(48);
  }

  static void applyListItem(ListItemView view) {
    view.setBackgroundColor(
        JsonTheme.color(
            "surfaceContainer", get(view.getContext(), ObjectUtils.colorSurfaceContainer)));
    view.setRippleColor(
        JsonTheme.color(
            "onSurfaceVariant", get(view.getContext(), ObjectUtils.colorOnSurfaceVariant)));
  }

  static ColorStateList createNavColorStateList(Context context) {
    int normalColor =
        JsonTheme.color("onSurfaceVariant", get(context, ObjectUtils.colorOnSurfaceVariant));
    int selectedColor = JsonTheme.color("primary", get(context, ObjectUtils.colorPrimary));
    int disabledColor =
        JsonTheme.color("onSurfaceVariant", get(context, ObjectUtils.colorOnSurfaceVariant));

    int[][] states =
        new int[][] {
          new int[] {android.R.attr.state_checked},
          new int[] {android.R.attr.state_enabled},
          new int[] {-android.R.attr.state_enabled}
        };

    int[] colors = new int[] {selectedColor, normalColor, disabledColor};

    return new ColorStateList(states, colors);
  }
}
