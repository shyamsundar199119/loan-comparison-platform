CREATE TABLE loan_applications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    applicant_first_name VARCHAR(100) NOT NULL,
    applicant_last_name VARCHAR(100) NOT NULL,
    applicant_email VARCHAR(255) NOT NULL,
    requested_loan_amount NUMERIC(15, 2) NOT NULL,
    requested_loan_term_months INTEGER NOT NULL,
    status VARCHAR(50) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraint: Ensures status only accepts PENDING, ACCEPTED, or EXPIRED
    CONSTRAINT chk_loan_application_status
        CHECK (status IN ('PENDING', 'ACCEPTED', 'EXPIRED'))
);

-- 2. Create the child Loan Offers table
CREATE TABLE loan_offers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    loan_application_id UUID NOT NULL,
    lender_name VARCHAR(150) NOT NULL,
    interest_rate NUMERIC(5, 2) NOT NULL,
    monthly_payment NUMERIC(15, 2) NOT NULL,
    loan_term_months INTEGER NOT NULL,
    total_repayment_amount NUMERIC(15, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraint: Ensures status only accepts PENDING, ACCEPTED, or REJECTED
    CONSTRAINT chk_loan_offer_status
        CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED')),

    -- Foreign key linking to parent table
    CONSTRAINT fk_loan_application
        FOREIGN KEY (loan_application_id)
        REFERENCES loan_applications(id)
        ON DELETE CASCADE,
    CONSTRAINT uq_loan_application_lender UNIQUE (loan_application_id, lender_name)
);

-- 3. Indexes for optimizing status lookups
CREATE INDEX idx_loan_applications_status ON loan_applications(status);
CREATE INDEX idx_loan_offers_status ON loan_offers(status);

-- 4. Index for foreign key performance
CREATE INDEX idx_loan_offers_application_id ON loan_offers(loan_application_id);

-- 5. Index for created_at to optimize queries based on creation time
CREATE INDEX idx_loan_applications_created_at ON loan_applications(created_at);
