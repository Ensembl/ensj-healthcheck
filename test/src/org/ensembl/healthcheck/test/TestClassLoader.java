package org.ensembl.healthcheck.test;

import com.google.gson.Gson;
import org.ensembl.ListHealthchecks;
import org.ensembl.healthcheck.GroupOfTests;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class TestClassLoader {

    @Test
    public void testEmptyGroups() {
        System.out.println("main");
        Gson gson = new Gson();
        String homedir = System.getProperty("user.home");
        String fileName = homedir + "/hc_groups.json";
        String[] args = {"-c", "group", "-o", fileName};
        ListHealthchecks.main(args);
        Path filePath = Paths.get(fileName);
        try {
            BufferedReader br = new BufferedReader(Files.newBufferedReader(filePath, Charset.defaultCharset()));
            List<String> resultList = gson.fromJson(br, List.class);
            // System.out.print(resultList);
            for (String temp : resultList) {
                // System.out.println(temp);
                Class<?> targetClass = null;
                targetClass = Class.forName(temp);
                GroupOfTests tests = (GroupOfTests) targetClass.newInstance();
                Assert.assertFalse(tests.getSetOfTests().isEmpty());
            }
            Files.delete(filePath);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
