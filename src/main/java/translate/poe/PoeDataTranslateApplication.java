package translate.poe;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import translate.poe.service.PoeDataCollector;

@SpringBootApplication
@RequiredArgsConstructor
public class PoeDataTranslateApplication implements CommandLineRunner {
    private final PoeDataCollector poeDataCollector;

    public static void main(String[] args) {
        SpringApplication.run(PoeDataTranslateApplication.class, args);
    }

    @Override
    public void run(String... args) {
        poeDataCollector.save();
    }
}
