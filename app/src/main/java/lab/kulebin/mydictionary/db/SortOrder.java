package lab.kulebin.mydictionary.db;

import lab.kulebin.mydictionary.model.Entry;

public enum SortOrder {
    NEWEST, OLDEST, A_Z, Z_A;

    public static final String SQL_SORT_QUERY_DESC = " DESC";
    public static final String SQL_SORT_QUERY_ASC = " ASC";

    public String getEntrySortOrderQueryParam() {
        switch (this) {
            case NEWEST:
                return Entry.ID + SQL_SORT_QUERY_DESC;
            case OLDEST:
                return Entry.ID + SQL_SORT_QUERY_ASC;
            case A_Z:
                return Entry.VALUE + SQL_SORT_QUERY_ASC;
            case Z_A:
                return Entry.VALUE + SQL_SORT_QUERY_DESC;
            default:
                return null;
        }
    }
}
