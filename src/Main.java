import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;


/**
  * 主类，提供文本处理和图算法的入口点.
  * 支持构建单词关系图、查询桥接词、生成新文本、计算最短路径、PageRank计算和随机游走等功能。
  */
public class Main {
  /**
      * 表示文本单词关系的图结构，使用邻接表存储单词之间的关系.
   * 支持多种图算法操作，包括桥接词查询、最短路径计算等。
      */
  static class Graph {
    /**
        * 图的邻接表表示，记录节点间的边关系.
     * Key为源节点，Value为目标节点及其出现次数的映射。
        */
    private final Map<String, Map<String, Integer>> adjList = new HashMap<>();
    
    /**
        * 存储各节点的PageRank值.
     */
    private Map<String, Double> pageRanks;
    
    /**
        * 随机数生成器，用于随机选择和桥接词插入等操作.
     */
    private final SecureRandom random = new SecureRandom();
    
    /**
        * 向图中添加边关系.
     *
         * @param src 源节点/单词
     * @param dest 目标节点/单词
     */
    void addEdge(String src, String dest) {
      src = src.toLowerCase();
      dest = dest.toLowerCase();
      adjList.putIfAbsent(src, new HashMap<>());
      adjList.get(src).put(dest, adjList.get(src).getOrDefault(dest, 0) + 1);
      adjList.putIfAbsent(dest, new HashMap<>());
    }
    
    /**
        * 获取图中的所有节点.
     *
         * @return 包含所有节点的集合
     */
    Set<String> getNodes() {
      return adjList.keySet();
    }
    
    /**
        * 获取指定节点的出边关系.
     *
         * @param node 要查询的节点
     * @return 该节点的出边映射(目标节点→出现次数)，如果节点不存在返回空映射
     */
    Map<String, Integer> getEdges(String node) {
      return adjList.getOrDefault(node.toLowerCase(), Collections.emptyMap());
    }
    
    /**
        * 检查图中是否包含指定节点.
     *
         * @param node 要检查的节点
     * @return 如果图中不包含该节点返回true，否则返回false
     */
    boolean containsNode(String node) {
      return !adjList.containsKey(node.toLowerCase());
    }
    
    /**
        * 查询两个单词之间的桥接词.
     *
         * @param word1 第一个单词
     * @param word2 第二个单词
     * @return 桥接词字符串结果
     */
    String queryBridgeWords(String word1, String word2) {
      word1 = word1.toLowerCase();
      word2 = word2.toLowerCase();
      List<String> bridges = new ArrayList<>();
      
      if (containsNode(word1)) {
        return "No " + word1;
      }
      if (containsNode(word2)) {
        return "No " + word2;
      }
      
      Map<String, Integer> word1Edges = getEdges(word1);
      for (String bridge : word1Edges.keySet()) {
        if (getEdges(bridge).containsKey(word2)) {
          bridges.add(bridge);
        }
      }
      
      if (bridges.isEmpty()) {
        return "No bridge words from " + word1 + " to " + word2;
      } else {
        StringJoiner sj = new StringJoiner(", ");
        bridges.forEach(sj::add);
        return "The bridge words from " + word1 + " to " + word2 + " are: " + sj;
      }
    }
    
    /**
        * 根据输入文本生成包含桥接词的新文本.
     *
         * @param inputText 输入文本
     * @return 生成的新文本，可能在相邻单词间插入了桥接词
     */
    String generateNewText(String inputText) {
      List<String> words = processText(inputText);
      List<String> result = new ArrayList<>();
      for (int i = 0; i < words.size() - 1; i++) {
        String current = words.get(i);
        String next = words.get(i + 1);
        result.add(current);
        List<String> bridges = new ArrayList<>();
        String word1 = current;
        String word2 = next;
        word1 = word1.toLowerCase();
        word2 = word2.toLowerCase();
        if (!containsNode(word1) && !containsNode(word2)) {
          Map<String, Integer> word1Edges = getEdges(word1);
          for (String bridge : word1Edges.keySet()) {
            if (getEdges(bridge).containsKey(word2)) {
              bridges.add(bridge);
            }
          }
        }
        if (!bridges.isEmpty()) {
          String selected = bridges.get(random.nextInt(bridges.size()));
          result.add(selected);
        }
      }
      result.add(words.getLast());
      return String.join(" ", result);
    }
    
