import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public class Main {
    static class Graph {
        private final Map<String, Map<String, Integer>> adjList = new HashMap<>();
        private Map<String, Double> pageRanks;
        private final Random random = new Random();

        // 添加边
        void addEdge(String src, String dest) {
            src = src.toLowerCase();
            dest = dest.toLowerCase();
            adjList.putIfAbsent(src, new HashMap<>());
            adjList.get(src).put(dest, adjList.get(src).getOrDefault(dest, 0) + 1);
            adjList.putIfAbsent(dest, new HashMap<>()); // 确保目标节点存在
        }

        // 获取所有节点
        Set<String> getNodes() {
            return adjList.keySet();
        }

        // 获取指定节点的出边
        Map<String, Integer> getEdges(String node) {
            return adjList.getOrDefault(node.toLowerCase(), Collections.emptyMap());
        }

        // 检查节点是否存在
        boolean containsNode(String node) {
            return adjList.containsKey(node.toLowerCase());
        }

        // 桥接查询
        List<String> queryBridgeWords(String word1, String word2) {
            word1 = word1.toLowerCase();
            word2 = word2.toLowerCase();
            List<String> bridges = new ArrayList<>();

            if (!containsNode(word1)) return Collections.singletonList("No " + word1);
            if (!containsNode(word2)) return Collections.singletonList("No " + word2);

            Map<String, Integer> word1Edges = getEdges(word1);
            for (String bridge : word1Edges.keySet()) {
                if (getEdges(bridge).containsKey(word2)) {
                    bridges.add(bridge);
                }
            }
            return bridges;
        }

        // 生成新文本
        String generateNewText(String input) {
            List<String> words = processText(input);
            List<String> result = new ArrayList<>();

            for (int i = 0; i < words.size() - 1; i++) {
                String current = words.get(i);
                String next = words.get(i + 1);
                result.add(current);

                List<String> bridges = queryBridgeWords(current, next);
                if (!bridges.isEmpty() && !bridges.getFirst().startsWith("No")) {
                    String selected = bridges.get(random.nextInt(bridges.size()));
                    result.add(selected);
                }
            }
            result.add(words.getLast());
            return String.join(" ", result);
        }

        // 最短路径（Dijkstra算法）
        List<String> shortestPath(String start, String end) {
            start = start.toLowerCase();
            end = end.toLowerCase();

            Map<String, Integer> dist = new HashMap<>();
            Map<String, String> prev = new HashMap<>();
            PriorityQueue<String> queue = new PriorityQueue<>(
                    Comparator.comparingInt(node -> dist.getOrDefault(node, Integer.MAX_VALUE))
            );

            for (String node : adjList.keySet()) {
                dist.put(node, Integer.MAX_VALUE);
            }
            dist.put(start, 0);
            queue.add(start);

            while (!queue.isEmpty()) {
                String current = queue.poll();
                if (current.equals(end)) break;

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
            for (String at = end; at != null; at = prev.get(at)) {
                path.addFirst(at);
            }
            return path.getFirst().equals(start) ? path : Collections.emptyList();
        }

        // PageRank计算
        void calculatePageRank() {
            int N = adjList.size();
            pageRanks = new HashMap<>();
            Map<String, Double> tempPR = new HashMap<>();

            // 初始化PR值
            double initialPR = 1.0 / N;
            adjList.keySet().forEach(node -> pageRanks.put(node, initialPR));

            for (int i = 0; i < 20; i++) {
                tempPR.clear();
                double sum = 0.0;

                for (String node : adjList.keySet()) {
                    double pr = (1 - 0.85) / N;
                    for (String inNode : getInNodes(node)) {
                        int outDegree = getEdges(inNode).size();
                        if (outDegree > 0) {
                            pr += 0.85 * pageRanks.get(inNode) / outDegree;
                        }
                    }
                    tempPR.put(node, pr);
                    sum += pr;
                }

                // 归一化
                final double total = sum;
                tempPR.replaceAll((k, v) -> v / total);
                pageRanks = new HashMap<>(tempPR);
            }
        }

        // 获取入边节点
        private Set<String> getInNodes(String node) {
            return adjList.keySet().stream()
                    .filter(n -> getEdges(n).containsKey(node))
                    .collect(Collectors.toSet());
        }

        // 随机游走
        List<String> randomWalk() {
            List<String> path = new ArrayList<>();
            Set<String> visitedEdges = new HashSet<>();

            List<String> nodes = new ArrayList<>(adjList.keySet());
            String current = nodes.get(random.nextInt(nodes.size()));
            path.add(current);

            while (true) {
                Map<String, Integer> edges = getEdges(current);
                if (edges.isEmpty()) break;

                List<String> candidates = new ArrayList<>(edges.keySet());
                String next = candidates.get(random.nextInt(candidates.size()));
                String edge = current + "->" + next;

                if (visitedEdges.contains(edge)) break;
                visitedEdges.add(edge);

                path.add(next);
                current = next;
            }
            return path;
        }
    }

    // 文本预处理
    static List<String> processText(String text) {
        return Arrays.stream(text.replaceAll("[^a-zA-Z ]", "").toLowerCase()
                        .split("\\s+"))
                .filter(word -> !word.isEmpty())
                .collect(Collectors.toList());
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        String path = "src/Easy Test.txt";

        String content = new String(Files.readAllBytes(Paths.get(path)));
        List<String> words = processText(content);
        Graph graph = new Graph();
        for (int i = 0; i < words.size() - 1; i++) {
            graph.addEdge(words.get(i), words.get(i+1));
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
            int choice = -1; // 初始化为无效值
            boolean validInput = false;

            // 循环直到输入有效数字
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
                    scanner.nextLine(); // 清除无效输入
                }
            }
            scanner.nextLine(); // 消费换行符（避免影响后续输入）

            switch (choice) {
                case 1:
                    showDirectedGraph(graph);
                    break;
                case 2:
                    System.out.print("Enter two words (separated by space): ");
                    String[] wordsInput = scanner.nextLine().split(" ");
                    List<String> bridges = graph.queryBridgeWords(wordsInput[0], wordsInput[1]);
                    handleBridgeOutput(bridges, wordsInput[0], wordsInput[1]);
                    break;
                case 3:
                    System.out.print("Enter new text: ");
                    String inputText = scanner.nextLine();
                    System.out.println("Generated: " + graph.generateNewText(inputText));
                    break;
                case 4:
                    System.out.print("Enter two words (separated by space): ");
                    String[] pathWords = scanner.nextLine().split(" ");
                    List<String> path1 = graph.shortestPath(pathWords[0], pathWords[1]);
                    System.out.println(path1.isEmpty() ? "No path" : "Path: " + String.join("→", path1));
                    break;
                case 5:
                    graph.calculatePageRank();
                    System.out.println("PageRank Values:");
                    graph.pageRanks.forEach((k, v) -> System.out.printf("%s: %.4f\n", k, v));
                    break;
                case 6:
                    List<String> walk = graph.randomWalk();
                    System.out.println("Random Walk: " + String.join(" → ", walk));
                    try (PrintWriter out = new PrintWriter("random_walk.txt")) {
                        out.println(String.join(" ", walk));
                    }
                    break;
                case 0:
                    return;
            }
        }
    }

    private static void handleBridgeOutput(List<String> bridges, String w1, String w2) {
        if (bridges.isEmpty()) {
            System.out.println("No bridge words from " + w1 + " to " + w2);
        } else if (bridges.getFirst().startsWith("No")) {
            System.out.println(bridges.getFirst() + " in the graph!");
        } else {
            StringJoiner sj = new StringJoiner(", ");
            bridges.forEach(sj::add);
            System.out.println("The bridge words from" + w1 + "to" + w2 + "is:" + sj);
        }
    }

    static void showDirectedGraph(Graph graph) {
        System.out.println("\nDirected Graph:");
        graph.getNodes().forEach(node -> {
            System.out.print(node + " → ");
            graph.getEdges(node).forEach((k, v) -> System.out.print(k + "(" + v + ") "));
            System.out.println();
        });
    }
}