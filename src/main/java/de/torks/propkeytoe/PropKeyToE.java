package de.torks.propkeytoe;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.codehaus.plexus.util.PropertyUtils.loadProperties;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class PropKeyToE extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    @Parameter(property = "resourceFiles", readonly = true, required = true)
    String resourceFiles;

    @Parameter(property = "targetPackage", readonly = true)
    String targetPackage;

    Path targetDirectory;

    @Override
    public void execute() throws MojoFailureException {
        if (targetPackage == null)
            targetPackage = "";
        prepareTargetPath();

        for (String resource : resourceFiles.split(",")) {
            getLog().debug(String.format("starting with bundle '%s'", resource));

            File resourceFile = new File(project.getBasedir().getAbsolutePath() + "/src/main/resources/" + resource + ".properties");

            List<String> keys = getKeys(resourceFile);
            createEnum(resource, keys);
        }
    }

    private void prepareTargetPath() {
        File file = new File(project.getBasedir().getAbsolutePath() + "/target/generated-sources/property-key-to-enum");
        file.mkdirs();
        file.mkdir();
        project.addCompileSourceRoot(file.toString());
        targetDirectory = file.toPath();
    }

    private void createEnum(String resource, List<String> keys) throws MojoFailureException {
        String enumName = createEnumName(resource);
        String fileContent = createFileContent(enumName, keys);
        createFile(enumName, fileContent);
    }

    private String createEnumName(String resource) {
        return resource.substring(0, 1).toUpperCase() + resource.substring(1);
    }

    private String createFileContent(String enumName, List<String> keys) {
        StringBuilder builder = new StringBuilder();
        if (!targetPackage.isEmpty()) {
            builder.append("package ").append(targetPackage).append(";").append("\n")
                    .append("\n");
        }
        builder.append("public enum ").append(enumName).append(" {").append("\n");
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            boolean lastEntry = i == keys.size() - 1;

            String enumEntryName = createEnumEntryName(key);
            builder.append("    ")
                    .append(enumEntryName)
                    .append("(\"").append(key).append("\")");
            if (lastEntry)
                builder.append(";");
            else
                builder.append(",");
            builder.append("\n");
        }
        builder.append("    private final String key;").append("\n")
                .append("\n").append("    ").append(enumName).append("(String key) {").append("\n")
                .append("        this.key = key;").append("\n")
                .append("    }").append("\n")
                .append("\n")
                .append("    public String getKey() {").append("\n")
                .append("        return key;").append("\n")
                .append("    }").append("\n")
                .append("}").append("\n");

        return builder.toString();
    }

    private String createEnumEntryName(String key) {
        return key
                .replace(".", "_")
                .replace("-", "_")
                .toUpperCase();
    }

    private void createFile(String enumName, String fileContent) throws MojoFailureException {
        String packagePath;
        if (targetPackage.isEmpty())
            packagePath = "";
        else
            packagePath = targetPackage.replace(".", "/") + "/";

        String directory = targetDirectory.toString() + "/" + packagePath;
        new File(directory).mkdirs();
        File file = new File(directory + enumName + ".java");
        try {
            file.createNewFile();

            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(fileContent);

            writer.close();
        } catch (IOException e) {
            throw new MojoFailureException("Couldn't create source file", e);
        }
    }

    List<String> getKeys(File resourceFile) throws MojoFailureException {
        try {
            Properties properties = loadProperties(resourceFile);
            List<String> result = new ArrayList<>();
            for (Object key : properties.keySet())
                result.add(key.toString());
            return result;
        } catch (IOException e) {
            throw new MojoFailureException("Failed to load properties", e);
        }
    }
}
