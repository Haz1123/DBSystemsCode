public class constants{
    public static final int DBLOAD_ARG_COUNT = 3;
    public static final int DBQUERY_ARG_COUNT = 3;

    public static final int DBLOAD_PAGE_SIZE_ARG = 1;
    public static final int DATAFILE_ARG = 2;
    public static final int DBQUERY_PAGE_SIZE_ARG = 0;
    public static final int DBQUERY_START_DATE_ARG = 1;
    public static final int DBQUERY_END_DATE_ARG = 2;

    public static final int PERSON_NAME_SIZE = 70;
    public static final int BIRTH_DATE_SIZE = 8;
    public static final int BIRTH_PLACE_SIZE = 354;
    public static final int DEATH_DATE_SIZE = 8;
    public static final int FIELD_SIZE = 242;
    public static final int GENRE_SIZE = 386;
    public static final int INSTRUMENT_SIZE = 541;
    public static final int NATIONALITY_SIZE = 119;
    public static final int THUMBNAIL_SIZE = 292;
    public static final int WIKIPAGE_ID_SIZE = 4;
    public static final int DESCRIPTION_SIZE = 466;
    public static final int TOTAL_SIZE = PERSON_NAME_SIZE +
                                         BIRTH_DATE_SIZE +
                                         BIRTH_PLACE_SIZE +
                                         DEATH_DATE_SIZE +
                                         FIELD_SIZE +
                                         GENRE_SIZE +
                                         INSTRUMENT_SIZE +
                                         NATIONALITY_SIZE +
                                         THUMBNAIL_SIZE +
                                         WIKIPAGE_ID_SIZE +
                                         DESCRIPTION_SIZE;

    public static final int PERSON_NAME_POS = 0;
    public static final int BIRTH_DATE_POS = 1;
    public static final int BIRTH_PLACE_POS = 2;
    public static final int DEATH_DATE_POS = 3;
    public static final int FIELD_POS = 4;
    public static final int GENRE_POS = 5;
    public static final int INSTRUMENT_POS = 6;
    public static final int NATIONALITY_POS = 7;
    public static final int THUMBNAIL_POS = 8;
    public static final int WIKIPAGE_ID_POS = 9;
    public static final int DESCRIPTION_POS = 10;
    public static final int MILLISECONDS_PER_SECOND = 1000000;

    public static final int PERSON_NAME_OFFSET = 0;
    public static final int BIRTH_DATE_OFFSET = PERSON_NAME_SIZE;
    public static final int BIRTH_PLACE_OFFSET = PERSON_NAME_SIZE +
                                         BIRTH_DATE_SIZE;
    public static final int  DEATH_DATE_OFFSET = PERSON_NAME_SIZE +
                                         BIRTH_DATE_SIZE +
                                         BIRTH_PLACE_SIZE;
    public static final int FIELD_OFFSET = PERSON_NAME_SIZE +
                                         BIRTH_DATE_SIZE +
                                         BIRTH_PLACE_SIZE +
                                         DEATH_DATE_SIZE;
    public static final int GENRE_OFFSET = PERSON_NAME_SIZE +
                                         BIRTH_DATE_SIZE +
                                         BIRTH_PLACE_SIZE +
                                         DEATH_DATE_SIZE +
                                         FIELD_SIZE;
    public static final int INSTRUMENT_OFFSET = PERSON_NAME_SIZE +
                                         BIRTH_DATE_SIZE +
                                         BIRTH_PLACE_SIZE +
                                         DEATH_DATE_SIZE +
                                         FIELD_SIZE +
                                         GENRE_SIZE;
    public static final int NATIONALITY_OFFSET = PERSON_NAME_SIZE +
                                         BIRTH_DATE_SIZE +
                                         BIRTH_PLACE_SIZE +
                                         DEATH_DATE_SIZE +
                                         FIELD_SIZE +
                                         GENRE_SIZE +
                                         INSTRUMENT_SIZE;
    public static final int THUMBNAIL_OFFSET = PERSON_NAME_SIZE +
                                         BIRTH_DATE_SIZE +
                                         BIRTH_PLACE_SIZE +
                                         DEATH_DATE_SIZE +
                                         FIELD_SIZE +
                                         GENRE_SIZE +
                                         INSTRUMENT_SIZE +
                                         NATIONALITY_SIZE;
    public static final int WIKIPAGE_ID_OFFSET = PERSON_NAME_SIZE +
                                         BIRTH_DATE_SIZE +
                                         BIRTH_PLACE_SIZE +
                                         DEATH_DATE_SIZE +
                                         FIELD_SIZE +
                                         GENRE_SIZE +
                                         INSTRUMENT_SIZE +
                                         NATIONALITY_SIZE +
                                         THUMBNAIL_SIZE;
    public static final int DESCRIPTION_OFFSET = PERSON_NAME_SIZE +
                                         BIRTH_DATE_SIZE +
                                         BIRTH_PLACE_SIZE +
                                         DEATH_DATE_SIZE +
                                         FIELD_SIZE +
                                         GENRE_SIZE +
                                         INSTRUMENT_SIZE +
                                         NATIONALITY_SIZE +
                                         THUMBNAIL_SIZE +
                                         WIKIPAGE_ID_SIZE;
}
