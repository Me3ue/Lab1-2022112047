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
            adjList.putIfAbsent(dest, new HashMap<>());
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
        String queryBridgeWords(String word1, String word2) {
            word1 = word1.toLowerCase();
            word2 = word2.toLowerCase();
            List<String> bridges = new ArrayList<>();

            if (!containsNode(word1)) return "No " + word1 + " in the graph!";
            if (!containsNode(word2)) return "No " + word2 + " in the graph!";

            Map<String, Integer> word1Edges = getEdges(word1);
            for (String bridge : word1Edges.keySet()) {
                if (getEdges(bridge).containsKey(word2)) {
                    bridges.add(bridge);
                }
            }

            if (bridges.isEmpty()) {
                return "No bridge words from " + word1 + " to " + word2 + "!";
            } else {
                StringJoiner sj = new StringJoiner(", ");
                bridges.forEach(sj::add);
                return "The bridge words from " + word1 + " to " + word2 + " are: " + sj.toString();
            }
        }

        // 生成新文本
        String generateNewText(String inputText) {
            List<String> words = processText(inputText);
            List<String> result = new ArrayList<>();

            for (int i = 0; i < words.size() - 1; i++) {
                String current = words.get(i);
                String next = words.get(i + 1);
                result.add(current);

                List<String> bridges = new ArrayList<>();
                if (containsNode(current) && containsNode(next)) {
                    Map<String, Integer> word1Edges = getEdges(current);
                    for (String bridge : word1Edges.keySet()) {
                        if (getEdges(bridge).containsKey(next)) {
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

        // 最短路径（Dijkstra算法）
        String calcShortestPath(String word1, String word2) {
            word1 = word1.toLowerCase();
            word2 = word2.toLowerCase();

            if (!containsNode(word1) || !containsNode(word2)) {
                return "No path exists!";
            }

            Map<String, Integer> dist = new HashMap<>();
            Map<String, String> prev = new HashMap<>();
            PriorityQueue<String> queue = new PriorityQueue<>(
                    Comparator.comparingInt(node -> dist.getOrDefault(node, Integer.MAX_VALUE))
            );

            for (String node : adjList.keySet()) {
                dist.put(node, Integer.MAX_VALUE);
            }
            dist.put(word1, 0);
            queue.add(word1);

            while (!queue.isEmpty()) {
                String current = queue.poll();
                if (current.equals(word2)) break;

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

            if (path.getFirst().equals(word1)) {
                return "The shortest path is: " + String.join("→", path) +
                        " with length " + dist.get(word2);
            } else {
                return "No path exists!";
            }
        }

        // PageRank计算
        Double calPageRank(String word) {
            calculatePageRank(); // 确保先计算PageRank
            return pageRanks.getOrDefault(word.toLowerCase(), 0.0);
        }

        private void calculatePageRank() {
            if (pageRanks != null) return; // 已经计算过则不再重复计算

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

        // 随机游走
        String randomWalk() {
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

            try (PrintWriter out = new PrintWriter("random_walk.txt")) {
                out.println(String.join(" ", path));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            return "Random walk: " + String.join(" → ", path);
        }

        // 获取入边节点
        private Set<String> getInNodes(String node) {
            return adjList.keySet().stream()
                    .filter(n -> getEdges(n).containsKey(node))
                    .collect(Collectors.toSet());
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
                    while (true) {
                        System.out.print("Enter a word: ");
                        String word = scanner.nextLine().toLowerCase();
                        if (graph.containsNode(word)) {
                            System.out.println("PageRank of " + word + ": " + graph.calPageRank(word));
                            break;
                        } else {
                            System.out.println("Word '" + word + "' not found in graph. Please try again.");
                        }
                    }
                    break;
                case 6:
                    System.out.println(graph.randomWalk());
                    break;
                case 0:
                    return;
            }
        }
    }

    static void showDirectedGraph(Graph G) {
        System.out.println("\nDirected Graph:");
        G.getNodes().forEach(node -> {
            Map<String, Integer> edges = G.getEdges(node);
            if (!edges.isEmpty()) {  // 只有当节点有出边时才显示
                System.out.print(node + " → ");
                edges.forEach((k, v) -> System.out.print(k + "(" + v + ") "));
                System.out.println();
            }
        });
    }
}