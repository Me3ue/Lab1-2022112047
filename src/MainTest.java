import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MainTest {
  private void runTestCase(int caseNumber) throws IOException {
    File tempFile = null;
    try {
      
      Main.Graph graph = new Main.Graph();
      String content = Files.readString(tempFile.toPath(), StandardCharsets.UTF_8);
      List<String> words = Main.processText(content);
      for (int i = 0; i < words.size() - 1; i++) {
        graph.addEdge(words.get(i), words.get(i + 1));
      }
      
      switch (caseNumber) {
        case 1:
          String result1 = graph.queryBridgeWords("scientist", "analyzed");
          assertTrue(result1.contains("carefully"), "应存在桥接词 carefully");
          assertEquals(4, graph.getNodes().size(), "节点数量验证");
          System.out.println("测试1通过");
          break;
        case 2:
          String result2 = graph.queryBridgeWords("analyzed", "data");
          assertTrue(result2.contains("No bridge words"), "直接连接不应有桥接词");
          System.out.println("测试2通过");
          break;
        case 3:
          String result3 = graph.queryBridgeWords("detailed", "report");
          assertTrue(result3.contains("No bridge words"), "直接连接不应有桥接词");
          System.out.println("测试3通过");
          break;
        case 4:
          String result4 = graph.queryBridgeWords("detailed", "zoo");
          assertEquals("No zoo", result4, "目标节点不存在验证");
          System.out.println("测试4通过");
          break;
        case 5:
          String result5 = graph.queryBridgeWords("@", "#");
          assertTrue(result5.startsWith("No"), "特殊字符处理验证");
          assertEquals(3, graph.getNodes().size(), "有效节点数量验证");
          System.out.println("测试5通过");
          break;
      }
    } finally {
      if (tempFile != null && tempFile.exists()) {
        tempFile.delete();
      }
    }
  }
  
  @Test
  void testCase1() throws IOException {
    runTestCase(1);
  }
  
  @Test
  void testCase2() throws IOException {
    runTestCase(2);
  }
  
  @Test
  void testCase3() throws IOException {
    runTestCase(3);
  }

  @Test
  void testCase4() throws IOException {
    runTestCase(4);
  }
  
  @Test
  void testCase5() throws IOException {
    runTestCase(5);
  }
}