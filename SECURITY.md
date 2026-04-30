# Threats
-> API key should be stored in .env file
-> Input validation needed
-> Prompt injection risk
-> Rate limiting required
-> Handle API failure



## Week 2(Day 9) Security Sign-Off

### JWT Authentication
- Verified token-based access control (simulated)
- Unauthorized requests are blocked

### Rate Limiting
- Implemented 30 requests per minute
- Tested and verified with 429 responses

### Injection Protection
- Prompt injection blocked successfully
- HTML sanitization implemented

### PII Audit
The system does not collect, store, or process any personally identifiable information (PII).

- Inputs are limited to risk descriptions
- No user identity data is used in prompts
- No logging of sensitive information is performed

### Status
All security measures verified and compliant.

