import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"unchecked"})
public class LocalAnalyzerTest {
    private static Class<?> courseAnalyzerClass;
    private static Object courseInfo;

    @BeforeAll
    static void setUp() {
        try {
            courseAnalyzerClass = Class.forName("OnlineCoursesAnalyzer");
            checkDeclarations();
            Constructor<?> constructor = courseAnalyzerClass.getDeclaredConstructor(String.class);
            if (constructor.getModifiers() != Modifier.PUBLIC) {
                throw new NoSuchMethodException("The constructor from class OnlineCoursesAnalyzer is not public!");
            }
            courseInfo = constructor.newInstance("resources/local.csv");
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException |
                 IllegalAccessException | NoSuchMethodException e) {
            e.printStackTrace();
            fail();
        }
    }

    static void checkDeclarations() {
        MethodEntity[] movieAnalyzerMethods = {
                new MethodEntity("getPtcpCountByInst", Map.class),
                new MethodEntity("getPtcpCountByInstAndSubject", Map.class),
                new MethodEntity("getCourseListOfInstructor", Map.class),
                new MethodEntity("getCourses", List.class, int.class, String.class),
                new MethodEntity("searchCourses", List.class, String.class, double.class, double.class),
                new MethodEntity("recommendCourses", List.class, int.class, int.class, int.class)
        };
        List<String> errorMessages = new ArrayList<>();
        for (MethodEntity m : movieAnalyzerMethods) {
            if (!m.checkForClass(courseAnalyzerClass)) {
                errorMessages.add("The method [" + m + "] from class OnlineCoursesAnalyzer does not exist!");
            }
        }
        assertTrue(errorMessages.isEmpty(), String.join(System.lineSeparator(), errorMessages));
    }