    /**
        * 计算两个节点之间的最短路径（使用Dijkstra算法）.
        *
        * @param word1 起始节点
     * @param word2 目标节点
     * @return 最短路径的字符串表示
     */
    String calcShortestPath(String word1, String word2) {
      word1 = word1.toLowerCase();
      word2 = word2.toLowerCase();
      
      Map<String, Integer> dist = new HashMap<>();
      
      PriorityQueue<String> queue = new PriorityQueue<>(
          Comparator.comparingInt(node -> dist.getOrDefault(node, Integer.MAX_VALUE))
      );
      
      for (String node : adjList.keySet()) {
        dist.put(node, Integer.MAX_VALUE);
      }
      dist.put(word1, 0);
      queue.add(word1);
      Map<String, String> prev = new HashMap<>();
      while (!queue.isEmpty()) {
        String current = queue.poll();
        if (current.equals(word2)) {
          break;
        }
        
        for (Map.Entry<String, Integer> edge : getEdges(current).entrySet()) {
          String neighbor = edge.getKey();
          int newDist = dist.get(current) + edge.getValue();
          if (newDist < dist.getOrDefault(neighbor, Integer.MAX_VALUE)) {
            dist.put(neighbor, newDist);
            prev.put(neighbor, current);
            queue.add(neighbor);
          }
        }
      }
      
      List<String> path = new LinkedList<>();
      for (String at = word2; at != null; at = prev.get(at)) {
        path.addFirst(at);
      }
      return path.getFirst().equals(word1) ? String.join("→", path) : "No path";
    }
    
    /**
        * 计算图中所有节点的PageRank值.
     * 使用迭代算法计算，默认迭代20次，阻尼系数为0.85。
        */
    void calculatePageRank() {
      int n = adjList.size();
      pageRanks = new HashMap<>();
      Map<String, Double> tempPr = new HashMap<>();
      
      double initialPr = 1.0 / n;
      adjList.keySet().forEach(node -> pageRanks.put(node, initialPr));
      
      for (int i = 0; i < 20; i++) {
        tempPr.clear();
        double sum = 0.0;
        
        for (String node : adjList.keySet()) {
          double pr = (1 - 0.85) / n;
          for (String inNode : getInNodes(node)) {
            int outDegree = getEdges(inNode).size();
            if (outDegree > 0) {
              pr += 0.85 * pageRanks.get(inNode) / outDegree;
            }
          }
          tempPr.put(node, pr);
          sum += pr;
        }
        
        final double total = sum;
        tempPr.replaceAll((k, v) -> v / total);
        pageRanks = new HashMap<>(tempPr);
      }
    }
    
    /**
        * 获取指定节点的PageRank值.
     *
         * @param word 目标节点
     * @return 该节点的PageRank值
     */
    Double calPageRank(String word) {
      if (pageRanks == null) {
        calculatePageRank();
      }
      return pageRanks.getOrDefault(word.toLowerCase(), 0.0);
    }
    
    /**
        * 获取指向指定节点的所有入边节点.
     *
         * @param node 目标节点
     * @return 包含所有指向该节点的源节点的集合
     */
    private Set<String> getInNodes(String node) {
      return adjList.keySet().stream()
          .filter(n -> getEdges(n).containsKey(node))
          .collect(Collectors.toSet());
    }
    
    /**
        * 在图上执行随机游走，生成节点序列.
     *
         * @return 随机游走经过的节点字符串
     */
    String randomWalk() {
      List<String> path = new ArrayList<>();
      Set<String> visitedEdges = new HashSet<>();
      
      List<String> nodes = new ArrayList<>(adjList.keySet());
      String current = nodes.get(random.nextInt(nodes.size()));
      path.add(current);
      
      while (true) {
        Map<String, Integer> edges = getEdges(current);
        if (edges.isEmpty()) {
          break;
        }
        
        List<String> candidates = new ArrayList<>(edges.keySet());
        String next = candidates.get(random.nextInt(candidates.size()));
        String edge = current + "->" + next;
        
        if (visitedEdges.contains(edge)) {
          break;
        }
        visitedEdges.add(edge);
        
        path.add(next);
        current = next;
      }
      return String.join(" → ", path);
    }
  }
  
