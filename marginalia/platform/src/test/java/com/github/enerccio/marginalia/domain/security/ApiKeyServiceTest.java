package com.github.enerccio.marginalia.domain.security;

import com.github.enerccio.marginalia.domain.security.model.ApiScope;
import com.github.enerccio.marginalia.domain.security.model.ApiKey;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.permissions.PermissionType;
import com.github.enerccio.marginalia.domain.security.service.ApiKeyService;
import com.github.enerccio.marginalia.domain.security.service.ApiKeyService.CreatedKey;
import com.github.enerccio.marginalia.domain.security.service.ApiKeyService.Principal;
import com.github.enerccio.marginalia.test.PlatformTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The keys of the HTTP API: who may have them, what is stored of them and when they stop working.
 */
class ApiKeyServiceTest extends PlatformTestBase {

    @Autowired
    private ApiKeyService apiKeyService;

    private User apiUser() throws Exception {
        User user = createUser();
        setPermission(user, PermissionType.API, true);
        return loginAs(user);
    }

    @Test
    void aUserWithoutThePermissionCannotCreateKeys() throws Exception {
        login();
        assertThatThrownBy(() -> apiKeyService.create("script", Set.of(ApiScope.READ), null)).isInstanceOf(SecurityException.class);
    }

    @Test
    void theKeyIsGivenOnceAndOnlyItsHashIsStored() throws Exception {
        User user = apiUser();
        CreatedKey created = apiKeyService.create("  script  ", Set.of(ApiScope.WRITE), null);

        assertThat(created.token()).startsWith(ApiKeyService.TOKEN_START + created.key().getKeyPrefix() + "_");
        ApiKey stored = apiKeyService.findForUser(created.key().getUuid());
        assertThat(stored.getName()).isEqualTo("script");
        assertThat(stored.getScopes()).as("write brings read").containsExactlyInAnyOrder(ApiScope.READ, ApiScope.WRITE);
        assertThat(stored.getSecretHash()).hasSize(64).doesNotContain(created.token());
        assertThat(stored.getOwner().getId()).isEqualTo(user.getId());

        Principal principal = apiKeyService.authenticate(created.token());
        assertThat(principal).isNotNull();
        assertThat(principal.user().getId()).isEqualTo(user.getId());
        assertThat(principal.scopes()).containsExactlyInAnyOrder(ApiScope.READ, ApiScope.WRITE);
        assertThat(apiKeyService.findForUser(created.key().getUuid()).getLastUsed()).isNotNull();
    }

    @Test
    void wrongAndMalformedKeysAreRefused() throws Exception {
        apiUser();
        CreatedKey created = apiKeyService.create("script", Set.of(ApiScope.READ), null);

        assertThat(apiKeyService.authenticate(created.token() + "x")).isNull();
        assertThat(apiKeyService.authenticate(created.token().substring(0, created.token().length() - 1))).isNull();
        assertThat(apiKeyService.authenticate(null)).isNull();
        assertThat(apiKeyService.authenticate("")).isNull();
        assertThat(apiKeyService.authenticate("mrg_")).isNull();
        assertThat(apiKeyService.authenticate("mrg_short_secret")).isNull();
        assertThat(apiKeyService.authenticate("Bearer " + created.token())).isNull();
    }

    @Test
    void aRevokedKeyStopsWorking() throws Exception {
        apiUser();
        CreatedKey created = apiKeyService.create("script", Set.of(ApiScope.READ), null);

        assertThat(apiKeyService.revoke(created.key().getUuid())).isTrue();
        assertThat(apiKeyService.authenticate(created.token())).isNull();
        assertThat(apiKeyService.findAllForUser()).noneMatch(k -> k.getUuid().equals(created.key().getUuid()));
        assertThat(apiKeyService.revoke(created.key().getUuid())).isFalse();
    }

