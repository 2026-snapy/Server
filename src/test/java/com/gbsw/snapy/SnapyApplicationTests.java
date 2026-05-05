package com.gbsw.snapy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "grpc.server.host=localhost",
        "grpc.server.port=50051"
})
class SnapyApplicationTests {

    @Test
    void contextLoads() {
    }

}
