package com.bloodbank.common.core.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class LookupConstants {

    // Lookup Category Codes
    public static final String CAT_GENDER = "GENDER";
    public static final String CAT_DOCUMENT_TYPE = "DOCUMENT_TYPE";
    public static final String CAT_TITLE = "TITLE";
    public static final String CAT_ID_TYPE = "ID_TYPE";
    public static final String CAT_DONATION_TYPE = "DONATION_TYPE";
    public static final String CAT_DEFERRAL_REASON = "DEFERRAL_REASON";
    public static final String CAT_MARITAL_STATUS = "MARITAL_STATUS";
    public static final String CAT_RELATIONSHIP = "RELATIONSHIP";
    public static final String CAT_COMPONENT_TYPE = "COMPONENT_TYPE";
    public static final String CAT_HOSPITAL_TYPE = "HOSPITAL_TYPE";
    public static final String CAT_BLOOD_BANK_TYPE = "BLOOD_BANK_TYPE";

    // Well-known Genders
    public static final String GENDER_MALE = "MALE";
    public static final String GENDER_FEMALE = "FEMALE";
    public static final String GENDER_OTHER = "OTHER";

    // Well-known Document Types
    public static final String DOC_AADHAAR = "AADHAAR";
    public static final String DOC_PAN = "PAN";
    public static final String DOC_DRIVING_LICENSE = "DRIVING_LICENSE";
    public static final String DOC_PASSPORT = "PASSPORT";

    // Well-known Titles
    public static final String TITLE_MR = "MR";
    public static final String TITLE_MRS = "MRS";
    public static final String TITLE_MS = "MS";
    public static final String TITLE_DR = "DR";

    // Well-known Donation Types
    public static final String DONATION_WHOLE_BLOOD = "WHOLE_BLOOD";
    public static final String DONATION_PLATELETS = "PLATELETS";
    public static final String DONATION_PLASMA = "PLASMA";
    public static final String DONATION_DOUBLE_RED_CELLS = "DOUBLE_RED_CELLS";

    // Well-known Deferral Reasons
    public static final String DEFER_LOW_HEMOGLOBIN = "LOW_HEMOGLOBIN";
    public static final String DEFER_TATTOO = "TATTOO";
    public static final String DEFER_MEDICATION = "MEDICATION";
    public static final String DEFER_TRAVEL = "TRAVEL";
    public static final String DEFER_UNDERWEIGHT = "UNDERWEIGHT";

    // Well-known Blood Groups
    public static final String BG_A_POS = "A_POS";
    public static final String BG_A_NEG = "A_NEG";
    public static final String BG_B_POS = "B_POS";
    public static final String BG_B_NEG = "B_NEG";
    public static final String BG_AB_POS = "AB_POS";
    public static final String BG_AB_NEG = "AB_NEG";
    public static final String BG_O_POS = "O_POS";
    public static final String BG_O_NEG = "O_NEG";
}
