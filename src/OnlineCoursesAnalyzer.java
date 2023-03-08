import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class OnlineCoursesAnalyzer {

    Supplier<Stream<Course>> courseStreamGenerator;
    String csvFile;
    public OnlineCoursesAnalyzer(String csvFile){
        this.csvFile = csvFile;
        courseStreamGenerator = ()->{
            try {
                return Files.lines(Paths.get(this.csvFile))
                        .map(a -> new Course(a));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }



}
class Course{
    String Institution;
    Integer courseNumber;
    String launchDate;
    String courseTitle;
    String instructors;
    String courseSubject;
    Integer year;
    Integer honorCodeCertificates;
    Integer Participant;
    Integer AuditedNum;
    Integer CertifiedNum;
    Double auditedProportion;
    Double certifiedProportion;
    Double certifiedAmongAuditedProportion;
    Double playedVideo;
    Double postedInForum;
    Double gradeHigherThanZero;
    Double totalCourseHour;
    Double medianHoursForCertification;
    Integer medianAge;
    Double maleProportion;
    Double femaleProportion;
    Double bachelorDegreeOrHigherProportion;

    public Course(String string){
        String[]strs = string.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
        this.Institution = strs[0];
        this.courseNumber = Integer.parseInt(strs[1]);
        this.launchDate = strs[2];
        this.courseTitle = strs[3];
        this.instructors = strs[4];
        this.courseSubject = strs[5];
        this.year = Integer.parseInt(strs[6]);
        this.honorCodeCertificates = Integer.parseInt(strs[7]);
        this.Participant = Integer.parseInt(strs[8]);
        this.AuditedNum = Integer.parseInt(strs[9]);
        this.CertifiedNum = Integer.parseInt(strs[10]);
        this.auditedProportion = Double.parseDouble(strs[11]);
        this.certifiedProportion = Double.parseDouble(strs[12]);
        this.certifiedAmongAuditedProportion = Double.parseDouble(strs[13]);
        this.playedVideo = Double.parseDouble(strs[14]);
        this.postedInForum = Double.parseDouble(strs[15]);
        this.gradeHigherThanZero = Double.parseDouble(strs[16]);
        this.totalCourseHour = Double.parseDouble(strs[17]);
        this.medianHoursForCertification = Double.parseDouble(strs[18]);
        this.medianAge = Integer.parseInt(strs[19]);
        this.maleProportion = Double.parseDouble(strs[20]);
        this.femaleProportion = Double.parseDouble(strs[21]);
        this.bachelorDegreeOrHigherProportion = Double.parseDouble(strs[22]);
    }
}
