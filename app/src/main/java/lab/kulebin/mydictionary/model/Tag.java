package lab.kulebin.mydictionary.model;

import org.json.JSONException;
import org.json.JSONObject;

import lab.kulebin.mydictionary.Constants;
import lab.kulebin.mydictionary.db.annotations.Table;
import lab.kulebin.mydictionary.db.annotations.dbInteger;
import lab.kulebin.mydictionary.db.annotations.dbLong;
import lab.kulebin.mydictionary.db.annotations.dbString;
import lab.kulebin.mydictionary.json.IJsonBuildable;

@Table(name = "tags")
public class Tag implements IJsonBuildable {

    @dbLong
    public static final String ID = Constants.ID_COLUMN;
    @dbInteger
    public static final String COLOR = "color";
    @dbString
    public static final String NAME = "name";

    private long id;
    private int color;
    private String name;

    public Tag(final long pId, final int pColor, final String pName) {
        id = pId;
        color = pColor;
        name = pName;
    }

    public long getId() {
        return id;
    }

    public void setId(final long pId) {
        id = pId;
    }

    public int getColor() {
        return color;
    }

    public void setColor(final int pColor) {
        color = pColor;
    }

    public String getName() {
        return name;
    }

    public void setName(final String pName) {
        name = pName;
    }

    @Override
    public String toJson() throws JSONException {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put(COLOR, getColor());
        jsonObject.put(NAME, getName());
        return jsonObject.toString();
    }
}
