import java.io.*;
import java.util.*;

public class run_test {

    public static void main(String[] args) {
        String baseDir =  System.getProperty("user.dir");
        String inputFolder = baseDir + "/testing/input";
        String outputFolder = baseDir + "/testing/curr_output";
        String expectedOutputFolder = baseDir + "/testing/expected_output";
        String lexerJarPath = baseDir + "/LEXER";

        // Create curr_output directory if it doesn't exist
        File currOutputDir = new File(outputFolder);
        if (!currOutputDir.exists()) {
            currOutputDir.mkdirs();
        }

        File inputDir = new File(inputFolder);
        File[] inputFiles = inputDir.listFiles();
        if (inputFiles == null) {
            System.out.println("Input folder is empty or does not exist.");
            return;
        }

        List<String> comparisonResults = new ArrayList<>();
        for (File inputFile : inputFiles) {
            if (inputFile.isFile()) {
                String fileName = inputFile.getName();
                fileName = fileName.substring(0, fileName.length() - 4); // Remove file extension
                String outputFilePath = outputFolder + "/" + fileName + "_Output.txt";
                String expectedOutputPath = expectedOutputFolder + "/" + fileName + "_Expected_Output.txt";

                try {
                    // Run the lexer on the input file
                    Process process = new ProcessBuilder(
                        "java", "-jar", lexerJarPath, inputFile.getAbsolutePath(), outputFilePath
                    ).start();
                    process.waitFor();

                    File generatedOutputFile = new File(outputFilePath);

                    if (generatedOutputFile.exists()) {
                        // If the generated output contains "ERROR", compare it to the string "ERROR"
                        if (generatedOutputFileContainsError(generatedOutputFile)) {
                            comparisonResults.add(fileName + ": MATCH (ERROR)");
                        } else {
                            // Normal comparison between expected and generated outputs
                            File expectedOutputFile = new File(expectedOutputPath);
                            if (expectedOutputFile.exists()) {
                                boolean areEqual = filesAreEqual(expectedOutputFile, generatedOutputFile);
                                comparisonResults.add(fileName + ": " + (areEqual ? "MATCH" : "DIFFERENT"));
                            } else {
                                comparisonResults.add(fileName + ": MISSING EXPECTED OUTPUT FILE");
                            }
                        }
                    } else {
                        comparisonResults.add(fileName + ": MISSING GENERATED OUTPUT FILE");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // Print all comparison results
        for (String result : comparisonResults) {
            System.out.println(result);
        }
    }

    // Check if the generated output file contains "ERROR"
    private static boolean generatedOutputFileContainsError(File generatedOutputFile) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(generatedOutputFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("ERROR")) {
                    return true;  // If any line contains "ERROR", consider it as an error output
                }
            }
        }
        return false;
    }

    // Method to compare two files
    private static boolean filesAreEqual(File file1, File file2) throws IOException {
        try (BufferedReader br1 = new BufferedReader(new FileReader(file1));
             BufferedReader br2 = new BufferedReader(new FileReader(file2))) {
            String line1 = null, line2 = null;
            while (true) {
                line1 = br1.readLine();
                line2 = br2.readLine();

                // Normalize lines by trimming whitespace
                if (line1 != null) line1 = line1.trim();
                if (line2 != null) line2 = line2.trim();

                // Compare the normalized lines
                if (line1 == null && line2 == null) {
                    // Both files ended, so they're the same
                    return true;
                }
                if (line1 == null || line2 == null || !line1.equals(line2)) {
                    // Files differ
                    return false;
                }
            }
        }
    }
}
