package dev.chirag45.breeze_core.outbox;

import com.clerk.backend_api.Clerk;
import com.clerk.backend_api.models.components.User;
import dev.chirag45.breeze_core.dto.translation.ProfileSnapshot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ClerkProfileClient {

    private final Clerk clerk;
    private final boolean configured;

    public ClerkProfileClient(
            Clerk clerk,
            @Value("${breeze.clerk.secret-key:}") String secretKey
    ) {
        this.clerk = clerk;
        this.configured = !secretKey.isBlank();
    }

    public boolean isConfigured() {
        return configured;
    }

    public ProfileSnapshot fetch(String clerkUserId) {
        User user = clerk.users()
                .get(clerkUserId)
                .user()
                .orElseThrow(() ->
                        new IllegalStateException("Clerk returned an empty user response"));

        if (!clerkUserId.equals(user.id())) {
            throw new IllegalStateException("Clerk returned a different user");
        }

        return mapToProfile(user);
    }

    private static ProfileSnapshot mapToProfile(User user) {
        String email = user.primaryEmailAddressId()
                .flatMap(primaryId ->
                        user.emailAddresses().stream()
                                .filter(address ->
                                        address.id()
                                                .filter(primaryId::equals)
                                                .isPresent())
                                .map(address -> address.emailAddress())
                                .findFirst())
                .orElse(null);

        String displayName = String.join(
                " ",
                user.firstName().orElse(""),
                user.lastName().orElse("")
        ).trim();

        if (displayName.isBlank()) {
            displayName = user.username().orElse(null);
        }

        return new ProfileSnapshot(
                checkedLength(email, 320),
                checkedLength(displayName, 255),
                checkedLength(user.imageUrl().orElse(null), 2048)
        );
    }

    private static String checkedLength(String value, int maxLength) {
        if (value != null && value.length() > maxLength) {
            throw new IllegalArgumentException(
                    "Clerk profile field exceeds Core column length"
            );
        }

        return value;
    }
}
