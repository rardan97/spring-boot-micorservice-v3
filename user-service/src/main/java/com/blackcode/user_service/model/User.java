package com.blackcode.user_service.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "tb_user")
public class User {

    @Id
    private String userId;

    private String nama;

    private String email;

    private Long departmentId;

    private Long addressId;

}
