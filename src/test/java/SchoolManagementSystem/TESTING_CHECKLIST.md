# Test checklist — after installing on a TEST COPY

## Startup
- [ ] Run `mvnw.cmd clean verify` on Windows (or build with IntelliJ Maven).
- [ ] No `Ambiguous mapping` duplicate endpoint error. Original `/api/admissions/grade1` stays owned by your existing `AdmissionGrade1Controller`.
- [ ] No JPA `Schema-validation` / `Table ... does not exist` / `Unknown column` error.
- [ ] `GET http://localhost:8080/sms.html` shows the new login screen.
- [ ] `GET http://localhost:8080/api/auth/csrf` returns a token and sends a cookie.

## Login / account security
- [ ] First-admin registration succeeds only when no admin exists AND server secret matches.
- [ ] Second first-admin registration fails.
- [ ] Wrong password fails with HTTP 401.
- [ ] Admin login creates a session; `/api/auth/me` shows `ROLE_ADMIN`.
- [ ] Admin registers teacher record through EXISTING teacher UI, then creates teacher account.
- [ ] Trying to register a teacher code not found in existing `Teacher` JPA entity fails.
- [ ] Teacher login shows `ROLE_TEACHER`, not admin.
- [ ] Teacher `GET /api/admin/dashboard` returns HTTP 403.
- [ ] Teacher can only access assigned grade and subject; cross-scope requests return HTTP 403.
- [ ] Logout makes protected API return 401. Disabled teacher session becomes invalid.
- [ ] All POST, PUT, DELETE require the matching CSRF token; forged missing-token request is HTTP 403.

## Admissions / counts
- [ ] Add 5 Grade 1 applications. Set cap 2. Selection contains exactly first 2 by submission order. All 5 remain saved.
- [ ] Grade 2–10 regular vacancy is computed after accurately entering ACTIVE current students in `sms_student_enrollment`.
- [ ] Mark a student inactive to create a vacancy; 50 km relocation ranks ahead of general applications.
- [ ] Grade 6 scholarship mark below cutoff does not get scholarship priority.
- [ ] Grade 6 extra seats and regular vacancies are distinct.
- [ ] Set A/L capacities 30 for each stream. Each stream independently selects at most 30, prioritizing existing enrolled students.
- [ ] Dashboard enrolled-by-grade, admissions per grade/stream, and property total match expected records.

## Teacher work
- [ ] Teacher enters attendance only for an enrolled student in their assigned subject.
- [ ] Teacher enters marks in an allowed subject only. Admin can query by studentId and subjectId.
- [ ] Teacher adds profile note and sees their permitted notes.
- [ ] Teacher uploads allowed file (max 10 MB). Admin can list and download it.
- [ ] File path traversal and non-allowed upload extensions are rejected.

## Important known limitations
- [ ] The NEW sms_* records are separate from original SubjectMarks/PhysicalProperties/Admission... tables.
- [ ] Verify existing Student SQL column names are indeed `Studentid`, `studentname`, `birthday`, `studentaddress`.
- [ ] Verify existing Account setup `APP_USER`, `SYSTEM_SETUP`, `TEACHER_SCOPE` table mappings match before migration.
- [ ] Current project entities and Maven dependencies must be checked before claiming full integration.
