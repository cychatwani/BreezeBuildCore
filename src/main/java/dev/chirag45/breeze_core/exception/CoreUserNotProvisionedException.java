package dev.chirag45.breeze_core.exception;

public class CoreUserNotProvisionedException extends RuntimeException {

    public CoreUserNotProvisionedException() {
        super("The Clerk user has not been provisioned in Breeze Core.");
    }
}
