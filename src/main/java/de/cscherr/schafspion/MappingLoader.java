package de.cscherr.schafspion;

import de.cscherr.schafspion.SchafSpion;
import org.slf4j.Logger;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MappingLoader {
    private static final Logger LOG = SchafSpion.LOG;
    private final Map<String, String> classMap = new HashMap<>();
    private final Map<String, String> fieldMap = new HashMap<>();

    /**
     * Loads mappings from a Tiny v2 format file in resources
     */
    public void loadMappings(String resourcePath) {
        LOG.info("Attempting to load mappings from: " + resourcePath);

        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                LOG.error("Could not find mapping file: " + resourcePath);
                return;
            }

            BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8)
            );

            // First line contains header and namespaces
            String headerLine = reader.readLine();
            LOG.info("Header line: '" + headerLine + "'");

            if (headerLine == null) {
                LOG.error("Mapping file is empty");
                return;
            }

            String[] headerParts = headerLine.split("\t");
            if (headerParts.length < 5 || !headerParts[0].equals("tiny") || !headerParts[1].equals("2")) {
                LOG.error("Invalid header format: " + headerLine);
                throw new IllegalArgumentException("Not a Tiny v2 mapping file");
            }

            // Find namespace indices (they start at index 3 in the header)
            int intermediaryIdx = -1;
            int namedIdx = -1;

            for (int i = 3; i < headerParts.length; i++) {
                LOG.info("Checking namespace [" + (i-3) + "]: '" + headerParts[i] + "'");
                if ("intermediary".equals(headerParts[i])) {
                    intermediaryIdx = i - 3;
                    LOG.info("Found intermediary at offset " + intermediaryIdx);
                }
                if ("named".equals(headerParts[i])) {
                    namedIdx = i - 3;
                    LOG.info("Found named at offset " + namedIdx);
                }
            }

            if (intermediaryIdx == -1 || namedIdx == -1) {
                LOG.error("Missing required namespaces. intermediary: " + intermediaryIdx + ", named: " + namedIdx);
                throw new IllegalArgumentException("Missing required namespaces");
            }

            // Process mappings
            String line;
            String currentClass = null;
            int lineCount = 1;

            while ((line = reader.readLine()) != null) {
                lineCount++;
                if (line.isEmpty()) continue;

                String[] parts = line.split("\t", -1);
                LOG.debug("Line {}: '{}' -> {} parts", lineCount, line, parts.length);

                if (parts[0].equals("c")) {
                    if (parts.length > Math.max(intermediaryIdx, namedIdx) + 1) {
                        String obfClass = parts[intermediaryIdx + 1];
                        String mappedClass = parts[namedIdx + 1];
                        classMap.put(obfClass, mappedClass);
                        currentClass = obfClass;
                        LOG.debug("Mapped class {} -> {}", obfClass, mappedClass);
                    }
                } else if (parts[0].equals("\tf") || parts[0].equals("f")) {
                    if (currentClass != null && parts.length > Math.max(intermediaryIdx, namedIdx) + 1) {
                        // Skip descriptor
                        String obfField = parts[intermediaryIdx + 1];
                        String mappedField = parts[namedIdx + 1];
                        fieldMap.put(currentClass + ":" + obfField, mappedField);
                        LOG.debug("Mapped field {}.{} -> {}", currentClass, obfField, mappedField);
                    }
                }
            }

            LOG.info("Processed {} lines", lineCount);
            LOG.info("Loaded {} class mappings and {} field mappings",
                classMap.size(), fieldMap.size());

        } catch (Exception e) {
            LOG.error("Failed to load mappings from " + resourcePath, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the mapped name for a class
     */
    public String getMappedClassName(String obfuscatedName) {
        // Convert to format used in mappings (net.minecraft.class_XXX -> net/minecraft/class_XXX)
        String lookupName = obfuscatedName.replace('.', '/');
        String mapped = classMap.get(lookupName);
        if (mapped != null) {
            // Get the simple class name
            String simpleName = mapped.substring(mapped.lastIndexOf('/') + 1);
            LOG.debug("Mapped class {} -> {}", lookupName, simpleName);
            return simpleName;
        }
        return obfuscatedName.substring(obfuscatedName.lastIndexOf('.') + 1);
    }

    /**
     * Gets the mapped name for a field
     */
    public String getMappedFieldName(String className, String fieldName) {
        if (!fieldName.startsWith("field_")) {
            return fieldName; // Already mapped or not an obfuscated field
        }

        String lookupKey = className.replace('.', '/') + ":" + fieldName;
        String mapped = fieldMap.get(lookupKey);
        if (mapped != null) {
            LOG.debug("Mapped field {} -> {}", lookupKey, mapped);
            return mapped;
        }
        return fieldName;
    }
}
