package reciter.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/reciter")
@Api(value = "PingController", tags = {"Health Check"})
public class PingController {

    @ApiOperation(value = "Health check", response = ResponseEntity.class)
    @GetMapping(value = "/ping", produces = "text/plain")
    @ResponseBody
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("Healthy");
    }
}