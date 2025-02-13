package reciter.security;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserLog {
    private String clientId;
    private String clientName;
    private String apiEndPoint;
    private String personIdentifier;
    private String timestamp;
}

