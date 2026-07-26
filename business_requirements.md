# Blood Bank Management System - Business Requirements Documentation

This document explains the functional scope, key personas, and core business workflows of the Blood Bank Management System (BBMS).

---

## 1. Core Objectives
The Blood Bank Management System is designed to solve critical operational challenges in blood banking:
- **Minimize Wastage**: Prevent blood bags from expiring by tracking real-time freshness and alert schedules.
- **Enforce Safety Compliance**: Mandate strict health screening for donors and quarantine testing before blood enters inventory.
- **Accelerate Emergency Response**: Facilitate compatibility-based reservation and rapid hospital request fulfillment.
- **Automate Communication**: Notify registered donors during critical stock shortages.

---

## 2. Key User Personas (RBAC Roles)

The system leverages Role-Based Access Control (RBAC) to enforce security and separate operational duties:

| Persona | Allowed Business Operations |
| :--- | :--- |
| **System Administrator** | User account management, security audits, database cleanups, and system parameter configuration. |
| **Blood Bank Staff / Technician** | Donor pre-screening, registering blood collections, entering laboratory screening test results, and managing stock. |
| **Registered Donor** | Scheduling donation appointments, tracking donation history, and checking personal health check reports. |
| **Hospital Representative** | Submitting blood requests, tracking request fulfillment, and registering patient transfusion/compatibility details. |

---

## 3. Microservice Business Domains & Specifications

### A. User & Authentication Service (`user-service`)
Responsible for Identity and Access Management (IAM) across the system.
- **Secure Registration & Login**: Multi-role login securing endpoints using JSON Web Tokens (JWT).
- **Access Control Enforcer**: Validates operations against user roles (Admin, Staff, Donor, Hospital).
- **Profile Registry**: Manages profile metadata (contact details, location preference, registration timestamp).

### B. Donor & Donation Service (`donor-service`)
Handles relationship management and collection events.
- **Donor Registration**: Records physical stats (age, weight, base blood group).
- **Pre-Donation Eligibility Screening**:
  - Implements a mandatory checklist (hemoglobin count, pulse, blood pressure, recent travel history).
  - Flags donors as **Eligible** or **Deferred** (temporary or permanent debarment based on medical criteria).
- **Appointment Booking**: Online calendar allowing donors to select time slots at specific collection centers.
- **Collection Event Logger**: Links the donor ID, collection timestamp, bag serial number, and collection type (Whole Blood, Plasma, Platelets).

### C. Blood Inventory & Request Service (`inventory-service`)
Maintains inventory integrity, testing compliance, and stock distribution.
- **Processing & Fractionation**: Tracks splitting raw blood bags into components (Red Cells, Plasma, Platelets).
- **Laboratory Safety Quarantine**:
  - Newly collected blood remains in **Quarantine** status.
  - Staff must log screening results for infectious diseases (HIV, Hepatitis B/C, Syphilis).
  - Only bags with all-negative results are changed to **Active Inventory**. Positive bags are flagged as **Biohazard** and routed for disposal.
- **Stock Rotation & Expiration Tracking**:
  - Real-time stock counts categorized by blood group (A±, B±, AB±, O±).
  - Automatic expiration alerts: Red Blood Cells (35–42 days), Platelets (5 days), Fresh Frozen Plasma (1 year).
- **Hospital Requests & Universal Matching**:
  - Evaluates hospital requests against active inventory.
  - Implements matching matrix (e.g., O- universal donor, AB+ universal recipient) to suggest alternative compatibility matching when precise group stock is low.
  - Reserves matched blood bags and handles dispatch tracking.

### D. Notification & Dispatch Service (`notification-service`)
Fosters engagement and handles critical system alerts asynchronously.
- **Appointment Reminders**: Automated notifications (Email/SMS) 24 hours prior to donation appointments.
- **Stock Shortage Broadcasting**: Monitors inventory thresholds and automatically blasts alerts to eligible local donors matching the scarce blood type.
- **Emergency Matching Notifications**: Alerts matching donors in the immediate vicinity when an urgent hospital request is made.
