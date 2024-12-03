package AdminHomePageDirectory.Chats;

import java.util.List;
import java.util.Map;

public class ChatUsersConstructor {
    private List<Map<String, Object>> deliveryDetails;
    private String email;
    private String fullName;
    private Long isResetPassTruEmail;
    private String password;
    private String role;
    private String username;
    private String userID;

    public ChatUsersConstructor() {
    }

    public ChatUsersConstructor(List<Map<String, Object>> deliveryDetails, String email, String fullName, Long isResetPassTruEmail, String password, String role, String username, String userID) {
        this.deliveryDetails = deliveryDetails;
        this.email = email;
        this.fullName = fullName;
        this.isResetPassTruEmail = isResetPassTruEmail;
        this.password = password;
        this.role = role;
        this.username = username;
        this.userID = userID;
    }

    public List<Map<String, Object>> getDeliveryDetails() {
        return deliveryDetails;
    }

    public void setDeliveryDetails(List<Map<String, Object>> deliveryDetails) {
        this.deliveryDetails = deliveryDetails;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Long getIsResetPassTruEmail() {
        return isResetPassTruEmail;
    }

    public void setIsResetPassTruEmail(Long isResetPassTruEmail) {
        this.isResetPassTruEmail = isResetPassTruEmail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
