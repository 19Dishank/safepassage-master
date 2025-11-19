# SafePassage - Complete Setup Guide

## 📋 Table of Contents
1. [Prerequisites](#prerequisites)
2. [Initial Setup](#initial-setup)
3. [Running the Application](#running-the-application)
4. [WiFi Network Changes](#wifi-network-changes)
5. [Troubleshooting](#troubleshooting)
6. [Project Structure](#project-structure)

---

## Prerequisites

### Required Software:
- **XAMPP** (Apache + MySQL + PHP)
- **Android Studio** (for Android app development)
- **Java JDK** (for Android development)
- **ADB** (Android Debug Bridge) - comes with Android Studio

### System Requirements:
- Windows 10/11
- PHP 7.4 or higher
- MySQL 5.7 or higher
- Android device or emulator

---

## Initial Setup

### 1. Start XAMPP Server

**IMPORTANT: XAMPP must be running for the app to work!**

1. Open **XAMPP Control Panel**
2. Start **Apache** (click "Start" button)
3. Start **MySQL** (click "Start" button)
4. Both should show green "Running" status

### 2. Import Database

1. Open **phpMyAdmin** (http://localhost/phpmyadmin)
2. Click on **"New"** to create a database
3. Name it: `safepassage`
4. Click **"Import"** tab
5. Select file: `safepassage.sql` (from project root)
6. Click **"Go"** to import

### 3. Create Feedbacks Table (Optional)

1. In phpMyAdmin, select `safepassage` database
2. Click **"SQL"** tab
3. Run the SQL from: `Admin/dashboard/sql/create_feedbacks_table.sql`

### 4. Create Installations State File (Optional)

The installations page will create this automatically, but you can create it manually:
- Location: `Admin/dashboard/api/installations_state.json`
- The file will be auto-generated on first API call

---

## Running the Application

### Backend (PHP/MySQL)

1. **Start XAMPP** (Apache + MySQL)
2. Open browser: `http://localhost/SafePassage/Admin/dashboard/pages/login.php`
3. Login with:
   - Username: `admin`
   - Password: `admin` (default)

### Android App

#### Option 1: Same WiFi Network (Recommended)

**Requirements:**
- ✅ Phone and laptop must be on the **SAME WiFi network**
- ✅ XAMPP server must be running
- ✅ Firewall must allow connections on port 80

**Steps:**

1. **Find your PC's IP address:**
   - Open Command Prompt
   - Type: `ipconfig`
   - Find **IPv4 Address** (e.g., `192.168.1.100`)

2. **Update Android App IP:**
   
   **File 1:** `app/src/main/java/com/dhruvbuildz/safepassageapp/Utils/IpConfig.kt`
   ```kotlin
   const val CURRENT_IP = "YOUR_PC_IP_HERE"  // Line 10
   ```
   Example: `const val CURRENT_IP = "192.168.1.100"`

   **File 2:** `app/src/main/res/xml/network_security_config.xml`
   - ✅ **No longer needed!** This file is now configured to allow all local IPs
   - You only need to update `IpConfig.kt`

3. **Rebuild and Install:**
   - Build the app in Android Studio
   - **Uninstall old app** from phone (important!)
   - Install new APK on phone

4. **Test Connection:**
   - Open app on phone
   - Try to login or register
   - If connection fails, check firewall settings

#### Option 2: USB Debugging (No WiFi Needed)

**Requirements:**
- ✅ USB cable to connect phone to laptop
- ✅ USB debugging enabled on phone
- ✅ XAMPP server must be running

**Steps:**

1. **Enable USB Debugging on Phone:**
   - Go to Settings → About Phone
   - Tap "Build Number" 7 times
   - Go to Settings → Developer Options
   - Enable "USB Debugging"

2. **Connect Phone via USB**

3. **Set up Port Forwarding:**
   ```bash
   adb reverse tcp:80 tcp:80
   ```

4. **Update Android App:**
   
   **File:** `app/src/main/java/com/dhruvbuildz/safepassageapp/Utils/IpConfig.kt`
   ```kotlin
   const val CURRENT_IP = "127.0.0.1"  // Line 10
   ```

5. **Rebuild and Install:**
   - Build the app in Android Studio
   - Uninstall old app from phone
   - Install new APK on phone

6. **Note:** You need to run `adb reverse tcp:80 tcp:80` every time you reconnect the phone

---

## WiFi Network Changes

### When You Change WiFi Networks

**IMPORTANT:** Phone and laptop must be on the **SAME WiFi network**!

### Steps to Update:

1. **Connect both devices to the same WiFi**

2. **Find your new PC IP address:**
   ```bash
   ipconfig
   ```
   Look for IPv4 Address

3. **Update 1 File Only:**

   **File:** `app/src/main/java/com/dhruvbuildz/safepassageapp/Utils/IpConfig.kt`
   - Line 10: Update `CURRENT_IP` to your new IP
   - Example: `const val CURRENT_IP = "192.168.1.100"`
   
   **Note:** `network_security_config.xml` is configured to allow all local IPs, so no update needed!

4. **Rebuild and Reinstall:**
   - Rebuild app in Android Studio
   - **Uninstall old app** from phone
   - Install new APK

5. **Verify XAMPP is Running:**
   - Check XAMPP Control Panel
   - Apache and MySQL should be "Running"

---

## Troubleshooting

### App Shows "Connection Timeout"

**Checklist:**
- ✅ XAMPP Apache is running
- ✅ XAMPP MySQL is running
- ✅ Phone and laptop on same WiFi
- ✅ IP address is correct in `IpConfig.kt`
- ✅ IP address is in `network_security_config.xml`
- ✅ Old app is uninstalled from phone
- ✅ New app is installed
- ✅ Windows Firewall allows port 80

**Solution:**
1. Verify IP with `ipconfig`
2. Test in browser: `http://YOUR_IP/SafePassage/Admin/dashboard/api/ping.php`
3. If browser works but app doesn't, check firewall settings
4. Try USB debugging method instead

### Database Connection Errors

**Checklist:**
- ✅ MySQL is running in XAMPP
- ✅ Database `safepassage` exists
- ✅ Database is imported correctly

**Solution:**
1. Check XAMPP MySQL status
2. Open phpMyAdmin and verify database exists
3. Re-import `safepassage.sql` if needed

### Admin Dashboard Not Loading

**Checklist:**
- ✅ Apache is running
- ✅ Files are in correct location: `D:\xampp\htdocs\SafePassage\`
- ✅ Database is imported

**Solution:**
1. Check Apache status in XAMPP
2. Verify file path is correct
3. Check browser console for errors

### App Works But Data Not Saving

**Checklist:**
- ✅ MySQL is running
- ✅ Database tables exist
- ✅ User has proper permissions

**Solution:**
1. Check MySQL status
2. Verify tables in phpMyAdmin
3. Check API endpoints are accessible

---

## Project Structure

```
SafePassage/
├── Admin/
│   └── dashboard/
│       ├── api/              # PHP API endpoints
│       ├── config/          # Database configuration
│       ├── pages/           # Admin dashboard pages
│       └── sql/             # SQL scripts
├── app/                     # Android app source code
│   └── src/main/
│       ├── java/            # Kotlin source files
│       └── res/             # Resources (XML, layouts)
├── safepassage.sql          # Database schema
└── README.md               # This file
```

---

## Important Notes

### XAMPP Server
- **MUST be running** for the app to work
- Apache handles PHP requests
- MySQL stores all data
- If you close XAMPP, the app will stop working

### Network Requirements
- Phone and laptop **must be on same WiFi** (for WiFi method)
- Or use USB debugging (no WiFi needed)
- Firewall must allow port 80 connections

### App Updates
- **Always uninstall old app** before installing new one
- This ensures latest IP configuration is loaded
- Cache can cause connection issues

### Database
- All user data is stored in MySQL
- Backup database regularly: Export from phpMyAdmin
- To reset: Re-import `safepassage.sql`

---

## Quick Reference

### Find IP Address
```bash
ipconfig
```

### Test Backend Connection
```
http://YOUR_IP/SafePassage/Admin/dashboard/api/ping.php
```

### USB Debugging Setup
```bash
adb reverse tcp:80 tcp:80
```

### Default Admin Login
- Username: `admin`
- Password: `admin`

### Database Info
- Name: `safepassage`
- User: `root`
- Password: `` (empty)

---

## Support

If you encounter issues:
1. Check XAMPP is running
2. Verify both devices on same WiFi
3. Check IP address is correct
4. Ensure old app is uninstalled
5. Review browser console / Logcat for errors

---

**Last Updated:** 2025-11-19