package lab.kulebin.mydictionary.db;


import lab.kulebin.mydictionary.model.Dictionary;
import lab.kulebin.mydictionary.model.Entry;

public final class Contract {
    public static final Class<?>[] MODELS = {
            Entry.class,
            Dictionary.class
    };
}
