package sanfranmotopark.cansave.us;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Locale;

/**
 * Created by kembp on 8/4/14.
 */
public class Money {

    private static final Currency USD = Currency.getInstance("USD");
    private static final RoundingMode DEFAULT_ROUNDING = RoundingMode.HALF_EVEN;

    private BigDecimal amount;
    private BigDecimal range;
    private Currency currency;

    public static Money dollars(BigDecimal amount) {
        return new Money(amount, USD);
    }

    public static Money dollars(BigDecimal amount, BigDecimal range) {
        return new Money(amount, range, USD, DEFAULT_ROUNDING);
    }

    Money(BigDecimal amount, Currency currency) {
        this(amount, currency, DEFAULT_ROUNDING);
    }

    Money(BigDecimal amount, Currency currency, RoundingMode rounding) {
        this(amount, BigDecimal.ZERO, currency, rounding);
    }

    Money(BigDecimal amount, BigDecimal range, Currency currency, RoundingMode rounding) {
        this.amount = amount;
        this.range = range;
        this.currency = currency;

        this.amount = amount.setScale(currency.getDefaultFractionDigits(), rounding);
        this.range = range.setScale(currency.getDefaultFractionDigits(), rounding);
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getRange() {
        return range;
    }

    public Currency getCurrency() {
        return currency;
    }

    @Override
    public String toString() {
        if (this.range.compareTo(BigDecimal.ZERO) == 0)
            return getCurrency().getSymbol() + " " + getAmount();
        return getCurrency().getSymbol() + " " + getAmount() + " - " + getCurrency().getSymbol() + " " + getRange();
    }

    public String toString(Locale locale) {
        if (this.range.compareTo(BigDecimal.ZERO) == 0)
            return getCurrency().getSymbol(locale) + " " + getAmount();
        return getCurrency().getSymbol(locale) + " " + getAmount() + " - " + getCurrency().getSymbol(locale) + " " + getRange();
    }
}
