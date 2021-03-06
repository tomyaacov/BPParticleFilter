package il.ac.bgu.cs.bp.samplebpjsproject;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CSVUtils {

    private static final String DEFAULT_SEPARATOR = ",";

    public static void writeLine(Writer w, List<String> values) throws IOException {
        writeLine(w, values, DEFAULT_SEPARATOR, ' ');
    }

    public static void writeLine(Writer w, List<String> values, String separators) throws IOException {
        writeLine(w, values, separators, ' ');
    }

    //https://tools.ietf.org/html/rfc4180
    private static String followCVSformat(String value) {

        String result = value;
        if (result.contains("\"")) {
            result = result.replace("\"", "\"\"");
        }
        return result;

    }

    public static void writeLine(Writer w, List<String> values, String separators, char customQuote) throws IOException {

        boolean first = true;

        //default customQuote is empty

        if (separators.equals(" ")) {
            separators = DEFAULT_SEPARATOR;
        }

        StringBuilder sb = new StringBuilder();
        for (String value : values) {
            if (!first) {
                sb.append(separators);
            }
            if (customQuote == ' ') {
                sb.append(followCVSformat(value));
            } else {
                sb.append(customQuote).append(followCVSformat(value)).append(customQuote);
            }

            first = false;
        }
        sb.append("\n");
        w.append(sb.toString());

    }

    public static void writeResults(String csvFile, List<Double> values) throws IOException{
        FileWriter writer = new FileWriter(csvFile);
        CSVUtils.writeLine(writer, values.stream().map(Objects::toString).collect(Collectors.toList()), System.lineSeparator());
        writer.flush();
        writer.close();
    }
    public static void writeResults(String csvFile, String value) throws IOException{
        FileWriter writer = new FileWriter(csvFile, true);
        writer.write(value);
        writer.flush();
        writer.close();
    }


}
