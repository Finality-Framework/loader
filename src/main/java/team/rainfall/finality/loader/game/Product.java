package team.rainfall.finality.loader.game;

public enum Product{
    AoH3("2772750"),
    AoH2("603850"),
    AoH2DE("3381680");
    String appID;
    Product(String appID){
        this.appID = appID;
    }

    public String getAppID(){
        return appID;
    }
}
