package de.avpptr.umweltzone.contract;

public interface Preferences {
    public static final String SHARED_PREFERENCES = "de.avpptr.umweltzone.SHARED_PREFERENCES";
    public static final String KEY_CENTER_LATITUDE = "de.avpptr.umweltzone.CENTER_LATITUDE";
    public static final String KEY_CENTER_LONGITUDE = "de.avpptr.umweltzone.CENTER_LONGITUDE";
    public static final String KEY_BOUNDING_BOX_SOUTHWEST_LATITUDE =
            "de.avpptr.umweltzone.BOUNDING_BOX_SOUTHWEST_LATITUDE";
    public static final String KEY_BOUNDING_BOX_SOUTHWEST_LONGITUDE =
            "de.avpptr.umweltzone.BOUNDING_BOX_SOUTHWEST_LONGITUDE";
    public static final String KEY_BOUNDING_BOX_NORTHEAST_LATITUDE =
            "de.avpptr.umweltzone.BOUNDING_BOX_NORTHEAST_LATITUDE";
    public static final String KEY_BOUNDING_BOX_NORTHEAST_LONGITUDE =
            "de.avpptr.umweltzone.BOUNDING_BOX_NORTHEAST_LONGITUDE";
    public static final String KEY_CITY_NAME = "de.avpptr.umweltzone.CITY_NAME";
    public static final String KEY_ZOOM_LEVEL = "de.avpptr.umweltzone.ZOOM_LEVEL";
}