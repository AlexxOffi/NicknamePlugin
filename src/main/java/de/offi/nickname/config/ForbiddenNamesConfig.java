package de.offi.nickname.config;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ForbiddenNamesConfig {
    private List<String> names = new ArrayList<>(List.of("Admin", "Moderator", "Staff", "Owner"));
    private List<String> patterns = new ArrayList<>(List.of(".*fuck.*", ".*shit.*"));

    public ForbiddenNamesConfig() {}

    public List<String> getNames() { return names; }
    public void setNames(List<String> names) { this.names = names; }

    public List<String> getPatterns() { return patterns; }
    public void setPatterns(List<String> patterns) { this.patterns = patterns; }

    public boolean isForbidden(String name) {
        String lower = name.toLowerCase();
        if (names.stream().anyMatch(n -> n.equalsIgnoreCase(name))) return true;
        return patterns.stream().anyMatch(p -> Pattern.matches(p.toLowerCase(), lower));
    }
}