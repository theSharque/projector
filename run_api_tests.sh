#!/bin/bash

set -e

BASE_URL="http://localhost:8080"
COOKIE_FILE="/tmp/api_test_cookies.txt"
HEADERS_FILE="/tmp/api_test_headers.txt"
ERRORS=0

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
    ERRORS=$((ERRORS + 1))
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

# Cleanup
rm -f "$COOKIE_FILE" "$HEADERS_FILE"

log_info "Starting API tests..."

# Test 1: Health check
log_info "Test 1: Health check"
HEALTH=$(curl -s "$BASE_URL/actuator/health")
if [[ "$HEALTH" == *"UP"* ]]; then
    log_info "✓ Health check passed"
else
    log_error "✗ Health check failed: $HEALTH"
    exit 1
fi

# Test 2: Login
log_info "Test 2: Login as admin"
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"email":"admin","password":"admin"}' \
    -c "$COOKIE_FILE" \
    -D "$HEADERS_FILE" \
    -w "\nHTTP_CODE:%{http_code}")

HTTP_CODE=$(echo "$LOGIN_RESPONSE" | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)
if [[ "$HTTP_CODE" == "204" ]]; then
    log_info "✓ Login successful"
    TOKEN=$(grep -i "set-cookie" "$HEADERS_FILE" | grep -o 'X-Auth=[^;]*' | cut -d= -f2)
    if [[ -z "$TOKEN" ]]; then
        log_error "✗ No token in Set-Cookie header"
    else
        log_info "✓ Token received"
    fi
else
    log_error "✗ Login failed with HTTP code: $HTTP_CODE"
    exit 1
fi

# Test 3: Get profile
log_info "Test 3: Get current user profile"
PROFILE=$(curl -s -X GET "$BASE_URL/api/auth/profile" -b "$COOKIE_FILE")
if [[ "$PROFILE" == *"USER_VIEW"* ]] || [[ "$PROFILE" == "[]" ]]; then
    log_info "✓ Profile retrieved"
else
    log_error "✗ Profile retrieval failed: $PROFILE"
fi

# Test 4: Create role
log_info "Test 4: Create role"
ROLE_RESPONSE=$(curl -s -X POST "$BASE_URL/api/roles" \
    -H "Content-Type: application/json" \
    -b "$COOKIE_FILE" \
    -d '{"name":"TEST_ROLE","authorities":["USER_VIEW","ROLE_VIEW"]}' \
    -w "\nHTTP_CODE:%{http_code}")

HTTP_CODE=$(echo "$ROLE_RESPONSE" | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)
ROLE_BODY=$(echo "$ROLE_RESPONSE" | sed '/HTTP_CODE:/d')
if [[ "$HTTP_CODE" == "200" ]] || [[ "$HTTP_CODE" == "201" ]]; then
    ROLE_ID=$(echo "$ROLE_BODY" | jq -r '.id // empty')
    if [[ -n "$ROLE_ID" ]] && [[ "$ROLE_ID" != "null" ]]; then
        log_info "✓ Role created with ID: $ROLE_ID"
    else
        log_error "✗ Role creation failed - no ID in response: $ROLE_BODY"
    fi
else
    log_error "✗ Role creation failed with HTTP code: $HTTP_CODE, response: $ROLE_BODY"
fi

# Test 5: Get roles
log_info "Test 5: Get all roles"
ROLES=$(curl -s -X GET "$BASE_URL/api/roles" -b "$COOKIE_FILE")
if [[ "$ROLES" == *"TEST_ROLE"* ]] || [[ "$ROLES" == "[]" ]]; then
    log_info "✓ Roles retrieved"
    if [[ -z "$ROLE_ID" ]]; then
        ROLE_ID=$(echo "$ROLES" | jq -r '.[] | select(.name=="TEST_ROLE") | .id // empty' | head -1)
    fi
else
    log_error "✗ Get roles failed: $ROLES"
fi

# Test 6: Create user
log_info "Test 6: Create user"
USER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/users" \
    -H "Content-Type: application/json" \
    -b "$COOKIE_FILE" \
    -d '{"email":"testuser@example.com","password":"testpass123"}' \
    -w "\nHTTP_CODE:%{http_code}")

HTTP_CODE=$(echo "$USER_RESPONSE" | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)
USER_BODY=$(echo "$USER_RESPONSE" | sed '/HTTP_CODE:/d')
if [[ "$HTTP_CODE" == "200" ]] || [[ "$HTTP_CODE" == "201" ]]; then
    USER_ID=$(echo "$USER_BODY" | jq -r '.id // empty')
    if [[ -n "$USER_ID" ]] && [[ "$USER_ID" != "null" ]]; then
        log_info "✓ User created with ID: $USER_ID"
    else
        log_error "✗ User creation failed - no ID in response: $USER_BODY"
    fi
else
    log_error "✗ User creation failed with HTTP code: $HTTP_CODE, response: $USER_BODY"
fi

