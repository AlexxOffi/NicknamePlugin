package de.offi.nickname.config;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    private final Path dataFolder;
    private final DumperOptions dumperOptions;
    private final LoaderOptions loaderOptions;

    public ConfigManager(Path dataFolder) {
        this.dataFolder = dataFolder;

        this.dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dumperOptions.setPrettyFlow(true);
        dumperOptions.setIndent(2);

        this.loaderOptions = new LoaderOptions();
    }

    public <T> T load(String filename, Class<T> clazz) {
        Path file = dataFolder.resolve(filename);

        if (!Files.exists(file)) {
            try {
                T defaultConfig = clazz.getDeclaredConstructor().newInstance();
                save(filename, defaultConfig);
                return defaultConfig;
            } catch (Exception e) {
                throw new RuntimeException("Failed to create default config", e);
            }
        }

        try (Reader reader = Files.newBufferedReader(file)) {
            Constructor constructor = new Constructor(clazz, loaderOptions);
            Yaml yaml = new Yaml(constructor);
            return yaml.load(reader);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config: " + filename, e);
        }
    }

    public <T> void save(String filename, T config) {
        try {
            Files.createDirectories(dataFolder);
            Path file = dataFolder.resolve(filename);

            Representer representer = new Representer(dumperOptions);
            representer.getPropertyUtils().setSkipMissingProperties(true);
            // Entfernt den !!classname Tag
            representer.addClassTag(config.getClass(), org.yaml.snakeyaml.nodes.Tag.MAP);
            
            Yaml yaml = new Yaml(representer, dumperOptions);

            try (Writer writer = Files.newBufferedWriter(file)) {
                yaml.dump(config, writer);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save config: " + filename, e);
        }
    }


    public void reload() {
        // Wird vom Plugin aufgerufen
    }
}