package me.dslztx;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import lombok.extern.slf4j.Slf4j;
import me.dslztx.assist.util.ClassPathResourceAssist;

@Slf4j
public class WorkPerformanceScoreTest {

    @Test
    public void main() {
        try {
            File file = ClassPathResourceAssist.locateFileNotInJar("perf.data");

            Assert.assertTrue(new WorkPerformanceScore(file).calculate() > 1.0D);

        } catch (Exception e) {
            log.error("", e);
            Assert.fail();
        }
    }
}