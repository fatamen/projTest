package tw.com.ispan.domain;

import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.persistence.Id;

@Document(collection = "users")
public class User {
    @Id
    private String id;
    private String name;
    private String email;

    // 無參構造函數
    public User() {
    }

    // 有參構造函數
    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // Getters 和 Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
