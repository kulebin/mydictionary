package lab.kulebin.mydictionary.db;

import lab.kulebin.mydictionary.model.DataCache;
import lab.kulebin.mydictionary.model.Dictionary;
import lab.kulebin.mydictionary.model.Entry;
import lab.kulebin.mydictionary.model.Tag;

public final class Contract {

    public static final Class<?>[] MODELS = {
            Entry.class,
            Dictionary.class,
            DataCache.class,
            Tag.class
    };

    public static final Class<?>[] FETCH_DATA_SET = {
            Entry.class,
            Dictionary.class,
            Tag.class
    };
}