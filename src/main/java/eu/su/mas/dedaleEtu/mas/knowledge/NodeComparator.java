package eu.su.mas.dedaleEtu.mas.knowledge;

import java.util.Comparator;

public class NodeComparator implements Comparator<Node> {
  public int compare(Node n1, Node n2) {
    return n2.getTimesVisited() - n1.getTimesVisited();
  }
}
