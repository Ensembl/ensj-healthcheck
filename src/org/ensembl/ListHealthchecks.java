package org.ensembl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.ensembl.healthcheck.GroupOfTests;
import org.ensembl.healthcheck.testcase.EnsTestCase;

import com.google.gson.Gson;

import uk.co.flamingpenguin.jewel.cli.ArgumentValidationException;
import uk.co.flamingpenguin.jewel.cli.CliFactory;
import uk.co.flamingpenguin.jewel.cli.Option;

public class ListHealthchecks {

    public interface ListHealthchecksOptions {

        @Option(helpRequest = true, description = "display help")
        boolean getHelp();

        @Option(shortName = "o", longName = "output_file", defaultValue="hc_list.json", description = "File to write HCs to")
        String getOutputFile();

        @Option(shortName = "c", longName = "class_type", defaultValue = "test", description = "File to write HCs to")
        String getClassType();

        @Option(shortName = "p", defaultValue = "org.ensembl.healthcheck", longName = "package", description = "File to write HCs to")
        String getPackage();

        @Option(shortName = "v", longName = "verbose", description = "Show detailed debugging output")
        boolean isVerbose();
    }

    public static void main(String[] args) {
        ListHealthchecksOptions opts = null;
        try {
            opts = CliFactory.parseArguments(ListHealthchecksOptions.class, args);
        } catch (ArgumentValidationException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        Class<?> targetClass = getClass(opts.getClassType());

        try {
            List<Class<?>> classesForPackage = PackageScan.getClassesForPackage(opts.getPackage(), true);
            List<String> noms = classesForPackage.stream()
                    .filter(c -> !Modifier.isAbstract(c.getModifiers()) && targetClass.isAssignableFrom(c))
                    .map(Class::getCanonicalName)
                    .collect(Collectors.toList());
            FileUtils.writeStringToFile(new File(opts.getOutputFile()), new Gson().toJson(noms), Charset.defaultCharset().toString());
        } catch (ClassNotFoundException e) {
            System.err.println(e.getMessage());
            System.exit(2);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(3);
        }
    }

    public static Class<?> getClass(String name) {
        Class<?> targetClass = null;
        switch (name) {
        case "test":
        case "testcase":
            targetClass = EnsTestCase.class;
            break;
        case "group":
        case "testgroup":
            targetClass = GroupOfTests.class;
            break;
        default:
            try {
                targetClass = Class.forName(name);
            } catch (ClassNotFoundException e1) {
                System.err.println("Class " + name + " not found");
                System.exit(1);
            }
            break;
        }
        return targetClass;
    }

}
