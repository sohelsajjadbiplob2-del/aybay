package com.aybay.app.data

// Firebase লাইভ সিঙ্কের সেটিংস।
//
// V1-এ এই মানগুলো খালি রাখা আছে, তাই সিঙ্ক বন্ধ থাকে আর অ্যাপ
// পুরোপুরি অফলাইনে চলে। লাইভ সিঙ্ক চালু করতে যা লাগবে:
//  1. Firebase Console-এ (console.firebase.google.com) নতুন প্রজেক্ট বানানো,
//     package name: com.aybay.app
//  2. Google Sign-In চালু করা (Authentication > Sign-in method)
//  3. Cloud Firestore চালু করা (production mode, নিচের নিয়মে শুধু
//     একই Google অ্যাকাউন্টে লগইন করা দুটি ফোনই ডেটা পড়তে/লিখতে পারবে)
//  4. প্রজেক্টের Web API key, Application ID আর OAuth client ID
//     এখানে বসানো (বা app/google-services.json ফাইল যোগ করা)
//
// এই ফাইলে কোনো আসল কী লেখা নেই — কী ছাড়া অ্যাপ বিল্ড হয় ও চলে,
// শুধু সিঙ্ক বন্ধ থাকে। কী বসালেই পরের বিল্ড থেকে লাইভ সিঙ্ক চালু হবে।
object SyncConfig {
    const val FIREBASE_PROJECT_ID: String = ""
    const val FIREBASE_API_KEY: String = ""
    const val FIREBASE_APP_ID: String = ""
    const val GOOGLE_OAUTH_CLIENT_ID: String = ""

    val isConfigured: Boolean
        get() = FIREBASE_PROJECT_ID.isNotBlank() &&
            FIREBASE_API_KEY.isNotBlank() &&
            FIREBASE_APP_ID.isNotBlank()
}
