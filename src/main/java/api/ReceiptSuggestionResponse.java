package api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Represents the result of an OCR parse
 */
public class ReceiptSuggestionResponse {
    @JsonProperty
    public final String merchantName;

    @JsonProperty
    public final BigDecimal amount;

    @JsonProperty
    public final String xycoord;

    public ReceiptSuggestionResponse(String merchantName, BigDecimal amount, String cropCoord) {
        this.merchantName = merchantName;
        this.amount = amount;
        this.xycoord = cropCoord;
    }
}