  /**
      * 对输入文本进行预处理，转换为单词列表.
   *
       * @param text 输入文本
   * @return 处理后的单词列表（已转换为小写并去除标点）
      */
  static List<String> processText(String text) {
    return Arrays.stream(text.replaceAll("[^a-zA-Z ]", "").toLowerCase()
            .split("\\s+"))
        .filter(word -> !word.isEmpty())
        .collect(Collectors.toList());
  }
  
  /**
      * 主程序入口，提供交互式菜单驱动界面.
   *
       * @param args 命令行参数（未使用）
      * @throws IOException 当文件读取失败时抛出
   */
  public static void main(String[] args) throws IOException {
    Scanner scanner = new Scanner(new InputStreamReader(System.in, StandardCharsets.UTF_8));
    String path = "src/Easy Test.txt";
    
    String content = Files.readString(Paths.get(path), StandardCharsets.UTF_8);
    List<String> words = processText(content);
    Graph graph = new Graph();
    for (int i = 0; i < words.size() - 1; i++) {
      graph.addEdge(words.get(i), words.get(i + 1));
    }
    
    while (true) {
      System.out.println("""
                    
                    1. Show Graph
                    2. Query Bridge Words
                    3. Generate New Text\
                    
                    4. Shortest Path
                    5. Calculate PageRank
                    6. Random Walk
                    0. Exit""");
      int choice = -1;
      boolean validInput = false;
      
      while (!validInput) {
        System.out.print("Choose function (0-6): ");
        try {
          choice = scanner.nextInt();
          if (choice >= 0 && choice <= 6) {
            validInput = true;
          } else {
            System.out.println("Error: Input must be between 0 and 6.");
          }
        } catch (InputMismatchException e) {
          System.out.println("Error: Please enter a number!");
          scanner.nextLine();
        }
      }
      scanner.nextLine();
      
      switch (choice) {
        case 1:
          showDirectedGraph(graph);
          break;
        case 2:
          System.out.print("Enter two words (separated by space): ");
          String[] wordsInput = scanner.nextLine().split(" ");
          System.out.println(graph.queryBridgeWords(wordsInput[0], wordsInput[1]));
          break;
        case 3:
          System.out.print("Enter new text: ");
          String inputText = scanner.nextLine();
          System.out.println("Generated: " + graph.generateNewText(inputText));
          break;
        case 4:
          System.out.print("Enter two words (separated by space): ");
          String[] pathWords = scanner.nextLine().split(" ");
          System.out.println(graph.calcShortestPath(pathWords[0], pathWords[1]));
          break;
        case 5:
          graph.calculatePageRank();
          System.out.println("PageRank Values:");
          graph.pageRanks.forEach((k, v) -> System.out.printf("%s: %.4f%n", k, v));
          System.out.print("Enter a word to get its PageRank: ");
          String word = scanner.nextLine();
          System.out.println("PageRank of " + word + ": " + graph.calPageRank(word));
          break;
        case 6:
          String walk = graph.randomWalk();
          System.out.println("Random Walk: " + walk);
          try (PrintWriter out = new PrintWriter("random_walk.txt", StandardCharsets.UTF_8)) {
            out.println(walk.replace(" → ", " "));
          }
          break;
        case 0:
          return;
        default:
      }
    }
  }
  
  /**
      * 显示有向图的结构.
   *
       * @param graph 要显示的图对象
   */
  static void showDirectedGraph(Graph graph) {
    System.out.println("\nDirected Graph:");
    graph.getNodes().forEach(node -> {
      System.out.print(node + " → ");
      graph.getEdges(node).forEach((k, v) -> System.out.print(k + "(" + v + ") "));
      System.out.println();
    });
  }
}