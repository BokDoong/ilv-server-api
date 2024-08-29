package hanium.server.i_luv_book.user.login.dto;

import lombok.Data;

@Data
public class KakaoAccessTokenDTO {

    private String token_type;
    private String access_token;
    private String id_token;
    private String expires_in;
    private String refresh_token;
    private String refresh_token_expires_in;
}
