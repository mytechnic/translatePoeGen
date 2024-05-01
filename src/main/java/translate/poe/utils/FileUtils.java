package translate.poe.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class FileUtils {

    public static void save(String fileName, String buffer) {
        File file = new File("./" + fileName);

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        if (file.exists()) {
            file.delete();
        }

        try {
            FileWriter fileWriter = new FileWriter(file);
            BufferedWriter writer = new BufferedWriter(fileWriter);
            writer.write(buffer);
            writer.close();
            fileWriter.close();
        } catch (Exception e) {
            throw new RuntimeException("파일 쓰기 에러: " + e.getMessage());
        }
    }
}
