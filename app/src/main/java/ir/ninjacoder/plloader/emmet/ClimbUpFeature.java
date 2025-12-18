package ir.ninjacoder.plloader.emmet;

import io.github.rosemoe.sora.data.CompletionItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/** ویژگی Climb-up (^) در Emmet. تمام منطق ^ داخل این کلاس است. */
public class ClimbUpFeature extends EmmetHelper {

  @Override
  public List<CompletionItem> listMethod(String prefix) {
    List<CompletionItem> items = new ArrayList<>();
    Node root = parseTree(prefix);
    String html = build(root);

    CompletionItem item = new CompletionItem();
    item.label = prefix;
    item.desc = "Emmet: expands(class ClimbUpFeature) → " + html.replace("\n", " ");
    item.commit = html;
    items.add(item);
    return items;
  }

  // ======================= CORE ENGINE =======================
  private Node parseTree(String input) {
    Node root = new Node("div"); // ریشه پیش‌فرض
    Node current = root;
    Stack<Node> stack = new Stack<>();
    stack.push(root);

    StringBuilder token = new StringBuilder();
    int multiplier = 1;

    for (int i = 0; i < input.length(); i++) {
      char ch = input.charAt(i);

      if (ch == '>') { // child
        Node child = current.addChild(token.toString(), multiplier);
        current = child;
        stack.push(current);
        token.setLength(0);
        multiplier = 1;
      } else if (ch == '+') { // sibling
        current.addSibling(token.toString(), multiplier);
        token.setLength(0);
        multiplier = 1;
      } else if (ch == '*') { // multiplier
        i++;
        StringBuilder number = new StringBuilder();
        while (i < input.length() && Character.isDigit(input.charAt(i++)))
          number.append(input.charAt(i - 1));
        i--;
        multiplier = Integer.parseInt(number.toString());
      } else if (ch == '^') { // climb-up
        int count = 1;
        while (i + 1 < input.length() && input.charAt(i + 1) == '^') {
          count++;
          i++;
        }
        // pop current to parent
        for (int j = 0; j < count && stack.size() > 1; j++) {
          stack.pop();
        }
        current = stack.peek();
        token.setLength(0);
        multiplier = 1;
      } else {
        token.append(ch);
      }
    }

    if (token.length() > 0) current.addChild(token.toString(), multiplier);
    return root.getChildren().get(0);
  }

  private String build(Node n) {
    StringBuilder html = new StringBuilder();
    for (int i = 0; i < n.multiplier; i++) {
      html.append("<").append(n.name).append(">");
      for (Node child : n.children) {
        html.append("\n").append(build(child));
      }
      html.append("</").append(n.name).append(">\n");
    }
    return html.toString();
  }

  // ======================= MODEL =======================
  static class Node {
    String name;
    int multiplier = 1;
    List<Node> children = new ArrayList<>();

    Node(String name) {
      this.name = name;
    }

    Node addChild(String n, int m) {
      Node child = new Node(n);
      child.multiplier = m;
      children.add(child);
      return child;
    }

    void addSibling(String n, int m) {
      Node sibling = new Node(n);
      sibling.multiplier = m;
      children.add(sibling);
    }

    List<Node> getChildren() {
      return children;
    }
  }
}
