package ForgotPasswordDirectory;

import android.content.Context;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Random;

public class ForgotPasswordLocalSmsService {

    public static void sendSMS(Context context, String phoneNumber, String messageContent) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, messageContent, null, null);
            Log.d("ForgotPasswordLocalSmsService", "SMS sent successfully.");
            Toast.makeText(context, "OTP sent successfully.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("ForgotPasswordLocalSmsService", "SMS failed to send.", e);
            Toast.makeText(context, "Failed to send OTP. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    public static int generateFourDigitNumber() {
        Random random = new Random();
        // Generate a random number between 1000 and 9999 (inclusive)
        return 1000 + random.nextInt(9000);
    }
}
