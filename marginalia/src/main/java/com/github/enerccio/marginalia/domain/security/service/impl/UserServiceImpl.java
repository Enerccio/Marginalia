package com.github.enerccio.marginalia.domain.security.service.impl;

import com.github.enerccio.marginalia.bound.SessionManager;
import com.github.enerccio.marginalia.domain.security.PersistedLoginInfo;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.repository.UserRepository;
import com.github.enerccio.marginalia.domain.security.service.UserService;
import com.github.enerccio.marginalia.domain.service.impl.BaseServiceImpl;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import com.github.enerccio.marginalia.domain.traits.NoTx;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class UserServiceImpl extends BaseServiceImpl<User, UserRepository> implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    private SessionManager sessionManager;

    @Override
    @CommonTxReadOnly
    public boolean existsUsers() throws Exception {
        return getRepository().existsUsers();
    }

    @Override
    @CommonTxReadOnly
    public User findByName(String name) throws Exception {
        return find(getRepository().findByName(name));
    }

    @Override
    @CommonTxReadOnly
    public boolean isLoginAvailable(User user, String login) throws Exception {
        User existing = findByName(login);
        return existing == null || (user != null && existing.getId().equals(user.getId()));
    }

    @Override
    @CommonTxReadOnly
    public boolean isLastAdmin(User user) throws Exception {
        User current = find(user);
        return current != null && current.isAdmin() && getRepository().countAdmins() <= 1;
    }

    @Override
    @CommonTx
    public void deleteUser(User user) throws Exception {
        User current = find(user);
        if (current == null || current.isDeleted()) {
            return;
        }
        if (isLastAdmin(current)) {
            throw new IllegalStateException("Cannot delete last administrator");
        }
        String login = current.getLogin();
        // free the unique login for reuse and drop all remembered logins
        current.setLogin(StringUtils.left(login, 27) + "#" + current.getUuid());
        current.setSavedLogins(null);
        delete(current, false);
        retireDataFolder(login);
    }

    /**
     * Data folder is named by login, it is moved away so a new user with the same login does not inherit the files.
     */
    private void retireDataFolder(String login) {
        File folder = new File(configuration.getDataFolder(), login);
        if (!folder.exists()) {
            return;
        }
        File target = new File(configuration.getDataFolder(), login + "-deleted");
        for (int i = 2; target.exists(); i++) {
            target = new File(configuration.getDataFolder(), login + "-deleted-" + i);
        }
        try {
            Files.move(folder.toPath(), target.toPath());
        } catch (IOException e) {
            log.warn("Failed to move data folder of deleted user {} to {}", login, target, e);
        }
    }

    @Override
    @CommonTxReadOnly
    public boolean authenticate(String username, String password) throws Exception {
        User user = findByName(username);
        if (user == null) {
            return false;
        }

        String storedPasswordData = user.getPasswordHash();
        if (StringUtils.isBlank(storedPasswordData) && StringUtils.isBlank(password)) {
            return true;
        }
        if (StringUtils.isBlank(password) || StringUtils.isBlank(storedPasswordData)) {
            return false;
        }

        String[] parts = storedPasswordData.split(":");
        if (parts.length != 2) {
            return false;
        }

        String salt = parts[0];
        String hash = parts[1];

        return MessageDigest.isEqual(
                hash.getBytes(StandardCharsets.UTF_8),
                hashPassword(password, salt).getBytes(StandardCharsets.UTF_8)
        );
    }

    @Override
    @CommonTx
    public User changePassword(User user, String password) throws Exception {
        byte[] saltBytes = new byte[16];
        secureRandom.nextBytes(saltBytes);
        String salt = Base64.getEncoder().encodeToString(saltBytes);
        String hash = hashPassword(password, salt);
        user.setPasswordHash(salt + ":" + hash);
        user.setSavedLogins(null);
        save(user);
        sessionManager.runForUsers(u -> user.getId().equals(u.getId()), (sessionInformation) -> {
            sessionInformation.boundVaadinSession.getSession().invalidate();
        }, true, true);
        return user;
    }

    /**
     * Removes the password, the user then logs in with the user name only and sets a new password themselves.
     */
    @Override
    @CommonTx
    public User clearPassword(User user) throws Exception {
        user.setPasswordHash(null);
        user.setSavedLogins(null);
        save(user);
        sessionManager.runForUsers(u -> user.getId().equals(u.getId()), (sessionInformation) -> {
            sessionInformation.boundVaadinSession.getSession().invalidate();
        }, true, true);
        return user;
    }

    @Override
    @CommonTx
    public PersistedLoginInfo authenticateFromCookie(User user, String identifier, String secret) throws Exception {
        List<PersistedLoginInfo> loginInfos = getPersistedLoginInfo(user);
        for (PersistedLoginInfo loginInfo : loginInfos) {
            if (loginInfo.getIdentifier().equals(identifier)) {
                if (loginInfo.getHashedSecret().equals(hashPersistedLoginSecret(user, secret))) {
                    if (configuration.getPersistentLoginInfoTTL() != null) {
                        long ctime = System.currentTimeMillis() - (configuration.getPersistentLoginInfoTTL() * 1000);
                        if (ctime >= loginInfo.getCreate()) {
                            // we passed the max login window
                            deletePersistedLoginInfo(user, loginInfo);
                            save(user);
                            return null;
                        }
                    }
                    PersistedLoginInfo newLoginInfo = generateNewPersistentInfo(user);
                    newLoginInfo.setIdentifier(loginInfo.getIdentifier());
                    newLoginInfo.setCreate(loginInfo.getCreate());
                    deletePersistedLoginInfo(user, loginInfo);
                    addPersistedLoginInfo(user, newLoginInfo);
                    save(user);
                    return newLoginInfo;
                }
            }
        }
        return null;
    }

    @Override
    @NoTx
    public void deletePersistedLoginInfo(User user, PersistedLoginInfo persistedLoginInfo) throws Exception {
        List<PersistedLoginInfo> persistedLoginInfos = getPersistedLoginInfo(user);
        List<PersistedLoginInfo> newPersistedLoginInfos = new ArrayList<>();
        for (PersistedLoginInfo loginInfo : persistedLoginInfos) {
            if (loginInfo.getIdentifier().equals(persistedLoginInfo.getIdentifier()))
                continue;
            long ctime = System.currentTimeMillis() - configuration.getPersistentLoginInfoTTL() * 1000;
            if (ctime >= loginInfo.getCreate()) {
                continue;
            }
            newPersistedLoginInfos.add(loginInfo);
        }
        serializePersistedLoginInfo(user, newPersistedLoginInfos);
    }

    @Override
    @NoTx
    public List<PersistedLoginInfo> getPersistedLoginInfo(User user) throws Exception {
        if (StringUtils.isBlank(user.getSavedLogins()))
            return Collections.emptyList();
        LinkedHashSet<PersistedLoginInfo> loginInfos = new LinkedHashSet<>();
        for (String savedLogin : user.getSavedLogins().split(Pattern.quote("|"))) {
            PersistedLoginInfo info = new PersistedLoginInfo();
            String[] parsed = savedLogin.split(Pattern.quote(";"));
            info.setIdentifier(parsed[0]);
            info.setHashedSecret(parsed[1]);
            info.setCreate(Long.parseLong(parsed[2]));
            info.setLastAccess(Long.parseLong(parsed[3]));
            loginInfos.add(info);
        }
        return loginInfos.stream().toList();
    }

    @Override
    @NoTx
    public PersistedLoginInfo generateNewPersistentInfo(User user) throws Exception {
        BigInteger prime = BigInteger.probablePrime(160, secureRandom);
        String secret = "" + prime;
        PersistedLoginInfo loginInfo = new PersistedLoginInfo();
        loginInfo.setIdentifier(UUID.randomUUID().toString());
        loginInfo.setCreate(System.currentTimeMillis());
        loginInfo.setLastAccess(System.currentTimeMillis());
        loginInfo.setPlainSecret(secret);
        loginInfo.setHashedSecret(hashPersistedLoginSecret(user, secret));
        return loginInfo;
    }

    @Override
    @NoTx
    public void addPersistedLoginInfo(User user, PersistedLoginInfo persistedLoginInfo) throws Exception {
        List<PersistedLoginInfo> persistedLoginInfos = getPersistedLoginInfo(user);
        List<PersistedLoginInfo> newPersistedLoginInfos = new ArrayList<>();
        for (PersistedLoginInfo loginInfo : persistedLoginInfos) {
            if (loginInfo.getIdentifier().equals(persistedLoginInfo.getIdentifier()))
                continue;
            long ctime = System.currentTimeMillis() - (configuration.getPersistentLoginInfoTTL() * 1000);
            if (ctime >= loginInfo.getCreate()) {
                continue;
            }
            newPersistedLoginInfos.add(loginInfo);
        }
        newPersistedLoginInfos.add(persistedLoginInfo);
        serializePersistedLoginInfo(user, newPersistedLoginInfos);
    }

    protected String hashPersistedLoginSecret(User user, String secret) throws Exception {
        secret = user.getId() + "_" + secret;
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(secret.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(digest.digest());
    }

    @NoTx
    protected void serializePersistedLoginInfo(User user, List<PersistedLoginInfo> persistedLoginInfos) throws Exception {
        user.setSavedLogins(persistedLoginInfos.stream().map(pli ->
                pli.getIdentifier() + ";" + pli.getHashedSecret() + ";" + pli.getCreate() + ";" + pli.getLastAccess()).collect(Collectors.joining("|")));
    }

    private String hashPassword(String password, String salt) throws Exception {
        char[] passwordChars = password.toCharArray();
        byte[] saltBytes = Base64.getDecoder().decode(salt);

        PBEKeySpec spec = new PBEKeySpec(passwordChars, saltBytes, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
        byte[] hash = factory.generateSecret(spec).getEncoded();

        return Base64.getEncoder().encodeToString(hash);
    }

}
