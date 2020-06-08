package utils;

import org.json.JSONObject;
import simEngine.LinearFct;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UtilsFuntions {

    private static Pattern pattern = Pattern.compile("(\\d*\\.?\\d*)\\*?t\\+(\\d*\\.?\\d*)", Pattern.MULTILINE);

    public static LinearFct parseLinearFct(String costFctStr) {
        LinearFct c = null;
        Matcher m = pattern.matcher(costFctStr);
        m.find();
        try {
            Float a = Float.valueOf(m.group(1));
            Float b = Float.valueOf(m.group(2));
            c = new LinearFct(a, b);
        } catch (Exception e) {
            System.err.println("Something went wrong while decoding the linear function!");
            e.printStackTrace();
        }
        return c;
    }

    // Read file content into string with - Files.readAllBytes(Path path)
    // https://howtodoinjava.com/java/io/java-read-file-to-string-examples/
    public static String readAllBytesJava7(String filePath) throws IOException {
        String content = "";
        content = new String(Files.readAllBytes(Paths.get(filePath)));
        return content;
    }

    public static JSONObject readFileAsJSON(String filePath) throws Exception {
        String jsonConfigString = null;
        try {
            jsonConfigString = readAllBytesJava7(filePath);
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception("Loading the json file was not successful!");
        }
        return new JSONObject(jsonConfigString);
    }
}
