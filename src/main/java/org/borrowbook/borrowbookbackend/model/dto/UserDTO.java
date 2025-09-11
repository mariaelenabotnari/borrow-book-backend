package org.borrowbook.borrowbookbackend.model.dto;

import lombok.Data;
import org.borrowbook.borrowbookbackend.model.entity.User;

@Data
public class UserDTO {
    private String username;
    private String email;

    public UserDTO(User user) {
        this.username = user.getUsername();
        this.email = user.getEmail();
    }
}
