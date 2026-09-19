package com.github.enerccio.marginalia.domain.security.model;

import com.github.enerccio.marginalia.domain.model.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(indexes = {
        @Index(name = "user_is_deleted_idx", columnList = "is_deleted"),
        @Index(name = "user_login_idx", columnList = "login"),
})
public class User extends BaseEntity {

    @Column(unique = true, length = 64)
    private String login;

    @Lob
    private String fullName;

    @Lob
    private String passwordHash;

    @Lob
    private String savedLogins;

    private boolean isAdmin;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getSavedLogins() {
        return savedLogins;
    }

    public void setSavedLogins(String savedLogins) {
        this.savedLogins = savedLogins;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
}
