import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OnlineCoursesAnalyzer {

    Supplier<Stream<Course>> courseStreamGenerator;
    Set<String> courseNumbersSet = new HashSet<>();
    Set<String> instructorsSet = new HashSet<>();
    String csvFile;

    public OnlineCoursesAnalyzer(String csvFile) {
        this.csvFile = csvFile;
        courseStreamGenerator = () -> {
            try {
                return Files.lines(Paths.get(this.csvFile)).skip(1).map(a -> {
                    try {
                        return new Course(a);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public Map<String, Integer> getPtcpCountByInst() {
        return courseStreamGenerator.get().sorted((c1, c2) -> {
            if (c1.institution.compareTo(c2.institution) == 1) {
                return 1;
            } else if (c1.institution.compareTo(c2.institution) == 0) {
                return 0;
            } else {
                return -1;
            }
        }).collect(Collectors.groupingBy(course -> course.institution, Collectors.summingInt(c -> c.participant)));
    }

    public Map<String, Integer> getPtcpCountByInstAndSubject() {
        return courseStreamGenerator.get()
            .collect(Collectors.groupingBy(
                course -> course.institution + "-" + course.courseSubject, Collectors.summingInt(course -> course.participant))).entrySet().stream()
            .sorted((o1, o2) -> {
                if (o1.getValue() != o2.getValue()) {
                    return Integer.compare(o2.getValue(), o1.getValue());
                } else {
                    return o1.getKey().compareTo(o2.getKey());
                }
            }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
                LinkedHashMap::new));
    }

    public Map<String, List<List<String>>> getCourseListOfInstructor() {
        List<Course> courses = courseStreamGenerator.get()
            .sorted(Comparator.comparing(o -> o.courseTitle)).toList();
        return instructorsSet.stream()
            .collect(Collectors.toMap(s -> s, new Function<String, List<List<String>>>() {

                public boolean contain(Course c, String s) {
                    String[] strings = c.instructors.split(",");
                    for (String string : strings) {
                        if (string.trim().equals(s)) {
                            return true;
                        }
                    }
                    return false;
                }

                @Override
                public List<List<String>> apply(String s) {
                    List<List<String>> containCourse = new ArrayList<>(5);

                    List<String> independent = new ArrayList<>();
                    List<String> notIndependent = new ArrayList<>();

                    for (Course cours : courses) {
                        if (this.contain(cours, s)) {
                            if (cours.isIndependent) {
                                independent.add(cours.courseTitle);
                            } else {
                                notIndependent.add(cours.courseTitle);
                            }
                        }
                    }
                    containCourse.add(independent.stream().distinct().toList());
                    containCourse.add(notIndependent.stream().distinct().toList());
                    return containCourse;
                }
            }));
    }

    //4
    public List<String> getCourses(int topK, String by) {
        return switch (by) {
            case "hours" -> courseStreamGenerator.get()
                .sorted((o1, o2) -> o2.totalCourseHour.compareTo(o1.totalCourseHour))
                .map(c -> c.courseTitle).distinct().limit(topK).collect(Collectors.toList());
            case "participants" -> courseStreamGenerator.get()
                .sorted((o1, o2) -> o2.participant.compareTo(o1.participant))
                .map(c -> c.courseTitle).distinct().limit(topK).collect(Collectors.toList());
            default -> null;
        };
    }

    //5
    public List<String> searchCourses(String courseSubject, double percentAudited,
        double totalCourseHours) {
        return courseStreamGenerator.get()
            .filter(c -> c.courseSubject.toLowerCase().contains(courseSubject.toLowerCase()))
            .filter(c -> c.auditedProportion >= percentAudited)
            .filter(c -> c.totalCourseHour <= totalCourseHours)
            .sorted(Comparator.comparing(course -> course.courseTitle)).map(c -> c.courseTitle)
            .distinct().toList();
    }

    //6
    public List<String> recommendCourses(int age, int gender, int isBachelorOrHigher) {
        List<Course> courses = courseStreamGenerator.get().toList();
        Map<String, Course> numCourse = courseNumbersSet.stream()
            .collect(Collectors.toMap(s -> s, s -> {
                List<Course> sameCourse = new ArrayList<>();
                courses.forEach(c -> {
                    if (c.courseNumber.equals(s)) {
                        sameCourse.add(c);
                    }
                });
                sameCourse.sort((o1, o2) -> o2.launchDate.compareTo(o1.launchDate));
                double averageMedianAge = 0;
                double averageMale = 0;
                double averageIsB = 0;
                for (Course course : sameCourse) {
                    averageMedianAge += course.medianAge;
                    averageMale += course.maleProportion;
                    averageIsB += course.bachelorDegreeOrHigherProportion;
                }
                sameCourse.get(0).averageMedianAge = averageMedianAge / sameCourse.size();
                sameCourse.get(0).averageMale = averageMale / sameCourse.size();
                sameCourse.get(0).averageisB = averageIsB / sameCourse.size();
                return sameCourse.get(0);
            }));
        numCourse.values()
            .forEach(course -> course.similarity = Math.pow(age - course.averageMedianAge, 2) + Math.pow(
                gender * 100 - course.averageMale, 2) + Math.pow(
                isBachelorOrHigher * 100 - course.averageisB, 2));
        return numCourse.entrySet().stream().map(e -> e.getValue())
            .sorted(new Comparator<>() {
                @Override
                public int compare(Course o1, Course o2) {
                    if (o1.similarity != o2.similarity) {
                        return Double.compare(o1.similarity, o2.similarity);
                    } else {
                        return o1.courseTitle.compareTo(o2.courseTitle);
                    }
                }
            }).map(c -> c.courseTitle).distinct().limit(10).collect(Collectors.toList());
    }


    class Course {

        double averageMedianAge;
        double averageMale;
        double averageisB;
        double similarity;
        boolean isIndependent;
        String institution;
        String courseNumber;
        Date launchDate;
        String courseTitle;
        String instructors;
        String courseSubject;
        Integer year;
        Integer honorCodeCertificates;
        Integer participant;
        Integer auditedNum;
        Integer certifiedNum;
        Double auditedProportion;
        Double certifiedProportion;
        Double certifiedAmongAuditedProportion;
        Double playedVideo;
        Double postedInForum;
        Double gradeHigherThanZero;
        Double totalCourseHour;
        Double medianHoursForCertification;
        Double medianAge;
        Double maleProportion;
        Double femaleProportion;
        Double bachelorDegreeOrHigherProportion;

        public Course(String string) throws ParseException {
            String[] strs = string.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
            SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");

            this.institution = strs[0];
            this.courseNumber = strs[1];
            this.launchDate = format.parse(strs[2]);
            this.courseTitle = strs[3].replace("\"", "").trim();
            this.instructors = strs[4].replace("\"", "");
            this.courseSubject = strs[5].replace("\"", "");
            this.year = Integer.parseInt(strs[6]);
            this.honorCodeCertificates = Integer.parseInt(strs[7]);
            this.participant = Integer.parseInt(strs[8]);
            this.auditedNum = Integer.parseInt(strs[9]);
            this.certifiedNum = Integer.parseInt(strs[10]);
            this.auditedProportion = Double.parseDouble(strs[11]);
            this.certifiedProportion = Double.parseDouble(strs[12]);
            this.certifiedAmongAuditedProportion = Double.parseDouble(strs[13]);
            this.playedVideo = Double.parseDouble(strs[14]);
            this.postedInForum = Double.parseDouble(strs[15]);
            this.gradeHigherThanZero = Double.parseDouble(strs[16]);
            this.totalCourseHour = Double.parseDouble(strs[17]);
            this.medianHoursForCertification = Double.parseDouble(strs[18]);
            this.medianAge = Double.parseDouble(strs[19]);
            this.maleProportion = Double.parseDouble(strs[20]);
            this.femaleProportion = Double.parseDouble(strs[21]);
            this.bachelorDegreeOrHigherProportion = Double.parseDouble(strs[22]);

            courseNumbersSet.add(this.courseNumber);
            String[] instructors = this.instructors.split(",");
            for (String instructor : instructors) {
                if (!instructor.contains("(")) {
                    instructorsSet.add(instructor.trim());
                }
            }
            if (this.instructors.contains(",")) {
                this.isIndependent = false;
            } else {
                this.isIndependent = true;
            }
        }
    }
}

