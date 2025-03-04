package by.alexhome.t1schoolproject.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@ConfigurationProperties(prefix = "spring.mail")
@Component
public class EmailProperties {
    private String host;
    private int port;
    private String username;
    private String password;
}