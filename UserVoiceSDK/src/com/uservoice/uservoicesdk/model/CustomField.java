package com.uservoice.uservoicesdk.model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CustomField extends BaseModel {
    private String name;
    private List<String> predefinedValues;
    private List<Integer> predefinedIDs;
    private boolean required;
    public static final  String PREFIX = "uf_sdk_translation_";

    @Override
    public void load(JSONObject object) throws JSONException {
        super.load(object);
        name = getString(object, "name");
        required = !object.getBoolean("allow_blank");
        predefinedValues = new ArrayList<String>();
        predefinedIDs = new ArrayList<Integer>();
        if (object.has("possible_values")) {
            JSONArray values = object.getJSONArray("possible_values");
            for (int i = 0; i < values.length(); i++) {
                JSONObject value = values.getJSONObject(i);
                predefinedValues.add(getString(value, "value"));
                predefinedIDs.add(value.getInt("id"));
            }
        }
    }

    @Override
    public void save(JSONObject object) throws JSONException {
        super.save(object);
        object.put("name", name);
        object.put("allow_blank", !required);
        JSONArray jsonPredefinedValues = new JSONArray();
        for (int i = 0; i < predefinedValues.size(); i++) {
            JSONObject predefinedValue = new JSONObject();
            predefinedValue.put("value", predefinedValues.get(i));
            predefinedValue.put("id", predefinedIDs.get(i));
            jsonPredefinedValues.put(predefinedValue);
        }
        object.put("possible_values", jsonPredefinedValues);
    }

    public boolean isRequired() {
        return required;
    }

    public boolean isPredefined() {
        return predefinedValues.size() > 0;
    }

    public List<String> getPredefinedValues() {
        return predefinedValues;
    }

    public List<Integer> getPredefinedIDs() {
        return predefinedIDs;
    }

    public String getName() {
        return name;
    }
}