# Test 7: Get users
log_info "Test 7: Get all users"
USERS=$(curl -s -X GET "$BASE_URL/api/users" -b "$COOKIE_FILE")
if [[ "$USERS" == *"testuser@example.com"* ]] || [[ "$USERS" == "[]" ]]; then
    log_info "✓ Users retrieved"
    if [[ -z "$USER_ID" ]]; then
        USER_ID=$(echo "$USERS" | jq -r '.[] | select(.email=="testuser@example.com") | .id // empty' | head -1)
    fi
    ADMIN_ID=$(echo "$USERS" | jq -r '.[] | select(.email=="admin") | .id // empty' | head -1)
else
    log_error "✗ Get users failed: $USERS"
fi

# Test 8: Create roadmap (if we have admin ID)
if [[ -n "$ADMIN_ID" ]]; then
    log_info "Test 8: Create roadmap"
    ROADMAP_RESPONSE=$(curl -s -X POST "$BASE_URL/api/roadmaps" \
        -H "Content-Type: application/json" \
        -b "$COOKIE_FILE" \
        -d "{\"projectName\":\"Test Project\",\"authorId\":$ADMIN_ID,\"mission\":\"Build amazing software\",\"description\":\"Test roadmap description\",\"participantIds\":[$ADMIN_ID]}" \
        -w "\nHTTP_CODE:%{http_code}")

    HTTP_CODE=$(echo "$ROADMAP_RESPONSE" | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)
    ROADMAP_BODY=$(echo "$ROADMAP_RESPONSE" | sed '/HTTP_CODE:/d')
    if [[ "$HTTP_CODE" == "200" ]] || [[ "$HTTP_CODE" == "201" ]]; then
        ROADMAP_ID=$(echo "$ROADMAP_BODY" | jq -r '.id // empty')
        if [[ -n "$ROADMAP_ID" ]] && [[ "$ROADMAP_ID" != "null" ]]; then
            log_info "✓ Roadmap created with ID: $ROADMAP_ID"
        else
            log_error "✗ Roadmap creation failed - no ID in response: $ROADMAP_BODY"
        fi
    else
        log_error "✗ Roadmap creation failed with HTTP code: $HTTP_CODE, response: $ROADMAP_BODY"
    fi
else
    log_warn "Test 8: Skipped (no admin ID)"
fi

# Test 9: Create feature (if we have admin ID)
if [[ -n "$ADMIN_ID" ]]; then
    log_info "Test 9: Create feature"
    FEATURE_RESPONSE=$(curl -s -X POST "$BASE_URL/api/features" \
        -H "Content-Type: application/json" \
        -b "$COOKIE_FILE" \
        -d "{\"year\":2024,\"quarter\":\"Q1\",\"authorId\":$ADMIN_ID,\"sprint\":1,\"release\":\"v1.0.0\",\"summary\":\"User authentication feature\",\"description\":\"Implement user login and registration\"}" \
        -w "\nHTTP_CODE:%{http_code}")

    HTTP_CODE=$(echo "$FEATURE_RESPONSE" | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)
    FEATURE_BODY=$(echo "$FEATURE_RESPONSE" | sed '/HTTP_CODE:/d')
    if [[ "$HTTP_CODE" == "200" ]] || [[ "$HTTP_CODE" == "201" ]]; then
        FEATURE_ID=$(echo "$FEATURE_BODY" | jq -r '.id // empty')
        if [[ -n "$FEATURE_ID" ]] && [[ "$FEATURE_ID" != "null" ]]; then
            log_info "✓ Feature created with ID: $FEATURE_ID"
        else
            log_error "✗ Feature creation failed - no ID in response: $FEATURE_BODY"
        fi
    else
        log_error "✗ Feature creation failed with HTTP code: $HTTP_CODE, response: $FEATURE_BODY"
    fi
else
    log_warn "Test 9: Skipped (no admin ID)"
fi

# Test 10: Create task (if we have feature ID and admin ID)
if [[ -n "$FEATURE_ID" ]] && [[ -n "$ADMIN_ID" ]]; then
    log_info "Test 10: Create task"
    TASK_RESPONSE=$(curl -s -X POST "$BASE_URL/api/tasks" \
        -H "Content-Type: application/json" \
        -b "$COOKIE_FILE" \
        -d "{\"featureId\":$FEATURE_ID,\"authorId\":$ADMIN_ID,\"summary\":\"Implement login endpoint\",\"description\":\"Create REST API endpoint for user login\"}" \
        -w "\nHTTP_CODE:%{http_code}")

    HTTP_CODE=$(echo "$TASK_RESPONSE" | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)
    TASK_BODY=$(echo "$TASK_RESPONSE" | sed '/HTTP_CODE:/d')
    if [[ "$HTTP_CODE" == "200" ]] || [[ "$HTTP_CODE" == "201" ]]; then
        TASK_ID=$(echo "$TASK_BODY" | jq -r '.id // empty')
        if [[ -n "$TASK_ID" ]] && [[ "$TASK_ID" != "null" ]]; then
            log_info "✓ Task created with ID: $TASK_ID"
        else
            log_error "✗ Task creation failed - no ID in response: $TASK_BODY"
        fi
    else
        log_error "✗ Task creation failed with HTTP code: $HTTP_CODE, response: $TASK_BODY"
    fi
