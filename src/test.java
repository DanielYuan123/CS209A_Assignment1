public class test {
    public static void main(String[] args) {
        String s = "MITx,6.002x,09/05/2012,Circuits and Electronics,Khurram Afridi,\"Science, Technology, Engineering, and Mathematics\",1,1,36105,5431,3003,15.04,8.32,54.98,83.2,8.17,28.97,418.94,64.45,26,88.28,11.72,60.68";
        String[] strings = s.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
        System.out.println(strings.length);
        for (String string : strings) {
            System.out.println(string);
        }
    }
}
