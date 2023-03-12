import java.util.List;
import java.util.Map;

public class test {
    public static void main(String[] args) {
        OnlineCoursesAnalyzer onlineCoursesAnalyzer = new OnlineCoursesAnalyzer("resources/local.csv");
        onlineCoursesAnalyzer.recommendCourses(25,1,1);
    }
}
