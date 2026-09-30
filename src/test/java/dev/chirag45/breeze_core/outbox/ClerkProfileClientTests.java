package dev.chirag45.breeze_core.outbox;

import com.clerk.backend_api.Clerk;
import com.clerk.backend_api.Users;
import com.clerk.backend_api.models.components.EmailAddress;
import com.clerk.backend_api.models.components.User;
import com.clerk.backend_api.models.operations.GetUserResponse;
import dev.chirag45.breeze_core.dto.translation.ProfileSnapshot;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ClerkProfileClientTests {

    @Test
    void translatesSdkUserToInternalProfile() {
        Clerk clerk = mock(Clerk.class);
        Users users = mock(Users.class);
        GetUserResponse response = mock(GetUserResponse.class);
        User user = mock(User.class);
        EmailAddress other = mock(EmailAddress.class);
        EmailAddress primary = mock(EmailAddress.class);

        when(clerk.users()).thenReturn(users);
        when(users.get("user_test")).thenReturn(response);
        when(response.user()).thenReturn(Optional.of(user));
        when(user.id()).thenReturn("user_test");
        when(user.primaryEmailAddressId()).thenReturn(Optional.of("email_primary"));
        when(user.emailAddresses()).thenReturn(List.of(other, primary));
        when(other.id()).thenReturn(Optional.of("email_other"));
        when(primary.id()).thenReturn(Optional.of("email_primary"));
        when(primary.emailAddress()).thenReturn("primary@example.com");
        when(user.firstName()).thenReturn(Optional.of("Breeze"));
        when(user.lastName()).thenReturn(Optional.of("Builder"));
        when(user.imageUrl()).thenReturn(Optional.of("https://example.com/avatar.png"));

        ProfileSnapshot profile = new ClerkProfileClient(clerk, "test-secret").fetch("user_test");

        assertThat(profile.email()).isEqualTo("primary@example.com");
        assertThat(profile.displayName()).isEqualTo("Breeze Builder");
        assertThat(profile.avatarUrl()).isEqualTo("https://example.com/avatar.png");
        verify(users).get("user_test");
    }
}
