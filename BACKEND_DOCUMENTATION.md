# Backend Integration Documentation - Swipe Right Accept/Reject Chat Feature

This document serves as the official integration guide for the backend engineering team to support the **Swipe Right Accept/Reject Chat Request** feature implemented in the Android application.

---

## 1. Overview & Business Flow

1. **User A Sends Initial Message / Connects**:
   - User A swipes right on User B or sends a message request.
   - A chat room record is created with `status = "PENDING"` and `initiated_by_uid = "user_a_id"`.
   - On User A's Android device, the chat input bar is **locked** with the banner: *"Waiting for recipient to accept message request..."*.
2. **User B Swipes Right to Accept**:
   - User B sees the pending request in their chat list or request banner.
   - User B swipes right (or clicks Accept).
   - Android app calls `POST /explore/swipe-match/action` sending `{ "action": "ACCEPT", "targetUserId": "user_a_id", "roomId": "room_123" }`.
   - Backend updates chat room status to `"ACCEPTED"`.
   - Backend broadcasts WebSocket event `CHAT_UPDATED` to User A.
3. **Real-time Unlocking**:
   - User A's Android app receives the `CHAT_UPDATED` WebSocket event and updates local Room DB.
   - The chat input bar on User A's screen **instantly unlocks**, allowing both users to exchange messages freely.

---

## 2. Database Schema Requirements

### Table / Collection: `chat_rooms`

Ensure your database entity includes the following fields:

| Field | Type | Description |
| :--- | :--- | :--- |
| `roomId` | String / UUID | Primary Key |
| `userAId` | String / UUID | Participant User A UID |
| `userBId` | String / UUID | Participant User B UID |
| `status` | String / Enum | Current room status: `"PENDING"`, `"ACCEPTED"`, or `"REJECTED"` |
| `initiatedByUid` | String / UUID | UID of the user who initiated the conversation/request |
| `updatedAt` | Timestamp / Long | Last modification timestamp |

---

## 3. REST API Contracts

### 1. Perform Swipe / Accept Request
- **Endpoint**: `POST /explore/swipe-match/action`
- **Headers**:
  - `Content-Type: application/json`
  - `Authorization: Bearer <JWT_TOKEN>`

#### Request Payload
```json
{
  "action": "ACCEPT",
  "targetUserId": "user_123",
  "roomId": "room_abc789",
  "note": null
}
```

> **Note**: `cardType` parameter is NOT included in the request payload.

#### Backend Business Rules
1. **Pre-Check (Idempotency)**:
   - Query `chat_rooms` table for an existing room between the authenticated user and `targetUserId`.
   - If room status is already `"ACCEPTED"`, immediately return `{ "status": "ACCEPTED", "roomId": "room_abc789" }` without processing duplicate DB updates.
2. **Accept Request**:
   - Update `chat_rooms` status to `"ACCEPTED"`.
   - Emit `CHAT_UPDATED` WebSocket event to both participants.
3. **New Connection Request (`action == "CONNECT"`)**:
   - Create a new room with `status = "PENDING"`, `initiatedByUid = currentUserId`.

#### Response Payload (`200 OK`)
```json
{
  "status": "ACCEPTED",
  "roomId": "room_abc789"
}
```

---

### 2. Fetch Room Status (HTTP Fallback Sync)
- **Endpoint**: `GET /chat/rooms/{roomId}/status`
- **Headers**:
  - `Authorization: Bearer <JWT_TOKEN>`

#### Response Payload (`200 OK`)
```json
{
  "roomId": "room_abc789",
  "status": "ACCEPTED"
}
```

---

## 4. WebSocket Event Contract

### Event: `CHAT_UPDATED`
Emit this WebSocket event to the connected WebSocket sessions of both participants (`targetUserId` and `currentUserId`) when a chat room's status changes.

#### WebSocket Event JSON Payload
```json
{
  "eventType": "CHAT_UPDATED",
  "payload": {
    "roomId": "room_abc789",
    "status": "ACCEPTED"
  }
}
```

---

## 5. Summary Checklist for Backend Developer

- [ ] Add/verify `status` (`"PENDING"`, `"ACCEPTED"`, `"REJECTED"`) column in `chat_rooms` database table.
- [ ] Add/verify `initiated_by_uid` column in `chat_rooms` database table.
- [ ] Handle `POST /explore/swipe-match/action` endpoint for `"ACCEPT"` and `"CONNECT"` actions.
- [ ] Implement idempotency pre-check on `POST /explore/swipe-match/action` (return existing `"ACCEPTED"` status if already accepted).
- [ ] Add `GET /chat/rooms/{roomId}/status` endpoint.
- [ ] Emit `CHAT_UPDATED` event over WebSocket when room status changes to `"ACCEPTED"`.
