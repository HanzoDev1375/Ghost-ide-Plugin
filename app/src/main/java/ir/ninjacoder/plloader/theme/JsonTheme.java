package ir.ninjacoder.plloader.theme;

import java.io.File;
import java.io.FileInputStream;
import java.io.StringReader;
import org.json.JSONObject;
import android.content.Context;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import android.content.res.Configuration;
import android.graphics.Color;

public class JsonTheme {
  private static JSONObject theme;

  public static void fromFile(Context context, String filePath) {
    try {
      if (filePath == null || filePath.isEmpty()) {
        theme = null; // تم رو غیرفعال کن
        return;
      }

      File file = new File(filePath);
      if (!file.exists()) {
        theme = null; // تم رو غیرفعال کن
        return;
      }

      FileInputStream fis = new FileInputStream(file);
      byte[] buffer = new byte[fis.available()];
      fis.read(buffer);
      fis.close();

      String json = new String(buffer, StandardCharsets.UTF_8);
      JSONObject object = new JSONObject(json);

      boolean dark =
          (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
              == Configuration.UI_MODE_NIGHT_YES;
      theme = object.getJSONObject(dark ? "dark" : "light");

    } catch (Exception err) {
      theme = null; // در صورت خطا تم رو غیرفعال کن
      err.printStackTrace();
    }
  }

  public static void init(Context ctx, String fileName) {
    try (InputStream is = ctx.getAssets().open(fileName)) {
      byte[] buffer = new byte[is.available()];
      is.read(buffer);
      String json = new String(buffer, StandardCharsets.UTF_8);
      JSONObject root = new JSONObject(json);
      boolean dark =
          (ctx.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
              == Configuration.UI_MODE_NIGHT_YES;
      theme = root.getJSONObject(dark ? "dark" : "light");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static int color(String key, int defaultValue) {
    if (theme == null) return defaultValue;
    try {

      return Color.parseColor(theme.getString(key));
    } catch (Exception e) {
      return defaultValue;
    }
  }
}
