package com.iota.campusX.Feature.Notification.data

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.iota.campusX.Feature.Chats.data.ChatMessage
import com.iota.campusX.Feature.Chats.data.ID
import com.iota.campusX.Feature.Notification.domain.GetNotification
import com.iota.campusX.Feature.Notification.domain.NotificationRepository
import com.iota.campusX.Feature.Post.domain.UseCases.GetSinglePostByIdUseCase
import com.iota.campusX.Feature.Post.domain.repository.PostRepositoryInterface
import com.iota.campusX.Feature.UserProfile.domain.repository.UserProfileRepository
import com.iota.campusX.Utils.ResultState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class NotificationImpl(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase,
    private val getSinglePostByIdUseCase: GetSinglePostByIdUseCase,
    private val userProfileRepository: UserProfileRepository
) : NotificationRepository {

    override suspend fun fetchPagedNotification(): Flow<PagingData<GetNotification>> {

        val query = firestore.collection("Users")
            .document(auth.currentUser?.uid ?:"")
            .collection("Notifications")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(10)


        return Pager(
            config = PagingConfig(
                pageSize = 10,
                prefetchDistance = 1
            ),
            pagingSourceFactory = {
                NotificationPagingSource(
                    newsQuery = query,
                    firestore = firestore,
                    getSinglePostByIdUseCase = getSinglePostByIdUseCase,
                    userProfileRepository = userProfileRepository,
                    auth = auth
                )
            }
        ).flow

    }

    override fun markNotificationAsRead() {

        val currentUser = auth.currentUser ?: return

        val userNotificationsRef = firestore.collection("Users")
            .document(currentUser.uid)
            .collection("Notifications")

        userNotificationsRef
            .whereEqualTo("read", false)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val batch = firestore.batch()
                    for (document in querySnapshot.documents) {
                        batch.update(document.reference, "read", true)
                    }
                    batch.commit()
                        .addOnSuccessListener {

                        }
                        .addOnFailureListener { e ->

                        }
                }
            }
            .addOnFailureListener { e ->

            }
    }

    override fun markRequestNotificationAsRead() {

        val currentUser = auth.currentUser ?: return

        val userNotificationsRef = firestore.collection("Users")
            .document(currentUser.uid)
            .collection("RequestNotification")

        userNotificationsRef
            .whereEqualTo("read", false)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val batch = firestore.batch()
                    for (document in querySnapshot.documents) {
                        batch.update(document.reference, "read", true)
                    }
                    batch.commit()
                        .addOnSuccessListener {

                        }
                        .addOnFailureListener { e ->

                        }
                }
            }
            .addOnFailureListener { e ->

            }
    }

    override fun getNotificationCount(): Flow<ResultState<Int>> {
        return callbackFlow {
            val currentUser = auth.currentUser ?: return@callbackFlow
            val userNotificationsRef = firestore.collection("Users")
                .document(currentUser.uid)
                .collection("Notifications")
            userNotificationsRef.whereEqualTo("read", false)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(ResultState.Error(error.message.toString()))
                    } else {
                        val unreadCount = snapshot?.size() ?: 0
                        trySend(ResultState.Success(unreadCount))

                    }
                }
            awaitClose()

        }
    }

    override fun getRequestNotificationCount(): Flow<ResultState<Int>> {

        return callbackFlow {

            val currentUser = auth.currentUser ?: return@callbackFlow

            val userNotificationsRef = firestore.collection("Users")
                .document(currentUser.uid)
                .collection("RequestNotification")

            userNotificationsRef.whereEqualTo("read", false)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(ResultState.Error(error.message.toString()))
                    } else {
                        val unreadCount = snapshot?.size() ?: 0
                        trySend(ResultState.Success(unreadCount))

                    }
                }
            awaitClose()
        }

    }

    override suspend fun deleteNotification(notificationId: String): Result<Unit> {
        return try {
            firestore.collection("Users")
                .document(auth.currentUser!!.uid)
                .collection("Notifications")
                .document(notificationId)
                .delete()
                .await()
            Result.success(Unit)
        }catch (e: Exception){
            Result.failure(e)
        }
    }

    override fun observeTotalUnreadCount(): Flow<Int> = callbackFlow {

        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            trySend(0)
            close()
            return@callbackFlow
        }

        val roomListeners = mutableMapOf<String, ValueEventListener>()

        // Use coroutine to fetch user chat rooms once
        firestore.collection("Chats").document(currentUserId).collection("Messages")
            .get()
            .addOnSuccessListener { messageDocs ->
                for (doc in messageDocs) {
                    val idData = doc.toObject(ID::class.java)
                    val roomId = idData.roomId

                    if (roomId.isNotEmpty()) {
                        val messagesRef = database.getReference("ChatRoom").child(roomId).child("messages")

                        val listener = object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                var roomUnread = 0
                                for (child in snapshot.children) {
                                    val message = child.getValue(ChatMessage::class.java)
                                    if (message != null && message.senderId != currentUserId && !message.read) {
                                        roomUnread++
                                    }
                                }

                                // Update total unread count by recomputing across all rooms
                                roomListeners[roomId] = this // save listener for cleanup
                                val allCounts = mutableListOf<Int>()
                                roomListeners.keys.forEach { rid ->
                                    val ref = database.getReference("ChatRoom").child(rid).child("messages")
                                    ref.get().addOnSuccessListener { data ->
                                        var count = 0
                                        for (m in data.children) {
                                            val msg = m.getValue(ChatMessage::class.java)
                                            if (msg != null && msg.senderId != currentUserId && !msg.read) {
                                                count++
                                            }
                                        }
                                        allCounts.add(count)
                                        if (allCounts.size == roomListeners.size) {
                                            trySend(allCounts.sum()) // emit the total
                                        }
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {}
                        }

                        messagesRef.addValueEventListener(listener)
                        roomListeners[roomId] = listener
                    }
                }
            }
            .addOnFailureListener {
                trySend(0)
                close()
            }

        awaitClose {
            roomListeners.forEach { (roomId, listener) ->
                database.getReference("ChatRoom").child(roomId).child("messages")
                    .removeEventListener(listener)
            }
        }
    }

    override suspend fun createNotification(createNotification: CreateNotification, creatorId: String): Result<Unit> {
        return try {

            val notificationsRef = firestore.collection("Users")
                .document(creatorId)
                .collection("Notifications")

            Log.d("CreateNotification", "createNotification: $createNotification")


            when (createNotification) {

                is CreateNotification.CommentNotification -> {

                    val docId = createNotification.postId + createNotification.type
                    val docRef = notificationsRef.document(docId)

                    val snapshot = docRef.get().await()

                    if (snapshot.exists()) {
                        // Append new comment to existing array, mark unread
                        firestore.runBatch { batch ->
                            batch.update(
                                docRef,
                                "commentContent",
                                FieldValue.arrayUnion(*createNotification.commentContent.toTypedArray())
                            )
                            batch.update(docRef, "read", false)
                            batch.update(docRef, "createdAt", FieldValue.serverTimestamp())
                        }.await()
                    } else {
                        // First notification → ensure commentContent is always a list
                        docRef.set(
                            createNotification.copy(
                                commentContent = listOfNotNull(createNotification.commentContent.firstOrNull())
                            )
                        ).await()
                    }
                }

                is CreateNotification.ConnectionRequestNotification -> {
                    // Use requesterId (auth.uid) as document ID for uniqueness
                    val requesterId = auth.currentUser?.uid ?: return Result.failure(
                        IllegalStateException("No authenticated user")
                    )

                    firestore.collection("Users")
                        .document(creatorId)
                        .collection("RequestNotification")
                        .document(createNotification.notificationId)
                        .set(createNotification)

                    notificationsRef.document(requesterId)
                        .set(createNotification)
                        .await()
                }

                is CreateNotification.LikeNotification -> {

                    val docId = createNotification.postId + createNotification.type
                    val docRef = notificationsRef.document(docId)

                    val snapshot = docRef.get().await()

                    if (snapshot.exists()) {
                        firestore.runBatch { batch ->
                            batch.update(
                                docRef,
                                "likes",
                                FieldValue.arrayUnion(auth.currentUser?.uid ?: "")
                            )
                            batch.update(docRef, "read", false)
                            batch.update(docRef, "createdAt", FieldValue.serverTimestamp())
                        }.await()
                    } else {
                        docRef.set(
                            createNotification.copy(
                                likes = listOfNotNull(auth.currentUser?.uid)
                            )
                        ).await()
                    }
                }
            }

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}