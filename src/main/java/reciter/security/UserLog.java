package reciter.security;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLog {
    private String clientId;
    private String clientName;
    private String apiEndPoint;
    private String personIdentifier;
    private String timestamp;
}

