package ru.taskmanagment.util;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class AppProperties {
    @Value("mflzkmfafoijafdomeboiafdafdmaruiafal")
    private String plainSecretKey;
    @Value("${security.jwt.token.expire-length}")
    private String expireLength;
}

