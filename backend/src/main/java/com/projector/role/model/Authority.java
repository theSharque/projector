package com.projector.role.model;

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

    Authority(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Authority fromName(String name) {
        for (Authority authority : values()) {
            if (authority.name.equals(name)) {
                return authority;
            }
        }

        throw new IllegalArgumentException("Unknown authority: " + name);
    }
}
