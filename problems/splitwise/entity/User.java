package entity;

import java.util.UUID;

public class User {
    
    private final UUID userID;
    private final String name;
    private final String email;
    private final Balance balance;
    
    public User(String name, String email) {
        this.userID = UUID.randomUUID();
        this.name = name;
        this.email = email;
        this.balance = new Balance(this);
    }

    public String getUserID() {
        return userID.toString();
    }

    public String getName() {
        return name;
    }

    public Balance getBalance() {
        return balance;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }
        if(obj == null || obj.getClass() != getClass()) {
            return false;
        }
        User user = (User) obj;
        return userID.equals(user.getUserID());
    }

    @Override
    public int hashCode() {
        return userID.hashCode();
    }

}