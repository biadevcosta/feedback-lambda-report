package com.feedback.report.adapter.out.cognito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersInGroupRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersInGroupResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserType;

@ExtendWith(MockitoExtension.class)
class CognitoAdminDirectoryTest {

    @Mock
    private CognitoIdentityProviderClient client;

    @Captor
    private ArgumentCaptor<ListUsersInGroupRequest> request;

    private CognitoAdminDirectory directory;

    @BeforeEach
    void setUp() {
        directory = new CognitoAdminDirectory(client, "pool-1", "admins");
    }

    @Test
    void should_readAllPages_when_groupIsPaginated() {
        // given
        when(client.listUsersInGroup(any(ListUsersInGroupRequest.class))).thenReturn(
                ListUsersInGroupResponse.builder().users(user("a@example.com", "true", true)).nextToken("page-2").build(),
                ListUsersInGroupResponse.builder().users(user("b@example.com", "true", true)).build());

        // when
        List<String> emails = directory.verifiedAdminEmails();

        // then
        assertThat(emails).containsExactly("a@example.com", "b@example.com");
        verify(client, times(2)).listUsersInGroup(request.capture());
        assertThat(request.getAllValues()).extracting(ListUsersInGroupRequest::nextToken).containsExactly(null, "page-2");
        assertThat(request.getValue().userPoolId()).isEqualTo("pool-1");
        assertThat(request.getValue().groupName()).isEqualTo("admins");
    }

    @Test
    void should_keepOnlyEnabledUsersWithVerifiedEmail() {
        when(client.listUsersInGroup(any(ListUsersInGroupRequest.class))).thenReturn(ListUsersInGroupResponse.builder()
                .users(
                        user("ok@example.com", "true", true),
                        user("unverified@example.com", "false", true),
                        user("disabled@example.com", "true", false),
                        UserType.builder().enabled(true)
                                .attributes(AttributeType.builder().name("email_verified").value("true").build())
                                .build())
                .build());

        assertThat(directory.verifiedAdminEmails()).containsExactly("ok@example.com");
    }

    @Test
    void should_returnEmpty_when_groupHasNoUsers() {
        when(client.listUsersInGroup(any(ListUsersInGroupRequest.class)))
                .thenReturn(ListUsersInGroupResponse.builder().users(List.of()).build());

        assertThat(directory.verifiedAdminEmails()).isEmpty();
    }

    private static UserType user(String email, String verified, boolean enabled) {
        return UserType.builder()
                .enabled(enabled)
                .attributes(
                        AttributeType.builder().name("email").value(email).build(),
                        AttributeType.builder().name("email_verified").value(verified).build())
                .build();
    }
}
