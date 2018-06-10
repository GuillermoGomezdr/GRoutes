package org.example.willy.GRoutes;

public class RouteInfo {

    private String routeName;
    private String userName;
    private String userID;

    public RouteInfo(String routeName, String userName, String userID) {
        this.routeName = routeName;
        this.userName = userName;
        this.userID = userID;
    }


    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
