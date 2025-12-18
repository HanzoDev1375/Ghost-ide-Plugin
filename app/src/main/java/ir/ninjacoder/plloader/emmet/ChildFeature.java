package ir.ninjacoder.plloader.emmet;

import io.github.rosemoe.sora.data.CompletionItem;
import java.util.ArrayList;
import java.util.List;

/** کلاس مستقل برای ویژگی Child (>) تمام منطق این ویژگی داخل همین کلاس قرار دارد. */
class ChildFeature extends EmmetHelper {

  @Override
  public List<CompletionItem> listMethod(String prefix) {
    List<CompletionItem> items = new ArrayList<>();
    String html = expand(prefix);
    CompletionItem item = new CompletionItem();
    item.label = prefix;
    item.desc = "Emmet: expands (ChildFeature)→ " + html.replace("\n", " ");
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
    Node root = new Node("div");
    Node current = root;
    StringBuilder token = new StringBuilder();

    for (int i = 0; i < input.length(); i++) {
      char ch = input.charAt(i);
      if (ch == '>') {
        current = current.addChild(token.toString());
        token.setLength(0);
      } else {
        token.append(ch);
      }
    }

    if (token.length() > 0) current.addChild(token.toString());
    return root.getChildren().get(0);
  }

  private String build(Node n) {
    StringBuilder html = new StringBuilder();
    html.append("<").append(n.name).append(">");
    for (Node child : n.children) html.append("\n").append(build(child));
    html.append("</").append(n.name).append(">\n");
    return html.toString();
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
