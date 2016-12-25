package lab.kulebin.mydictionary.app;

import lab.kulebin.mydictionary.model.Entry;

public enum SortOrder {
    NEWEST, OLDEST, A_Z, Z_A;

    public String getEntrySortOrderQueryParam() {
        switch (this) {
            case NEWEST:
                return Entry.CREATION_DATE + Constants.SQL_SORT_QUERY_DESC;
            case OLDEST:
                return Entry.CREATION_DATE + Constants.SQL_SORT_QUERY_ASC;
            case A_Z:
                return Entry.VALUE + Constants.SQL_SORT_QUERY_ASC;
            case Z_A:
                return Entry.VALUE + Constants.SQL_SORT_QUERY_DESC;
            default:
                return null;
        }
    }

}
