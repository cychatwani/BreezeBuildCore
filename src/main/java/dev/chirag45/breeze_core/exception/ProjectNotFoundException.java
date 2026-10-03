package dev.chirag45.breeze_core.exception;

public class ProjectNotFoundException extends RuntimeException {

    public ProjectNotFoundException() {
        super("Project was not found.");
    }
}
