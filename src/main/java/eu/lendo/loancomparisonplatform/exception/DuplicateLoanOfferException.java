package eu.lendo.loancomparisonplatform.exception;

public class DuplicateLoanOfferException extends RuntimeException {
    public DuplicateLoanOfferException(String message) {
        super(message);
    }
}
