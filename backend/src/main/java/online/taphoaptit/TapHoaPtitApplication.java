package online.taphoaptit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = "online.taphoaptit.entity")  // ✅ ép Spring quét entity
public class TapHoaPtitApplication {
    public static void main(String[] args) {
        SpringApplication.run(TapHoaPtitApplication.class, args);
    }
}
