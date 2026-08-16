package eu.lendo.loancomparisonplatform.exception;

public class LoanOfferNotFoundException extends RuntimeException {
    public LoanOfferNotFoundException(String message) {
        super(message);
    }
}
