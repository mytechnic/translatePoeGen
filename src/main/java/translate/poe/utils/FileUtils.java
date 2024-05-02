package translate.poe.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    public static void save(String fileName, String buffer) {
        File file = new File(fileName);

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

    public static String read(String fileName) {
        File file = new File(fileName);

        if (!file.getParentFile().exists()) {
            throw new RuntimeException("파일 경로 없음.");
        }

        if (!file.exists()) {
            throw new RuntimeException("파일을 찾을 수 없음.");
        }

        StringBuilder buffer = new StringBuilder();
        try {
            FileReader fileReader = new FileReader(file);
            int ch;
            while ((ch = fileReader.read()) != -1) {
                buffer.append((char) ch);
            }
            fileReader.close();
            return buffer.toString();
        } catch (Exception e) {
            throw new RuntimeException("파일 쓰기 에러: " + e.getMessage());
        }
    }

    public static List<String> readList(String fileName) {
        File file = new File(fileName);

        if (!file.exists()) {
            throw new RuntimeException("파일을 찾을 수 없음.");
        }

        List<String> bufferList = new ArrayList<>();
        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while (true) {
                String line = bufferedReader.readLine();
                if (line == null) {
                    break;
                }
                bufferList.add(line);
            }
            bufferedReader.close();
            fileReader.close();
            return bufferList;
        } catch (Exception e) {
            throw new RuntimeException("파일 쓰기 에러: " + e.getMessage());
        }
    }
}
