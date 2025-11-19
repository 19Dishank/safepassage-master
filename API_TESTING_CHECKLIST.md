# SafePassage API Testing Checklist

## ✅ Pre-Testing Setup
- [ ] XAMPP Apache server running
- [ ] Backend accessible at: http://192.168.104.87/SafePassage/Admin/dashboard/
- [ ] Latest APK built and installed on phone
- [ ] Phone and PC on same WiFi network

## 🧪 API Endpoint Tests

### 1. User Registration API
**Endpoint**: `register_user.php`
- [ ] Open app and go to Register screen
- [ ] Fill in: Name, Email, Password, Confirm Password
- [ ] Tap "Create Account"
- **Expected**: "Account Created Successfully" message
- **Check**: User appears in admin dashboard
- **Log Tag**: Look for "Register_Screen" in logs

### 2. User Login API (Active User)
**Endpoint**: `user_login.php`
- [ ] Go to Login screen
- [ ] Enter credentials for ACTIVE user
- [ ] Tap "Login"
- **Expected**: "Login Successful" message
- **Expected**: Navigate to PIN verification or setup
- **Log Tag**: Look for "Login_Screen" in logs

### 3. User Login API (Deactivated User)
**Endpoint**: `user_login.php`
- [ ] Deactivate a user in admin dashboard
- [ ] Try to login with deactivated user
- [ ] Tap "Login"
- **Expected**: "Account is deactivated" message
- **Expected**: Stay on login screen
- **Log Tag**: Look for "Login_Screen" in logs

### 4. PIN Registration API
**Endpoint**: `register_pin.php`
- [ ] Complete login with new user (no PIN set)
- [ ] Should redirect to PIN setup screen
- [ ] Enter 4-digit PIN twice
- [ ] Tap "Set Pin"
- **Expected**: "Pin Set Successfully" message
- **Expected**: Navigate to MainActivity
- **Log Tag**: Look for "SetPinActivity" in logs

### 5. PIN Verification API
**Endpoint**: `verify_pin.php`
- [ ] Login with existing user (PIN already set)
- [ ] Should show PIN verification screen
- [ ] Enter correct PIN
- [ ] Tap "Verify PIN"
- **Expected**: Navigate to MainActivity
- **Log Tag**: Look for "ReinstallVerification" in logs

### 6. PIN Verification API (Wrong PIN)
**Endpoint**: `verify_pin.php`
- [ ] Enter incorrect PIN
- [ ] Tap "Verify PIN"
- **Expected**: "Incorrect PIN" message
- **Expected**: Stay on verification screen

## 🔍 Debugging Tips

### View Logs in Android Studio
```bash
# Filter by app package
adb logcat | findstr "com.dhruvbuildz.safepassageapp"

# Filter by specific tags
adb logcat | findstr "Register_Screen\|Login_Screen\|SetPinActivity\|ReinstallVerification"
```

### Check API URLs in Logs
Look for log messages containing:
- "Attempting backend registration: http://192.168.104.87/..."
- "Backend login response: ..."
- "PIN verification response: ..."

### Common Issues & Solutions
1. **Connection Timeout**: 
   - Check XAMPP is running
   - Verify IP address in strings.xml
   - Ensure phone and PC on same network

2. **Navigation Issues**:
   - Check PIN setup/verification flow
   - Verify user has correct status in database

3. **API Response Issues**:
   - Check backend PHP error logs
   - Verify database connection
   - Check JSON payload format

## ✅ Success Criteria
- [ ] All 6 API endpoints work correctly
- [ ] User registration creates user in database
- [ ] Active users can login and navigate to MainActivity
- [ ] Deactivated users cannot login
- [ ] PIN setup works for new users
- [ ] PIN verification works for existing users
- [ ] App navigation flows work end-to-end

## 📱 Complete Flow Test
1. Register new user → Should work
2. Login with new user → Should go to PIN setup
3. Set PIN → Should go to MainActivity
4. Logout and login again → Should go to PIN verification
5. Enter correct PIN → Should go to MainActivity
6. Deactivate user in admin → Should not be able to login
