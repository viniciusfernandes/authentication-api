# Feature Specifications

## Feature 1 — User Registration

**User Story**  
As a new user, I want to create an account using my email and password so that I can access the system’s services.

**Acceptance Criteria**
- Registration form with fields: **Full Name**, **Email**, **Password**, **Confirm Password**.
- Password must have **minimum 8 characters**, **at least 1 uppercase letter**, **1 number**, and **1 special symbol**.
- **Email** uniqueness is validated in the backend.
- Passwords are stored **hashed** (e.g., **BCrypt**).
- On success, show a confirmation message and **redirect to the login page**.

---

## Feature 2 — User Login

**User Story**  
As a user, I want to log in with my email and password so that I can authenticate and get access to protected resources.

**Acceptance Criteria**
- Login form with fields: **Email**, **Password**.
- On successful login, return a **JWT** (or **OAuth2 token**).
- On failure, return descriptive error messages (e.g., invalid credentials, locked account).
- Frontend stores the token securely (e.g., **httpOnly cookie** or **localStorage**, based on the final decision).

---

## Feature 3 — External Provider Login (Google & Facebook)

**User Story**  
As a user, I want to log in with my **Google** or **Facebook** account so that I don’t need to create a new password.

**Acceptance Criteria**
- “**Login with Google**” and “**Login with Facebook**” buttons are available on the login page.
- OAuth2 flow is handled via **Spring Security** plus the appropriate provider SDKs.
- Store the external provider **ID** in the user profile for future logins.
- If this is the first login, redirect the user to a **Complete Profile** page to capture missing data (e.g., phone).

---

## Feature 4 — Account Verification (Email Confirmation)

**User Story**  
As a new user, I want to confirm my email before I can log in so that the system verifies my identity.

**Acceptance Criteria**
- After registration, send a **verification email** with a unique link (**token valid for 24h**).
- Backend validates the token and **activates the account**.
- If the token is expired, offer an option to **resend the email**.

---

## Feature 5 — Password Recovery

**User Story**  
As a user, I want to reset my password if I forget it so that I can regain access.

**Acceptance Criteria**
- “**Forgot Password?**” link is available on the login form.
- User enters an email; backend sends a **reset link** (**valid for 1h**).
- Reset form enforces the **new password** policy/validation.
- Notify the user after a **successful reset**.

---

## Feature 6 — Role‑Based Authorization

**User Story**  
As an admin, I want to define user roles (e.g., **USER**, **ADMIN**, **MANAGER**) so that access to services is restricted properly.

**Acceptance Criteria**
- Users are stored with **roles** in the database.
- **JWT** includes **role claims**.
- API endpoints are secured with Spring Security annotations (e.g., `@PreAuthorize("hasRole('ADMIN')")`).
- Frontend hides UI components that the user is **not authorized** to see.

---

## Feature 7 — User Profile Management

**User Story**  
As a logged‑in user, I want to edit my profile information so that my data is up to date.

**Acceptance Criteria**
- Profile page with editable fields: **Full Name**, **Phone**, **Profile Picture**.
- **Email** cannot be changed (or requires **re‑verification**).
- **Save** action updates the data in the backend.

---

------------------------------------------------------------------------

# Summary

This project enforces **clean architecture**, **DDD principles**, and
**strict folder conventions** for both backend and frontend.
The goal is to ensure scalability, maintainability, and consistency
across the entire authentication ecosystem.