    @Test
    void aUserCannotRevokeTheKeyOfAnother() throws Exception {
        apiUser();
        CreatedKey created = apiKeyService.create("script", Set.of(ApiScope.READ), null);

        apiUser();
        assertThat(apiKeyService.revoke(created.key().getUuid())).isFalse();
        assertThat(apiKeyService.authenticate(created.token())).isNotNull();

        loginAdmin();
        assertThat(apiKeyService.revoke(created.key().getUuid())).as("an administrator can").isTrue();
        assertThat(apiKeyService.authenticate(created.token())).isNull();
    }

    @Test
    void anExpiredKeyStopsWorking() throws Exception {
        apiUser();
        assertThatThrownBy(() -> apiKeyService.create("old", Set.of(ApiScope.READ), new Date(System.currentTimeMillis() - 1000)))
                .isInstanceOf(IllegalArgumentException.class);

        CreatedKey created = apiKeyService.create("short", Set.of(ApiScope.READ), new Date(System.currentTimeMillis() + 60_000));
        assertThat(apiKeyService.authenticate(created.token())).isNotNull();

        ApiKey stored = apiKeyService.findForUser(created.key().getUuid());
        stored.setExpires(new Date(System.currentTimeMillis() - 1));
        apiKeyService.save(stored);
        assertThat(apiKeyService.authenticate(created.token())).isNull();
    }

    @Test
    void takingThePermissionAwayStopsTheKeys() throws Exception {
        User user = apiUser();
        CreatedKey created = apiKeyService.create("script", Set.of(ApiScope.WRITE), null);
        assertThat(apiKeyService.authenticate(created.token())).isNotNull();

        setPermission(user, PermissionType.API, false);
        assertThat(apiKeyService.authenticate(created.token())).isNull();
    }

    @Test
    void onlyAnAdministratorGivesAdministrationScopesAndTheyLastOnlyWhileTheOwnerIsOne() throws Exception {
        apiUser();
        assertThatThrownBy(() -> apiKeyService.create("update", Set.of(ApiScope.UPDATE), null)).isInstanceOf(SecurityException.class);
        assertThatThrownBy(() -> apiKeyService.create("mixed", Set.of(ApiScope.READ, ApiScope.EXTENSIONS), null))
                .isInstanceOf(SecurityException.class);

        User admin = loginAdmin();
        CreatedKey created = apiKeyService.create("deploy", Set.of(ApiScope.READ, ApiScope.EXTENSIONS), null);
        assertThat(apiKeyService.authenticate(created.token()).scopes()).containsExactlyInAnyOrder(ApiScope.READ, ApiScope.EXTENSIONS);

        User stored = userService.find(admin.getId());
        stored.setAdmin(false);
        userService.save(stored);
        // no longer an administrator, and no API permission either
        assertThat(apiKeyService.authenticate(created.token())).isNull();
        setPermission(stored, PermissionType.API, true);
        assertThat(apiKeyService.authenticate(created.token()).scopes()).as("the administration scopes are dropped")
                .containsExactly(ApiScope.READ);
    }

    @Test
    void aKeyNeedsAScopeAndTakesAnyCombination() throws Exception {
        apiUser();
        assertThatThrownBy(() -> apiKeyService.create("nothing", Set.of(), null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> apiKeyService.create("nothing", null, null)).isInstanceOf(IllegalArgumentException.class);

        loginAdmin();
        CreatedKey all = apiKeyService.create("all", List.of(ApiScope.values()), null);
        assertThat(apiKeyService.authenticate(all.token()).scopes()).containsExactlyInAnyOrder(ApiScope.values());
        CreatedKey update = apiKeyService.create("update only", Set.of(ApiScope.UPDATE), null);
        assertThat(apiKeyService.authenticate(update.token()).scopes()).containsExactly(ApiScope.UPDATE);
    }

    @Test
    void theNameIsNeeded() throws Exception {
        apiUser();
        assertThatThrownBy(() -> apiKeyService.create(" ", Set.of(ApiScope.READ), null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> apiKeyService.create("x".repeat(101), Set.of(ApiScope.READ), null)).isInstanceOf(IllegalArgumentException.class);
    }
}
