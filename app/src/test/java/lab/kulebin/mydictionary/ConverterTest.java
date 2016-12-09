package lab.kulebin.mydictionary;


import org.junit.Test;

import lab.kulebin.mydictionary.utils.Converter;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ConverterTest {

    @Test
    public void testConvertStringArrayToString() {
        //Verify method can handle null param
        assertEquals(null, Converter.convertStringArrayToString(null));
        //Verify method correct builds String from StringArray with single element
        assertEquals("single word", Converter.convertStringArrayToString(new String[]{"single word"}));
        //Verify method correct builds String from StringArray with many elements
        assertEquals("single word||second||third", Converter.convertStringArrayToString(new String[]{"single word", "second", "third"}));
    }

    @Test
    public void testConvertStringToStringArray() {
        //Verify method can handle null param
        assertArrayEquals(null, Converter.convertStringToStringArray(null));
        //Verify method correct builds StringArray from String without separator
        assertArrayEquals(new String[]{"simple string"}, Converter.convertStringToStringArray("simple string"));
        //Verify method correct builds StringArray from String with many separators
        assertArrayEquals(new String[]{"first word", "second", "third"}, Converter.convertStringToStringArray("first word||second||third"));
    }
}
