package org.barrelmancer.civilization.configuration;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ServerConfiguration {
    public static String getString(String configName, String path, Map<String, Object> placeholders) {
        FileConfiguration config = Configuration.getConfiguration(configName);
        String message = config.getString(path);

        if (message == null) {
            return "Missing: " + configName + "." + path;
        }

        return format(message, placeholders);
    }

    public static String getString(String configName, String path) {
        return getString(configName, path, Map.of());
    }

    public static int getInt(String configName, String path) {
        FileConfiguration config = Configuration.getConfiguration(configName);

        if (config.contains(path)) {
            if (config.isInt(path)) {
                return config.getInt(path);
            }
            String value = config.getString(path);
            if (value != null) {
                try {
                    return Integer.parseInt(value.trim());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Cannot parse '" + value + "' as integer in " + configName + "." + path);
                }
            }
        }

        throw new IllegalArgumentException("Missing integer value: " + configName + "." + path);
    }

    public static List<Integer> getIntList(String configName, String path) {
        FileConfiguration config = Configuration.getConfiguration(configName);
        List<?> rawList = config.getList(path);

        if (rawList == null) {
            throw new IllegalArgumentException("Missing or invalid list: " + configName + "." + path);
        }

        return rawList.stream()
                .map(obj -> {
                    if (obj instanceof Integer) {
                        return (Integer) obj;
                    } else if (obj instanceof String) {
                        try {
                            return Integer.parseInt((String) obj);
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("Cannot parse '" + obj + "' as integer in " + configName + "." + path);
                        }
                    } else {
                        throw new IllegalArgumentException("Invalid type in integer list: " + obj.getClass().getSimpleName() + " in " + configName + "." + path);
                    }
                })
                .collect(Collectors.toList());
    }
    public static double getDouble(String configName, String path) {
        FileConfiguration config = Configuration.getConfiguration(configName);

        if (config.contains(path)) {
            if (config.isDouble(path)) {
                return config.getDouble(path);
            }
            String value = config.getString(path);
            if (value != null) {
                try {
                    return Double.parseDouble(value.trim());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Cannot parse '" + value + "' as double in " + configName + "." + path);
                }
            }
        }

        throw new IllegalArgumentException("Missing double value: " + configName + "." + path);
    }

    public static Set<String> getKeys(String configName, String path) {
        FileConfiguration config = Configuration.getConfiguration(configName);
        ConfigurationSection section = config.getConfigurationSection(path);

        if (section == null) {
            return Set.of();
        }

        return section.getKeys(false);
    }
    public static String format(String text, Map<String, Object> placeholders) {
        String result = text;

        for (Map.Entry<String, Object> entry : placeholders.entrySet()) {
            String placeholder = "%" + entry.getKey() + "%";
            String value = String.valueOf(entry.getValue());
            result = result.replace(placeholder, value);
        }
        return result;
    }
}