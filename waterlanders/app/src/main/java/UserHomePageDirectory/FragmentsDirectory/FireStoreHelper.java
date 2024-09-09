package UserHomePageDirectory.FragmentsDirectory;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class FireStoreHelper {

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public FireStoreHelper() {
        // Initialize Firestore and FirebaseAuth
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    // Method to get user data
    public void getUserData(final FirestoreCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            Log.d("FirestoreHelper", "userId: " + userId);
            DocumentReference docRef = db.collection("users").document(userId);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            callback.onSuccess(document);
                        } else {
                            callback.onFailure("Document does not exist");
                        }
                    } else {
                        callback.onFailure("Failed to retrieve data");
                    }
                }
            });
        } else {
            callback.onFailure("User not logged in");
        }
    }

    // Callback interface for Firestore data retrieval
    public interface FirestoreCallback {
        void onSuccess(DocumentSnapshot document);
        void onFailure(String errorMessage);
    }
}