    <K, V> String mapToString(Object obj) {
        Map<K, V> map = (Map<K, V>) obj;
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            sb.append(entry.getKey());
            sb.append(" == ");
            sb.append(entry.getValue());
            sb.append("\n");
        }
        if (sb.length() == 0) return "";
        return sb.substring(0, sb.length() - 1).strip();
    }

    String listToString(Object obj) {
        List<String> list = (List<String>) obj;
        StringBuilder sb = new StringBuilder();
        for (String s : list) {
            sb.append(s);
            sb.append("\n");
        }
        if (sb.length() == 0) return "";
        return sb.substring(0, sb.length() - 1).strip();
    }

    <K, V> boolean compareMapWithoutOrder(Object obj, String expected) {
        Map<K, V> objMap = (Map<K, V>) obj;
        List<Item<K, V>> expectedList = new ArrayList<>();
        String[] rows = expected.split("\n");
        for (String row : rows) {
            String[] strings = row.split(" == ");
            expectedList.add((Item<K, V>) new Item<>(strings[0], strings[1]));
        }
        if (objMap.size() != expectedList.size()) {
            return false;
        }
        for (Map.Entry<K, V> entry : objMap.entrySet()) {
            Item<K, V> item = (Item<K, V>) new Item<>(entry.getKey().toString(), entry.getValue().toString());
            if (!expectedList.contains(item)) {
                return false;
            }
        }
        return true;
    }

    @Test
    void testGetPtcpCountByInst() {
        try {
            Method method = courseAnalyzerClass.getMethod("getPtcpCountByInst");
            Object res = method.invoke(courseInfo);
            assertTrue(res instanceof Map<?, ?>);
            String expected = Files.readString(Paths.get("resources", "local_answer", "Q1.txt"),
                            StandardCharsets.UTF_8)
                    .replace("\r", "").strip();
            assertEquals(expected, mapToString(res));
        } catch (NoSuchMethodException | InvocationTargetException |
                 IllegalAccessException | IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void testGetPtcpCountByInstAndSubject() {
        try {
            Method method = courseAnalyzerClass.getMethod("getPtcpCountByInstAndSubject");
            Object res = method.invoke(courseInfo);
            assertTrue(res instanceof Map<?, ?>);
            String expected = Files.readString(Paths.get("resources", "local_answer", "Q2.txt"),
                            StandardCharsets.UTF_8)
                    .replace("\r", "").strip();
            assertEquals(expected, mapToString(res));
        } catch (NoSuchMethodException | InvocationTargetException |
                 IllegalAccessException | IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void testGetCourseListOfInstructor() {
        try {
            Method method = courseAnalyzerClass.getMethod("getCourseListOfInstructor");
            Object res = method.invoke(courseInfo);
            assertTrue(res instanceof Map<?, ?>);
            String expected = Files.readString(Paths.get("resources", "local_answer", "Q3.txt"),
                            StandardCharsets.UTF_8)
                    .replace("\r", "").strip();
            assertTrue(compareMapWithoutOrder(res, expected));
        } catch (NoSuchMethodException | InvocationTargetException |
                 IllegalAccessException | IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void testGetCourses() {
        try {
            Method method = courseAnalyzerClass.getMethod("getCourses", int.class, String.class);
            Object res1 = method.invoke(courseInfo, 10, "hours");
            assertTrue(res1 instanceof List<?>);
            String expected1 = Files.readString(Paths.get("resources", "local_answer", "Q4_1.txt"),
                            StandardCharsets.UTF_8)
                    .replace("\r", "").strip();
            assertEquals(expected1, listToString(res1));
            Object res2 = method.invoke(courseInfo, 15, "participants");
            assertTrue(res2 instanceof List<?>);
            String expected2 = Files.readString(Paths.get("resources", "local_answer", "Q4_2.txt"),
                            StandardCharsets.UTF_8)
                    .replace("\r", "").strip();
            assertEquals(expected2, listToString(res2));
        } catch (NoSuchMethodException | InvocationTargetException |
                 IllegalAccessException | IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void testSearchCourses() {
        try {
            Method method = courseAnalyzerClass.getMethod("searchCourses", String.class, double.class, double.class);
            Object res1 = method.invoke(courseInfo, "computer", 20.0, 700);
            assertTrue(res1 instanceof List<?>);
            String expected1 = Files.readString(Paths.get("resources", "local_answer", "Q5_1.txt"),
                            StandardCharsets.UTF_8)
                    .replace("\r", "").strip();
            assertEquals(expected1, listToString(res1));
            Object res2 = method.invoke(courseInfo, "SCIENCE", 25.0, 400);
            assertTrue(res2 instanceof List<?>);
            String expected2 = Files.readString(Paths.get("resources", "local_answer", "Q5_2.txt"),
                            StandardCharsets.UTF_8)
                    .replace("\r", "").strip();
            assertEquals(expected2, listToString(res2));
        } catch (NoSuchMethodException | InvocationTargetException |
                 IllegalAccessException | IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void testRecommendCourses() {
        try {
            Method method = courseAnalyzerClass.getMethod("recommendCourses", int.class, int.class, int.class);
            Object res1 = method.invoke(courseInfo, 25, 1, 1);
            assertTrue(res1 instanceof List<?>);
            String expected1 = Files.readString(Paths.get("resources", "local_answer", "Q6_1.txt"),
                            StandardCharsets.UTF_8)
                    .replace("\r", "").strip();
            assertEquals(expected1, listToString(res1));
            Object res2 = method.invoke(courseInfo, 30, 0, 1);
            assertTrue(res2 instanceof List<?>);
            String expected2 = Files.readString(Paths.get("resources", "local_answer", "Q6_2.txt"),
                            StandardCharsets.UTF_8)
                    .replace("\r", "").strip();
            assertEquals(expected2, listToString(res2));
            Object res3 = method.invoke(courseInfo, 35, 1, 0);
            assertTrue(res3 instanceof List<?>);
            String expected3 = Files.readString(Paths.get("resources", "local_answer", "Q6_3.txt"),
                            StandardCharsets.UTF_8)
                    .replace("\r", "").strip();
            assertEquals(expected3, listToString(res3));
        } catch (NoSuchMethodException | InvocationTargetException |
                 IllegalAccessException | IOException e) {
            e.printStackTrace();
            fail();
        }
    }

}
