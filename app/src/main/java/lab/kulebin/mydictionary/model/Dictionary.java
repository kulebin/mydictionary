package lab.kulebin.mydictionary.model;

import org.json.JSONException;
import org.json.JSONObject;

import lab.kulebin.mydictionary.Constants;
import lab.kulebin.mydictionary.db.annotations.Table;
import lab.kulebin.mydictionary.db.annotations.dbInteger;
import lab.kulebin.mydictionary.db.annotations.dbLong;
import lab.kulebin.mydictionary.db.annotations.dbString;
import lab.kulebin.mydictionary.json.IJsonBuildable;

@Table(name = "dictionaries")
public class Dictionary implements IJsonBuildable {

    @dbLong
    public static final String ID = Constants.ID_COLUMN;
    @dbString
    public static final String NAME = "name";
    @dbInteger
    public static final String MENU_ID = "menuId";

    private long mId;
    private String mName;
    private int mMenuId;

    public Dictionary(final long pId, final String pName, final int pMenuId) {
        mId = pId;
        mName = pName;
        mMenuId = pMenuId;
    }

    public long getId() {
        return mId;
    }

    public void setId(long pId) {
        mId = pId;
    }

    public int getMenuId() {
        return mMenuId;
    }

    public void setMenuId(int pMenuId) {
        mMenuId = pMenuId;
    }

    public String getName() {
        return mName;
    }

    public void setName(final String pName) {
        mName = pName;
    }

    @Override
    public String toJson() throws JSONException {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put(MENU_ID, getMenuId());
        jsonObject.put(NAME, getName());
        return jsonObject.toString();
    }
}
