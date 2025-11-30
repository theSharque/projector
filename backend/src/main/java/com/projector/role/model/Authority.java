package com.projector.role.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Authority {
    USER_VIEW("USER_VIEW"),
    USER_EDIT("USER_EDIT"),
    ROLE_VIEW("ROLE_VIEW"),
    ROLE_EDIT("ROLE_EDIT"),
    ROADMAP_VIEW("ROADMAP_VIEW"),
    ROADMAP_EDIT("ROADMAP_EDIT"),
    FEATURE_VIEW("FEATURE_VIEW"),
    FEATURE_EDIT("FEATURE_EDIT"),
    TASK_VIEW("TASK_VIEW"),
    TASK_EDIT("TASK_EDIT");

    private final String name;

    private static final Set<String> AUTHORITY_NAMES = Stream.of(values())
            .map(authority -> authority.name)
            .collect(Collectors.toUnmodifiableSet());
    private static final Map<String, Authority> NAME_TO_AUTHORITY = new HashMap<>();

    static {
        for (Authority authority : values()) {
            NAME_TO_AUTHORITY.put(authority.name, authority);
        }
    }

    Authority(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Authority fromName(String name) {
        if (!AUTHORITY_NAMES.contains(name)) {
            throw new IllegalArgumentException("Unknown authority: " + name);
        }
        return NAME_TO_AUTHORITY.get(name);
    }
}
