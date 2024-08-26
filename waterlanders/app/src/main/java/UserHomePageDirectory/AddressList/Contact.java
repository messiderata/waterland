package UserHomePageDirectory.AddressList;

public class Contact {
    private String fullName;
    private String mobileNumber;
    private String address;

    public Contact(String fullName, String mobileNumber, String address) {
        this.fullName = fullName;
        this.mobileNumber = mobileNumber;
        this.address = address;
    }

    public String getFullName() {
        return fullName;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getAddress() {
        return address;
    }
}
