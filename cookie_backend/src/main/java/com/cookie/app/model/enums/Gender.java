package com.cookie.app.model.enums;

public enum Gender {
    MALE(Genders.MALE),
    FEMALE(Genders.FEMALE),
    OTHER(Genders.OTHER);

    Gender(String gender) {}

    public static class Genders {
        public static final String MALE = "Male";
        public static final String FEMALE = "Female";
        public static final String OTHER = "Other";
    }
}
