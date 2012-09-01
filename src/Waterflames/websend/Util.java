package Waterflames.websend;

public class Util {
    public static String stringArrayToString(String[] strings){
        StringBuilder buffer = new StringBuilder();
        for(int i = 0;i<strings.length;i++){
            buffer.append(strings[i]);
        }
        return buffer.toString();
    }
}
