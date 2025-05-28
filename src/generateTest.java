import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class generateTest {
  private void runTestCase(int caseNumber) throws IOException {
    File tempFile = null;
    try {
      tempFile = File.createTempFile("testCase" + caseNumber, ".txt");
      
      Main.Graph graph = new Main.Graph();
      String path = "src/Easy Test.txt";
      String content = Files.readString(Paths.get(path), StandardCharsets.UTF_8);
      List<String> words = Main.processText(content);
      for (int i = 0; i < words.size() - 1; i++) {
        graph.addEdge(words.get(i), words.get(i + 1));
      }
      
      switch (caseNumber) {
        case 1:
          String result1 = graph.generateNewText("hello");
          assertTrue(result1.contains("hello"), "直接输出");
          System.out.println("测试1通过");
          break;
        case 2:
          String result2 = graph.generateNewText("The scientist analyzed the data");
          assertTrue(result2.contains(
              "the scientist carefully analyzed the data"), "插入桥接词");
          System.out.println("测试2通过");
          break;
        case 3:
          String result3 = graph.generateNewText("shared the report with the team");
          assertTrue(result3.contains("shared the report with the team"), "无桥接词");
          System.out.println("测试3通过");
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
}