package ru.taskmanagment.util;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class AppProperties {
    //@Value("${security.jwt.token.secret-key}")
    @Value("mflzkmfafoijafdomeboiafdafdmaruiaf")
    private String plainSecretKey;
    @Value("${security.jwt.token.expire-length}")
    private String expireLength;
}

