package lab.kulebin.mydictionary;


import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import lab.kulebin.mydictionary.model.Dictionary;
import lab.kulebin.mydictionary.model.Entry;
import lab.kulebin.mydictionary.json.JsonHelper;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class JsonHelperTest {

    @Test
    public void testBuildDictionaryJsonObject() throws JSONException {
        Dictionary dictionary = new Dictionary(12, "English");
        String expectedResult = "{\"" + Dictionary.NAME + "\":\"English\",\"" + Dictionary.ID + "\":12}";
        //Verify json string is built correct
        assertEquals(expectedResult, JsonHelper.buildDictionaryJsonObject(dictionary).toString());
    }

    @Test
    public void testBuildEntryJsonObject() throws JSONException {
        //Verify json string is built correct in case all fields are filled in
        Entry entry = new Entry(11, 12, "word", "transcription", 1480806939211L, -1, "url", "url", new String[]{}, new String[]{});
        String expectedResult = "{\"transcription\":\"transcription\",\"imageUrl\":\"url\",\"dictionaryId\":12,\"id\":11,\"creationDate\":1480806939211,\"lastEditionDate\":-1,\"value\":\"word\",\"soundUrl\":\"url\"}";
        assertEquals(expectedResult, JsonHelper.buildEntryJsonObject(entry).toString());

        //Verify json string is built correct in case only ID, Dic ID, Creation date and value are filled in
        Entry entry1 = new Entry(11, 12, "word", null, 1480806939211L, -1, null, null, null, null);
        String expectedResult1 = "{\"dictionaryId\":12,\"id\":11,\"creationDate\":1480806939211,\"lastEditionDate\":-1,\"value\":\"word\"}";
        assertEquals(expectedResult1, JsonHelper.buildEntryJsonObject(entry1).toString());
    }
}
