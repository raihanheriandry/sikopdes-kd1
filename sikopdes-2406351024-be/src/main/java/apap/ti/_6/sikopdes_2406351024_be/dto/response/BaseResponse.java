package apap.ti._6.sikopdes_2406351024_be.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.time.ZoneId;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse<T> {
    private int status;
    private String message;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime timestamp;

    private T data;

    public static <T> BaseResponse<T> success(T data, String message) {
        return BaseResponse.<T>builder()
                .status(200)
                .message(message)
                .timestamp(OffsetDateTime.now(ZoneId.of("Asia/Jakarta")))
                .data(data)
                .build();
    }

    public static <T> BaseResponse<T> success(T data) {
        return success(data, "Success");
    }

    public static <T> BaseResponse<T> created(T data, String message) {
        return BaseResponse.<T>builder()
                .status(201)
                .message(message)
                .timestamp(OffsetDateTime.now(ZoneId.of("Asia/Jakarta")))
                .data(data)
                .build();
    }

    public static <T> BaseResponse<T> error(int status, String message) {
        return BaseResponse.<T>builder()
                .status(status)
                .message(message)
                .timestamp(OffsetDateTime.now(ZoneId.of("Asia/Jakarta")))
                .data(null)
                .build();
    }
}
