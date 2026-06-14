package reciter.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/reciter")
@Tag(name = "Health Check")
public class PingController {

    @Operation(summary = "Health check")
    @GetMapping(value = "/ping", produces = "text/plain")
    @ResponseBody
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("Healthy");
    }
}