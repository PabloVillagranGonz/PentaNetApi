package org.example.centrosnetapi.dtos.Auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDTO {

    private String email;
    private String password;
    private Long centroId;
}