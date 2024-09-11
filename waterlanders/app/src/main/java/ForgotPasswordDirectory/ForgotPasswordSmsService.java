package ForgotPasswordDirectory;

import android.util.Log;

//import com.twilio.Twilio;
//import com.twilio.rest.api.v2010.account.Message;
//import com.twilio.type.PhoneNumber;

import java.util.Random;

public class ForgotPasswordSmsService {
    public static final String ACCOUNT_SID = "your_account_sid";
    public static final String AUTH_TOKEN = "your_auth_token";

//    static {
//        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
//    }

    public static void sendSms(String to, String body) {
//        Message message = Message.creator(
//                        new PhoneNumber(to),
//                        new PhoneNumber("your_twilio_phone_number"),
//                        body)
//                .create();
//        Log.d("SMS SERVICE", "Message SID: "+ message.getSid());
        Log.d("SMS SERVICE", "Message SID: ");
    }

    public static int generateFourDigitNumber() {
        Random random = new Random();
        // Generate a random number between 1000 and 9999 (inclusive)
        return 1000 + random.nextInt(9000);
    }
}
