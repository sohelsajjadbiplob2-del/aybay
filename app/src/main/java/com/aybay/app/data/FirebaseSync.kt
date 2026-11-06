package com.aybay.app.data

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

// লাইভ সিঙ্ক: দুজন একই জিমেইল দিয়ে Google Sign-In করলে দুজনের
// ফোনে একই Firestore কালেকশন দেখা যায়। অফলাইনে লেখা Room-এ জমা
// থাকে, নেট আসলে Firestore-এ পাঠানো হয় আর অন্য ফোনে আসে।
//
// SyncConfig খালি থাকলে Firebase চালু হয় না — তখন অ্যাপ শুধু
// ফোনের ভেতরের Room ডেটাবেসে চলে, সিঙ্ক বন্ধ থাকে।
class FirebaseSync(context: Context) {
    private val appContext = context.applicationContext
    private var firestore: FirebaseFirestore? = null
    private var auth: FirebaseAuth? = null

    val isAvailable: Boolean get() = firestore != null

    val isSignedIn: Boolean get() = auth?.currentUser != null

    val signedInEmail: String? get() = auth?.currentUser?.email

    init {
        if (SyncConfig.isConfigured) {
            try {
                val options = FirebaseOptions.Builder()
                    .setProjectId(SyncConfig.FIREBASE_PROJECT_ID)
                    .setApiKey(SyncConfig.FIREBASE_API_KEY)
                    .setApplicationId(SyncConfig.FIREBASE_APP_ID)
                    .build()
                val app = FirebaseApp.initializeApp(appContext, options, "aybay")
                firestore = FirebaseFirestore.getInstance(app)
                firestore?.firestoreSettings = FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .build()
                auth = FirebaseAuth.getInstance(app)
            } catch (e: Exception) {
                firestore = null
                auth = null
            }
        }
    }

    // একই জিমেইলে লগইন করা দুজনের শেয়ার্ড কালেকশন। Firestore
    // নিয়মে শুধু লগইন করা অ্যাকাউন্টের ডেটাই পড়া/লেখা যাবে।
    private fun collectionName(): String {
        val uid = auth?.currentUser?.uid ?: return ""
        return "users/$uid/transactions"
    }

    suspend fun pushTransaction(item: Transaction): Boolean {
        val db = firestore ?: return false
        val coll = collectionName()
        if (coll.isEmpty()) return false
        return try {
            val data = mapOf(
                "type" to item.type,
                "amount" to item.amount,
                "category" to item.category,
                "account" to item.account,
                "date" to item.date,
                "time" to item.time,
                "note" to item.note,
                "payer" to item.payer,
                "updatedAt" to item.updatedAt
            )
            db.collection(coll).document(item.id.toString())
                .set(data, SetOptions.merge()).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun pushDueItem(item: DueItem): Boolean {
        val db = firestore ?: return false
        val uid = auth?.currentUser?.uid ?: return false
        return try {
            val data = mapOf(
                "kind" to item.kind,
                "direction" to item.direction,
                "name" to item.name,
                "amount" to item.amount,
                "date" to item.date,
                "dueDate" to item.dueDate,
                "isPaid" to item.isPaid
            )
            db.collection("users/$uid/dues").document(item.id.toString())
                .set(data, SetOptions.merge()).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun pushNote(item: Note): Boolean {
        val db = firestore ?: return false
        val uid = auth?.currentUser?.uid ?: return false
        return try {
            val data = mapOf(
                "title" to item.title,
                "body" to item.body,
                "urgent" to item.urgent,
                "createdAt" to item.createdAt
            )
            db.collection("users/$uid/notes").document(item.id.toString())
                .set(data, SetOptions.merge()).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun signOut() {
        auth?.signOut()
    }
}
