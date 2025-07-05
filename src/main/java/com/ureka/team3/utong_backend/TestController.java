package com.ureka.team3.utong_backend;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("/")
    public String home() {
        return "😁 dev cicd 성공 제발 ㄹㅇ 액튜에이터 넣었고 다했다 진짜 제발 cors도했다";
    }

}
