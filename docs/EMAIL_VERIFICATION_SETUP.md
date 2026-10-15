# Email verification OTP setup

F-Cinema requires newly registered customer accounts to verify their email before login.

## 1. Run the database migration

On the existing `FCinemaDB` database, run this script in SQL Server Management Studio:

`database/migrations/20261014_add_email_verification.sql`

Existing accounts are marked as verified by the migration so current staff, managers, administrators, and customers are not locked out. New customer registrations explicitly set `email_verified = 0`.

For a brand-new database, the updated `database/FCinemaDB_schema.sql` already includes the column.

## 2. Configure the sender email

The application uses Gmail SMTP. Configure these Windows environment variables with a Gmail account and its **Google App Password** (not the normal account password):

```powershell
setx FCINEMA_EMAIL "fcinemasystem@gmail.com"
setx FCINEMA_EMAIL_APP_PASSWORD "swpgroup2"
```

Close and reopen NetBeans after setting the variables, then restart Tomcat. Never commit the sender's app password to GitHub.

## 3. Registration flow

1. Registration creates an active customer with `email_verified = 0`.
2. F-Cinema sends a six-digit OTP to the registered email.
3. The OTP expires after five minutes. Up to five incorrect attempts are allowed per OTP.
4. A valid OTP updates `email_verified = 1`; only then can the customer log in.
5. Unverified accounts receive a login message with a link to the email verification page. That page can resend the OTP.
6. Forgot-password OTP uses separate session attributes and remains independent from email verification.

## 4. Manual test checklist

- Register a new customer with an email you can access. Confirm an OTP email is received.
- Try to log in before verifying: login must be blocked and the user should see the email verification link.
- Enter the correct OTP: the app should redirect to login with a success message; login should then work.
- Enter a wrong OTP five times: that OTP should be invalidated and the user should request another.
- Wait more than five minutes and submit the OTP: the page should require a new code.
- Use the forgot-password flow and confirm it still sends a password-reset OTP independently.
