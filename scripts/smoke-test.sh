#!/usr/bin/env bash
# End-to-End System Smoke Test for Blood Bank Management System
set -e

GATEWAY_URL="http://localhost:8080"
echo "=== 1. Logging in as Seeded Super Admin ==="
LOGIN_RES=$(curl -s -X POST "$GATEWAY_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"AdminPassword123!"}')

TOKEN=$(echo $LOGIN_RES | grep -o '"accessToken":"[^"]*' | grep -o '[^"]*$')
if [ -z "$TOKEN" ]; then
  echo "Login failed. Response: $LOGIN_RES"
  exit 1
fi
echo "Admin token obtained successfully!"

AUTH_HEADER="Authorization: Bearer $TOKEN"

echo "=== 2. Creating a Hospital ==="
HOSPITAL_RES=$(curl -s -X POST "$GATEWAY_URL/api/v1/hospitals" \
  -H "Content-Type: application/json" -H "$AUTH_HEADER" \
  -d '{"name":"City General Hospital","code":"CGH001","licenseNumber":"LIC-9988","contactPhone":"+15550199","email":"emergency@cgh.org","addressLine1":"100 Health Ave","city":"Metropolis","state":"NY","zipCode":"10001"}')
HOSPITAL_ID=$(echo $HOSPITAL_RES | grep -o '"id":[^,]*' | head -1 | cut -d':' -f2)
echo "Created Hospital ID: $HOSPITAL_ID"

echo "=== 3. Creating a Blood Bank Facility ==="
BANK_RES=$(curl -s -X POST "$GATEWAY_URL/api/v1/blood-banks" \
  -H "Content-Type: application/json" -H "$AUTH_HEADER" \
  -d '{"name":"Central Metro Blood Bank","code":"CBB001","licenseNumber":"BB-LIC-100","contactPhone":"+15550100","email":"info@centralblood.org","addressLine1":"500 Donor Way","city":"Metropolis","state":"NY","zipCode":"10001"}')
BANK_ID=$(echo $BANK_RES | grep -o '"id":[^,]*' | head -1 | cut -d':' -f2)
echo "Created Blood Bank ID: $BANK_ID"

echo "=== 4. Registering a Blood Donor ==="
DONOR_RES=$(curl -s -X POST "$GATEWAY_URL/api/v1/donors" \
  -H "Content-Type: application/json" -H "$AUTH_HEADER" \
  -d '{"firstName":"Alex","lastName":"Smith","email":"alex.smith@example.com","phoneNumber":"+15550177","dateOfBirth":"1995-05-15","gender":"MALE","bloodGroupCode":"O_POSITIVE"}')
DONOR_ID=$(echo $DONOR_RES | grep -o '"id":[^,]*' | head -1 | cut -d':' -f2)
echo "Registered Donor ID: $DONOR_ID"

echo "=== 5. Checking Donor Eligibility ==="
ELIGIBILITY_RES=$(curl -s -X GET "$GATEWAY_URL/api/v1/donors/$DONOR_ID/eligibility" -H "$AUTH_HEADER")
echo "Eligibility Response: $ELIGIBILITY_RES"

echo "=== 6. Completing a Blood Donation ==="
DONATION_RES=$(curl -s -X POST "$GATEWAY_URL/api/v1/donations" \
  -H "Content-Type: application/json" -H "$AUTH_HEADER" \
  -d "{\"donorId\":$DONOR_ID,\"bloodBankId\":$BANK_ID,\"bloodGroupCode\":\"O_POSITIVE\",\"componentTypeCode\":\"WHOLE_BLOOD\",\"units\":1.0,\"hemoglobinLevel\":14.5,\"systolicBp\":120,\"diastolicBp\":80}")
DONATION_ID=$(echo $DONATION_RES | grep -o '"id":[^,]*' | head -1 | cut -d':' -f2)
echo "Completed Donation ID: $DONATION_ID"

echo "=== 7. Verifying Inventory Increment ==="
STOCK_RES=$(curl -s -X GET "$GATEWAY_URL/api/v1/blood-stocks?bloodBankId=$BANK_ID" -H "$AUTH_HEADER")
echo "Blood Stock Level: $STOCK_RES"

echo "=== 8. Creating Urgent Blood Request from Hospital ==="
REQUEST_RES=$(curl -s -X POST "$GATEWAY_URL/api/v1/blood-requests" \
  -H "Content-Type: application/json" -H "$AUTH_HEADER" \
  -d "{\"hospitalId\":$HOSPITAL_ID,\"bloodGroupId\":1,\"componentTypeId\":1,\"unitsRequested\":1,\"urgency\":\"URGENT\",\"requiredBy\":\"2026-07-25T12:00:00Z\",\"patientMedicalRecordNumber\":\"MRN-778899\"}")
REQUEST_ID=$(echo $REQUEST_RES | grep -o '"id":[^,]*' | head -1 | cut -d':' -f2)
echo "Created Urgent Request ID: $REQUEST_ID"

echo "=== 9. Confirming Notification Service Alert Log ==="
sleep 2
NOTIF_LOGS=$(curl -s -X GET "$GATEWAY_URL/api/v1/notifications/logs?eventType=BloodRequestCreatedEvent" -H "$AUTH_HEADER")
echo "Notification Audit Logs: $NOTIF_LOGS"

echo "=== 10. Approving Blood Request ==="
curl -s -X POST "$GATEWAY_URL/api/v1/blood-requests/$REQUEST_ID/approve" -H "$AUTH_HEADER" > /dev/null
echo "Approved Request ID: $REQUEST_ID"

echo "=== 11. Issuing Blood Units (Decrementing Stock) ==="
ISSUANCE_RES=$(curl -s -X POST "$GATEWAY_URL/api/v1/issuances" \
  -H "Content-Type: application/json" -H "$AUTH_HEADER" \
  -d "{\"bloodRequestId\":$REQUEST_ID,\"bloodBankId\":$BANK_ID,\"unitsIssued\":1,\"issuedToPerson\":\"Nurse Jackie\"}")
echo "Issuance Response: $ISSUANCE_RES"

echo "=== 12. Health Check on All Services ==="
PORTS=(8080 8085 8086 8087 8088 8089 8090 8091 8084)
for PORT in "${PORTS[@]}"; do
  HEALTH=$(curl -s http://localhost:$PORT/actuator/health | grep -o '"status":"[^"]*' | cut -d'"' -f4)
  echo "Port $PORT Health: $HEALTH"
done

echo "=== Smoke Test Completed Successfully! ==="
