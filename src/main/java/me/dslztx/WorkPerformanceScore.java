package me.dslztx;

import java.io.File;
import java.math.BigDecimal;

import org.apache.commons.configuration2.Configuration;

import me.dslztx.assist.util.ArrayAssist;
import me.dslztx.assist.util.ConfigLoadAssist;

/**
 * S=0.25*(min(100,JN0*6+JN1*3+JN2*2+JN3*2))+0.6(min(100,WH*0.3))+min(15,PJN*2) -(TJD0*2+TJD1*1+TJD2*0.25)-JFScore
 */
public class WorkPerformanceScore {

    File dataFile;

    public WorkPerformanceScore(File dataFile) {
        this.dataFile = dataFile;
    }

    public static void main(String[] args) {
        if (ArrayAssist.isEmpty(args) || args.length != 1) {
            throw new RuntimeException("specify data file");
        }

        File file = new File(args[0]);
        if (!file.exists()) {
            throw new RuntimeException("data file not exist");
        }

        WorkPerformanceScore workPerformanceScore = new WorkPerformanceScore(file);

        System.out.println(workPerformanceScore.calculate());
    }

    public double calculate() {

        int jn0 = 0;
        int jn1 = 0;
        int jn2 = 0;
        int jn3 = 0;
        int wh = 0;
        int pjn = 0;
        int tjd0 = 0;
        int tjd1 = 0;
        int tjd2 = 0;

        int jfScore = 0;

        Configuration configuration = ConfigLoadAssist.propConfig(dataFile.getAbsolutePath(), "UTF-8");

        try {
            jn0 = configuration.getInt("JN0");
            jn1 = configuration.getInt("JN1");
            jn2 = configuration.getInt("JN2");
            jn3 = configuration.getInt("JN3");

            wh = configuration.getInt("WH");

            pjn = configuration.getInt("PJN");

            tjd0 = configuration.getInt("TJD0");
            tjd1 = configuration.getInt("TJD1");
            tjd2 = configuration.getInt("TJD2");

            jfScore = configuration.getInt("JFScore");
        } catch (Exception e) {
            throw new RuntimeException("");
        }

        // S=0.25*(min(100,JN0*6+JN1*3+JN2*2+JN3*2))+0.6(min(100,WH*0.3))+min(15,PJN*2)
        // -(TJD0*2+TJD1*1+TJD2*0.25)-JFScore
        double score = 0.25 * Math.min(100, jn0 * 6 + jn1 * 3 + jn2 * 2 + jn3 * 2) + 0.6 * (Math.min(100, wh * 0.3))
            + Math.min(15, pjn * 2) - (tjd0 * 2 + tjd1 * 1 + tjd2 * 0.25) - jfScore;

        BigDecimal scoreBigDecimal = new BigDecimal(score);

        return scoreBigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

}
