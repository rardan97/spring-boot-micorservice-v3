package com.blackcode.auth_service.model;

import jakarta.persistence.*;
import lombok.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "tb_user_auth")
public class UserAuth {

    @Id
    private String userId;

    private String username;

    private String password;

    public UserAuth(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
