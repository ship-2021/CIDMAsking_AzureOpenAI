package runners;

import org.junit.Test;
import org.junit.runner.JUnitCore;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

//public class RepeatTest {
//
//    @Test
//    public void runCucumberScenarioMultipleTimes() throws IOException {
//        for (int i = 1; i <= 2; i++) {
//            System.out.println("====== Execution #" + i + " ======");
//            JUnitCore.runClasses(TestRunner.class);
//
//            // Rename each generated report to a unique file
//            File src = new File("target/cucumber-report.json");
//            File dest = new File("target/reports/cucumber-report-" + i + ".json");
//            Files.createDirectories(dest.getParentFile().toPath());
//            Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
//        }
//    }
//}
