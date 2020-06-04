package utils;

import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class UtilFuntions {

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