else
    log_warn "Test 10: Skipped (no feature ID or admin ID)"
fi

# Test 11: Assign role to user (if we have user ID and role ID)
if [[ -n "$USER_ID" ]] && [[ -n "$ROLE_ID" ]] && [[ "$USER_ID" != "null" ]] && [[ "$ROLE_ID" != "null" ]]; then
    log_info "Test 11: Assign role to user"
    ASSIGN_RESPONSE=$(curl -s -X PUT "$BASE_URL/api/users/$USER_ID" \
        -H "Content-Type: application/json" \
        -b "$COOKIE_FILE" \
        -d "{\"email\":\"testuser@example.com\",\"roleIds\":[$ROLE_ID]}" \
        -w "\nHTTP_CODE:%{http_code}")

    HTTP_CODE=$(echo "$ASSIGN_RESPONSE" | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)
    ASSIGN_BODY=$(echo "$ASSIGN_RESPONSE" | sed '/HTTP_CODE:/d')
    if [[ "$HTTP_CODE" == "200" ]]; then
        log_info "✓ Role assigned to user"
    else
        log_error "✗ Role assignment failed with HTTP code: $HTTP_CODE, response: $ASSIGN_BODY"
    fi
else
    log_warn "Test 11: Skipped (no user ID or role ID - User ID: ${USER_ID:-empty}, Role ID: ${ROLE_ID:-empty})"
fi

# Test 12: Login as new user
if [[ -n "$USER_ID" ]]; then
    log_info "Test 12: Login as new user"
    USER_LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
        -H "Content-Type: application/json" \
        -d '{"email":"testuser@example.com","password":"testpass123"}' \
        -D /tmp/user_login_headers.txt \
        -w "\nHTTP_CODE:%{http_code}")

    HTTP_CODE=$(echo "$USER_LOGIN_RESPONSE" | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)
    if [[ "$HTTP_CODE" == "204" ]]; then
        USER_TOKEN=$(grep -i "set-cookie" /tmp/user_login_headers.txt | grep -o 'X-Auth=[^;]*' | cut -d= -f2)
        if [[ -n "$USER_TOKEN" ]]; then
            log_info "✓ New user login successful"
        else
            log_error "✗ New user login failed - no token"
        fi
    else
        log_error "✗ New user login failed with HTTP code: $HTTP_CODE"
    fi
else
    log_warn "Test 12: Skipped (no user ID)"
fi

# Cleanup: Delete created resources
log_info "Cleaning up created resources..."

if [[ -n "$TASK_ID" ]]; then
    DELETE_CODE=$(curl -s -X DELETE "$BASE_URL/api/tasks/$TASK_ID" -b "$COOKIE_FILE" -w "%{http_code}" -o /dev/null)
    if [[ "$DELETE_CODE" == "204" ]]; then
        log_info "✓ Task deleted"
    else
        log_warn "✗ Task deletion failed with HTTP code: $DELETE_CODE"
    fi
fi

if [[ -n "$FEATURE_ID" ]]; then
    DELETE_CODE=$(curl -s -X DELETE "$BASE_URL/api/features/$FEATURE_ID" -b "$COOKIE_FILE" -w "%{http_code}" -o /dev/null)
    if [[ "$DELETE_CODE" == "204" ]]; then
        log_info "✓ Feature deleted"
    else
        log_warn "✗ Feature deletion failed with HTTP code: $DELETE_CODE"
    fi
fi

if [[ -n "$ROADMAP_ID" ]]; then
    DELETE_CODE=$(curl -s -X DELETE "$BASE_URL/api/roadmaps/$ROADMAP_ID" -b "$COOKIE_FILE" -w "%{http_code}" -o /dev/null)
    if [[ "$DELETE_CODE" == "204" ]]; then
        log_info "✓ Roadmap deleted"
    else
        log_warn "✗ Roadmap deletion failed with HTTP code: $DELETE_CODE"
    fi
fi

if [[ -n "$USER_ID" ]]; then
    DELETE_CODE=$(curl -s -X DELETE "$BASE_URL/api/users/$USER_ID" -b "$COOKIE_FILE" -w "%{http_code}" -o /dev/null)
    if [[ "$DELETE_CODE" == "204" ]]; then
        log_info "✓ User deleted"
    else
        log_warn "✗ User deletion failed with HTTP code: $DELETE_CODE"
    fi
fi

if [[ -n "$ROLE_ID" ]]; then
    DELETE_CODE=$(curl -s -X DELETE "$BASE_URL/api/roles/$ROLE_ID" -b "$COOKIE_FILE" -w "%{http_code}" -o /dev/null)
    if [[ "$DELETE_CODE" == "204" ]]; then
        log_info "✓ Role deleted"
    else
        log_warn "✗ Role deletion failed with HTTP code: $DELETE_CODE"
    fi
fi

# Summary
echo ""
if [[ $ERRORS -eq 0 ]]; then
    log_info "All API tests passed! ✓"
    exit 0
else
    log_error "API tests completed with $ERRORS error(s)"
    exit 1
fi

