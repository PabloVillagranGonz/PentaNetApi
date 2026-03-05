package org.example.centrosnetapi.dtos.Auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequestDTO {

    private String currentPassword;
    private String newPassword;
}