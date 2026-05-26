namespace SPS.SaludPay.DTOs;

public record ApiResponseDto<T>(string Status, T? Data, string Message, string Timestamp)
{
    public static ApiResponseDto<T> Ok(T data, string msg) =>
        new("OK", data, msg, DateTime.UtcNow.ToString("O"));

    public static ApiResponseDto<object?> Error(string msg) =>
        new ApiResponseDto<object?>("ERROR", null, msg, DateTime.UtcNow.ToString("O"));
}
