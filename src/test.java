import java.util.List;
import java.util.Map;

public class test {
    public static void main(String[] args) {
        OnlineCoursesAnalyzer onlineCoursesAnalyzer = new OnlineCoursesAnalyzer("local.csv");
        Map<String,List<List<String>>> out = onlineCoursesAnalyzer.getCourseListOfInstructor();
        for (Map.Entry<String, List<List<String>>> stringListEntry : out.entrySet()) {
            System.out.println(stringListEntry.getKey()+" == "+stringListEntry.getValue());
        }
    }
}
