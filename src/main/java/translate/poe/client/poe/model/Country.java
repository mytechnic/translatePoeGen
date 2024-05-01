package translate.poe.client.poe.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Country {
    KR("https://poe.game.daum.net/"),
    US("https://www.pathofexile.com/");

    private final String baseUrl;
}
