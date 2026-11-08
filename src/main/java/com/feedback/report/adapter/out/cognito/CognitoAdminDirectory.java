package com.feedback.report.adapter.out.cognito;

import com.feedback.report.application.port.out.AdminDirectory;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersInGroupRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersInGroupResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserType;

/** Lists the members of the admins group in the Cognito User Pool (report recipients). */
public final class CognitoAdminDirectory implements AdminDirectory {

    private final CognitoIdentityProviderClient client;
    private final String userPoolId;
    private final String groupName;

    public CognitoAdminDirectory(CognitoIdentityProviderClient client, String userPoolId, String groupName) {
        this.client = client;
        this.userPoolId = userPoolId;
        this.groupName = groupName;
    }

    @Override
    public List<String> verifiedAdminEmails() {
        List<String> emails = new ArrayList<>();
        String nextToken = null;
        do {
            ListUsersInGroupResponse page = client.listUsersInGroup(ListUsersInGroupRequest.builder()
                    .userPoolId(userPoolId)
                    .groupName(groupName)
                    .nextToken(nextToken)
                    .build());
            page.users().stream()
                    .filter(user -> !Boolean.FALSE.equals(user.enabled()))
                    .filter(user -> "true".equals(attribute(user, "email_verified").orElse(null)))
                    .flatMap(user -> attribute(user, "email").stream())
                    .forEach(emails::add);
            nextToken = page.nextToken();
        } while (nextToken != null && !nextToken.isEmpty());
        return emails;
    }

    private static Optional<String> attribute(UserType user, String name) {
        return user.attributes().stream()
                .filter(attribute -> name.equals(attribute.name()))
                .map(AttributeType::value)
                .filter(value -> value != null && !value.isBlank())
                .findFirst();
    }
}
