package ir.ninjacoder.plloader.csslsp;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.List;

public class LspContent {
  public static boolean hasProperties(String jsonContent) {
    try {
      JSONObject root = new JSONObject(jsonContent);
      return root.has("properties") && root.getJSONArray("properties").length() > 0;
    } catch (JSONException e) {
      e.printStackTrace();
      return false;
    }
  }

  public static List<NameValue> extractAllNameValue(String jsonContent) {
    List<NameValue> result = new ArrayList<>();

    try {
      JSONObject root = new JSONObject(jsonContent);

      // 1. properties ها
      if (root.has("properties")) {
        JSONArray properties = root.getJSONArray("properties");
        for (int i = 0; i < properties.length(); i++) {
          JSONObject property = properties.getJSONObject(i);

          // property اصلی
          if (property.has("name") && property.has("value")) {
            String name = property.getString("name");
            String value = property.getString("value");
            if (!name.isEmpty() && !value.isEmpty()) {
              result.add(new NameValue(name, value));
            }
          }

          // values های داخل property
          if (property.has("values")) {
            JSONArray values = property.getJSONArray("values");
            extractValues(values, result);
          }
        }
      }

      // 2. atrules ها
      if (root.has("atrules")) {
        JSONArray atrules = root.getJSONArray("atrules");
        for (int i = 0; i < atrules.length(); i++) {
          JSONObject atrule = atrules.getJSONObject(i);

          // atrule اصلی
          if (atrule.has("name")) {
            String name = atrule.getString("name");
            result.add(new NameValue(name, ""));
          }

          // values های داخل atrule
          if (atrule.has("values")) {
            JSONArray values = atrule.getJSONArray("values");
            extractValues(values, result);
          }
        }
      }

      // 3. selectors ها
      if (root.has("selectors")) {
        JSONArray selectors = root.getJSONArray("selectors");
        for (int i = 0; i < selectors.length(); i++) {
          JSONObject selector = selectors.getJSONObject(i);
          if (selector.has("name") && selector.has("value")) {
            String name = selector.getString("name");
            String value = selector.getString("value");
            result.add(new NameValue(name, value));
          }
        }
      }

      // 4. values های اصلی
      if (root.has("values")) {
        JSONArray values = root.getJSONArray("values");
        extractValues(values, result);
      }

    } catch (JSONException e) {
      e.printStackTrace();
    }

    return result;
  }

  private static void extractValues(JSONArray values, List<NameValue> result) throws JSONException {
    for (int i = 0; i < values.length(); i++) {
      JSONObject valueObj = values.getJSONObject(i);

      if (valueObj.has("name")) {
        String name = valueObj.getString("name");
        String value = valueObj.has("value") ? valueObj.getString("value") : "";
        result.add(new NameValue(name, value));
      }

      // values های تو در تو
      if (valueObj.has("values")) {
        JSONArray nestedValues = valueObj.getJSONArray("values");
        extractValues(nestedValues, result);
      }
    }
  }
}
