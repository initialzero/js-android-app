package com.jaspersoft.android.jaspermobile.support.system;

/**
 * @author Tom Koptel
 * @since 2.6
 */
final class Profile {
    private final String alias;
    private final String username;
    private final String password;
    private final String organization;
    private final String url;
    private final String version;
    private final String edition;

    private Profile(Builder builder) {
        this.alias = builder.alias;
        this.username = builder.username;
        this.password = builder.password;
        this.organization = builder.organization;
        this.url = builder.url;
        this.version = builder.version;
        this.edition = builder.edition;
    }

    public String getAlias() {
        return alias;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getOrganization() {
        return organization;
    }

    public String getUrl() {
        return url;
    }

    public String getVersion() {
        return version;
    }

    public String getEdition() {
        return edition;
    }

    @Override
    public String toString() {
        return "Profile{" +
                "alias='" + alias + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", organization='" + organization + '\'' +
                ", url='" + url + '\'' +
                ", version='" + version + '\'' +
                ", edition='" + edition + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Profile profile = (Profile) o;

        if (alias != null ? !alias.equals(profile.alias) : profile.alias != null) return false;
        if (username != null ? !username.equals(profile.username) : profile.username != null)
            return false;
        if (password != null ? !password.equals(profile.password) : profile.password != null)
            return false;
        if (organization != null ? !organization.equals(profile.organization) : profile.organization != null)
            return false;
        if (url != null ? !url.equals(profile.url) : profile.url != null) return false;
        if (version != null ? !version.equals(profile.version) : profile.version != null)
            return false;
        return edition != null ? edition.equals(profile.edition) : profile.edition == null;
    }

    @Override
    public int hashCode() {
        int result = alias != null ? alias.hashCode() : 0;
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (organization != null ? organization.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (edition != null ? edition.hashCode() : 0);
        return result;
    }

    public static class Builder {
        private String alias;
        private String username;
        private String password;
        private String organization = "";
        private String url;
        private String version;
        private String edition;

        public Builder alias(String alias) {
            this.alias = alias;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder organization(String organization) {
            this.organization = organization == null ? "" : organization;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder edition(String edition) {
            this.edition = edition;
            return this;
        }

        public Profile build() {
            return new Profile(this);
        }
    }
}

