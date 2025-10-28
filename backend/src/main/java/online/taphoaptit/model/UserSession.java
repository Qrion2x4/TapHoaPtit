package online.taphoaptit.model;

import org.springframework.stereotype.Component;

@Component
public class UserSession {
    private String currentUser;

    public String getCurrentUser() { return currentUser; }
    public void setCurrentUser(String currentUser) { this.currentUser = currentUser; }
    public boolean isLoggedIn() { return currentUser != null; }
    public void logout() { currentUser = null; }
}
