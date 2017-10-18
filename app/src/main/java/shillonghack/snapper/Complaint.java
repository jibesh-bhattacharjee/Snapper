package shillonghack.snapper;

/**
 * Created by Dell on 9/16/2017.
 */

public class Complaint {
    String complaintCaption;
    String userID;
    String complaintPriority;
    String loc;
    String UploadLocation;
    String imgURL;
    public Complaint() {

    }

    public Complaint(String complaintCaption,String loc, String userID, String complaintPriority,String imgURL,String approxLocation) {
        this.complaintCaption = complaintCaption;
        this.userID = userID;
        this.complaintPriority = complaintPriority;
        this.loc = loc;
        this.imgURL = imgURL;
        this.UploadLocation = approxLocation;
    }

    public String getComplaintCaption() {
        return complaintCaption;
    }

    public String getImgURL() {
        return imgURL;
    }

    public String getLoc() {
        return loc;
    }

    public String getUserID() {
        return userID;
    }

    public String getComplaintPriority() {
        return complaintPriority;
    }

    public void setComplaintCaption(String complaintCaption) {
        this.complaintCaption = complaintCaption;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setLoc(String loc) {
        this.loc = loc;
    }

    public void setComplaintPriority(String complaintPriority) {
        this.complaintPriority = complaintPriority;
    }

    public void setImgURL(String imgURL) {
        this.imgURL = imgURL;
    }
}
