package com.github.enerccio.marginalia;

import com.github.enerccio.marginalia.domain.security.model.User;
import org.springframework.beans.factory.InitializingBean;

import java.io.File;

public class Configuration implements InitializingBean {

    private Long persistentLoginInfoTTL = 60 * 60 * 24 * 30L;
    private boolean allowPersistentLogin = true;
    private File folder;
    private File dataFolder;

    public Long getPersistentLoginInfoTTL() {
        return persistentLoginInfoTTL;
    }

    public void setPersistentLoginInfoTTL(Long persistentLoginInfoTTL) {
        this.persistentLoginInfoTTL = persistentLoginInfoTTL;
    }

    public boolean isAllowPersistentLogin() {
        return allowPersistentLogin;
    }

    public void setAllowPersistentLogin(boolean allowPersistentLogin) {
        this.allowPersistentLogin = allowPersistentLogin;
    }

    public File getFolder() {
        return folder;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        String homeDir = System.getProperty("user.home");
        folder = new File(homeDir, ".marginalia");
        if (!folder.exists() && !folder.mkdirs()) {
            throw new RuntimeException("Cannot create folder " + folder.getAbsolutePath());
        }

        dataFolder = new File(folder, "data");
        if (!dataFolder.exists())
            dataFolder.mkdirs();
    }

    public String resolveDb(String db) {
        return "jdbc:sqlite:" + folder.getAbsolutePath() + File.separator + db;
    }

    public File getUserDataFolder(User user) {
        File file = new File(dataFolder, user.getLogin());
        if (!file.exists())
            file.mkdirs();
        return file;
    }

    public File getImagesFolder(User user) {
        File file = new File(getUserDataFolder(user), "images");
        if (!file.exists())
            file.mkdirs();
        return file;
    }

    public File getResourcesFolder(User user) {
        File file = new File(getUserDataFolder(user), "resources");
        if (!file.exists())
            file.mkdirs();
        return file;
    }
}
