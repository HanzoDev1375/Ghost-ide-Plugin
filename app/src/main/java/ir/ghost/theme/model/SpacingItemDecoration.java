package ir.ghost.theme.model;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SpacingItemDecoration extends RecyclerView.ItemDecoration {
  private final int verticalSpace;
  private final int horizontalSpace;

  public SpacingItemDecoration(int verticalSpaceInDp, int horizontalSpaceInDp, Context context) {
    float density = context.getResources().getDisplayMetrics().density;
    this.verticalSpace = Math.round(verticalSpaceInDp * density);
    this.horizontalSpace = Math.round(horizontalSpaceInDp * density);
  }

  @Override
  public void getItemOffsets(
      @NonNull Rect outRect,
      @NonNull View view,
      @NonNull RecyclerView parent,
      @NonNull RecyclerView.State s) {
    int position = parent.getChildAdapterPosition(view);

    // فقط بین آیتم‌ها فاصله (نه برای آیتم اول از بالا)
    if (position == 0) {
      outRect.top = 0; // اولین آیتم از بالا فاصله ندارد
    } else {
      outRect.top = verticalSpace; // بقیه آیتم‌ها از بالا فاصله دارند
    }

    outRect.bottom = verticalSpace; // همه از پایین فاصله دارند

    // فاصله از چپ و راست
    outRect.left = horizontalSpace;
    outRect.right = horizontalSpace;
  }
}
