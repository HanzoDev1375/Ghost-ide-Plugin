package ir.ninjacoder.plloader.emmet;

import io.github.rosemoe.sora.data.CompletionItem;
import java.util.ArrayList;
import java.util.List;

/** کلاس مستقل برای ویژگی Sibling (+) تمام منطق این ویژگی داخل همین کلاس قرار دارد. */
public class SiblingFeature extends EmmetHelper {

  @Override
  public List<CompletionItem> listMethod(String prefix) {
    List<CompletionItem> items = new ArrayList<>();
    String html = expand(prefix);
    CompletionItem item = new CompletionItem();
    item.label = prefix;
    item.desc = "Emmet: expands (SiblingFeature) → " + html.replace("\n", " ");
    item.commit = html;
    items.add(item);
    return items;
  }

  // ======================= CORE ENGINE =======================
  private String expand(String code) {
    Node root = parseTree(code);
    return build(root);
  }

  private Node parseTree(String input) {
    Node root = new Node("div"); // root نمایشی
    Node current = root;
    StringBuilder token = new StringBuilder();

    for (int i = 0; i < input.length(); i++) {
      char ch = input.charAt(i);
      if (ch == '+') {
        if (token.length() > 0) {
          current.addChild(cleanTag(token.toString())); // sibling اضافه می‌شود
          token.setLength(0);
        }
        // current ثابت می‌ماند، چون sibling است
      } else {
        token.append(ch);
      }
    }

    if (token.length() > 0) current.addChild(cleanTag(token.toString()));

    return root;
  }

  private String build(Node n) {
    StringBuilder html = new StringBuilder();
    if (n.name.equals("div") && n.children.size() > 0) {
      // root صرفاً container است، فقط children را build می‌کنیم
      for (Node child : n.children) html.append(build(child));
    } else {
      html.append("<").append(n.name).append(">");
      for (Node child : n.children) html.append("\n").append(build(child));
      html.append("</").append(n.name).append(">\n");
    }
    return html.toString();
  }

  private String cleanTag(String input) {
    String cleaned = input.replaceAll("[<>/]", "").trim();
    return cleaned.isEmpty() ? "div" : cleaned;
  }

  // ======================= MODEL =======================
  static class Node {
    String name;
    List<Node> children = new ArrayList<>();

    Node(String name) {
      this.name = name;
    }

    Node addChild(String n) {
      Node child = new Node(n);
      children.add(child);
      return child;
    }

    List<Node> getChildren() {
      return children;
    }
  }
}
